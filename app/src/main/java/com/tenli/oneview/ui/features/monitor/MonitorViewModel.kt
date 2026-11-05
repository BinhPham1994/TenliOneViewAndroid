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
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.VMSServiceModel
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.util.toUserFriendlyMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer

sealed class CameraTreeNode {
    abstract val key: String
    data class VMSNode(val vms: VMSServiceModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class GroupNode(val group: CameraGroupModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class CameraLeaf(val camera: CameraModel, override val key: String) : CameraTreeNode()
}

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
    val retryCount: Int = 0,
    val fallbackImageUrl: String = "",
    val channel: Int = 1,
    val hasFallbackTried: Boolean = false,
    val snapshotRetried: Boolean = false
)

data class MonitorUiState(
    val isLoading: Boolean = false,
    val gridSize: Int = 1, // 1 (1x1) or 4 (2x2)
    val activeSlotIndex: Int = 0,
    val blinkStates: List<Boolean> = List(MonitorViewModel.SLOT_COUNT) { false },
    val searchQuery: String = "",
    val selectedCameras: List<SelectedCamera?> = List(MonitorViewModel.SLOT_COUNT) { null },
    val expandedNodes: Set<String> = emptySet(),
    val selectedFilterNodeKey: String = MonitorViewModel.FILTER_ALL,
    val showGroupFilterSheet: Boolean = false,
    val currentCameraPage: Int = 1,
    val cameraSnapshots: Map<Int, String?> = emptyMap(),
    val error: String? = null,

    // ---- Dữ liệu suy ra từ cây camera – tính sẵn trong ViewModel, UI chỉ việc đọc ----
    val hasAvailableCameras: Boolean = false,
    val totalAllCamerasCount: Int = 0,
    val groupTreeRows: List<GroupSheetRow> = emptyList(),
    val pagedCameraItems: List<CameraCardItem> = emptyList(),
    val totalCameraPages: Int = 1
)

/**
 * Chỉ mục tính sẵn từ cây camera. Dựng 1 lần trên [Dispatchers.Default] mỗi khi cây thay đổi,
 * nhờ đó mọi thao tác lọc / đếm / phân trang / tìm kiếm sau đó chỉ là tra cứu O(1) hoặc duyệt O(n) rất rẻ
 * (trước đây mỗi lần đổi trang/gõ phím lại duyệt toàn bộ cây nhiều lần trên Main thread).
 */
private class CameraTreeIndex(val tree: List<CameraTreeNode>) {
    private val nodeByKey = HashMap<String, CameraTreeNode>()
    private val camerasByNodeKey = HashMap<String, List<CameraCardItem>>()
    private val normalizedNameById = HashMap<Int, String>()
    private val ipTextById = HashMap<Int, String>()

    /** Các VMS có ít nhất 1 camera */
    val vmsNodes: List<CameraTreeNode.VMSNode>

    /** Toàn bộ camera (không trùng id) theo thứ tự duyệt cây */
    val allCameras: List<CameraCardItem>
    val totalCameraCount: Int
    private val cameraById: Map<Int, CameraModel>

    init {
        tree.forEach { collect(it) }
        vmsNodes = tree.filterIsInstance<CameraTreeNode.VMSNode>().filter { count(it) > 0 }
        allCameras = mergeUnique(tree.map { camerasOf(it) })
        totalCameraCount = tree.sumOf { count(it) }
        cameraById = allCameras.associate { it.camera.id to it.camera }
        for (item in allCameras) {
            val cam = item.camera
            normalizedNameById[cam.id] = MonitorViewModel.normalizeVietnameseText(cam.name)
            ipTextById[cam.id] = MonitorViewModel.getCameraIpText(cam).lowercase()
        }
    }

    /** Duyệt hậu thứ tự: danh sách camera của node = hợp (giữ thứ tự, không trùng id) của các node con. */
    private fun collect(node: CameraTreeNode): List<CameraCardItem> {
        // Giữ ngữ nghĩa "node đầu tiên khớp key theo thứ tự duyệt" như hàm tìm kiếm cũ
        nodeByKey.putIfAbsent(node.key, node)
        val items = when (node) {
            is CameraTreeNode.CameraLeaf -> {
                val cam = node.camera
                if (cam.id != 0) listOf(CameraCardItem(key = "cam-${cam.id}", camera = cam)) else emptyList()
            }
            is CameraTreeNode.GroupNode -> mergeUnique(node.children.map { collect(it) })
            is CameraTreeNode.VMSNode -> mergeUnique(node.children.map { collect(it) })
        }
        camerasByNodeKey.putIfAbsent(node.key, items)
        return items
    }

    private fun mergeUnique(lists: List<List<CameraCardItem>>): List<CameraCardItem> {
        if (lists.size == 1) return lists[0]
        val map = LinkedHashMap<Int, CameraCardItem>()
        for (list in lists) for (item in list) map.putIfAbsent(item.camera.id, item)
        return map.values.toList()
    }

    private fun camerasOf(node: CameraTreeNode): List<CameraCardItem> = camerasByNodeKey[node.key].orEmpty()

    fun count(node: CameraTreeNode): Int = camerasOf(node).size

    fun hasNode(key: String): Boolean = nodeByKey.containsKey(key)

    fun findCamera(id: Int): CameraModel? = cameraById[id]

