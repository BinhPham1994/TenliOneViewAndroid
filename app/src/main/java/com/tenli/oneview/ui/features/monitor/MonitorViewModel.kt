package com.tenli.oneview.ui.features.monitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.CachedMonitorData
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.VMSServiceModel
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

sealed class CameraTreeNode {
    abstract val key: String
    data class VMSNode(val vms: VMSServiceModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class GroupNode(val group: CameraGroupModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class CameraLeaf(val camera: CameraModel, override val key: String) : CameraTreeNode()
}

data class VmsFilterChip(
    val key: String,
    val name: String,
    val totalCount: Int,
    val activeCount: Int,
    val isActive: Boolean,
    val isGroupFiltered: Boolean
)

data class GroupSheetRow(
    val key: String,
    val title: String,
    val depth: Int,
    val hasSubGroups: Boolean,
    val isExpanded: Boolean,
    val count: Int,
    val isVmsNode: Boolean = false
)

data class CameraCardItem(
    val key: String,
    val camera: CameraModel
)

data class SelectedCamera(
    val camera: CameraModel,
    val streamUrl: String,
    val videoCodecTag: String = "",
    val retryCount: Int = 0,
    val streamType: String = "LIVE",
    val fallbackImageUrl: String = "",
    val vmsName: String = "",
    val channel: Int = 1,
    val hasFallbackTried: Boolean = false,
    val snapshotRetried: Boolean = false
)

enum class MonitorTab {
    EVENTS, PLAYBACK
}

data class MonitorUiState(
    val isLoading: Boolean = false,
    val treeData: List<CameraTreeNode> = emptyList(),
    val gridSize: Int = 1, // 1 (1x1) or 4 (2x2)
    val activeSlotIndex: Int = 0,
    val blinkStates: List<Boolean> = List(4) { false },
    val searchQuery: String = "",
    val selectedCameras: List<SelectedCamera?> = List(4) { null },
    val expandedNodes: Set<String> = emptySet(),
    val activeVmsKey: String = "",
    val sheetVmsKey: String = "",
    val selectedFilterNodeKey: String = "all",
    val showGroupFilterSheet: Boolean = false,
    val currentCameraPage: Int = 1,
    val cameraSnapshots: Map<Int, String?> = emptyMap(),
    val error: String? = null,
    val aiServices: List<com.tenli.oneview.model.network.AIServiceModel> = emptyList(),
    val selectedTab: MonitorTab = MonitorTab.EVENTS,
    val selectedTimeFilter: TimeFilter = TimeFilter.TODAY,
    val events: List<com.tenli.oneview.model.network.EventData> = emptyList(),
    val playbacks: List<com.tenli.oneview.model.network.VideoModel> = emptyList(),
    val isPaginating: Boolean = false,
    val hasMoreEvents: Boolean = true,
    val hasMorePlaybacks: Boolean = true,
    val cameraStatusMap: Map<String, String> = emptyMap(),
    val recordStatusMap: Map<String, String> = emptyMap()
)

@OptIn(FlowPreview::class)
class MonitorViewModel(application: Application) : AndroidViewModel(application) {
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)
    private val eventApi = LoginAuthClient.create(com.tenli.oneview.data.network.api.EventApi::class.java)

    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    private val streamJobs = arrayOfNulls<Job>(4)
    private val blinkJobs = arrayOfNulls<Job>(4)
    private var isMonitorTabActive: Boolean = false
    private val pendingSnapshotIds = java.util.Collections.synchronizedSet(mutableSetOf<Int>())
    private val retriedCardSnapshotIds = java.util.Collections.synchronizedSet(mutableSetOf<Int>())

    companion object {
        const val CAMERA_PAGE_SIZE = 10
        private val RTSP_HOST_REGEX = Regex("(?i)^rtsp://(?:[^@/\\s]+@)?([^:/?\\s]+)")
        private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")

        fun normalizeVietnameseText(str: String): String {
            if (str.isEmpty()) return ""
            return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replace(DIACRITICS_REGEX, "")
                .replace('đ', 'd')
                .replace('Đ', 'd')
                .lowercase()
                .trim()
        }

        fun extractHostFromRtsp(rtspUrl: String): String {
            if (rtspUrl.isBlank()) return ""
            val match = RTSP_HOST_REGEX.find(rtspUrl.trim())
            return match?.groupValues?.getOrNull(1)?.lowercase().orEmpty()
        }

        fun getCameraIpText(cam: CameraModel): String {
            val mainHost = extractHostFromRtsp(cam.main)
            if (mainHost.isNotEmpty()) return mainHost
            val subHost = extractHostFromRtsp(cam.sub)
            if (subHost.isNotEmpty()) return subHost
            return ""
        }

        fun doesCameraMatchSearch(cam: CameraModel, rawQuery: String): Boolean {
            val trimmed = rawQuery.trim()
            if (trimmed.isEmpty()) return true
            val normalizedQuery = normalizeVietnameseText(trimmed)
            val rawLower = trimmed.lowercase()

            val normalizedName = normalizeVietnameseText(cam.name)
            if (normalizedName.contains(normalizedQuery)) return true

            val ipText = getCameraIpText(cam)
            if (ipText.isNotEmpty() && ipText.lowercase().contains(rawLower)) return true

            return false
        }

        fun collectUniqueCamerasFromNode(node: CameraTreeNode?): List<CameraCardItem> {
            if (node == null) return emptyList()
            val map = linkedMapOf<Int, CameraCardItem>()
            fun walk(n: CameraTreeNode) {
                when (n) {
                    is CameraTreeNode.CameraLeaf -> {
                        val cam = n.camera
                        if (cam.id != 0 && !map.containsKey(cam.id)) {
                            map[cam.id] = CameraCardItem(key = "cam-${cam.id}", camera = cam)
                        }
                    }
                    is CameraTreeNode.GroupNode -> {
                        for (child in n.children) walk(child)
                    }
                    is CameraTreeNode.VMSNode -> {
                        for (child in n.children) walk(child)
                    }
                }
            }
            walk(node)
            return map.values.toList()
        }

        fun countNodeCameras(node: CameraTreeNode?): Int {
            return collectUniqueCamerasFromNode(node).size
        }

        fun findTreeNodeByKey(nodes: List<CameraTreeNode>, targetKey: String): CameraTreeNode? {
            for (node in nodes) {
                if (node.key == targetKey) return node
                when (node) {
                    is CameraTreeNode.VMSNode -> {
                        val found = findTreeNodeByKey(node.children, targetKey)
                        if (found != null) return found
                    }
                    is CameraTreeNode.GroupNode -> {
                        val found = findTreeNodeByKey(node.children, targetKey)
                        if (found != null) return found
                    }
                    is CameraTreeNode.CameraLeaf -> {}
                }
            }
            return null
        }

        fun isDescendantOrSelfKey(node: CameraTreeNode?, targetKey: String): Boolean {
            if (node == null || targetKey.isEmpty()) return false
            if (node.key == targetKey) return true
            return when (node) {
                is CameraTreeNode.VMSNode -> node.children.any { isDescendantOrSelfKey(it, targetKey) }
                is CameraTreeNode.GroupNode -> node.children.any { isDescendantOrSelfKey(it, targetKey) }
                is CameraTreeNode.CameraLeaf -> false
            }
        }

        fun computeVmsFilterChips(state: MonitorUiState): List<VmsFilterChip> {
            val result = mutableListOf<VmsFilterChip>()
            for (node in state.treeData) {
                if (node is CameraTreeNode.VMSNode) {
                    val totalCount = countNodeCameras(node)
                    if (totalCount <= 0) continue

                    val isFilterInThisVms = state.selectedFilterNodeKey != "all" &&
                        isDescendantOrSelfKey(node, state.selectedFilterNodeKey)
                    val isSubGroupFiltered = isFilterInThisVms && state.selectedFilterNodeKey != node.key

                    var activeCount = totalCount
                    if (isSubGroupFiltered) {
                        val subNode = findTreeNodeByKey(node.children, state.selectedFilterNodeKey)
                        if (subNode != null) {
                            activeCount = countNodeCameras(subNode)
                        }
                    }

                    result.add(
                        VmsFilterChip(
                            key = node.key,
                            name = node.vms.name.ifBlank { "VMS" },
                            totalCount = totalCount,
                            activeCount = activeCount,
                            isActive = state.activeVmsKey == node.key,
                            isGroupFiltered = isSubGroupFiltered
                        )
                    )
                }
            }
            return result
        }

        fun computeGroupTreeRows(state: MonitorUiState): List<GroupSheetRow> {
            val vmsNodes = state.treeData.filterIsInstance<CameraTreeNode.VMSNode>()
                .filter { countNodeCameras(it) > 0 }
            if (vmsNodes.isEmpty()) return emptyList()

            val rows = mutableListOf<GroupSheetRow>()

            fun walkGroups(nodes: List<CameraTreeNode>, depth: Int) {
                for (node in nodes) {
                    if (node is CameraTreeNode.GroupNode) {
                        val count = countNodeCameras(node)
                        if (count <= 0) continue
                        val subGroups = node.children.filter {
                            it is CameraTreeNode.GroupNode && countNodeCameras(it) > 0
                        }
                        val hasSubGroups = subGroups.isNotEmpty()
                        val isExpanded = state.expandedNodes.contains(node.key)
                        rows.add(
                            GroupSheetRow(
                                key = node.key,
                                title = node.group.name,
                                depth = depth,
                                hasSubGroups = hasSubGroups,
                                isExpanded = isExpanded,
                                count = count,
                                isVmsNode = false
                            )
                        )
                        if (hasSubGroups && isExpanded) {
                            walkGroups(subGroups, depth + 1)
                        }
                    }
                }
            }

            for (vmsNode in vmsNodes) {
                val vmsCount = countNodeCameras(vmsNode)
                if (vmsCount <= 0) continue
                val subGroups = vmsNode.children.filter {
                    it is CameraTreeNode.GroupNode && countNodeCameras(it) > 0
                }
                val hasSubGroups = subGroups.isNotEmpty()
                val isExpanded = state.expandedNodes.contains(vmsNode.key)
                rows.add(
                    GroupSheetRow(
                        key = vmsNode.key,
                        title = vmsNode.vms.name.ifBlank { "VMS" },
                        depth = 0,
                        hasSubGroups = hasSubGroups,
                        isExpanded = isExpanded,
                        count = vmsCount,
                        isVmsNode = true
                    )
                )
                if (hasSubGroups && isExpanded) {
                    walkGroups(subGroups, 1)
                }
            }
            return rows
        }

        fun computeDisplayedCameraItems(state: MonitorUiState): List<CameraCardItem> {
            val targetNode: CameraTreeNode? = if (state.selectedFilterNodeKey != "all") {
                findTreeNodeByKey(state.treeData, state.selectedFilterNodeKey)
            } else {
                null
            }

            val baseCameras = if (targetNode != null) {
                collectUniqueCamerasFromNode(targetNode)
            } else {
                val map = linkedMapOf<Int, CameraCardItem>()
                for (vmsNode in state.treeData) {
                    for (item in collectUniqueCamerasFromNode(vmsNode)) {
                        if (!map.containsKey(item.camera.id)) {
                            map[item.camera.id] = item
                        }
                    }
                }
                map.values.toList()
            }

            if (state.searchQuery.isBlank()) return baseCameras
            return baseCameras.filter { doesCameraMatchSearch(it.camera, state.searchQuery) }
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                MonitorViewModel(application)
            }
        }
    }