    fun camerasForFilter(filterKey: String): List<CameraCardItem> {
        if (filterKey == MonitorViewModel.FILTER_ALL) return allCameras
        val node = nodeByKey[filterKey] ?: return allCameras
        return camerasOf(node)
    }

    fun matchesSearch(cam: CameraModel, normalizedQuery: String, rawLowerQuery: String): Boolean {
        if (normalizedNameById[cam.id].orEmpty().contains(normalizedQuery)) return true
        val ip = ipTextById[cam.id].orEmpty()
        return ip.isNotEmpty() && ip.contains(rawLowerQuery)
    }

    fun groupRows(expandedNodes: Set<String>): List<GroupSheetRow> {
        if (vmsNodes.isEmpty()) return emptyList()
        val rows = mutableListOf<GroupSheetRow>()

        fun subGroupsOf(children: List<CameraTreeNode>) =
            children.filter { it is CameraTreeNode.GroupNode && count(it) > 0 }

        fun walkGroups(nodes: List<CameraTreeNode>, depth: Int) {
            for (node in nodes) {
                if (node !is CameraTreeNode.GroupNode) continue
                val count = count(node)
                if (count <= 0) continue
                val subGroups = subGroupsOf(node.children)
                val hasSubGroups = subGroups.isNotEmpty()
                val isExpanded = expandedNodes.contains(node.key)
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
                if (hasSubGroups && isExpanded) walkGroups(subGroups, depth + 1)
            }
        }

        for (vmsNode in vmsNodes) {
            val subGroups = subGroupsOf(vmsNode.children)
            val hasSubGroups = subGroups.isNotEmpty()
            val isExpanded = expandedNodes.contains(vmsNode.key)
            rows.add(
                GroupSheetRow(
                    key = vmsNode.key,
                    title = vmsNode.vms.name.ifBlank { "VMS" },
                    depth = 0,
                    hasSubGroups = hasSubGroups,
                    isExpanded = isExpanded,
                    count = count(vmsNode),
                    isVmsNode = true
                )
            )
            if (hasSubGroups && isExpanded) walkGroups(subGroups, 1)
        }
        return rows
    }
}

class MonitorViewModel(application: Application) : AndroidViewModel(application) {
    private val app: Application get() = getApplication()
    private val vmsApi get() = ApiProvider.vmsApi
    private val eventApi get() = ApiProvider.eventApi

    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    /** Chỉ được đọc/ghi trên Main thread (cùng luồng với _uiState.update) */
    private var treeIndex = CameraTreeIndex(emptyList())

    /** AI services chỉ dùng để lưu kèm cache Monitor (các màn hình khác đọc lại), UI màn hình này không hiển thị */
    private var aiServices: List<AIServiceModel> = emptyList()

    private val streamJobs = arrayOfNulls<Job>(SLOT_COUNT)
    private val blinkJobs = arrayOfNulls<Job>(SLOT_COUNT)
    private var snapshotLookupJob: Job? = null
    private var isMonitorTabActive: Boolean = false
    private val pendingSnapshotIds = java.util.Collections.synchronizedSet(mutableSetOf<Int>())
    private val retriedCardSnapshotIds = java.util.Collections.synchronizedSet(mutableSetOf<Int>())

    companion object {
        const val CAMERA_PAGE_SIZE = 10
        const val SLOT_COUNT = 4
        const val FILTER_ALL = "all"

        private const val PREF_EXPANDED_NODES = "saved_expanded_nodes"
        private const val PREF_GRID_SIZE = "saved_monitor_grid_size"
        private const val PREF_GRID_CAMERA_IDS = "saved_monitor_grid_camera_ids"
        private const val PREF_CAMERA_ID = "saved_monitor_camera_id"

        /** Đồng bộ LiveVideoView.vue: poll `LiveStream` mỗi 2000ms, tối đa 10 lần trên mobile */
        private const val LIVE_POLL_MAX_ATTEMPTS = 10
        private const val LIVE_POLL_INTERVAL_MS = 2000L
        private const val BLINK_DURATION_MS = 1000L
        private const val RETRY_MAX_DELAY_MS = 8000L
        private const val NO_IMAGE = "NO_IMAGE"

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
            return extractHostFromRtsp(cam.sub)
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                MonitorViewModel(application)
            }
        }

        /** Đồng bộ LiveVideoView.vue (line 969): trên mobile ưu tiên luồng phụ (channel = 1, độ phân giải thấp) nếu có */
        private fun preferredChannel(camera: CameraModel): Int = if (camera.sub.isNotBlank()) 1 else 0

        private fun List<SelectedCamera?>.toPaddedMutableList(): MutableList<SelectedCamera?> =
            toMutableList().also { list -> while (list.size < SLOT_COUNT) list.add(null) }

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

                val vmsNode = CameraTreeNode.VMSNode(service, mutableListOf(), "vms-$serviceId")
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
                fun camKey(cam: CameraModel) =
                    if (serviceId == 0) "cam-$serviceId-${cam.id}" else "cam-$serviceId-${cam.cameraId}"

                for (item in camGroups) {
                    val cam = cameraById[item.cameraId] ?: continue
                    groupedCameraIds.add(cam.id)
                    val groupNode = groupMapData["group-$serviceId-${item.groupId}"] ?: continue
                    groupNode.children.add(CameraTreeNode.CameraLeaf(cam, camKey(cam)))
                }

                for (cam in cameras) {
                    if (groupedCameraIds.contains(cam.id)) continue
                    vmsNode.children.add(CameraTreeNode.CameraLeaf(cam, camKey(cam)))
                }