    private fun getBaseDomain(): String {
        var base = com.tenli.oneview.data.local.UserSession.domain
            .ifEmpty { com.tenli.oneview.BuildConfig.DOMAIN_CLOUD }
            .trim()
            .trimEnd('/')
        if (base.endsWith("/api/v1")) {
            base = base.removeSuffix("/api/v1")
        }
        return base
    }

    private fun resolveFullMediaUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty() || trimmed == "NO_IMAGE") return ""
        if (trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.startsWith("file://") ||
            trimmed.startsWith("data:")
        ) {
            return trimmed
        }
        val base = getBaseDomain()
        return if (trimmed.startsWith("/")) {
            "$base$trimmed"
        } else {
            "$base/$trimmed"
        }
    }

    private fun resolveWebSocketUrl(rawLink: String): String {
        val trimmed = rawLink.trim()
        if (trimmed.isEmpty()) return ""
        var resolved = trimmed
        if (resolved.startsWith("/")) {
            resolved = "${getBaseDomain()}$resolved"
        } else if (!resolved.startsWith("ws://") &&
            !resolved.startsWith("wss://") &&
            !resolved.startsWith("http://") &&
            !resolved.startsWith("https://")
        ) {
            resolved = "${getBaseDomain()}/$resolved"
        }
        if (resolved.startsWith("http://")) resolved = resolved.replaceFirst("http://", "ws://")
        if (resolved.startsWith("https://")) resolved = resolved.replaceFirst("https://", "wss://")
        return resolved
    }

    init {
        val prefs = GlobalData.preferences
        val savedNodes = prefs.getStringSet("saved_expanded_nodes", emptySet()) ?: emptySet()
        val savedGridSize = prefs.getInt("saved_monitor_grid_size", 1).let { if (it == 4) 4 else 1 }
        _uiState.update { it.copy(expandedNodes = savedNodes, gridSize = savedGridSize) }

        loadData()

        viewModelScope.launch {
            val app = application as com.tenli.oneview.TenliApp
            val wsManager = app.container.webSocketManager
            wsManager.cameraStatusMap
                .debounce(800L)
                .collect { statusMap ->
                    _uiState.update { it.copy(cameraStatusMap = statusMap) }
                }
        }
        viewModelScope.launch {
            val app = application as com.tenli.oneview.TenliApp
            val wsManager = app.container.webSocketManager
            wsManager.recordStatusMap
                .debounce(800L)
                .collect { recMap ->
                    _uiState.update { it.copy(recordStatusMap = recMap) }
                }
        }
    }

    fun setMonitorTabActive(active: Boolean) {
        if (isMonitorTabActive == active) return
        isMonitorTabActive = active
        if (!active) {
            for (i in 0..3) {
                streamJobs[i]?.cancel()
                streamJobs[i] = null
            }
        }
    }

    fun setGridSize(size: Int) {
        val normalized = if (size == 4) 4 else 1
        if (_uiState.value.gridSize == normalized) return
        GlobalData.preferences.edit().putInt("saved_monitor_grid_size", normalized).apply()

        _uiState.update { state ->
            val newActiveSlot = if (normalized == 1) {
                0
            } else {
                val firstEmpty = (0 until 4).firstOrNull { state.selectedCameras.getOrNull(it) == null }
                firstEmpty ?: 0
            }
            state.copy(gridSize = normalized, activeSlotIndex = newActiveSlot)
        }

        if (normalized == 4) {
            val currentCameras = _uiState.value.selectedCameras
            for (idx in 1..3) {
                val selected = currentCameras.getOrNull(idx)
                if (selected != null && selected.streamUrl.isEmpty()) {
                    ensureCameraSnapshotUrl(selected.camera, idx, force = false)
                    startLiveStreamPolling(selected.camera, idx, channel = selected.channel, isFallback = false)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { state ->
            val nextState = state.copy(searchQuery = query, currentCameraPage = 1)
            clampAndNormalizeState(nextState)
        }
        loadVisibleCameraSnapshots()
    }

    fun handleCellTap(index: Int) {
        if (index !in 0..3) return
        if (_uiState.value.gridSize == 4) {
            _uiState.update { it.copy(activeSlotIndex = index) }
        }
    }

    fun triggerBlink(index: Int) {
        if (index !in 0..3) return
        blinkJobs[index]?.cancel()
        _uiState.update { state ->
            val updated = state.blinkStates.toMutableList()
            while (updated.size < 4) updated.add(false)
            updated[index] = true
            state.copy(blinkStates = updated)
        }
        blinkJobs[index] = viewModelScope.launch {
            delay(1000L)
            _uiState.update { state ->
                val updated = state.blinkStates.toMutableList()
                while (updated.size < 4) updated.add(false)
                updated[index] = false
                state.copy(blinkStates = updated)
            }
        }
    }

    fun handleVmsChipClick(vmsKey: String) {
        _uiState.update { state ->
            val vmsNode = state.treeData.filterIsInstance<CameraTreeNode.VMSNode>()
                .firstOrNull { it.key == vmsKey }
            val isCurrentFilterInVms = state.selectedFilterNodeKey != "all" &&
                isDescendantOrSelfKey(vmsNode, state.selectedFilterNodeKey)

            val nextFilterKey = if (!isCurrentFilterInVms) vmsKey else state.selectedFilterNodeKey
            val nextPage = if (!isCurrentFilterInVms) 1 else state.currentCameraPage

            clampAndNormalizeState(
                state.copy(
                    activeVmsKey = vmsKey,
                    sheetVmsKey = vmsKey,
                    selectedFilterNodeKey = nextFilterKey,
                    currentCameraPage = nextPage,
                    showGroupFilterSheet = true
                )
            )
        }
        loadVisibleCameraSnapshots()
    }

    fun openGroupFilterSheet() {
        _uiState.update { it.copy(showGroupFilterSheet = true) }
    }

    fun closeGroupFilterSheet() {
        _uiState.update { it.copy(showGroupFilterSheet = false) }
    }

    fun handleGroupRowClick(groupKey: String) {
        _uiState.update { state ->
            val nextFilterKey = if (groupKey == "all" || state.selectedFilterNodeKey == groupKey) {
                "all"
            } else {
                groupKey
            }
            val matchedVms = state.treeData.filterIsInstance<CameraTreeNode.VMSNode>()
                .firstOrNull { isDescendantOrSelfKey(it, nextFilterKey) }
            val nextActiveVms = matchedVms?.key ?: state.activeVmsKey
            clampAndNormalizeState(
                state.copy(
                    selectedFilterNodeKey = nextFilterKey,
                    activeVmsKey = nextActiveVms,
                    currentCameraPage = 1,
                    showGroupFilterSheet = false
                )
            )
        }
        loadVisibleCameraSnapshots()
    }

    fun setCameraPage(page: Int) {
        _uiState.update { state ->
            val allItems = computeDisplayedCameraItems(state)
            val totalPages = maxOf(1, (allItems.size + CAMERA_PAGE_SIZE - 1) / CAMERA_PAGE_SIZE)
            val clamped = page.coerceIn(1, totalPages)
            state.copy(currentCameraPage = clamped)
        }
        loadVisibleCameraSnapshots()
    }

    fun prevCameraPage() {
        val cur = _uiState.value.currentCameraPage
        if (cur > 1) {
            setCameraPage(cur - 1)
        }
    }

    fun nextCameraPage() {
        val state = _uiState.value
        val allItems = computeDisplayedCameraItems(state)
        val totalPages = maxOf(1, (allItems.size + CAMERA_PAGE_SIZE - 1) / CAMERA_PAGE_SIZE)
        if (state.currentCameraPage < totalPages) {
            setCameraPage(state.currentCameraPage + 1)
        }
    }

    private fun clampAndNormalizeState(state: MonitorUiState): MonitorUiState {
        val vmsNodes = state.treeData.filterIsInstance<CameraTreeNode.VMSNode>()
            .filter { countNodeCameras(it) > 0 }

        var activeVms = state.activeVmsKey
        var sheetVms = state.sheetVmsKey
        var selectedFilter = state.selectedFilterNodeKey

        if (vmsNodes.isEmpty()) {
            activeVms = ""
            sheetVms = ""
            selectedFilter = "all"
        } else {
            val firstKey = vmsNodes.first().key
            if (activeVms.isEmpty() || vmsNodes.none { it.key == activeVms }) {
                activeVms = firstKey
            }
            if (sheetVms.isEmpty() || vmsNodes.none { it.key == sheetVms }) {
                sheetVms = activeVms.ifEmpty { firstKey }
            }
            if (selectedFilter != "all" && findTreeNodeByKey(state.treeData, selectedFilter) == null) {
                selectedFilter = "all"
            }
        }

        val normalized = state.copy(
            activeVmsKey = activeVms,
            sheetVmsKey = sheetVms,
            selectedFilterNodeKey = selectedFilter
        )
        val allItems = computeDisplayedCameraItems(normalized)
        val totalPages = maxOf(1, (allItems.size + CAMERA_PAGE_SIZE - 1) / CAMERA_PAGE_SIZE)
        val clampedPage = normalized.currentCameraPage.coerceIn(1, totalPages)
        return normalized.copy(currentCameraPage = clampedPage)
    }

    private fun saveSelectedCamerasToPrefs(cameras: List<SelectedCamera?>) {
        val prefs = GlobalData.preferences
        val idsCsv = (0 until 4).joinToString(",") { idx ->
            (cameras.getOrNull(idx)?.camera?.id ?: -1).toString()
        }
        val editor = prefs.edit().putString("saved_monitor_grid_camera_ids", idsCsv)
        val firstCamId = cameras.getOrNull(0)?.camera?.id
        if (firstCamId != null) {
            editor.putInt("saved_monitor_camera_id", firstCamId)
        } else {
            editor.remove("saved_monitor_camera_id")
        }
        editor.apply()
    }

    private fun restoreOrSelectInitialCameras(tree: List<CameraTreeNode>) {
        val currentState = _uiState.value
        if (currentState.selectedCameras.any { it != null }) return

        val prefs = GlobalData.preferences
        val savedCsv = prefs.getString("saved_monitor_grid_camera_ids", null)
        val savedIds = if (!savedCsv.isNullOrBlank()) {
            savedCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else {
            val singleId = prefs.getInt("saved_monitor_camera_id", -1)
            listOf(singleId, -1, -1, -1)
        }

        var restoredAny = false
        for (idx in 0 until 4) {
            val camId = savedIds.getOrNull(idx) ?: -1
            if (camId > 0) {
                val cam = findCameraById(tree, camId)
                if (cam != null) {
                    restoredAny = true
                    addCamera(cam, idx, startPolling = idx < _uiState.value.gridSize)
                }
            }
        }

        if (!restoredAny) {
            val firstCamera = findFirstCamera(tree)
            if (firstCamera != null) {
                addCamera(firstCamera, 0, startPolling = true)
            }
        }
    }

    fun loadVisibleCameraSnapshots() {
        val state = _uiState.value
        val allItems = computeDisplayedCameraItems(state)
        val start = (state.currentCameraPage - 1) * CAMERA_PAGE_SIZE
        val visibleCameras = allItems.drop(start).take(CAMERA_PAGE_SIZE).map { it.camera }
        if (visibleCameras.isEmpty()) return

        val app = getApplication<Application>()
        val immediateUpdates = mutableMapOf<Int, String?>()
        val toFetch = mutableListOf<CameraModel>()

        for (cam in visibleCameras) {
            val camId = cam.id
            if (camId == 0) continue

            // Nếu đã có kết quả trong state (kể cả null khi camera thực sự không có ảnh snapshot)
            // thì không gọi lại API trên mỗi lần đổi trang/bộ lọc
            if (state.cameraSnapshots.containsKey(camId)) {
                val existingSnap = state.cameraSnapshots[camId]
                if (!existingSnap.isNullOrEmpty() || !MonitorCacheManager.isSnapshotCacheFresh(app, camId)) {
                    continue
                }
            }

            val cachedRaw = MonitorCacheManager.getCachedCameraImageLink(app, camId, cam.cameraId)
            if (!cachedRaw.isNullOrBlank()) {
                val resolved = resolveFullMediaUrl(cachedRaw)
                if (resolved.isNotEmpty()) {
                    immediateUpdates[camId] = resolved
                }
                if (MonitorCacheManager.isSnapshotCacheFresh(app, camId)) {
                    continue
                }
            }

            if (!pendingSnapshotIds.contains(camId)) {
                toFetch.add(cam)
            }
        }

        if (immediateUpdates.isNotEmpty()) {
            _uiState.update { cur ->
                cur.copy(cameraSnapshots = cur.cameraSnapshots + immediateUpdates)
            }
        }

        for (cam in toFetch) {
            fetchCardSnapshot(cam, force = false)
        }
    }

    private fun fetchCardSnapshot(cam: CameraModel, force: Boolean) {
        val camId = cam.id
        if (camId == 0) return
        if (!force && !pendingSnapshotIds.add(camId)) return
        if (force) pendingSnapshotIds.add(camId)

        val app = getApplication<Application>()
        viewModelScope.launch {
            try {
                var resolvedLink = ""
                val res = runCatching { vmsApi.getCameraImageLink(camId) }.getOrNull()
                if (res?.isSuccessful == true) {
                    val rawLink = res.body()?.link?.trim().orEmpty()
                    if (rawLink.isNotEmpty() && rawLink != "NO_IMAGE") {
                        MonitorCacheManager.setCachedCameraImageLink(app, camId, rawLink, cam.cameraId)
                        resolvedLink = resolveFullMediaUrl(rawLink)
                    }
                }

                // Fallback giống MobileLivePage.vue: lấy thumbnail từ bản ghi VideoList gần nhất nếu CameraImageLink không có
                if (resolvedLink.isEmpty()) {
                    val videoRes = runCatching {
                        vmsApi.getVideoList(camera = camId, count = 1, from = null, to = null)
                    }.getOrNull()
                    if (videoRes?.isSuccessful == true) {
                        val firstVideo = videoRes.body()?.firstOrNull()
                        val rawThumb = firstVideo?.thumbnailLink?.ifBlank { firstVideo.imageLink }?.trim().orEmpty()
                        if (rawThumb.isNotEmpty() && rawThumb != "NO_IMAGE") {
                            MonitorCacheManager.setCachedCameraImageLink(app, camId, rawThumb, cam.cameraId)
                            resolvedLink = resolveFullMediaUrl(rawThumb)
                        }
                    }
                }

                _uiState.update { cur ->
                    val currentVal = cur.cameraSnapshots[camId]
                    val nextMap = cur.cameraSnapshots.toMutableMap()
                    if (resolvedLink.isNotEmpty()) {
                        nextMap[camId] = resolvedLink
                    } else if (currentVal.isNullOrEmpty() || force) {
                        nextMap[camId] = null
                    }
                    cur.copy(cameraSnapshots = nextMap)
                }
            } catch (_: Exception) {
                _uiState.update { cur ->
                    if (!cur.cameraSnapshots.containsKey(camId) || force) {
                        val nextMap = cur.cameraSnapshots.toMutableMap()
                        nextMap[camId] = null
                        cur.copy(cameraSnapshots = nextMap)
                    } else {
                        cur
                    }
                }
            } finally {
                pendingSnapshotIds.remove(camId)
            }
        }
    }

    fun onCardSnapshotError(cam: CameraModel) {
        val camId = cam.id
        if (camId == 0) return
        MonitorCacheManager.removeCachedCameraImageLink(getApplication(), camId, cam.cameraId)

        if (retriedCardSnapshotIds.add(camId)) {
            _uiState.update { cur ->
                val nextMap = cur.cameraSnapshots.toMutableMap()
                nextMap.remove(camId)
                cur.copy(cameraSnapshots = nextMap)
            }
            fetchCardSnapshot(cam, force = true)
        } else {
            _uiState.update { cur ->
                val nextMap = cur.cameraSnapshots.toMutableMap()
                nextMap[camId] = null
                cur.copy(cameraSnapshots = nextMap)
            }
        }
    }

    private fun buildTree(
        vmsList: List<VMSServiceModel>,
        groupList: List<CameraGroupModel>,
        cameraList: List<CameraModel>,
        cameraInGroups: List<CameraInGroupModel>
    ): List<CameraTreeNode> {
        val treeData = mutableListOf<CameraTreeNode>()
        val groupMapData = mutableMapOf<String, CameraTreeNode.GroupNode>()

        val fullVmsList = mutableListOf(
            VMSServiceModel(id = 0, name = "VMS Local", privateHost = "", publicHost = "", apiKey = "")
        )
        fullVmsList.addAll(vmsList)

        val camerasByVms = cameraList.groupBy { it.vmsId }
        val groupsByVms = groupList.groupBy { it.vmsId }
        val cameraInGroupByVms = mutableMapOf<Int, MutableList<CameraInGroupModel>>()
        val cameraById = cameraList.associateBy { it.id }

        for (item in cameraInGroups) {
            val cam = cameraById[item.cameraId] ?: continue
            cameraInGroupByVms.getOrPut(cam.vmsId) { mutableListOf() }.add(item)
        }

        for (service in fullVmsList) {
            val serviceId = service.id

            val groups = groupsByVms[serviceId] ?: emptyList()
            val cameras = camerasByVms[serviceId] ?: emptyList()
            val camGroups = cameraInGroupByVms[serviceId] ?: emptyList()

            val vmsNodeKey = "vms-$serviceId"
            val vmsNode = CameraTreeNode.VMSNode(service, mutableListOf(), vmsNodeKey)
            treeData.add(vmsNode)

            for (group in groups) {
                if (group.parentGroupId == 0) continue

                val groupNodeKey = "group-$serviceId-${group.id}"
                val groupNode = CameraTreeNode.GroupNode(group, mutableListOf(), groupNodeKey)
                groupMapData[groupNodeKey] = groupNode

                val parentNode = groupMapData["group-$serviceId-${group.parentGroupId}"]
                if (parentNode != null) {
                    parentNode.children.add(groupNode)
                } else {
                    vmsNode.children.add(groupNode)
                }
            }

            val groupedCameraIds = mutableSetOf<Int>()

            for (item in camGroups) {
                val cam = cameraById[item.cameraId] ?: continue
                groupedCameraIds.add(cam.id)

                val groupNode = groupMapData["group-$serviceId-${item.groupId}"] ?: continue

                val camKey = if (serviceId == 0) "cam-$serviceId-${cam.id}" else "cam-$serviceId-${cam.cameraId}"
                val camNode = CameraTreeNode.CameraLeaf(cam, camKey)
                groupNode.children.add(camNode)
            }

            for (cam in cameras) {
                if (groupedCameraIds.contains(cam.id)) continue

                val camKey = if (serviceId == 0) "cam-$serviceId-${cam.id}" else "cam-$serviceId-${cam.cameraId}"
                val camNode = CameraTreeNode.CameraLeaf(cam, camKey)
                vmsNode.children.add(camNode)
            }

            sortNodeChildren(vmsNode.children)
        }

        fun pruneEmptyNodes(nodes: MutableList<CameraTreeNode>): MutableList<CameraTreeNode> {
            val it = nodes.iterator()
            while (it.hasNext()) {
                val node = it.next()
                if (node is CameraTreeNode.GroupNode) {
                    pruneEmptyNodes(node.children)
                    if (node.children.isEmpty()) {
                        it.remove()
                    }
                } else if (node is CameraTreeNode.VMSNode) {
                    pruneEmptyNodes(node.children)
                    if (node.children.isEmpty()) {
                        it.remove()
                    }
                }
            }
            return nodes
        }

        val prunedTree = pruneEmptyNodes(treeData)
        return prunedTree.toList()
    }

    private fun ensureDefaultExpandedNodes(tree: List<CameraTreeNode>) {
        if (_uiState.value.expandedNodes.isEmpty() && tree.isNotEmpty()) {
            val defaults = tree.map { it.key }.toSet()
            _uiState.update { it.copy(expandedNodes = defaults) }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            val cachedData = MonitorCacheManager.getMonitorData(getApplication())
            if (cachedData != null && (cachedData.vmsList.isNotEmpty() || cachedData.cameraList.isNotEmpty())) {
                val finalTree = buildTree(cachedData.vmsList, cachedData.groupList, cachedData.cameraList, cachedData.cameraInGroups)
                _uiState.update { state ->
                    clampAndNormalizeState(
                        state.copy(
                            isLoading = false,
                            error = null,
                            treeData = finalTree,
                            aiServices = cachedData.aiServices.ifEmpty { state.aiServices }
                        )
                    )
                }
                ensureDefaultExpandedNodes(finalTree)
                restoreOrSelectInitialCameras(finalTree)
                loadVisibleCameraSnapshots()
            } else {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                // Gọi song song cả 5 API cây camera & AI Services để giảm thời gian tải xuống 1 RTT duy nhất
                coroutineScope {
                    val vmsDeferred = async { runCatching { vmsApi.getVMSServiceList() }.getOrNull() }
                    val groupDeferred = async { runCatching { vmsApi.getCameraGroupList() }.getOrNull() }
                    val cameraDeferred = async { runCatching { vmsApi.getCameraList() }.getOrNull() }
                    val cameraInGroupDeferred = async { runCatching { vmsApi.getCameraInGroupList() }.getOrNull() }
                    val aiServiceDeferred = async { runCatching { eventApi.getAIServiceList() }.getOrNull() }

                    val vmsResponse = vmsDeferred.await()
                    val groupResponse = groupDeferred.await()
                    val cameraResponse = cameraDeferred.await()
                    val cameraInGroupResponse = cameraInGroupDeferred.await()
                    val aiServiceResponse = aiServiceDeferred.await()

                    if (vmsResponse?.isSuccessful == true &&
                        groupResponse?.isSuccessful == true &&
                        cameraResponse?.isSuccessful == true &&
                        cameraInGroupResponse?.isSuccessful == true
                    ) {
                        val vmsList = vmsResponse.body() ?: emptyList()
                        val groupList = groupResponse.body() ?: emptyList()
                        val cameraList = cameraResponse.body() ?: emptyList()
                        val cameraInGroups = cameraInGroupResponse.body() ?: emptyList()
                        val aiServices = if (aiServiceResponse?.isSuccessful == true) {
                            aiServiceResponse.body() ?: emptyList()
                        } else _uiState.value.aiServices

                        val finalTree = buildTree(vmsList, groupList, cameraList, cameraInGroups)

                        _uiState.update { state ->
                            clampAndNormalizeState(
                                state.copy(isLoading = false, treeData = finalTree, aiServices = aiServices)
                            )
                        }
                        ensureDefaultExpandedNodes(finalTree)
                        restoreOrSelectInitialCameras(finalTree)
                        loadVisibleCameraSnapshots()

                        MonitorCacheManager.saveMonitorData(
                            getApplication(),
                            CachedMonitorData(
                                vmsList = vmsList,
                                groupList = groupList,
                                cameraList = cameraList,
                                cameraInGroups = cameraInGroups,
                                aiServices = aiServices,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    } else if (_uiState.value.treeData.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load data") }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (it.treeData.isEmpty()) e.toUserFriendlyMessage() else null
                    )
                }
            }
        }
    }

    private fun sortNodeChildren(children: MutableList<CameraTreeNode>) {
        if (children.size <= 1) return
        children.sortWith(Comparator { a, b ->
            val aIsLeaf = if (a is CameraTreeNode.CameraLeaf) 1 else 0
            val bIsLeaf = if (b is CameraTreeNode.CameraLeaf) 1 else 0
            aIsLeaf.compareTo(bIsLeaf)
        })
        for (child in children) {
            if (child is CameraTreeNode.GroupNode && child.children.size > 1) {
                sortNodeChildren(child.children)
            } else if (child is CameraTreeNode.VMSNode && child.children.size > 1) {
                sortNodeChildren(child.children)
            }
        }
    }

    fun toggleNodeExpansion(nodeId: String) {
        _uiState.update { state ->
            val newExpanded = state.expandedNodes.toMutableSet()
            if (newExpanded.contains(nodeId)) {
                newExpanded.remove(nodeId)
            } else {
                newExpanded.add(nodeId)
            }

            val prefs = GlobalData.preferences
            prefs.edit().putStringSet("saved_expanded_nodes", newExpanded).apply()

            state.copy(expandedNodes = newExpanded)
        }
    }

    private fun getVmsNameForCamera(vmsId: Int): String {
        for (node in _uiState.value.treeData) {
            if (node is CameraTreeNode.VMSNode && node.vms.id == vmsId) {
                return node.vms.name
            }
        }
        return ""
    }

    /**
     * Xử lý chọn camera trên màn hình Trực tiếp:
     * - Chế độ 1x1:
     *   + Nếu camera đang phát ở ô 0 -> nháy viền (`triggerBlink(0)`).
     *   + Nếu bấm vào camera khác -> phát ngay camera mới vào ô 0 (`addCamera(camera, 0)`).
     * - Chế độ 2x2:
     *   + Nếu camera đã có trong 1 trong 4 ô -> chuyển `activeSlotIndex` tới ô đó và nháy viền (`triggerBlink(existingIndex)`).
     *   + Nếu cả 4 ô đều đã có camera đang phát -> báo lưới đã đầy, không ghi đè.
     *   + Nếu còn ô trống -> ưu tiên đặt vào `activeSlotIndex` nếu trống, hoặc ô trống đầu tiên, sau đó chuyển `activeSlotIndex` sang ô trống tiếp theo.
     */
    fun selectCameraFromTree(
        camera: CameraModel,
        onGridFull: (() -> Unit)? = null
    ) {
        val currentState = _uiState.value
        if (currentState.gridSize == 1) {
            _uiState.update { it.copy(activeSlotIndex = 0) }
            val currentCam0 = currentState.selectedCameras.getOrNull(0)
            if (currentCam0?.camera?.id == camera.id) {
                triggerBlink(0)
                return
            }
            addCamera(camera, 0)
            return
        }

        // Chế độ 2x2 (gridSize == 4)
        val existingIndex = (0 until 4).indexOfFirst { idx ->
            currentState.selectedCameras.getOrNull(idx)?.camera?.id == camera.id
        }
        if (existingIndex != -1) {
            _uiState.update { it.copy(activeSlotIndex = existingIndex) }
            triggerBlink(existingIndex)
            return
        }

        val preferredSlot = currentState.activeSlotIndex.coerceIn(0, 3)
        val targetSlot = if (currentState.selectedCameras.getOrNull(preferredSlot) == null) {
            preferredSlot
        } else {
            (0 until 4).firstOrNull { idx ->
                currentState.selectedCameras.getOrNull(idx) == null
            }
        }

        if (targetSlot == null) {
            if (onGridFull != null) {
                onGridFull()
            } else {
                com.tenli.oneview.ui.component.MobileTopPopupManager.warning(
                    "Lưới đang phát đã đầy, không thể phát thêm!"
                )
            }
            return
        }

        addCamera(camera, targetSlot)

        val updatedCameras = _uiState.value.selectedCameras
        val nextEmpty = (0 until 4).firstOrNull { idx ->
            idx != targetSlot && updatedCameras.getOrNull(idx) == null
        }
        _uiState.update {
            it.copy(activeSlotIndex = nextEmpty ?: targetSlot)
        }
    }

    fun addCamera(camera: CameraModel, slotIndex: Int, startPolling: Boolean = true) {
        if (slotIndex !in 0..3) return
        val currentState = _uiState.value

        val existing = currentState.selectedCameras.getOrNull(slotIndex)
        if (existing?.camera?.id == camera.id && existing.streamUrl.startsWith("ws")) {
            return
        }

        val vmsName = getVmsNameForCamera(camera.vmsId)
        // Đồng bộ LiveVideoView.vue (line 969): trên mobile ưu tiên luồng phụ (channel = 1) nếu camera có sub stream
        val initialChannel = if (camera.sub.isNotBlank()) 1 else 0

        // Ưu tiên lấy ngay ảnh snapshot đã cache (0ms) giống cachedSnapshotUrl trên PWA
        val cachedRawSnap = MonitorCacheManager.getCachedCameraImageLink(
            getApplication(),
            camera.id,
            camera.cameraId
        ).orEmpty()
        val initialSnapshotUrl = resolveFullMediaUrl(cachedRawSnap)

        val updatedCameras = currentState.selectedCameras.toMutableList()
        while (updatedCameras.size < 4) updatedCameras.add(null)
        updatedCameras[slotIndex] = SelectedCamera(
            camera = camera,
            streamUrl = "",
            fallbackImageUrl = initialSnapshotUrl,
            vmsName = vmsName,
            channel = initialChannel,
            hasFallbackTried = false,
            snapshotRetried = false
        )

        _uiState.update { it.copy(selectedCameras = updatedCameras) }
        saveSelectedCamerasToPrefs(updatedCameras)

        if (startPolling && isMonitorTabActive) {
            ensureCameraSnapshotUrl(camera, slotIndex, force = false)
            startLiveStreamPolling(camera, slotIndex, channel = initialChannel, isFallback = false)
            if (slotIndex == 0) {
                fetchListData()
            }
        }
    }

    private fun ensureCameraSnapshotUrl(camera: CameraModel, slotIndex: Int, force: Boolean) {
        val app = getApplication<Application>()
        if (!force && MonitorCacheManager.isSnapshotCacheFresh(app, camera.id)) {
            val cached = MonitorCacheManager.getCachedCameraImageLink(app, camera.id, camera.cameraId)
            if (!cached.isNullOrBlank()) {
                return
            }
        }

        viewModelScope.launch {
            try {
                val response = vmsApi.getCameraImageLink(camera.id)
                if (response.isSuccessful) {
                    val rawLink = response.body()?.link?.trim().orEmpty()
                    if (rawLink.isNotEmpty() && rawLink != "NO_IMAGE") {
                        MonitorCacheManager.setCachedCameraImageLink(app, camera.id, rawLink, camera.cameraId)
                        val resolvedUrl = resolveFullMediaUrl(rawLink)
                        _uiState.update { state ->
                            val cameras = state.selectedCameras.toMutableList()
                            val current = cameras.getOrNull(slotIndex)
                            if (current != null && current.camera.id == camera.id) {
                                // Chỉ cập nhật nếu chưa có frame chụp trực tiếp mới hơn từ luồng live
                                val latestCached = MonitorCacheManager.getCachedCameraImageLink(app, camera.id, camera.cameraId)
                                val finalSnap = if (latestCached != null && latestCached.startsWith("file://")) {
                                    latestCached
                                } else {
                                    resolvedUrl
                                }
                                cameras[slotIndex] = current.copy(fallbackImageUrl = finalSnap)
                            }
                            state.copy(selectedCameras = cameras)
                        }
                    } else if (force) {
                        clearSlotSnapshotUrl(camera.id, slotIndex)
                    }
                } else if (force) {
                    clearSlotSnapshotUrl(camera.id, slotIndex)
                }
            } catch (_: Exception) {
                if (force) {
                    clearSlotSnapshotUrl(camera.id, slotIndex)
                }
            }
        }
    }

    private fun clearSlotSnapshotUrl(cameraId: Int, slotIndex: Int) {
        _uiState.update { state ->
            val cameras = state.selectedCameras.toMutableList()
            val current = cameras.getOrNull(slotIndex)
            if (current != null && current.camera.id == cameraId) {
                cameras[slotIndex] = current.copy(fallbackImageUrl = "", snapshotRetried = true)
            }
            state.copy(selectedCameras = cameras)
        }
    }

    /**
     * Đồng bộ `onSnapshotError()` trong `LiveVideoView.vue` (lines 540-559):
     * Khi ảnh snapshot cache bị lỗi hoặc hết hạn, xoá khỏi cache và gọi lại `CameraImageLink/{id}` với `force = true` 1 lần.
     */
    fun onSnapshotLoadError(slotIndex: Int) {
        val current = _uiState.value.selectedCameras.getOrNull(slotIndex) ?: return
        val camera = current.camera
        MonitorCacheManager.removeCachedCameraImageLink(getApplication(), camera.id, camera.cameraId)

        if (!current.snapshotRetried) {
            _uiState.update { state ->
                val cameras = state.selectedCameras.toMutableList()
                val slotCam = cameras.getOrNull(slotIndex)
                if (slotCam != null && slotCam.camera.id == camera.id) {
                    cameras[slotIndex] = slotCam.copy(snapshotRetried = true)
                }
                state.copy(selectedCameras = cameras)
            }
            ensureCameraSnapshotUrl(camera, slotIndex, force = true)
        } else {
            clearSlotSnapshotUrl(camera.id, slotIndex)
        }
    }

    fun playVideo(videoUrl: String, streamType: String, fallbackImageUrl: String = "") {
        if (videoUrl.isBlank()) return
        streamJobs[0]?.cancel()

        _uiState.update { state ->
            val cameras = state.selectedCameras.toMutableList()
            val currentSelection = cameras[0]
            if (currentSelection != null) {
                val fullUrl = resolveFullMediaUrl(videoUrl)
                val fullFallback = if (fallbackImageUrl.isNotBlank()) {
                    resolveFullMediaUrl(fallbackImageUrl)
                } else {
                    currentSelection.fallbackImageUrl
                }

                cameras[0] = currentSelection.copy(
                    streamUrl = fullUrl,
                    streamType = streamType,
                    fallbackImageUrl = fullFallback
                )
            }
            state.copy(selectedCameras = cameras)
        }
    }

    fun refreshActiveStreams() {
        val currentState = _uiState.value
        val cameras = currentState.selectedCameras.toMutableList()
        var hasActive = false
        val app = getApplication<Application>()

        cameras.forEachIndexed { index, selectedCamera ->
            if (selectedCamera != null && index < currentState.gridSize) {
                val latestSnap = MonitorCacheManager.getCachedCameraImageLink(
                    app,
                    selectedCamera.camera.id,
                    selectedCamera.camera.cameraId
                )?.let { resolveFullMediaUrl(it) } ?: selectedCamera.fallbackImageUrl

                cameras[index] = selectedCamera.copy(
                    streamUrl = "",
                    retryCount = 0,
                    hasFallbackTried = false,
                    fallbackImageUrl = latestSnap
                )
                hasActive = true
            }
        }

        if (hasActive) {
            _uiState.update { it.copy(selectedCameras = cameras) }

            currentState.selectedCameras.forEachIndexed { index, selectedCamera ->
                if (selectedCamera != null && index < currentState.gridSize) {
                    val initialChannel = if (selectedCamera.camera.sub.isNotBlank()) 1 else 0
                    ensureCameraSnapshotUrl(selectedCamera.camera, index, force = false)
                    startLiveStreamPolling(selectedCamera.camera, index, channel = initialChannel, isFallback = false)
                }
            }
        }
    }

    /**
     * Đồng bộ cơ chế polling `check()` + `loadLiveStream()` + `fallbackStream()` của `LiveVideoView.vue` (lines 861-1023):
     * - Gọi `POST /VMS/api/LiveStream` mỗi 2000ms (tối đa 10 lần trên mobile) cho đến khi cả `link` và `videoCodec` đều có dữ liệu.
     * - Nếu hết 10 lần mà chưa có codec và camera có luồng phụ (`sub`), tự động chuyển đổi kênh (`sub <-> main`) để thử tiếp.
     */
    private fun startLiveStreamPolling(
        camera: CameraModel,
        slotIndex: Int,
        channel: Int,
        isFallback: Boolean = false
    ) {
        if (slotIndex !in 0..3) return
        streamJobs[slotIndex]?.cancel()
        if (!isMonitorTabActive) return

        streamJobs[slotIndex] = viewModelScope.launch {
            val maxPollAttempts = 10
            for (attempt in 1..maxPollAttempts) {
                val activeCam = _uiState.value.selectedCameras.getOrNull(slotIndex)
                if (activeCam == null || activeCam.camera.id != camera.id) {
                    return@launch
                }

                try {
                    val requestModel = LiveStreamModel(
                        cameraId = camera.id,
                        channel = channel,
                        link = "",
                        videoCodec = "",
                        audioCodec = "",
                        videoCodecTag = ""
                    )
                    val response = vmsApi.createLiveStream(requestModel)
                    if (response.isSuccessful) {
                        val liveStreamModel = response.body()
                        val rawLink = liveStreamModel?.link?.trim().orEmpty()
                        val videoCodec = liveStreamModel?.videoCodec?.trim().orEmpty()

                        if (rawLink.isNotEmpty() && videoCodec.isNotEmpty()) {
                            val wsUrl = resolveWebSocketUrl(rawLink)
                            _uiState.update { state ->
                                val cameras = state.selectedCameras.toMutableList()
                                val currentSelection = cameras.getOrNull(slotIndex)
                                if (currentSelection != null && currentSelection.camera.id == camera.id) {
                                    cameras[slotIndex] = currentSelection.copy(
                                        streamUrl = wsUrl,
                                        videoCodecTag = liveStreamModel?.videoCodecTag?.trim().orEmpty(),
                                        channel = channel,
                                        hasFallbackTried = isFallback,
                                        retryCount = 0,
                                        streamType = "LIVE"
                                    )
                                }
                                state.copy(
                                    selectedCameras = cameras,
                                    error = null
                                )
                            }
                            return@launch
                        }
                    }
                } catch (_: kotlinx.coroutines.CancellationException) {
                    throw kotlinx.coroutines.CancellationException()
                } catch (_: Exception) {
                    // Tiếp tục chờ tới lượt poll tiếp theo sau 2000ms
                }

                if (attempt < maxPollAttempts) {
                    kotlinx.coroutines.delay(2000L)
                }
            }

            // Đã thử hết 10 lần poll cho channel hiện tại mà chưa nhận được link + videoCodec
            val currentSelection = _uiState.value.selectedCameras.getOrNull(slotIndex) ?: return@launch
            if (currentSelection.camera.id != camera.id) return@launch

            if (!isFallback && camera.sub.isNotBlank()) {
                val fallbackChannel = if (channel == 1) 0 else 1
                _uiState.update { state ->
                    val cameras = state.selectedCameras.toMutableList()
                    val cur = cameras.getOrNull(slotIndex)
                    if (cur != null && cur.camera.id == camera.id) {
                        cameras[slotIndex] = cur.copy(
                            streamUrl = "",
                            channel = fallbackChannel,
                            hasFallbackTried = true,
                            retryCount = 0
                        )
                    }
                    state.copy(selectedCameras = cameras)
                }
                startLiveStreamPolling(camera, slotIndex, channel = fallbackChannel, isFallback = true)
            } else {
                _uiState.update { state ->
                    val cameras = state.selectedCameras.toMutableList()
                    val cur = cameras.getOrNull(slotIndex)
                    if (cur != null && cur.camera.id == camera.id) {
                        cameras[slotIndex] = cur.copy(
                            streamUrl = "error://no-video",
                            retryCount = 6
                        )
                    }
                    state.copy(selectedCameras = cameras)
                }
            }
        }
    }

    fun resetRetryCount(slotIndex: Int) {
        val currentState = _uiState.value
        val cameras = currentState.selectedCameras.toMutableList()
        val currentSelection = cameras.getOrNull(slotIndex) ?: return
        val latestSnap = MonitorCacheManager.getCachedCameraImageLink(
            getApplication(),
            currentSelection.camera.id,
            currentSelection.camera.cameraId
        )?.let { resolveFullMediaUrl(it) } ?: currentSelection.fallbackImageUrl
        cameras[slotIndex] = currentSelection.copy(retryCount = 0, fallbackImageUrl = latestSnap)
        _uiState.update { it.copy(selectedCameras = cameras) }
    }

    fun removeCamera(slotIndex: Int) {
        if (slotIndex !in 0..3) return
        streamJobs[slotIndex]?.cancel()
        streamJobs[slotIndex] = null
        var updatedCameras: List<SelectedCamera?> = emptyList()
        _uiState.update { state ->
            val cameras = state.selectedCameras.toMutableList()
            cameras[slotIndex] = null
            updatedCameras = cameras
            state.copy(selectedCameras = cameras, activeSlotIndex = slotIndex)
        }
        saveSelectedCamerasToPrefs(updatedCameras)
    }

    fun retryCameraStream(slotIndex: Int) {
        if (slotIndex !in 0..3) return
        val currentState = _uiState.value
        val cameras = currentState.selectedCameras.toMutableList()
        val currentSelection = cameras.getOrNull(slotIndex) ?: return

        // Nếu luồng hiện tại lỗi sau vài lần thử và chưa fallback kênh (sub <-> main), chuyển sang kênh còn lại giống playerError() trong LiveVideoView.vue
        if (currentSelection.retryCount >= 2 && !currentSelection.hasFallbackTried && currentSelection.camera.sub.isNotBlank()) {
            val nextChannel = if (currentSelection.channel == 1) 0 else 1
            cameras[slotIndex] = currentSelection.copy(
                streamUrl = "",
                channel = nextChannel,
                hasFallbackTried = true,
                retryCount = 0
            )
            _uiState.update { it.copy(selectedCameras = cameras) }
            startLiveStreamPolling(currentSelection.camera, slotIndex, channel = nextChannel, isFallback = true)
            return
        }

        if (currentSelection.retryCount >= 5) {
            if (currentSelection.retryCount == 5) {
                cameras[slotIndex] = currentSelection.copy(
                    retryCount = 6,
                    streamUrl = "error://failed"
                )
                _uiState.update { it.copy(selectedCameras = cameras) }
            }
            return
        }

        val nextRetry = currentSelection.retryCount + 1
        cameras[slotIndex] = currentSelection.copy(
            streamUrl = "",
            retryCount = nextRetry
        )
        _uiState.update { it.copy(selectedCameras = cameras) }

        streamJobs[slotIndex]?.cancel()
        streamJobs[slotIndex] = viewModelScope.launch {
            val delayMs = Math.min(1000 * Math.pow(2.0, (nextRetry - 1).toDouble()).toLong(), 8000L)
            kotlinx.coroutines.delay(delayMs)
            startLiveStreamPolling(
                camera = currentSelection.camera,
                slotIndex = slotIndex,
                channel = currentSelection.channel,
                isFallback = currentSelection.hasFallbackTried
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setTab(tab: MonitorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        fetchListData()
    }

    fun setTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(selectedTimeFilter = filter) }
        fetchListData()
    }

    private fun fetchListData() {
        val selectedCam = _uiState.value.selectedCameras.firstOrNull()?.camera ?: return

        viewModelScope.launch {
            try {
                val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(_uiState.value.selectedTimeFilter)

                _uiState.update { it.copy(hasMoreEvents = true, hasMorePlaybacks = true) }

                if (_uiState.value.selectedTab == MonitorTab.EVENTS) {
                    val uuid = selectedCam.extra?.uuid
                    if (uuid != null) {
                        val response = eventApi.getDataList(
                            cameraUUID = uuid,
                            from = fromTime / 1000,
                            to = toTime / 1000,
                            count = 20
                        )
                        if (response.isSuccessful) {
                            val newEvents = response.body() ?: emptyList()
                            _uiState.update { it.copy(events = newEvents, hasMoreEvents = newEvents.size == 20) }
                        }
                    }
                } else {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val fromStr = sdf.format(java.util.Date(fromTime))
                    val toStr = sdf.format(java.util.Date(toTime))

                    val response = vmsApi.getVideoList(
                        camera = selectedCam.id,
                        count = 20,
                        from = fromStr,
                        to = toStr
                    )
                    if (response.isSuccessful) {
                        val newPlaybacks = response.body() ?: emptyList()
                        _uiState.update { it.copy(playbacks = newPlaybacks, hasMorePlaybacks = newPlaybacks.size == 20) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MonitorViewModel", "Error fetching list data", e)
            }
        }
    }

    fun loadMoreData() {
        val currentState = _uiState.value
        if (currentState.isPaginating) return

        val selectedCam = currentState.selectedCameras.firstOrNull()?.camera ?: return

        if (currentState.selectedTab == MonitorTab.EVENTS && (!currentState.hasMoreEvents || currentState.events.isEmpty())) return
        if (currentState.selectedTab == MonitorTab.PLAYBACK && (!currentState.hasMorePlaybacks || currentState.playbacks.isEmpty())) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPaginating = true) }
            try {
                val (fromTime, toTime) = TimeFilter.resolveTimeRangeMillis(currentState.selectedTimeFilter)

                if (currentState.selectedTab == MonitorTab.EVENTS) {
                    val uuid = selectedCam.extra?.uuid
                    if (uuid != null) {
                        val lastId = currentState.events.last().id
                        val response = eventApi.getDataList(
                            cameraUUID = uuid,
                            lastId = lastId,
                            from = fromTime / 1000,
                            to = toTime / 1000,
                            count = 20
                        )
                        if (response.isSuccessful) {
                            val newEvents = response.body() ?: emptyList()
                            _uiState.update {
                                it.copy(
                                    events = it.events + newEvents,
                                    hasMoreEvents = newEvents.size == 20,
                                    isPaginating = false
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isPaginating = false) }
                        }
                    } else {
                        _uiState.update { it.copy(isPaginating = false) }
                    }
                } else {
                    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault())
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    val fromStr = sdf.format(java.util.Date(fromTime))
                    val lastVideoTimeStr = currentState.playbacks.last().time

                    val response = vmsApi.getVideoList(
                        camera = selectedCam.id,
                        count = 20,
                        from = fromStr,
                        to = lastVideoTimeStr
                    )
                    if (response.isSuccessful) {
                        val newPlaybacks = response.body() ?: emptyList()
                        _uiState.update {
                            it.copy(
                                playbacks = it.playbacks + newPlaybacks,
                                hasMorePlaybacks = newPlaybacks.size == 20,
                                isPaginating = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isPaginating = false) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MonitorViewModel", "Error loading more list data", e)
                _uiState.update { it.copy(isPaginating = false) }
            }
        }
    }

    private fun findCameraById(nodes: List<CameraTreeNode>, id: Int): CameraModel? {
        for (node in nodes) {
            when (node) {
                is CameraTreeNode.CameraLeaf -> if (node.camera.id == id) return node.camera
                is CameraTreeNode.GroupNode -> {
                    val found = findCameraById(node.children, id)
                    if (found != null) return found
                }
                is CameraTreeNode.VMSNode -> {
                    val found = findCameraById(node.children, id)
                    if (found != null) return found
                }
            }
        }
        return null
    }

    private fun findFirstCamera(nodes: List<CameraTreeNode>): CameraModel? {
        for (node in nodes) {
            when (node) {
                is CameraTreeNode.CameraLeaf -> return node.camera
                is CameraTreeNode.GroupNode -> {
                    val found = findFirstCamera(node.children)
                    if (found != null) return found
                }
                is CameraTreeNode.VMSNode -> {
                    val found = findFirstCamera(node.children)
                    if (found != null) return found
                }
            }
        }
        return null
    }
}