                sortNodeChildren(vmsNode.children)
            }

            pruneEmptyNodes(treeData)
            return treeData.toList()
        }

        private fun pruneEmptyNodes(nodes: MutableList<CameraTreeNode>) {
            val it = nodes.iterator()
            while (it.hasNext()) {
                val children = when (val node = it.next()) {
                    is CameraTreeNode.GroupNode -> node.children
                    is CameraTreeNode.VMSNode -> node.children
                    is CameraTreeNode.CameraLeaf -> continue
                }
                pruneEmptyNodes(children)
                if (children.isEmpty()) it.remove()
            }
        }

        /** Nhóm con đứng trước, camera lẻ đứng sau (sort ổn định – giữ thứ tự gốc trong cùng loại) */
        private fun sortNodeChildren(children: MutableList<CameraTreeNode>) {
            if (children.size <= 1) return
            children.sortBy { if (it is CameraTreeNode.CameraLeaf) 1 else 0 }
            for (child in children) {
                when (child) {
                    is CameraTreeNode.GroupNode -> sortNodeChildren(child.children)
                    is CameraTreeNode.VMSNode -> sortNodeChildren(child.children)
                    is CameraTreeNode.CameraLeaf -> Unit
                }
            }
        }

        private fun findFirstCamera(nodes: List<CameraTreeNode>): CameraModel? {
            for (node in nodes) {
                val found = when (node) {
                    is CameraTreeNode.CameraLeaf -> node.camera
                    is CameraTreeNode.GroupNode -> findFirstCamera(node.children)
                    is CameraTreeNode.VMSNode -> findFirstCamera(node.children)
                }
                if (found != null) return found
            }
            return null
        }
    }

    init {
        val prefs = GlobalData.preferences
        val savedNodes = prefs.getStringSet(PREF_EXPANDED_NODES, emptySet()) ?: emptySet()
        val savedGridSize = if (prefs.getInt(PREF_GRID_SIZE, 1) == 4) 4 else 1
        _uiState.update { it.copy(expandedNodes = savedNodes, gridSize = savedGridSize) }

        loadData()
    }

    // ---------------------------------------------------------------------------------------------
    // URL helpers
    // ---------------------------------------------------------------------------------------------

    private fun getBaseDomain(): String {
        var base = UserSession.domain
            .trim()
            .trimEnd('/')
        if (base.endsWith("/api/v1")) {
            base = base.removeSuffix("/api/v1")
        }
        return base
    }

    private fun resolveFullMediaUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        if (trimmed.isEmpty() || trimmed == NO_IMAGE) return ""
        if (trimmed.startsWith("http://") ||
            trimmed.startsWith("https://") ||
            trimmed.startsWith("file://") ||
            trimmed.startsWith("data:")
        ) {
            return trimmed
        }
        val base = getBaseDomain()
        return if (trimmed.startsWith("/")) "$base$trimmed" else "$base/$trimmed"
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

    /** Đọc link snapshot đã cache (có truy cập đĩa) trên IO. Trả về null nếu chưa có cache. */
    private suspend fun readCachedSnapshot(camera: CameraModel): String? = withContext(Dispatchers.IO) {
        MonitorCacheManager.getCachedCameraImageLink(app, camera.id, camera.cameraId)?.let(::resolveFullMediaUrl)
    }

    // ---------------------------------------------------------------------------------------------
    // State helpers
    // ---------------------------------------------------------------------------------------------

    /**
     * Chuẩn hoá bộ lọc/trang và tính lại dữ liệu suy ra (danh sách camera theo trang, cây nhóm...).
     * Chỉ gọi khi cây / bộ lọc / tìm kiếm / trang / nhóm mở rộng thay đổi. Giữ nguyên instance list cũ nếu nội dung
     * không đổi để Compose bỏ qua recomposition.
     */
    private fun MonitorUiState.withDerivedData(): MonitorUiState {
        val index = treeIndex
        val filterKey = if (selectedFilterNodeKey != FILTER_ALL &&
            (index.vmsNodes.isEmpty() || !index.hasNode(selectedFilterNodeKey))
        ) FILTER_ALL else selectedFilterNodeKey

        val baseCameras = index.camerasForFilter(filterKey)
        val query = searchQuery.trim()
        val displayed = if (query.isEmpty()) {
            baseCameras
        } else {
            val normalizedQuery = normalizeVietnameseText(query)
            val rawLower = query.lowercase()
            baseCameras.filter { index.matchesSearch(it.camera, normalizedQuery, rawLower) }
        }

        val totalPages = maxOf(1, (displayed.size + CAMERA_PAGE_SIZE - 1) / CAMERA_PAGE_SIZE)
        val page = currentCameraPage.coerceIn(1, totalPages)
        val start = (page - 1) * CAMERA_PAGE_SIZE
        val paged = displayed.subList(start.coerceAtMost(displayed.size), (start + CAMERA_PAGE_SIZE).coerceAtMost(displayed.size)).toList()
        val rows = index.groupRows(expandedNodes)

        return copy(
            selectedFilterNodeKey = filterKey,
            currentCameraPage = page,
            hasAvailableCameras = index.vmsNodes.isNotEmpty(),
            totalAllCamerasCount = index.totalCameraCount,
            groupTreeRows = if (rows == groupTreeRows) groupTreeRows else rows,
            pagedCameraItems = if (paged == pagedCameraItems) pagedCameraItems else paged,
            totalCameraPages = totalPages
        )
    }

    /** Cập nhật 1 ô đang phát nếu ô đó vẫn đang giữ đúng camera [cameraId]. */
    private inline fun updateSlot(
        slotIndex: Int,
        cameraId: Int,
        crossinline transform: (SelectedCamera) -> SelectedCamera
    ) {
        _uiState.update { state ->
            val current = state.selectedCameras.getOrNull(slotIndex)
            if (current == null || current.camera.id != cameraId) return@update state
            val next = transform(current)
            if (next == current) {
                state
            } else {
                state.copy(selectedCameras = state.selectedCameras.toPaddedMutableList().also { it[slotIndex] = next })
            }
        }
    }

    private fun cancelStreamJob(slotIndex: Int) {
        streamJobs[slotIndex]?.cancel()
        streamJobs[slotIndex] = null
    }

    // ---------------------------------------------------------------------------------------------
    // Tab / grid / selection
    // ---------------------------------------------------------------------------------------------

    fun setMonitorTabActive(active: Boolean) {
        if (isMonitorTabActive == active) return
        isMonitorTabActive = active
        if (!active) {
            // Dừng mọi vòng poll LiveStream / retry khi màn hình không hiển thị hoặc app vào nền
            for (i in 0 until SLOT_COUNT) cancelStreamJob(i)
            snapshotLookupJob?.cancel()
        }
    }

    fun setGridSize(size: Int) {
        val normalized = if (size == 4) 4 else 1
        if (_uiState.value.gridSize == normalized) return
        GlobalData.preferences.edit().putInt(PREF_GRID_SIZE, normalized).apply()

        if (normalized == 1) {
            // Các ô 2..4 không còn hiển thị: dừng poll và bỏ link live cũ (link WS dùng 1 lần) để khi quay lại 2x2 lấy link mới
            for (idx in 1 until SLOT_COUNT) cancelStreamJob(idx)
        }

        _uiState.update { state ->
            val newActiveSlot = if (normalized == 1) {
                0
            } else {
                (0 until SLOT_COUNT).firstOrNull { state.selectedCameras.getOrNull(it) == null } ?: 0
            }
            val cameras = if (normalized == 1) {
                state.selectedCameras.mapIndexed { idx, cam ->
                    if (idx == 0 || cam == null) cam
                    else cam.copy(streamUrl = "", retryCount = 0, hasFallbackTried = false)
                }
            } else {
                state.selectedCameras
            }
            state.copy(gridSize = normalized, activeSlotIndex = newActiveSlot, selectedCameras = cameras)
        }

        if (normalized == 4) {
            val currentCameras = _uiState.value.selectedCameras
            for (idx in 1 until SLOT_COUNT) {
                val selected = currentCameras.getOrNull(idx)
                if (selected != null && selected.streamUrl.isEmpty()) {
                    ensureCameraSnapshotUrl(selected.camera, idx, force = false)
                    startLiveStreamPolling(selected.camera, idx, channel = selected.channel, isFallback = false)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, currentCameraPage = 1).withDerivedData() }
        loadVisibleCameraSnapshots()
    }

    fun handleCellTap(index: Int) {
        if (index !in 0 until SLOT_COUNT) return
        if (_uiState.value.gridSize == 4) {
            _uiState.update { it.copy(activeSlotIndex = index) }
        }
    }

    private fun setBlink(index: Int, value: Boolean) {
        _uiState.update { state ->
            val updated = state.blinkStates.toMutableList()
            while (updated.size < SLOT_COUNT) updated.add(false)
            updated[index] = value
            state.copy(blinkStates = updated)
        }
    }

    fun triggerBlink(index: Int) {
        if (index !in 0 until SLOT_COUNT) return
        blinkJobs[index]?.cancel()
        setBlink(index, true)
        blinkJobs[index] = viewModelScope.launch {
            delay(BLINK_DURATION_MS)
            setBlink(index, false)
        }
    }

    fun openGroupFilterSheet() {
        _uiState.update { it.copy(showGroupFilterSheet = true) }
    }

    fun closeGroupFilterSheet() {
        _uiState.update { it.copy(showGroupFilterSheet = false) }
    }

    fun handleGroupRowClick(groupKey: String) {
        _uiState.update { state ->
            val nextFilterKey = if (groupKey == FILTER_ALL || state.selectedFilterNodeKey == groupKey) {
                FILTER_ALL
            } else {
                groupKey
            }
            state.copy(
                selectedFilterNodeKey = nextFilterKey,
                currentCameraPage = 1,
                showGroupFilterSheet = false
            ).withDerivedData()
        }
        loadVisibleCameraSnapshots()
    }

    fun setCameraPage(page: Int) {
        _uiState.update { it.copy(currentCameraPage = page).withDerivedData() }
        loadVisibleCameraSnapshots()
    }

    fun prevCameraPage() {
        val cur = _uiState.value.currentCameraPage
        if (cur > 1) setCameraPage(cur - 1)
    }

    fun nextCameraPage() {
        val state = _uiState.value
        if (state.currentCameraPage < state.totalCameraPages) setCameraPage(state.currentCameraPage + 1)
    }

    fun toggleNodeExpansion(nodeId: String) {
        val current = _uiState.value.expandedNodes
        val next = if (current.contains(nodeId)) current - nodeId else current + nodeId
        GlobalData.preferences.edit().putStringSet(PREF_EXPANDED_NODES, next).apply()
        _uiState.update { it.copy(expandedNodes = next).withDerivedData() }
    }

    private fun saveSelectedCamerasToPrefs(cameras: List<SelectedCamera?>) {
        val idsCsv = (0 until SLOT_COUNT).joinToString(",") { idx ->
            (cameras.getOrNull(idx)?.camera?.id ?: -1).toString()
        }
        val editor = GlobalData.preferences.edit().putString(PREF_GRID_CAMERA_IDS, idsCsv)
        val firstCamId = cameras.getOrNull(0)?.camera?.id
        if (firstCamId != null) {
            editor.putInt(PREF_CAMERA_ID, firstCamId)
        } else {
            editor.remove(PREF_CAMERA_ID)
        }
        editor.apply()
    }

    private fun restoreOrSelectInitialCameras() {
        if (_uiState.value.selectedCameras.any { it != null }) return

        val prefs = GlobalData.preferences
        val savedCsv = prefs.getString(PREF_GRID_CAMERA_IDS, null)
        val savedIds = if (!savedCsv.isNullOrBlank()) {
            savedCsv.split(",").mapNotNull { it.trim().toIntOrNull() }
        } else {
            listOf(prefs.getInt(PREF_CAMERA_ID, -1), -1, -1, -1)
        }

        var restoredAny = false
        for (idx in 0 until SLOT_COUNT) {
            val camId = savedIds.getOrNull(idx) ?: -1
            if (camId > 0) {
                val cam = treeIndex.findCamera(camId)
                if (cam != null) {
                    restoredAny = true
                    addCamera(cam, idx, startPolling = idx < _uiState.value.gridSize)
                }
            }
        }

        if (!restoredAny) {
            findFirstCamera(treeIndex.tree)?.let { addCamera(it, 0, startPolling = true) }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Snapshot thẻ camera (danh sách)
    // ---------------------------------------------------------------------------------------------

    fun loadVisibleCameraSnapshots() {
        val state = _uiState.value
        val visibleCameras = state.pagedCameraItems.map { it.camera }.filter { it.id != 0 }
        if (visibleCameras.isEmpty()) return
        val knownSnapshots = state.cameraSnapshots

        // Chỉ xử lý trang đang hiển thị: huỷ lượt tra cứu của trang/bộ lọc trước
        snapshotLookupJob?.cancel()
        snapshotLookupJob = viewModelScope.launch {
            val immediateUpdates = HashMap<Int, String?>()
            val toFetch = ArrayList<CameraModel>()

            // Tra cache (đọc đĩa) trên IO thay vì Main thread
            withContext(Dispatchers.IO) {
                for (cam in visibleCameras) {
                    val camId = cam.id
                    val isFresh = MonitorCacheManager.isSnapshotCacheFresh(app, camId)

                    // Nếu đã có kết quả trong state (kể cả null khi camera thực sự không có ảnh snapshot)
                    // thì không gọi lại API trên mỗi lần đổi trang/bộ lọc
                    if (knownSnapshots.containsKey(camId)) {
                        if (!knownSnapshots[camId].isNullOrEmpty() || !isFresh) continue
                    }

                    val cachedRaw = MonitorCacheManager.getCachedCameraImageLink(app, camId, cam.cameraId)
                    if (!cachedRaw.isNullOrBlank()) {
                        val resolved = resolveFullMediaUrl(cachedRaw)
                        if (resolved.isNotEmpty()) immediateUpdates[camId] = resolved
                        if (isFresh) continue
                    }

                    if (!pendingSnapshotIds.contains(camId)) toFetch.add(cam)
                }
            }

            if (immediateUpdates.isNotEmpty()) {
                _uiState.update { cur -> cur.copy(cameraSnapshots = cur.cameraSnapshots + immediateUpdates) }
            }
            toFetch.forEach { fetchCardSnapshot(it, force = false) }
        }
    }

    private fun fetchCardSnapshot(cam: CameraModel, force: Boolean) {
        val camId = cam.id
        if (camId == 0) return
        if (!force && !pendingSnapshotIds.add(camId)) return
        if (force) pendingSnapshotIds.add(camId)

        viewModelScope.launch {
            try {
                var resolvedLink = ""
                val res = runCatching { vmsApi.getCameraImageLink(camId) }.getOrNull()
                if (res?.isSuccessful == true) {
                    val rawLink = res.body()?.link?.trim().orEmpty()
                    if (rawLink.isNotEmpty() && rawLink != NO_IMAGE) {
                        withContext(Dispatchers.IO) {
                            MonitorCacheManager.setCachedCameraImageLink(app, camId, rawLink, cam.cameraId)
                        }
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
                        if (rawThumb.isNotEmpty() && rawThumb != NO_IMAGE) {
                            withContext(Dispatchers.IO) {
                                MonitorCacheManager.setCachedCameraImageLink(app, camId, rawThumb, cam.cameraId)
                            }
                            resolvedLink = resolveFullMediaUrl(rawThumb)
                        }
                    }
                }

                _uiState.update { cur ->
                    val currentVal = cur.cameraSnapshots[camId]
                    when {
                        resolvedLink.isNotEmpty() -> cur.copy(cameraSnapshots = cur.cameraSnapshots + (camId to resolvedLink))
                        currentVal.isNullOrEmpty() || force -> cur.copy(cameraSnapshots = cur.cameraSnapshots + (camId to null))
                        else -> cur
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { cur ->
                    if (!cur.cameraSnapshots.containsKey(camId) || force) {
                        cur.copy(cameraSnapshots = cur.cameraSnapshots + (camId to null))
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
        viewModelScope.launch(Dispatchers.IO) {
            MonitorCacheManager.removeCachedCameraImageLink(app, camId, cam.cameraId)
        }

        if (retriedCardSnapshotIds.add(camId)) {
            _uiState.update { cur -> cur.copy(cameraSnapshots = cur.cameraSnapshots - camId) }
            fetchCardSnapshot(cam, force = true)
        } else {
            _uiState.update { cur -> cur.copy(cameraSnapshots = cur.cameraSnapshots + (camId to null)) }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Load cây camera
    // ---------------------------------------------------------------------------------------------

    private suspend fun buildIndex(
        vmsList: List<VMSServiceModel>,
        groupList: List<CameraGroupModel>,
        cameraList: List<CameraModel>,
        cameraInGroups: List<CameraInGroupModel>
    ): CameraTreeIndex = withContext(Dispatchers.Default) {
        CameraTreeIndex(buildTree(vmsList, groupList, cameraList, cameraInGroups))
    }

    private fun applyTree(index: CameraTreeIndex) {
        treeIndex = index
        _uiState.update { state ->
            // Mặc định mở rộng các VMS ở lần đầu (chưa có trạng thái lưu)
            val expanded = if (state.expandedNodes.isEmpty() && index.tree.isNotEmpty()) {
                index.tree.map { it.key }.toSet()
            } else {
                state.expandedNodes
            }
            state.copy(isLoading = false, error = null, expandedNodes = expanded).withDerivedData()
        }
        restoreOrSelectInitialCameras()
        loadVisibleCameraSnapshots()
    }

    private fun loadData() {
        viewModelScope.launch {
            val cachedData = MonitorCacheManager.getMonitorData(app)
            if (cachedData != null && (cachedData.vmsList.isNotEmpty() || cachedData.cameraList.isNotEmpty())) {
                if (cachedData.aiServices.isNotEmpty()) aiServices = cachedData.aiServices
                applyTree(
                    buildIndex(cachedData.vmsList, cachedData.groupList, cachedData.cameraList, cachedData.cameraInGroups)
                )
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
                        if (aiServiceResponse?.isSuccessful == true) {
                            aiServices = aiServiceResponse.body() ?: emptyList()
                        }

                        applyTree(buildIndex(vmsList, groupList, cameraList, cameraInGroups))

                        MonitorCacheManager.saveMonitorData(
                            app,
                            CachedMonitorData(
                                vmsList = vmsList,
                                groupList = groupList,
                                cameraList = cameraList,
                                cameraInGroups = cameraInGroups,
                                aiServices = aiServices,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    } else if (treeIndex.tree.isEmpty()) {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to load data") }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val hasTree = treeIndex.tree.isNotEmpty()
                _uiState.update {
                    it.copy(isLoading = false, error = if (hasTree) null else e.toUserFriendlyMessage())
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Live stage (1x1 / 2x2)
    // ---------------------------------------------------------------------------------------------

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
            if (currentState.selectedCameras.getOrNull(0)?.camera?.id == camera.id) {
                triggerBlink(0)
                return
            }
            addCamera(camera, 0)
            return
        }

        // Chế độ 2x2 (gridSize == 4)
        val existingIndex = (0 until SLOT_COUNT).indexOfFirst { idx ->
            currentState.selectedCameras.getOrNull(idx)?.camera?.id == camera.id
        }
        if (existingIndex != -1) {
            _uiState.update { it.copy(activeSlotIndex = existingIndex) }
            triggerBlink(existingIndex)
            return
        }

        val preferredSlot = currentState.activeSlotIndex.coerceIn(0, SLOT_COUNT - 1)
        val targetSlot = if (currentState.selectedCameras.getOrNull(preferredSlot) == null) {
            preferredSlot
        } else {
            (0 until SLOT_COUNT).firstOrNull { idx -> currentState.selectedCameras.getOrNull(idx) == null }
        }

        if (targetSlot == null) {
            if (onGridFull != null) {
                onGridFull()
            } else {
                MobileTopPopupManager.warning("Lưới đang phát đã đầy, không thể phát thêm!")
            }
            return
        }

        addCamera(camera, targetSlot)

        val updatedCameras = _uiState.value.selectedCameras
        val nextEmpty = (0 until SLOT_COUNT).firstOrNull { idx ->
            idx != targetSlot && updatedCameras.getOrNull(idx) == null
        }
        _uiState.update { it.copy(activeSlotIndex = nextEmpty ?: targetSlot) }
    }

    fun addCamera(camera: CameraModel, slotIndex: Int, startPolling: Boolean = true) {
        if (slotIndex !in 0 until SLOT_COUNT) return

        val existing = _uiState.value.selectedCameras.getOrNull(slotIndex)
        if (existing?.camera?.id == camera.id && existing.streamUrl.startsWith("ws")) return

        val initialChannel = preferredChannel(camera)
        var updatedCameras: List<SelectedCamera?> = emptyList()
        _uiState.update { state ->
            val cameras = state.selectedCameras.toPaddedMutableList()
            cameras[slotIndex] = SelectedCamera(camera = camera, streamUrl = "", channel = initialChannel)
            updatedCameras = cameras
            state.copy(selectedCameras = cameras)
        }
        saveSelectedCamerasToPrefs(updatedCameras)

        // Gắn ngay ảnh snapshot đã cache (đọc đĩa trên IO) giống cachedSnapshotUrl trên PWA
        viewModelScope.launch {
            val cachedSnap = readCachedSnapshot(camera).orEmpty()
            if (cachedSnap.isNotEmpty()) {
                updateSlot(slotIndex, camera.id) {
                    if (it.fallbackImageUrl.isEmpty()) it.copy(fallbackImageUrl = cachedSnap) else it
                }
            }
        }

        if (startPolling && isMonitorTabActive) {
            ensureCameraSnapshotUrl(camera, slotIndex, force = false)
            startLiveStreamPolling(camera, slotIndex, channel = initialChannel, isFallback = false)
        }
    }

    private fun ensureCameraSnapshotUrl(camera: CameraModel, slotIndex: Int, force: Boolean) {
        viewModelScope.launch {
            if (!force) {
                val hasFreshCache = withContext(Dispatchers.IO) {
                    MonitorCacheManager.isSnapshotCacheFresh(app, camera.id) &&
                        !MonitorCacheManager.getCachedCameraImageLink(app, camera.id, camera.cameraId).isNullOrBlank()
                }
                if (hasFreshCache) return@launch
            }

            try {
                val response = vmsApi.getCameraImageLink(camera.id)
                val rawLink = if (response.isSuccessful) response.body()?.link?.trim().orEmpty() else ""
                if (rawLink.isNotEmpty() && rawLink != NO_IMAGE) {
                    val finalSnap = withContext(Dispatchers.IO) {
                        MonitorCacheManager.setCachedCameraImageLink(app, camera.id, rawLink, camera.cameraId)
                        // Chỉ dùng link API nếu chưa có frame chụp trực tiếp mới hơn từ luồng live
                        val latestCached = MonitorCacheManager.getCachedCameraImageLink(app, camera.id, camera.cameraId)
                        if (latestCached != null && latestCached.startsWith("file://")) latestCached else resolveFullMediaUrl(rawLink)
                    }
                    updateSlot(slotIndex, camera.id) { it.copy(fallbackImageUrl = finalSnap) }
                } else if (force) {
                    clearSlotSnapshotUrl(camera.id, slotIndex)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (force) clearSlotSnapshotUrl(camera.id, slotIndex)
            }
        }
    }

    private fun clearSlotSnapshotUrl(cameraId: Int, slotIndex: Int) {
        updateSlot(slotIndex, cameraId) { it.copy(fallbackImageUrl = "", snapshotRetried = true) }
    }

    /** Cập nhật ảnh nền của ô theo cache mới nhất (vd. frame vừa chụp từ luồng live). */
    private fun refreshSlotSnapshotFromCache(slotIndex: Int, camera: CameraModel) {
        viewModelScope.launch {
            val latest = readCachedSnapshot(camera) ?: return@launch
            updateSlot(slotIndex, camera.id) { it.copy(fallbackImageUrl = latest) }
        }
    }

    /**
     * Đồng bộ `onSnapshotError()` trong `LiveVideoView.vue` (lines 540-559):
     * Khi ảnh snapshot cache bị lỗi hoặc hết hạn, xoá khỏi cache và gọi lại `CameraImageLink/{id}` với `force = true` 1 lần.
     */
    fun onSnapshotLoadError(slotIndex: Int) {
        val current = _uiState.value.selectedCameras.getOrNull(slotIndex) ?: return
        val camera = current.camera
        viewModelScope.launch(Dispatchers.IO) {
            MonitorCacheManager.removeCachedCameraImageLink(app, camera.id, camera.cameraId)
        }

        if (!current.snapshotRetried) {
            updateSlot(slotIndex, camera.id) { it.copy(snapshotRetried = true) }
            ensureCameraSnapshotUrl(camera, slotIndex, force = true)
        } else {
            clearSlotSnapshotUrl(camera.id, slotIndex)
        }
    }

    fun refreshActiveStreams() {
        val state = _uiState.value
        val active = (0 until state.gridSize).mapNotNull { idx ->
            state.selectedCameras.getOrNull(idx)?.let { idx to it }
        }
        if (active.isEmpty()) return

        _uiState.update { cur ->
            val cameras = cur.selectedCameras.toPaddedMutableList()
            for ((idx, selected) in active) {
                val slotCam = cameras[idx]
                if (slotCam != null && slotCam.camera.id == selected.camera.id) {
                    cameras[idx] = slotCam.copy(streamUrl = "", retryCount = 0, hasFallbackTried = false)
                }
            }
            cur.copy(selectedCameras = cameras)
        }

        for ((idx, selected) in active) {
            refreshSlotSnapshotFromCache(idx, selected.camera)
            ensureCameraSnapshotUrl(selected.camera, idx, force = false)
            startLiveStreamPolling(selected.camera, idx, channel = preferredChannel(selected.camera), isFallback = false)
        }
    }

    /**
     * Đồng bộ cơ chế polling `check()` + `loadLiveStream()` + `fallbackStream()` của `LiveVideoView.vue` (lines 861-1023):
     * - Gọi `POST /VMS/api/LiveStream` mỗi 2000ms (tối đa 10 lần trên mobile) cho đến khi cả `link` và `videoCodec` đều có dữ liệu.
     * - Nếu hết 10 lần mà chưa có codec và camera có luồng phụ (`sub`), tự động chuyển đổi kênh (`sub <-> main`) để thử tiếp.
     * - Không poll khi tab Trực tiếp không hiển thị / app ở nền (job bị huỷ trong [setMonitorTabActive]).
     */
    private fun startLiveStreamPolling(
        camera: CameraModel,
        slotIndex: Int,
        channel: Int,
        isFallback: Boolean = false
    ) {
        if (slotIndex !in 0 until SLOT_COUNT) return
        streamJobs[slotIndex]?.cancel()
        if (!isMonitorTabActive) return

        streamJobs[slotIndex] = viewModelScope.launch {
            for (attempt in 1..LIVE_POLL_MAX_ATTEMPTS) {
                val activeCam = _uiState.value.selectedCameras.getOrNull(slotIndex)
                if (activeCam == null || activeCam.camera.id != camera.id) return@launch

                try {
                    val response = vmsApi.createLiveStream(
                        LiveStreamModel(
                            cameraId = camera.id,
                            channel = channel,
                            link = "",
                            videoCodec = "",
                            audioCodec = "",
                            videoCodecTag = ""
                        )
                    )
                    if (response.isSuccessful) {
                        val liveStreamModel = response.body()
                        val rawLink = liveStreamModel?.link?.trim().orEmpty()
                        val videoCodec = liveStreamModel?.videoCodec?.trim().orEmpty()

                        if (rawLink.isNotEmpty() && videoCodec.isNotEmpty()) {
                            val wsUrl = resolveWebSocketUrl(rawLink)
                            updateSlot(slotIndex, camera.id) {
                                it.copy(
                                    streamUrl = wsUrl,
                                    channel = channel,
                                    hasFallbackTried = isFallback,
                                    retryCount = 0
                                )
                            }
                            _uiState.update { it.copy(error = null) }
                            return@launch
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    // Tiếp tục chờ tới lượt poll tiếp theo
                }

                if (attempt < LIVE_POLL_MAX_ATTEMPTS) delay(LIVE_POLL_INTERVAL_MS)
            }

            // Đã thử hết số lần poll cho channel hiện tại mà chưa nhận được link + videoCodec
            val currentSelection = _uiState.value.selectedCameras.getOrNull(slotIndex) ?: return@launch
            if (currentSelection.camera.id != camera.id) return@launch

            if (!isFallback && camera.sub.isNotBlank()) {
                val fallbackChannel = if (channel == 1) 0 else 1
                updateSlot(slotIndex, camera.id) {
                    it.copy(streamUrl = "", channel = fallbackChannel, hasFallbackTried = true, retryCount = 0)
                }
                startLiveStreamPolling(camera, slotIndex, channel = fallbackChannel, isFallback = true)
            } else {
                updateSlot(slotIndex, camera.id) { it.copy(streamUrl = "error://no-video", retryCount = 6) }
            }
        }
    }

    fun resetRetryCount(slotIndex: Int) {
        val current = _uiState.value.selectedCameras.getOrNull(slotIndex) ?: return
        updateSlot(slotIndex, current.camera.id) { it.copy(retryCount = 0) }
        refreshSlotSnapshotFromCache(slotIndex, current.camera)
    }

    fun removeCamera(slotIndex: Int) {
        if (slotIndex !in 0 until SLOT_COUNT) return
        cancelStreamJob(slotIndex)
        var updatedCameras: List<SelectedCamera?> = emptyList()
        _uiState.update { state ->
            val cameras = state.selectedCameras.toPaddedMutableList()
            cameras[slotIndex] = null
            updatedCameras = cameras
            state.copy(selectedCameras = cameras, activeSlotIndex = slotIndex)
        }
        saveSelectedCamerasToPrefs(updatedCameras)
    }

    fun retryCameraStream(slotIndex: Int) {
        if (slotIndex !in 0 until SLOT_COUNT) return
        val currentSelection = _uiState.value.selectedCameras.getOrNull(slotIndex) ?: return
        val cameraId = currentSelection.camera.id

        // Nếu luồng hiện tại lỗi sau vài lần thử và chưa fallback kênh (sub <-> main), chuyển sang kênh còn lại giống playerError() trong LiveVideoView.vue
        if (currentSelection.retryCount >= 2 && !currentSelection.hasFallbackTried && currentSelection.camera.sub.isNotBlank()) {
            val nextChannel = if (currentSelection.channel == 1) 0 else 1
            updateSlot(slotIndex, cameraId) {
                it.copy(streamUrl = "", channel = nextChannel, hasFallbackTried = true, retryCount = 0)
            }
            startLiveStreamPolling(currentSelection.camera, slotIndex, channel = nextChannel, isFallback = true)
            return
        }

        if (currentSelection.retryCount >= 5) {
            if (currentSelection.retryCount == 5) {
                updateSlot(slotIndex, cameraId) { it.copy(retryCount = 6, streamUrl = "error://failed") }
            }
            return
        }

        val nextRetry = currentSelection.retryCount + 1
        updateSlot(slotIndex, cameraId) { it.copy(streamUrl = "", retryCount = nextRetry) }

        streamJobs[slotIndex]?.cancel()
        if (!isMonitorTabActive) return
        streamJobs[slotIndex] = viewModelScope.launch {
            // Exponential backoff: 1s, 2s, 4s, 8s (tối đa 8s)
            delay((1000L shl (nextRetry - 1)).coerceAtMost(RETRY_MAX_DELAY_MS))
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
}
