package com.tenli.oneview.ui.features.monitor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.Coil
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import com.tenli.oneview.data.local.CachedMonitorData
import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.local.UserSession
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.RecordSchedule
import com.tenli.oneview.model.network.VMSServiceModel
import com.tenli.oneview.model.network.VideoModel
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.features.event.formatVideoQueryTime
import com.tenli.oneview.ui.features.event.parseVideoQueryTime
import com.tenli.oneview.ui.features.event.resolveApiEventType
import com.tenli.oneview.ui.features.event.uniqueKey
import com.tenli.oneview.ui.features.home.TimeFilter
import com.tenli.oneview.ui.features.main.CameraDetailTab
import com.tenli.oneview.ui.utils.AiTypeHelper
import com.tenli.oneview.util.EventBus
import com.tenli.oneview.util.NetworkMonitor
import com.tenli.oneview.util.safeApiCall
import com.tenli.oneview.util.toUserFriendlyMessage
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed class CameraTreeNode {
    abstract val key: String
    data class VMSNode(val vms: VMSServiceModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class GroupNode(val group: CameraGroupModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class CameraLeaf(val camera: CameraModel, override val key: String) : CameraTreeNode()
}

@Immutable
data class GroupSheetRow(
    val key: String,
    val title: String,
    val depth: Int,
    val hasSubGroups: Boolean,
    val isExpanded: Boolean,
    val count: Int,
    val isVmsNode: Boolean = false
)

@Immutable
data class CameraCardItem(
    val key: String,
    val camera: CameraModel
)

@Immutable
data class SelectedCamera(
    val camera: CameraModel,
    val streamUrl: String,
    val retryCount: Int = 0,
    val fallbackImageUrl: String = "",
    val channel: Int = 1,
    val hasFallbackTried: Boolean = false,
    val snapshotRetried: Boolean = false
)

@Immutable
data class PlaybackDayGroup(
    val dateKey: String, // "yyyy-MM-dd"
    val videos: List<VideoModel>,
    val hasMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val labelVi: String = "",
    val labelEn: String = ""
)

@Immutable
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
    val totalFilteredCamerasCount: Int = 0,
    val groupTreeRows: List<GroupSheetRow> = emptyList(),
    val pagedCameraItems: List<CameraCardItem> = emptyList(),
    val totalCameraPages: Int = 1,
    val selectedFilterGroupName: String = "",
    val selectedFilterVmsName: String = "",

    // ---- Màn hình chi tiết Camera: 3 tính năng (Trực tiếp / Xem lại VMS / Sự kiện) ----
    val cameraDetailTab: CameraDetailTab = CameraDetailTab.LIVE,

    // Xem lại (chỉ lấy danh sách file ghi hình từ VMS API, không gọi NVR)
    val defaultPlaybackChannel: String = "main", // "main" | "sub"
    val playbackChannel: String = "main", // "main" | "sub"
    val playbackTimeFilter: TimeFilter = TimeFilter.TODAY,
    val playbackDayGroups: List<PlaybackDayGroup> = emptyList(),
    val isLoadingPlaybacks: Boolean = false,
    val isLoadingMorePlaybackDays: Boolean = false,
    val hasMorePlaybackDays: Boolean = false,
    val selectedPlaybackVideo: VideoModel? = null,
    val isPlaybackPlaying: Boolean = false,

    // Sự kiện theo UUID của camera đang chọn
    val cameraEvents: List<EventData> = emptyList(),
    val isLoadingCameraEvents: Boolean = false,
    val isRefreshingCameraEvents: Boolean = false,
    val isPaginatingCameraEvents: Boolean = false,
    val hasMoreCameraEvents: Boolean = false,
    val cameraEventTimeFilter: TimeFilter = TimeFilter.TODAY,
    val cameraEventAiType: String? = null
) {
    val hasActivePlaybackFilter: Boolean
        get() = playbackTimeFilter != TimeFilter.TODAY || playbackChannel != defaultPlaybackChannel

    val hasActiveCameraEventFilter: Boolean
        get() = cameraEventTimeFilter != TimeFilter.TODAY || cameraEventAiType != null
}

/**
 * Chỉ mục tính sẵn từ cây camera. Dựng 1 lần trên [Dispatchers.Default] mỗi khi cây thay đổi,
 * nhờ đó mọi thao tác lọc / đếm / phân trang / tìm kiếm sau đó chỉ là tra cứu O(1) hoặc duyệt O(n) rất rẻ
 * (trước đây mỗi lần đổi trang/gõ phím lại duyệt toàn bộ cây nhiều lần trên Main thread).
 */
private class CameraTreeIndex(val tree: List<CameraTreeNode>) {
    private val nodeByKey = HashMap<String, CameraTreeNode>()
    private val camerasByNodeKey = HashMap<String, List<CameraCardItem>>()
    private val vmsNameByNodeKey = HashMap<String, String>()
    private val parentGroupKeyByNodeKey = HashMap<String, String>()
    private val normalizedNameById = HashMap<Int, String>()
    private val ipTextById = HashMap<Int, String>()
    private val locationById = HashMap<Int, String>()

    /** Các VMS có ít nhất 1 camera */
    val vmsNodes: List<CameraTreeNode.VMSNode>

    /** Toàn bộ camera (không trùng id) theo thứ tự duyệt cây */
    val allCameras: List<CameraCardItem>
    val totalCameraCount: Int
    private val cameraById: Map<Int, CameraModel>

    init {
        tree.forEach { collect(it, vmsName = "", groupPath = "", parentGroupKey = "") }
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
    private fun collect(
        node: CameraTreeNode,
        vmsName: String,
        groupPath: String,
        parentGroupKey: String
    ): List<CameraCardItem> {
        // Giữ ngữ nghĩa "node đầu tiên khớp key theo thứ tự duyệt" như hàm tìm kiếm cũ
        nodeByKey.putIfAbsent(node.key, node)
        val items = when (node) {
            is CameraTreeNode.CameraLeaf -> {
                val cam = node.camera
                if (cam.id != 0) {
                    val loc = when {
                        groupPath.isNotEmpty() && vmsName.isNotEmpty() -> "$groupPath - $vmsName"
                        groupPath.isNotEmpty() -> groupPath
                        vmsName.isNotEmpty() -> vmsName
                        else -> ""
                    }
                    if (loc.isNotEmpty()) {
                        locationById.putIfAbsent(cam.id, loc)
                    }
                    listOf(CameraCardItem(key = "cam-${cam.id}", camera = cam))
                } else {
                    emptyList()
                }
            }
            is CameraTreeNode.GroupNode -> {
                vmsNameByNodeKey.putIfAbsent(node.key, vmsName)
                if (parentGroupKey.isNotEmpty()) {
                    parentGroupKeyByNodeKey.putIfAbsent(node.key, parentGroupKey)
                }
                val gName = node.group.name.trim()
                // Nếu có cấu trúc nhóm cha - con thì giữ nhóm cha lớn nhất làm đại diện đường dẫn
                val nextGroupPath = when {
                    groupPath.isNotEmpty() -> groupPath
                    gName.isNotEmpty() -> gName
                    else -> ""
                }
                mergeUnique(
                    node.children.map {
                        collect(it, vmsName = vmsName, groupPath = nextGroupPath, parentGroupKey = node.key)
                    }
                )
            }
            is CameraTreeNode.VMSNode -> {
                val vName = node.vms.name.trim().ifBlank { "VMS" }
                vmsNameByNodeKey.putIfAbsent(node.key, vName)
                mergeUnique(node.children.map { collect(it, vmsName = vName, groupPath = "", parentGroupKey = "") })
            }
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

    fun locationLabel(id: Int): String = locationById[id].orEmpty()

    fun camerasForFilter(filterKey: String): List<CameraCardItem> {
        if (filterKey == MonitorViewModel.FILTER_ALL) return allCameras
        val node = nodeByKey[filterKey] ?: return allCameras
        return camerasOf(node)
    }

    /**
     * Trả về cặp `(groupName, vmsName)` cho bộ lọc [filterKey]:
     * - Khi chọn `FILTER_ALL`: trả về `("" to "")` (UI hiển thị "Tất cả camera").
     * - Khi chọn 1 nhóm (kể cả có cấu trúc nhóm cha - con): lấy nhóm cha lớn nhất chứa toàn bộ camera đang hiển thị
     *   và luôn hiển thị ở dạng `Nhóm - VMS`.
     * - Khi chọn 1 VMS: nếu VMS chỉ có 1 nhóm cha chứa toàn bộ camera của VMS đó thì hiển thị `Nhóm - VMS`,
     *   ngược lại hiển thị tên VMS.
     */
    fun filterPathParts(filterKey: String): Pair<String, String> {
        if (filterKey == MonitorViewModel.FILTER_ALL) return "" to ""
        val node = nodeByKey[filterKey] ?: return "" to ""
        return when (node) {
            is CameraTreeNode.GroupNode -> {
                var targetGroup: CameraTreeNode.GroupNode = node
                val targetCount = count(targetGroup)
                while (true) {
                    val parentKey = parentGroupKeyByNodeKey[targetGroup.key] ?: break
                    val parentNode = nodeByKey[parentKey] as? CameraTreeNode.GroupNode ?: break
                    if (count(parentNode) == targetCount) {
                        targetGroup = parentNode
                    } else {
                        break
                    }
                }
                val gName = targetGroup.group.name.trim()
                val vName = vmsNameByNodeKey[targetGroup.key].orEmpty()
                gName to vName
            }
            is CameraTreeNode.VMSNode -> {
                val vName = node.vms.name.trim().ifBlank { "VMS" }
                "" to vName
            }
            is CameraTreeNode.CameraLeaf -> "" to ""
        }
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

    var activePlaybackVideo by mutableStateOf<VideoModel?>(null)
        private set
    var activePlaybackPlaying by mutableStateOf(false)
        private set

    /** Chỉ được đọc/ghi trên Main thread (cùng luồng với _uiState.update) */
    private var treeIndex = CameraTreeIndex(emptyList())

    /** Dữ liệu gốc chưa lọc phân quyền để dựng lại cây khi quyền tài khoản thay đổi */
    private var rawVmsList: List<VMSServiceModel> = emptyList()
    private var rawGroupList: List<CameraGroupModel> = emptyList()
    private var rawCameraList: List<CameraModel> = emptyList()
    private var rawCameraInGroups: List<CameraInGroupModel> = emptyList()

    /** AI services chỉ dùng để lưu kèm cache Monitor (các màn hình khác đọc lại), UI màn hình này không hiển thị */
    private var aiServices: List<AIServiceModel> = emptyList()

    private val streamJobs = arrayOfNulls<Job>(SLOT_COUNT)
    private val blinkJobs = arrayOfNulls<Job>(SLOT_COUNT)
    private var snapshotLookupJob: Job? = null
    private var playbackFetchJob: Job? = null
    private var playbackLoadMoreDaysJob: Job? = null
    private var playbackThumbPrefetchJob: Job? = null
    private val playbackDayJobs = java.util.concurrent.ConcurrentHashMap<String, Job>()
    private val playbackSessionCache = java.util.concurrent.ConcurrentHashMap<String, CachedPlaybackSession>()
    private var cameraEventFetchJob: Job? = null
    private var cameraEventPaginateJob: Job? = null
    private var lastCameraEventRawId: Int? = null
    private var playbackLoadedForCameraId: Int? = null
    private var eventsLoadedForCameraId: Int? = null
    private var playbackCurrentSearchDayMs: Long? = null
    private var playbackMinSearchDayMs: Long? = null
    private var playbackRangeMinMs: Long = 0L
    private var playbackRangeMaxMs: Long = 0L

    private data class CachedPlaybackSession(
        val dayGroups: List<PlaybackDayGroup>,
        val hasMoreDays: Boolean,
        val nextSearchDayMs: Long?,
        val minSearchDayMs: Long?,
        val rangeMinMs: Long,
        val rangeMaxMs: Long,
        val updatedAt: Long
    )

    private var isMonitorTabActive: Boolean = false
    private val cardSnapshotJobs = java.util.concurrent.ConcurrentHashMap<Int, Job>()
    @Volatile
    private var currentVisibleCameras: List<CameraModel> = emptyList()
    @Volatile
    private var currentVisibleCameraIds: Set<Int> = emptySet()
    private val retriedCardSnapshotIds = java.util.Collections.synchronizedSet(mutableSetOf<Int>())
    private val snapshotFetchSemaphore = kotlinx.coroutines.sync.Semaphore(4)

    companion object {
        const val CAMERA_PAGE_SIZE = 10
        const val SLOT_COUNT = 4
        const val FILTER_ALL = "all"

        private const val PREF_EXPANDED_NODES = "saved_expanded_nodes"
        private const val PREF_GRID_SIZE = "saved_monitor_grid_size"
        private const val PREF_GRID_CAMERA_IDS = "saved_monitor_grid_camera_ids"
        private const val PREF_CAMERA_ID = "saved_monitor_camera_id"

        /** Đồng bộ PlaybackPage.vue (VMS mode): mỗi ngày tải 12 video, mỗi lô tối đa 8 ngày */
        private const val PLAYBACK_PER_DAY_COUNT = 12
        private const val PLAYBACK_DAYS_BATCH = 8

        /** Đồng bộ EventViewModel: mỗi batch lấy 50 bản ghi thô, lọc tối thiểu 20 sự kiện hợp lệ */
        private const val EVENT_RAW_BATCH_SIZE = 50
        private const val EVENT_PAGE_TARGET_COUNT = 20
        private const val EVENT_MAX_BATCH_ATTEMPTS = 5

        /** Đồng bộ LiveVideoView.vue: poll `LiveStream` mỗi 2000ms, tối đa 10 lần trên mobile */
        private const val LIVE_POLL_MAX_ATTEMPTS = 10
        private const val LIVE_POLL_INTERVAL_MS = 2000L
        private const val BLINK_DURATION_MS = 1000L
        private const val RETRY_MAX_DELAY_MS = 8000L
        private const val NO_IMAGE = "NO_IMAGE"
        private const val SNAPSHOT_CARD_WIDTH_PX = 420
        private const val SNAPSHOT_CARD_HEIGHT_PX = 236

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
            val permittedCameraList = UserPermissionManager.filterPermittedCameras(cameraList)
            if (permittedCameraList.isEmpty()) return emptyList()

            val treeData = mutableListOf<CameraTreeNode>()
            val groupMapData = mutableMapOf<String, CameraTreeNode.GroupNode>()

            val fullVmsList = mutableListOf(
                VMSServiceModel(id = 0, name = "VMS Local", privateHost = "", publicHost = "", apiKey = "")
            )
            fullVmsList.addAll(vmsList)

            val camerasByVms = permittedCameraList.groupBy { it.vmsId }
            val groupsByVms = groupList.groupBy { it.vmsId }
            val cameraInGroupByVms = mutableMapOf<Int, MutableList<CameraInGroupModel>>()
            val cameraById = permittedCameraList.associateBy { it.id }

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
                fun camKey(cam: CameraModel) = "cam-$serviceId-${cam.id}"

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
        _uiState.update { it.copy(expandedNodes = savedNodes, gridSize = 1) }

        // Nếu dữ liệu cây camera đã được preload sẵn trong RAM, áp dụng ngay tức thì (0ms)
        val memCached = MonitorCacheManager.getMonitorDataInMemory()
        if (memCached != null && (memCached.vmsList.isNotEmpty() || memCached.cameraList.isNotEmpty())) {
            rawVmsList = memCached.vmsList
            rawGroupList = memCached.groupList
            rawCameraList = memCached.cameraList
            rawCameraInGroups = memCached.cameraInGroups
            if (memCached.aiServices.isNotEmpty()) aiServices = memCached.aiServices
            applyTree(
                CameraTreeIndex(
                    buildTree(memCached.vmsList, memCached.groupList, memCached.cameraList, memCached.cameraInGroups)
                )
            )
        }

        // Lắng nghe cập nhật snapshot (vd. frame vừa chụp từ luồng live) để đồng bộ ngay lên thẻ camera & ô live
        viewModelScope.launch {
            MonitorCacheManager.snapshotUpdates.collect { (camId, rawLink) ->
                val resolved = resolveFullMediaUrl(rawLink)
                if (resolved.isNotEmpty()) {
                    _uiState.update { cur ->
                        val updatedMap = if (cur.cameraSnapshots[camId] == resolved) {
                            cur.cameraSnapshots
                        } else {
                            cur.cameraSnapshots + (camId to resolved)
                        }
                        val updatedSelected = cur.selectedCameras.map { sel ->
                            // Chỉ cập nhật ảnh nền ô live nếu ô đó chưa có ảnh nền, hoặc khi nhận được frame mới chụp trực tiếp từ luồng Live (file://)
                            if (sel != null &&
                                sel.camera.id == camId &&
                                sel.fallbackImageUrl != resolved &&
                                (sel.fallbackImageUrl.isEmpty() || resolved.startsWith("file://"))
                            ) {
                                sel.copy(fallbackImageUrl = resolved)
                            } else {
                                sel
                            }
                        }
                        if (updatedMap === cur.cameraSnapshots && updatedSelected == cur.selectedCameras) {
                            cur
                        } else {
                            cur.copy(cameraSnapshots = updatedMap, selectedCameras = updatedSelected)
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            EventBus.networkRestored.collect {
                loadData()
            }
        }

        viewModelScope.launch {
            var lastVersion = UserPermissionManager.permissionState.value.version
            UserPermissionManager.permissionState.collect { permState ->
                if (permState.version != lastVersion) {
                    lastVersion = permState.version
                    if (rawCameraList.isNotEmpty() || rawVmsList.isNotEmpty()) {
                        val rebuiltIndex = buildIndex(rawVmsList, rawGroupList, rawCameraList, rawCameraInGroups)
                        applyTree(rebuiltIndex)
                    }
                    val openCam = _uiState.value.selectedCameras.getOrNull(0)?.camera
                    if (openCam != null &&
                        (!permState.canAccessLiveStream || !UserPermissionManager.isCameraPermitted(openCam))
                    ) {
                        closeLiveCamera()
                    } else if (openCam != null) {
                        val currentDetailTab = _uiState.value.cameraDetailTab
                        if (currentDetailTab == CameraDetailTab.PLAYBACK && !permState.canAccessPlayback) {
                            selectCameraDetailTab(CameraDetailTab.LIVE)
                        } else if (currentDetailTab == CameraDetailTab.EVENTS && !permState.canAccessEvents) {
                            selectCameraDetailTab(CameraDetailTab.LIVE)
                        }
                    }
                }
            }
        }

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

    fun resolveMediaUrl(rawUrl: String): String = resolveFullMediaUrl(rawUrl)

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
        MonitorCacheManager.getCachedCameraImageLink(app, camera.id)?.let(::resolveFullMediaUrl)
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

        // Ưu tiên nạp đồng bộ tức thì (0ms) từ RAM cache cho toàn bộ camera được lọc
        var updatedSnapshots = cameraSnapshots
        var snapshotsChanged = false
        for (item in displayed) {
            val cam = item.camera
            val camId = cam.id
            if (camId != 0 && updatedSnapshots[camId].isNullOrEmpty()) {
                val memRaw = MonitorCacheManager.getCachedCameraImageLinkInMemory(camId)
                if (!memRaw.isNullOrBlank()) {
                    val resolved = resolveFullMediaUrl(memRaw)
                    if (resolved.isNotEmpty()) {
                        if (!snapshotsChanged) {
                            updatedSnapshots = HashMap(cameraSnapshots)
                            snapshotsChanged = true
                        }
                        (updatedSnapshots as MutableMap<Int, String?>)[camId] = resolved
                    }
                }
            }
        }

        val rows = index.groupRows(expandedNodes)
        val (filterGroupName, filterVmsName) = index.filterPathParts(filterKey)

        return copy(
            selectedFilterNodeKey = filterKey,
            currentCameraPage = 1,
            cameraSnapshots = updatedSnapshots,
            hasAvailableCameras = index.vmsNodes.isNotEmpty(),
            totalAllCamerasCount = index.totalCameraCount,
            totalFilteredCamerasCount = displayed.size,
            groupTreeRows = if (rows == groupTreeRows) groupTreeRows else rows,
            pagedCameraItems = if (displayed == pagedCameraItems) pagedCameraItems else displayed,
            totalCameraPages = 1,
            selectedFilterGroupName = filterGroupName,
            selectedFilterVmsName = filterVmsName
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
            // Dừng mọi vòng poll LiveStream / retry, huỷ tải snapshot và tác vụ phụ khi màn hình không hiển thị hoặc app vào nền.
            // Đồng thời xóa ngay streamUrl cũ (token WS chỉ dùng 1 lần) để khi quay lại không khởi tạo nhầm ExoPlayer với link cũ ở Frame 1.
            for (i in 0 until SLOT_COUNT) cancelStreamJob(i)
            cancelAllCardSnapshotFetches()
            cancelCameraDetailSubJobs()
            _uiState.update { cur ->
                val hasActiveStreamUrl = cur.selectedCameras.any { it != null && it.streamUrl.isNotEmpty() }
                if (!hasActiveStreamUrl) {
                    cur
                } else {
                    cur.copy(
                        selectedCameras = cur.selectedCameras.map { sel ->
                            if (sel != null && sel.streamUrl.isNotEmpty()) {
                                sel.copy(streamUrl = "", retryCount = 0, hasFallbackTried = false)
                            } else {
                                sel
                            }
                        }
                    )
                }
            }
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

    // ---------------------------------------------------------------------------------------------
    // Snapshot thẻ camera (danh sách) - Ưu tiên cache (RAM 0ms + Disk) & chỉ tải mạng cho camera đang hiển thị
    // ---------------------------------------------------------------------------------------------

    /**
     * Được gọi từ UI mỗi khi tập hợp các thẻ camera đang hiển thị trong khung nhìn (`LazyVerticalGrid`)
     * thay đổi. Ngay lập tức hủy các job đang tải dở của những camera đã cuộn khuất khỏi màn hình,
     * đồng thời nạp ảnh cache và tải làm mới cho các camera đang hiển thị.
     */
    fun onVisibleCamerasChanged(visibleCameras: List<CameraModel>) {
        val validVisible = visibleCameras.filter { it.id != 0 }
        val newVisibleIds = validVisible.mapTo(HashSet(validVisible.size)) { it.id }
        currentVisibleCameras = validVisible
        currentVisibleCameraIds = newVisibleIds

        // Hủy ngay lập tức các yêu cầu tải snapshot đang dở của camera không còn nằm trong vùng hiển thị
        val iterator = cardSnapshotJobs.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (!newVisibleIds.contains(entry.key)) {
                entry.value.cancel()
                iterator.remove()
            }
        }

        if (validVisible.isNotEmpty()) {
            loadVisibleCameraSnapshots(validVisible)
        }
    }

    /**
     * Hủy toàn bộ các tác vụ tải snapshot thẻ camera đang chạy (ví dụ khi mở màn hình xem Live hoặc rời tab).
     */
    fun cancelAllCardSnapshotFetches() {
        snapshotLookupJob?.cancel()
        snapshotLookupJob = null
        val iterator = cardSnapshotJobs.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.cancel()
            iterator.remove()
        }
    }

    fun loadVisibleCameraSnapshots(explicitVisible: List<CameraModel>? = null) {
        if (!isMonitorTabActive) return
        val state = _uiState.value
        val allDisplayedCameras = state.pagedCameraItems.map { it.camera }.filter { it.id != 0 }
        val visibleCameras = (explicitVisible ?: currentVisibleCameras)
            .filter { it.id != 0 }
            .ifEmpty { allDisplayedCameras.take(8) }
        if (allDisplayedCameras.isEmpty() && visibleCameras.isEmpty()) return

        // 1. Nạp đồng bộ tức thì (0ms) từ RAM cache cho toàn bộ camera để thẻ có ảnh ngay khi cuộn tới
        val syncUpdates = HashMap<Int, String?>()
        for (cam in allDisplayedCameras) {
            val camId = cam.id
            if (state.cameraSnapshots[camId].isNullOrEmpty()) {
                val memRaw = MonitorCacheManager.getCachedCameraImageLinkInMemory(camId)
                if (!memRaw.isNullOrBlank()) {
                    val resolved = resolveFullMediaUrl(memRaw)
                    if (resolved.isNotEmpty()) {
                        syncUpdates[camId] = resolved
                    }
                }
            }
        }
        if (syncUpdates.isNotEmpty()) {
            _uiState.update { cur -> cur.copy(cameraSnapshots = cur.cameraSnapshots + syncUpdates) }
        }

        // 2. Chỉ đọc đĩa lần đầu nếu RAM cache của MonitorCacheManager chưa được nạp;
        // các lần cuộn tiếp theo bỏ qua hoàn toàn vòng lặp IO để cuộn mượt 120fps.
        snapshotLookupJob?.cancel()
        snapshotLookupJob = viewModelScope.launch {
            if (!MonitorCacheManager.isSnapshotsLoadedInMemory()) {
                val diskUpdates = HashMap<Int, String?>()
                val currentSnapshots = _uiState.value.cameraSnapshots

                withContext(Dispatchers.IO) {
                    for (cam in allDisplayedCameras) {
                        val camId = cam.id
                        if (currentSnapshots[camId].isNullOrEmpty()) {
                            val cachedRaw = MonitorCacheManager.getCachedCameraImageLink(app, camId)
                            if (!cachedRaw.isNullOrBlank()) {
                                val resolved = resolveFullMediaUrl(cachedRaw)
                                if (resolved.isNotEmpty()) {
                                    diskUpdates[camId] = resolved
                                }
                            }
                        }
                    }
                }

                if (diskUpdates.isNotEmpty()) {
                    _uiState.update { cur -> cur.copy(cameraSnapshots = cur.cameraSnapshots + diskUpdates) }
                }
            }

            // Nhịp chờ ngắn (60ms) để khi người dùng vuốt nhanh qua hàng loạt camera, các camera chỉ lướt ngang
            // không phát sinh gọi mạng thừa
            delay(60L)
            if (!isMonitorTabActive || _uiState.value.selectedCameras.getOrNull(0) != null) return@launch

            val activeVisible = currentVisibleCameras.ifEmpty { visibleCameras }
            if (!NetworkMonitor.isNetworkAvailable()) {
                // Khi mất mạng: các camera chưa có ảnh cache trong RAM/đĩa thì đánh dấu null ngay để tắt vòng xoay loading
                val offlineNullUpdates = HashMap<Int, String?>()
                val curSnaps = _uiState.value.cameraSnapshots
                for (cam in activeVisible) {
                    if (cam.id != 0 && !curSnaps.containsKey(cam.id)) {
                        offlineNullUpdates[cam.id] = null
                    }
                }
                if (offlineNullUpdates.isNotEmpty()) {
                    _uiState.update { cur -> cur.copy(cameraSnapshots = cur.cameraSnapshots + offlineNullUpdates) }
                }
                return@launch
            }

            val toFetch = withContext(Dispatchers.IO) {
                activeVisible.filter { cam ->
                    val camId = cam.id
                    camId != 0 &&
                        !MonitorCacheManager.isSnapshotCacheFresh(app, camId) &&
                        cardSnapshotJobs[camId]?.isActive != true
                }
            }

            if (isMonitorTabActive && _uiState.value.selectedCameras.getOrNull(0) == null) {
                for (cam in toFetch) {
                    if (currentVisibleCameraIds.isEmpty() || currentVisibleCameraIds.contains(cam.id)) {
                        fetchCardSnapshot(cam, force = false)
                    }
                }
            }
        }
    }

    /**
     * Tải trước và giải mã ảnh snapshot mới vào MemoryCache/DiskCache của Coil ở nền trước khi đổi URL trên UI,
     * giúp chuyển từ ảnh cache cũ sang ảnh mới mượt tuyệt đối (không nháy trắng, không hiện vòng xoay loading).
     * Nếu coroutine bị hủy (do camera cuộn khuất), [CancellationException] được ném ra ngay để Coil dừng tải.
     */
    private suspend fun preloadCardSnapshotIntoCoil(camId: Int, url: String): Boolean {
        if (url.isEmpty()) return false
        return try {
            val request = ImageRequest.Builder(app)
                .data(MonitorCacheManager.coilDataModel(url))
                .size(SNAPSHOT_CARD_WIDTH_PX, SNAPSHOT_CARD_HEIGHT_PX)
                .precision(Precision.INEXACT)
                .allowHardware(true)
                .allowRgb565(true)
                .memoryCacheKey(MonitorCacheManager.cardMemCacheKey(camId, url))
                .diskCacheKey(MonitorCacheManager.diskCacheKey(camId, url))
                .build()
            Coil.imageLoader(app).execute(request) is SuccessResult
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    private fun fetchCardSnapshot(cam: CameraModel, force: Boolean) {
        val camId = cam.id
        if (camId == 0 || !isMonitorTabActive) return
        if (!force && currentVisibleCameraIds.isNotEmpty() && !currentVisibleCameraIds.contains(camId)) return
        val existingJob = cardSnapshotJobs[camId]
        if (!force && existingJob?.isActive == true) return
        if (force) existingJob?.cancel()

        val job = viewModelScope.launch {
            try {
                snapshotFetchSemaphore.acquire()
                try {
                    if (!isMonitorTabActive) return@launch
                    if (!force && currentVisibleCameraIds.isNotEmpty() && !currentVisibleCameraIds.contains(camId)) {
                        return@launch
                    }

                    var resolvedLink = ""
                    var rawFetchedLink = ""
                    val res = try {
                        vmsApi.getCameraImageLink(camId)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        null
                    }
                    if (res?.isSuccessful == true) {
                        val rawLink = res.body()?.link?.trim().orEmpty()
                        if (rawLink.isNotEmpty() && rawLink != NO_IMAGE) {
                            rawFetchedLink = rawLink
                            resolvedLink = resolveFullMediaUrl(rawLink)
                        }
                    }

                    // Fallback giống MobileLivePage.vue: lấy thumbnail từ bản ghi VideoList gần nhất nếu CameraImageLink không có
                    if (resolvedLink.isEmpty() && isMonitorTabActive) {
                        if (!force && currentVisibleCameraIds.isNotEmpty() && !currentVisibleCameraIds.contains(camId)) {
                            return@launch
                        }
                        val videoRes = try {
                            vmsApi.getVideoList(camera = camId, count = 1, from = null, to = null)
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            null
                        }
                        if (videoRes?.isSuccessful == true) {
                            val firstVideo = videoRes.body()?.firstOrNull()
                            val rawThumb = firstVideo?.thumbnailLink?.ifBlank { firstVideo.imageLink }?.trim().orEmpty()
                            if (rawThumb.isNotEmpty() && rawThumb != NO_IMAGE) {
                                rawFetchedLink = rawThumb
                                resolvedLink = resolveFullMediaUrl(rawThumb)
                            }
                        }
                    }

                    if (!force && currentVisibleCameraIds.isNotEmpty() && !currentVisibleCameraIds.contains(camId)) {
                        return@launch
                    }

                    val existingDisplayedUrl = _uiState.value.cameraSnapshots[camId]
                    if (resolvedLink.isNotEmpty()) {
                        // Nếu thẻ đang hiển thị sẵn ảnh cache cũ và server trả về link ảnh mới khác link cũ,
                        // nạp sẵn ảnh mới vào RAM của Coil trước khi cập nhật state để đổi ảnh 0ms không nháy
                        if (!existingDisplayedUrl.isNullOrEmpty() && existingDisplayedUrl != resolvedLink) {
                            val loadedOk = withContext(Dispatchers.IO) { preloadCardSnapshotIntoCoil(camId, resolvedLink) }
                            if (!loadedOk && !force) {
                                MonitorCacheManager.markSnapshotCheckedInSession(camId)
                                return@launch
                            }
                        }
                        val effectiveLink = withContext(Dispatchers.IO) {
                            MonitorCacheManager.setCachedCameraImageLink(app, camId, rawFetchedLink)
                            MonitorCacheManager.getCachedCameraImageLink(app, camId)
                                ?.let(::resolveFullMediaUrl)
                                ?.takeIf { it.isNotEmpty() }
                                ?: resolvedLink
                        }
                        _uiState.update { cur ->
                            if (cur.cameraSnapshots[camId] == effectiveLink) cur
                            else cur.copy(cameraSnapshots = cur.cameraSnapshots + (camId to effectiveLink))
                        }
                    } else {
                        MonitorCacheManager.markSnapshotCheckedInSession(camId)
                        _uiState.update { cur ->
                            val currentVal = cur.cameraSnapshots[camId]
                            if (currentVal.isNullOrEmpty() || force) {
                                cur.copy(cameraSnapshots = cur.cameraSnapshots + (camId to null))
                            } else {
                                cur
                            }
                        }
                    }
                } finally {
                    snapshotFetchSemaphore.release()
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
                val thisJob = kotlinx.coroutines.currentCoroutineContext()[Job]
                if (thisJob != null) {
                    cardSnapshotJobs.remove(camId, thisJob)
                } else {
                    cardSnapshotJobs.remove(camId)
                }
            }
        }
        cardSnapshotJobs[camId] = job
    }

    fun onCardSnapshotError(cam: CameraModel) {
        val camId = cam.id
        if (camId == 0) return
        // Khi thiết bị đang mất kết nối mạng, giữ nguyên link snapshot đã cache trên đĩa tránh xóa nhầm
        if (!NetworkMonitor.isNetworkAvailable()) return
        viewModelScope.launch(Dispatchers.IO) {
            MonitorCacheManager.removeCachedCameraImageLink(app, camId)
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
        // Không tự động chọn camera phát live khi mới vào màn hình: chỉ hiển thị lưới snapshot camera,
        // người dùng chạm vào camera nào mới mở màn hình xem trực tiếp của camera đó.
        loadVisibleCameraSnapshots()
    }

    private fun loadData() {
        viewModelScope.launch {
            if (treeIndex.tree.isEmpty()) {
                val cachedData = MonitorCacheManager.getMonitorData(app)
                if (cachedData != null && (cachedData.vmsList.isNotEmpty() || cachedData.cameraList.isNotEmpty())) {
                    rawVmsList = cachedData.vmsList
                    rawGroupList = cachedData.groupList
                    rawCameraList = cachedData.cameraList
                    rawCameraInGroups = cachedData.cameraInGroups
                    if (cachedData.aiServices.isNotEmpty()) aiServices = cachedData.aiServices
                    applyTree(
                        buildIndex(cachedData.vmsList, cachedData.groupList, cachedData.cameraList, cachedData.cameraInGroups)
                    )
                } else {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
            }

            try {
                // Gọi song song cả 5 API cây camera & AI Services để giảm thời gian tải xuống 1 RTT duy nhất
                coroutineScope {
                    val vmsDeferred = async { safeApiCall { vmsApi.getVMSServiceList() } }
                    val groupDeferred = async { safeApiCall { vmsApi.getCameraGroupList() } }
                    val cameraDeferred = async { safeApiCall { vmsApi.getCameraList() } }
                    val cameraInGroupDeferred = async { safeApiCall { vmsApi.getCameraInGroupList() } }
                    val aiServiceDeferred = async { safeApiCall { eventApi.getAIServiceList() } }

                    val vmsResult = vmsDeferred.await()
                    val groupResult = groupDeferred.await()
                    val cameraResult = cameraDeferred.await()
                    val cameraInGroupResult = cameraInGroupDeferred.await()
                    val aiServiceResult = aiServiceDeferred.await()

                    val vmsResponse = vmsResult.getOrNull()
                    val groupResponse = groupResult.getOrNull()
                    val cameraResponse = cameraResult.getOrNull()
                    val cameraInGroupResponse = cameraInGroupResult.getOrNull()
                    val aiServiceResponse = aiServiceResult.getOrNull()

                    if (vmsResponse?.isSuccessful == true &&
                        groupResponse?.isSuccessful == true &&
                        cameraResponse?.isSuccessful == true &&
                        cameraInGroupResponse?.isSuccessful == true
                    ) {
                        val vmsList = vmsResponse.body() ?: emptyList()
                        val groupList = groupResponse.body() ?: emptyList()
                        val cameraList = cameraResponse.body() ?: emptyList()
                        val cameraInGroups = cameraInGroupResponse.body() ?: emptyList()
                        rawVmsList = vmsList
                        rawGroupList = groupList
                        rawCameraList = cameraList
                        rawCameraInGroups = cameraInGroups
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
                    } else {
                        val firstFailure = cameraResult.exceptionOrNull()
                            ?: vmsResult.exceptionOrNull()
                            ?: groupResult.exceptionOrNull()
                            ?: cameraInGroupResult.exceptionOrNull()
                        val errMsg = firstFailure?.toUserFriendlyMessage()
                            ?: Throwable().toUserFriendlyMessage()
                        // Giữ nguyên dữ liệu cây camera cache trên màn hình (nếu có) nhưng vẫn báo lỗi backend cho người dùng
                        _uiState.update { it.copy(isLoading = false, error = errMsg) }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.toUserFriendlyMessage())
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
    /**
     * Mở màn hình xem trực tiếp 1x1 cho [camera] được chọn từ danh sách lưới.
     */
    fun openLiveCamera(camera: CameraModel) {
        cancelAllCardSnapshotFetches()
        cancelCameraDetailSubJobs()
        playbackLoadedForCameraId = null
        eventsLoadedForCameraId = null
        lastCameraEventRawId = null
        val preferredPlaybackChan = resolvePreferredPlaybackChannel(camera)
        val cachedPlayback = getPlaybackSessionCache(camera.id, TimeFilter.TODAY, preferredPlaybackChan)
        if (cachedPlayback != null) {
            playbackCurrentSearchDayMs = cachedPlayback.nextSearchDayMs
            playbackMinSearchDayMs = cachedPlayback.minSearchDayMs
            playbackRangeMinMs = cachedPlayback.rangeMinMs
            playbackRangeMaxMs = cachedPlayback.rangeMaxMs
        }
        activePlaybackVideo = null
        activePlaybackPlaying = false
        _uiState.update {
            it.copy(
                gridSize = 1,
                activeSlotIndex = 0,
                cameraDetailTab = CameraDetailTab.LIVE,
                defaultPlaybackChannel = preferredPlaybackChan,
                playbackChannel = preferredPlaybackChan,
                playbackTimeFilter = TimeFilter.TODAY,
                playbackDayGroups = cachedPlayback?.dayGroups ?: emptyList(),
                isLoadingPlaybacks = false,
                isLoadingMorePlaybackDays = false,
                hasMorePlaybackDays = cachedPlayback?.hasMoreDays ?: false,
                selectedPlaybackVideo = null,
                isPlaybackPlaying = false,
                cameraEvents = emptyList(),
                isLoadingCameraEvents = false,
                isRefreshingCameraEvents = false,
                isPaginatingCameraEvents = false,
                hasMoreCameraEvents = false,
                cameraEventTimeFilter = TimeFilter.TODAY,
                cameraEventAiType = null
            )
        }
        addCamera(camera, 0, startPolling = true)
        // Pre-warm danh sách playback ngầm sau khi hiệu ứng trượt mở Live hoàn tất (~350ms)
        // để khi người dùng chuyển sang tab "Xem lại", danh sách hiển thị tức thì 0ms.
        if (UserPermissionManager.canAccessPlayback()) {
            playbackFetchJob = viewModelScope.launch(Dispatchers.IO) {
                delay(350L)
                val activeCam = _uiState.value.selectedCameras.getOrNull(0)?.camera
                if (activeCam?.id == camera.id && playbackLoadedForCameraId != camera.id) {
                    fetchCameraPlaybacks(isBackgroundPrewarm = true)
                }
            }
        }
    }

    private fun cancelCameraDetailSubJobs() {
        playbackFetchJob?.cancel()
        playbackFetchJob = null
        playbackLoadMoreDaysJob?.cancel()
        playbackLoadMoreDaysJob = null
        playbackThumbPrefetchJob?.cancel()
        playbackThumbPrefetchJob = null
        for (job in playbackDayJobs.values) job.cancel()
        playbackDayJobs.clear()
        cameraEventFetchJob?.cancel()
        cameraEventFetchJob = null
        cameraEventPaginateJob?.cancel()
        cameraEventPaginateJob = null
    }

    /**
     * Đồng bộ `resolvePreferredChannel(cam)` trong `PlaybackPage.vue` (lines 723-729):
     * Nếu luồng chính không bật ghi hình mà luồng phụ có bật ghi hình -> ưu tiên `"sub"`, ngược lại `"main"`.
     */
    private fun resolvePreferredPlaybackChannel(camera: CameraModel): String {
        val mainRec = camera.enableRecord || hasValidRecordSchedule(camera.recordSchedule)
        val subRec = camera.enableSubRecord || hasValidRecordSchedule(camera.subRecordSchedule)
        return if (!mainRec && subRec) "sub" else "main"
    }

    private fun hasValidRecordSchedule(schedule: RecordSchedule?): Boolean {
        val repeat = schedule?.repeat ?: return false
        return repeat.any { dayRanges -> dayRanges.any { it.to > it.from } }
    }

    /**
     * Chuyển đổi giữa 3 tính năng trong menu bottom của camera đang chọn:
     * - [CameraDetailTab.LIVE]: Trực tiếp
     * - [CameraDetailTab.PLAYBACK]: Xem lại (chỉ lấy danh sách file ghi hình từ VMS API, không gọi NVR)
     * - [CameraDetailTab.EVENTS]: Sự kiện (gọi API sự kiện theo UUID của camera đang chọn)
     */
    fun selectCameraDetailTab(tab: CameraDetailTab) {
        val currentCam = _uiState.value.selectedCameras.getOrNull(0)?.camera ?: return
        if (tab == CameraDetailTab.PLAYBACK && !UserPermissionManager.canAccessPlayback()) return
        if (tab == CameraDetailTab.EVENTS && !UserPermissionManager.canAccessEvents()) return
        if (_uiState.value.cameraDetailTab == tab) return

        if (tab != CameraDetailTab.LIVE) {
            // Dừng ngay vòng poll LiveStream và xóa link WS одноразовый khi rời tab Trực tiếp
            cancelStreamJob(0)
            updateSlot(0, currentCam.id) {
                it.copy(streamUrl = "", retryCount = 0, hasFallbackTried = false)
            }
        }

        var resolvedVideo: VideoModel? = null
        var resolvedPlaying = false
        _uiState.update { state ->
            val nextVideo = if (tab == CameraDetailTab.PLAYBACK) state.selectedPlaybackVideo else null
            val nextPlaying = if (nextVideo != null) state.isPlaybackPlaying else false
            resolvedVideo = nextVideo
            resolvedPlaying = nextPlaying
            state.copy(
                cameraDetailTab = tab,
                // Khi rời tab Xem lại thì đóng trình phát video ghi hình đang mở (nếu có)
                selectedPlaybackVideo = nextVideo,
                isPlaybackPlaying = nextPlaying
            )
        }
        activePlaybackVideo = resolvedVideo
        activePlaybackPlaying = resolvedPlaying

        when (tab) {
            CameraDetailTab.LIVE -> {
                reloadLiveCamera()
            }
            CameraDetailTab.PLAYBACK -> {
                if (playbackLoadedForCameraId != currentCam.id) {
                    fetchCameraPlaybacks()
                }
            }
            CameraDetailTab.EVENTS -> {
                if (eventsLoadedForCameraId != currentCam.id) {
                    fetchCameraEvents(isUserRefresh = false)
                }
            }
        }
    }

    /**
     * Tải lại luồng trực tiếp của camera đang mở trên màn hình Live 1x1.
     */
    fun reloadLiveCamera() {
        val current = _uiState.value.selectedCameras.getOrNull(0) ?: return
        updateSlot(0, current.camera.id) {
            it.copy(streamUrl = "", retryCount = 0, hasFallbackTried = false)
        }
        if (current.fallbackImageUrl.isEmpty()) {
            ensureCameraSnapshotUrl(current.camera, 0, force = false)
        }
        startLiveStreamPolling(
            camera = current.camera,
            slotIndex = 0,
            channel = current.channel,
            isFallback = false
        )
    }

    /**
     * Đóng màn hình xem trực tiếp 1x1, dừng luồng live và quay lại danh sách lưới camera.
     */
    fun closeLiveCamera() {
        cancelCameraDetailSubJobs()
        playbackLoadedForCameraId = null
        eventsLoadedForCameraId = null
        activePlaybackVideo = null
        activePlaybackPlaying = false
        removeCamera(0)
    }

    /**
     * Đổi luồng phát trực tiếp giữa kênh phụ (SD - `channel = 1`) và kênh chính (HD - `channel = 0`).
     */
    fun switchLiveChannel(channel: Int) {
        val current = _uiState.value.selectedCameras.getOrNull(0) ?: return
        val normalizedChannel = if (channel == 0) 0 else 1
        if (current.channel == normalizedChannel && !current.streamUrl.startsWith("error://")) return
        updateSlot(0, current.camera.id) {
            it.copy(
                streamUrl = "",
                channel = normalizedChannel,
                hasFallbackTried = true,
                retryCount = 0
            )
        }
        startLiveStreamPolling(
            camera = current.camera,
            slotIndex = 0,
            channel = normalizedChannel,
            isFallback = true
        )
    }

    fun getCameraLocationLabel(camera: CameraModel): String =
        treeIndex.locationLabel(camera.id)

    fun selectCameraFromTree(
        camera: CameraModel,
        onGridFull: (() -> Unit)? = null
    ) {
        openLiveCamera(camera)
    }

    fun addCamera(camera: CameraModel, slotIndex: Int, startPolling: Boolean = true) {
        if (slotIndex !in 0 until SLOT_COUNT) return

        val existing = _uiState.value.selectedCameras.getOrNull(slotIndex)
        if (existing?.camera?.id == camera.id && existing.streamUrl.startsWith("ws")) return

        val initialChannel = preferredChannel(camera)
        // Lấy ngay ảnh snapshot đang hiển thị trên thẻ hoặc đã cache trong RAM (0ms) để khung phát live có ảnh nền ngay từ frame đầu tiên
        val initialFallback = _uiState.value.cameraSnapshots[camera.id]
            ?.takeIf { it.isNotEmpty() }
            ?: MonitorCacheManager.getCachedCameraImageLinkInMemory(camera.id)
                ?.let(::resolveFullMediaUrl)
                .orEmpty()
        var updatedCameras: List<SelectedCamera?> = emptyList()
        _uiState.update { state ->
            val cameras = state.selectedCameras.toPaddedMutableList()
            cameras[slotIndex] = SelectedCamera(
                camera = camera,
                streamUrl = "",
                fallbackImageUrl = initialFallback,
                channel = initialChannel
            )
            updatedCameras = cameras
            state.copy(selectedCameras = cameras)
        }
        saveSelectedCamerasToPrefs(updatedCameras)

        if (initialFallback.isEmpty()) {
            // Fallback đọc đĩa trên IO nếu RAM cache chưa nạp xong
            viewModelScope.launch {
                val cachedSnap = readCachedSnapshot(camera).orEmpty()
                if (cachedSnap.isNotEmpty()) {
                    updateSlot(slotIndex, camera.id) {
                        if (it.fallbackImageUrl.isEmpty()) it.copy(fallbackImageUrl = cachedSnap) else it
                    }
                }
            }
        }

        if (startPolling && isMonitorTabActive) {
            // Nếu thẻ đã có sẵn snapshot hiển thị, giữ nguyên snapshot đó làm ảnh nền trong lúc kết nối luồng Live
            // để tránh đổi sang ảnh khác giữa chừng. Chỉ gọi mạng tải snapshot nếu chưa có ảnh nền.
            if (initialFallback.isEmpty()) {
                ensureCameraSnapshotUrl(camera, slotIndex, force = false)
            }
            startLiveStreamPolling(camera, slotIndex, channel = initialChannel, isFallback = false)
        }
    }

    private fun ensureCameraSnapshotUrl(camera: CameraModel, slotIndex: Int, force: Boolean) {
        viewModelScope.launch {
            if (!force) {
                val currentSlot = _uiState.value.selectedCameras.getOrNull(slotIndex)
                if (currentSlot?.camera?.id == camera.id && currentSlot.fallbackImageUrl.isNotEmpty()) {
                    val hasFreshCache = withContext(Dispatchers.IO) {
                        MonitorCacheManager.isSnapshotCacheFresh(app, camera.id) &&
                            !MonitorCacheManager.getCachedCameraImageLink(app, camera.id).isNullOrBlank()
                    }
                    if (hasFreshCache) return@launch
                }
            }

            try {
                var rawLink = ""
                val response = runCatching { vmsApi.getCameraImageLink(camera.id) }.getOrNull()
                if (response?.isSuccessful == true) {
                    val fetched = response.body()?.link?.trim().orEmpty()
                    if (fetched.isNotEmpty() && fetched != NO_IMAGE) {
                        rawLink = fetched
                    }
                }
                if (rawLink.isEmpty()) {
                    val videoRes = runCatching {
                        vmsApi.getVideoList(camera = camera.id, count = 1, from = null, to = null)
                    }.getOrNull()
                    if (videoRes?.isSuccessful == true) {
                        val firstVideo = videoRes.body()?.firstOrNull()
                        val rawThumb = firstVideo?.thumbnailLink?.ifBlank { firstVideo.imageLink }?.trim().orEmpty()
                        if (rawThumb.isNotEmpty() && rawThumb != NO_IMAGE) {
                            rawLink = rawThumb
                        }
                    }
                }

                if (rawLink.isNotEmpty() && rawLink != NO_IMAGE) {
                    val finalSnap = withContext(Dispatchers.IO) {
                        MonitorCacheManager.setCachedCameraImageLink(app, camera.id, rawLink)
                        val latestCached = MonitorCacheManager.getCachedCameraImageLink(app, camera.id)
                        if (latestCached != null && latestCached.startsWith("file://")) latestCached else resolveFullMediaUrl(rawLink)
                    }
                    updateSlot(slotIndex, camera.id) { slot ->
                        if (force || slot.fallbackImageUrl.isEmpty()) {
                            slot.copy(fallbackImageUrl = finalSnap)
                        } else {
                            slot
                        }
                    }
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
        if (!NetworkMonitor.isNetworkAvailable()) return
        val current = _uiState.value.selectedCameras.getOrNull(slotIndex) ?: return
        val camera = current.camera
        viewModelScope.launch(Dispatchers.IO) {
            MonitorCacheManager.removeCachedCameraImageLink(app, camera.id)
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

    // ---------------------------------------------------------------------------------------------
    // Xem lại (VMS VideoList API - tuyệt đối không gọi NVR)
    // ---------------------------------------------------------------------------------------------

    private val dateKeyFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
    private val dayDisplayFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }
    private val timeOnlyFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    }
    private val dateTimeFormatterThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
    }

    private fun playbackCacheKey(cameraId: Int, filter: TimeFilter, channel: String): String =
        "${cameraId}_${filter.name}_$channel"

    private fun getPlaybackSessionCache(
        cameraId: Int,
        filter: TimeFilter,
        channel: String
    ): CachedPlaybackSession? = playbackSessionCache[playbackCacheKey(cameraId, filter, channel)]

    private fun putPlaybackSessionCache(
        cameraId: Int,
        filter: TimeFilter,
        channel: String,
        dayGroups: List<PlaybackDayGroup>,
        hasMoreDays: Boolean
    ) {
        if (cameraId <= 0) return
        playbackSessionCache[playbackCacheKey(cameraId, filter, channel)] = CachedPlaybackSession(
            dayGroups = dayGroups,
            hasMoreDays = hasMoreDays,
            nextSearchDayMs = playbackCurrentSearchDayMs,
            minSearchDayMs = playbackMinSearchDayMs,
            rangeMinMs = playbackRangeMinMs,
            rangeMaxMs = playbackRangeMaxMs,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun resolveDayLabels(dateKey: String, dayStartMs: Long): Pair<String, String> {
        val targetCal = Calendar.getInstance().apply { timeInMillis = dayStartMs }
        val todayCal = Calendar.getInstance()
        if (targetCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
            targetCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Hôm nay" to "Today"
        }
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        if (targetCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
            targetCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR)
        ) {
            return "Hôm qua" to "Yesterday"
        }
        val formatted = dayDisplayFormatterThreadLocal.get()?.format(Date(dayStartMs)) ?: dateKey
        return formatted to formatted
    }

    private fun normalizeVideoModel(v: VideoModel): VideoModel {
        val millis = if (v.epochMillis > 0L) v.epochMillis else (parseVideoQueryTime(v.time) ?: 0L)
        val dispTime = if (millis > 0L) {
            timeOnlyFormatterThreadLocal.get()?.format(Date(millis)).orEmpty()
        } else {
            v.time
        }
        val dispDateTime = if (millis > 0L) {
            dateTimeFormatterThreadLocal.get()?.format(Date(millis)).orEmpty()
        } else {
            v.time
        }
        return v.copy(
            videoLink = resolveFullMediaUrl(v.videoLink),
            thumbnailLink = resolveFullMediaUrl(v.thumbnailLink),
            imageLink = resolveFullMediaUrl(v.imageLink),
            sourceType = "vms",
            displayTime = dispTime,
            displayDateTime = dispDateTime,
            epochMillis = millis
        )
    }

    /**
     * Prefetch ảnh thumbnail của các video playback vào RAM/Disk cache của Coil ở nền
     * để khi người dùng cuộn xuống các hàng tiếp theo, ảnh đã giải mã sẵn trong RAM (hiển thị 0ms, 120fps).
     */
    private fun prefetchPlaybackThumbnails(cameraId: Int, videos: List<VideoModel>) {
        if (videos.isEmpty()) return
        playbackThumbPrefetchJob?.cancel()
        playbackThumbPrefetchJob = viewModelScope.launch(Dispatchers.IO) {
            val loader = Coil.imageLoader(app)
            val memCache = loader.memoryCache
            val urlsToFetch = videos.asSequence()
                .map { it.thumbnailLink.ifEmpty { it.imageLink } }
                .filter { it.isNotEmpty() && !MonitorCacheManager.failedPlaybackThumbUrls.contains(it) }
                .distinct()
                .take(24)
                .toList()

            for (url in urlsToFetch) {
                val memKey = MonitorCacheManager.playbackThumbMemCacheKey(cameraId, url)
                if (memCache != null && memCache[coil.memory.MemoryCache.Key(memKey)] != null) {
                    continue
                }
                val req = ImageRequest.Builder(app)
                    .data(MonitorCacheManager.coilDataModel(url))
                    .size(SNAPSHOT_CARD_WIDTH_PX, SNAPSHOT_CARD_HEIGHT_PX)
                    .precision(Precision.INEXACT)
                    .allowHardware(true)
                    .allowRgb565(true)
                    .memoryCacheKey(memKey)
                    .diskCacheKey(MonitorCacheManager.playbackThumbDiskCacheKey(cameraId, url))
                    .build()
                loader.enqueue(req)
            }
        }
    }

    private fun startOfDayMillis(timeMs: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timeMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun endOfDayMillis(timeMs: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timeMs
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        return cal.timeInMillis
    }

    private fun formatDateKey(dayMs: Long): String =
        dateKeyFormatterThreadLocal.get()?.format(Date(dayMs))
            ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dayMs))

    private fun parseDateKeyStartMillis(dateKey: String): Long? =
        runCatching {
            dateKeyFormatterThreadLocal.get()?.parse(dateKey)?.time
        }.getOrNull()?.let(::startOfDayMillis)

    fun applyPlaybackFilters(filter: TimeFilter, channel: String) {
        val normalizedChannel = if (channel == "sub") "sub" else "main"
        val cameraId = _uiState.value.selectedCameras.getOrNull(0)?.camera?.id ?: 0
        val cached = getPlaybackSessionCache(cameraId, filter, normalizedChannel)
        activePlaybackVideo = null
        activePlaybackPlaying = false
        _uiState.update {
            it.copy(
                playbackTimeFilter = filter,
                playbackChannel = normalizedChannel,
                selectedPlaybackVideo = null,
                isPlaybackPlaying = false,
                playbackDayGroups = cached?.dayGroups ?: emptyList(),
                hasMorePlaybackDays = cached?.hasMoreDays ?: false
            )
        }
        fetchCameraPlaybacks(isBackgroundPrewarm = cached != null && cached.dayGroups.isNotEmpty())
    }

    fun resetPlaybackFilters() {
        val defaultChannel = _uiState.value.defaultPlaybackChannel
        applyPlaybackFilters(TimeFilter.TODAY, defaultChannel)
    }

    fun selectPlaybackVideo(video: VideoModel?) {
        var resolvedVideo: VideoModel? = null
        var resolvedPlaying = false
        _uiState.update { state ->
            if (video == null) {
                resolvedVideo = null
                resolvedPlaying = false
                state.copy(selectedPlaybackVideo = null, isPlaybackPlaying = false)
            } else {
                val current = state.selectedPlaybackVideo
                val isSame = current != null &&
                    current.videoLink == video.videoLink &&
                    current.time == video.time
                if (isSame) {
                    val nextPlaying = !state.isPlaybackPlaying
                    resolvedVideo = current
                    resolvedPlaying = nextPlaying
                    state.copy(isPlaybackPlaying = nextPlaying)
                } else {
                    resolvedVideo = video
                    resolvedPlaying = true
                    state.copy(selectedPlaybackVideo = video, isPlaybackPlaying = true)
                }
            }
        }
        activePlaybackVideo = resolvedVideo
        activePlaybackPlaying = resolvedPlaying
    }

    fun setPlaybackPlaying(isPlaying: Boolean) {
        var resolvedPlaying = false
        _uiState.update { state ->
            if (state.selectedPlaybackVideo == null) {
                resolvedPlaying = false
                if (!state.isPlaybackPlaying) state else state.copy(isPlaybackPlaying = false)
            } else if (state.isPlaybackPlaying == isPlaying) {
                resolvedPlaying = isPlaying
                state
            } else {
                resolvedPlaying = isPlaying
                state.copy(isPlaybackPlaying = isPlaying)
            }
        }
        activePlaybackPlaying = resolvedPlaying
    }

    /**
     * Đồng bộ `fetchVideosActual()` + `fetchDaysBatch()` trong `PlaybackPage.vue` (khi `playbackSource === 'vms'`):
     * Chỉ gọi `GET /VMS/api/VideoList` theo `camera.id`, `count`, `from`, `to`, `channel` (`main` | `sub`).
     * Tuyệt đối không gọi bất kỳ API nào của NVR.
     */
    fun fetchCameraPlaybacks(isBackgroundPrewarm: Boolean = false) {
        val camera = _uiState.value.selectedCameras.getOrNull(0)?.camera ?: return
        val channel = _uiState.value.playbackChannel
        val filter = _uiState.value.playbackTimeFilter

        playbackFetchJob?.cancel()
        playbackLoadMoreDaysJob?.cancel()
        for (job in playbackDayJobs.values) job.cancel()
        playbackDayJobs.clear()

        val (minMs, maxMs) = TimeFilter.resolveTimeRangeMillis(filter)
        playbackRangeMinMs = minMs
        playbackRangeMaxMs = maxMs
        playbackCurrentSearchDayMs = startOfDayMillis(maxMs)
        playbackMinSearchDayMs = startOfDayMillis(minMs)

        val cached = getPlaybackSessionCache(camera.id, filter, channel)
        val hasCachedGroups = cached != null && cached.dayGroups.isNotEmpty()

        _uiState.update {
            it.copy(
                isLoadingPlaybacks = !isBackgroundPrewarm && !hasCachedGroups,
                isLoadingMorePlaybackDays = false,
                hasMorePlaybackDays = if (hasCachedGroups) cached!!.hasMoreDays else false,
                playbackDayGroups = if (hasCachedGroups) cached!!.dayGroups else emptyList()
            )
        }

        playbackFetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                fetchPlaybackDaysBatchInternal(
                    cameraId = camera.id,
                    filter = filter,
                    channel = channel,
                    targetDaysCount = PLAYBACK_DAYS_BATCH,
                    isInitial = true
                )
                playbackLoadedForCameraId = camera.id
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingPlaybacks = false,
                        isLoadingMorePlaybackDays = false,
                        hasMorePlaybackDays = false
                    )
                }
            }
        }
    }

    fun loadMorePlaybackDays() {
        val state = _uiState.value
        val camera = state.selectedCameras.getOrNull(0)?.camera ?: return
        if (state.isLoadingPlaybacks || state.isLoadingMorePlaybackDays || !state.hasMorePlaybackDays) return

        playbackLoadMoreDaysJob?.cancel()
        playbackLoadMoreDaysJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingMorePlaybackDays = true) }
            try {
                fetchPlaybackDaysBatchInternal(
                    cameraId = camera.id,
                    filter = state.playbackTimeFilter,
                    channel = state.playbackChannel,
                    targetDaysCount = PLAYBACK_DAYS_BATCH,
                    isInitial = false
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingMorePlaybackDays = false) }
            }
        }
    }

    private suspend fun fetchPlaybackDaysBatchInternal(
        cameraId: Int,
        filter: TimeFilter,
        channel: String,
        targetDaysCount: Int,
        isInitial: Boolean
    ) {
        val accumulatedNewGroups = mutableListOf<PlaybackDayGroup>()
        var anyBatchSucceeded = false
        var firstBatchError: Throwable? = null
        var hasMoreDays = false
        var scanRound = 0
        val maxScanRounds = 4 // Tối đa quét 4 lô (32 ngày) nếu các ngày gần nhất trống

        while (scanRound < maxScanRounds) {
            scanRound++
            val startCursor = playbackCurrentSearchDayMs
            val minDay = playbackMinSearchDayMs
            if (startCursor == null || minDay == null || startCursor < minDay) {
                hasMoreDays = false
                break
            }

            val dayMillisList = mutableListOf<Long>()
            val cal = Calendar.getInstance().apply { timeInMillis = startCursor }
            while (cal.timeInMillis >= minDay && dayMillisList.size < targetDaysCount) {
                dayMillisList.add(cal.timeInMillis)
                cal.add(Calendar.DAY_OF_YEAR, -1)
            }

            if (dayMillisList.isEmpty()) {
                playbackCurrentSearchDayMs = null
                hasMoreDays = false
                break
            }

            val nextSearchDay = cal.timeInMillis
            hasMoreDays = nextSearchDay >= minDay
            playbackCurrentSearchDayMs = if (hasMoreDays) nextSearchDay else null

            val fetchedResults = coroutineScope {
                dayMillisList.map { dayStartMs ->
                    async {
                        val dateKey = formatDateKey(dayStartMs)
                        val (labelVi, labelEn) = resolveDayLabels(dateKey, dayStartMs)
                        val dayEndMs = endOfDayMillis(dayStartMs)
                        val fromMs = maxOf(dayStartMs, playbackRangeMinMs)
                        val toMs = minOf(dayEndMs, playbackRangeMaxMs)
                        val fromStr = formatVideoQueryTime(fromMs)
                        val toStr = formatVideoQueryTime(toMs)

                        val result = safeApiCall {
                            vmsApi.getVideoList(
                                camera = cameraId,
                                count = PLAYBACK_PER_DAY_COUNT,
                                from = fromStr,
                                to = toStr,
                                channel = channel
                            )
                        }
                        val response = result.getOrNull()
                        val rawVideos = if (response?.isSuccessful == true) {
                            response.body().orEmpty()
                        } else {
                            emptyList()
                        }

                        val normalizedVideos = rawVideos.map(::normalizeVideoModel)

                        PlaybackDayGroup(
                            dateKey = dateKey,
                            videos = normalizedVideos,
                            hasMore = rawVideos.size >= PLAYBACK_PER_DAY_COUNT,
                            isLoadingMore = false,
                            labelVi = labelVi,
                            labelEn = labelEn
                        ) to result.exceptionOrNull()
                    }
                }.awaitAll()
            }

            val activeCamId = _uiState.value.selectedCameras.getOrNull(0)?.camera?.id
            if (activeCamId != cameraId) return

            val fetchedGroups = fetchedResults.map { it.first }
            val roundSucceeded = fetchedResults.any { it.second == null }
            if (roundSucceeded) anyBatchSucceeded = true
            if (firstBatchError == null) {
                firstBatchError = fetchedResults.firstNotNullOfOrNull { it.second }
            }

            val nonEmpty = fetchedGroups.filter { it.videos.isNotEmpty() }
            accumulatedNewGroups.addAll(nonEmpty)

            // Nếu đã tìm thấy ít nhất 1 ngày có bản ghi, hoặc không còn ngày cũ hơn, hoặc mạng lỗi -> dừng vòng quét
            if (accumulatedNewGroups.isNotEmpty() || !hasMoreDays || !roundSucceeded) {
                break
            }
        }

        val activeCamId = _uiState.value.selectedCameras.getOrNull(0)?.camera?.id
        if (activeCamId != cameraId) return

        var finalMergedGroups: List<PlaybackDayGroup> = emptyList()
        val effectiveHasMoreDays = hasMoreDays && anyBatchSucceeded

        _uiState.update { state ->
            val merged = if (isInitial) {
                accumulatedNewGroups
            } else {
                val existingByKey = state.playbackDayGroups.associateBy { it.dateKey }.toMutableMap()
                for (grp in accumulatedNewGroups) {
                    existingByKey[grp.dateKey] = grp
                }
                existingByKey.values.sortedByDescending { it.dateKey }
            }
            finalMergedGroups = merged
            state.copy(
                playbackDayGroups = merged,
                isLoadingPlaybacks = false,
                isLoadingMorePlaybackDays = false,
                hasMorePlaybackDays = effectiveHasMoreDays,
                error = if (!anyBatchSucceeded) firstBatchError?.toUserFriendlyMessage() ?: state.error else state.error
            )
        }

        if (anyBatchSucceeded) {
            putPlaybackSessionCache(
                cameraId = cameraId,
                filter = filter,
                channel = channel,
                dayGroups = finalMergedGroups,
                hasMoreDays = effectiveHasMoreDays
            )
            val newVideosForPrefetch = accumulatedNewGroups.flatMap { it.videos }
            prefetchPlaybackThumbnails(cameraId, newVideosForPrefetch)
        }
    }

    /**
     * Đồng bộ `loadMoreForDay(dateKey)` trong `PlaybackPage.vue` (lines 1047-1177, VMS mode):
     * Lấy mốc thời gian của video cuối cùng trong ngày trừ đi 1 giây làm `to` tiếp theo và gọi `VMS/api/VideoList`.
     */
    fun loadMorePlaybackForDay(dateKey: String) {
        val state = _uiState.value
        val camera = state.selectedCameras.getOrNull(0)?.camera ?: return
        val targetGroup = state.playbackDayGroups.find { it.dateKey == dateKey } ?: return
        if (targetGroup.isLoadingMore || !targetGroup.hasMore || targetGroup.videos.isEmpty()) return

        val lastVideo = targetGroup.videos.last()
        val lastTimeMs = if (lastVideo.epochMillis > 0L) {
            lastVideo.epochMillis
        } else {
            parseVideoQueryTime(lastVideo.time) ?: return
        }
        val dayStartMs = parseDateKeyStartMillis(dateKey) ?: return
        val fromMs = maxOf(dayStartMs, playbackRangeMinMs)
        val nextToMs = (lastTimeMs - 1000L).coerceAtLeast(fromMs)

        val fromStr = formatVideoQueryTime(fromMs)
        val nextToStr = formatVideoQueryTime(nextToMs)
        val channel = state.playbackChannel
        val filter = state.playbackTimeFilter

        _uiState.update { cur ->
            cur.copy(
                playbackDayGroups = cur.playbackDayGroups.map { grp ->
                    if (grp.dateKey == dateKey) grp.copy(isLoadingMore = true) else grp
                }
            )
        }

        playbackDayJobs[dateKey]?.cancel()
        playbackDayJobs[dateKey] = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = runCatching {
                    vmsApi.getVideoList(
                        camera = camera.id,
                        count = PLAYBACK_PER_DAY_COUNT,
                        from = fromStr,
                        to = nextToStr,
                        channel = channel
                    )
                }.getOrNull()

                val rawVideos = if (response?.isSuccessful == true) {
                    response.body().orEmpty()
                } else {
                    emptyList()
                }

                val newVideos = rawVideos.map(::normalizeVideoModel)

                val activeCamId = _uiState.value.selectedCameras.getOrNull(0)?.camera?.id
                if (activeCamId != camera.id) return@launch

                var updatedGroups: List<PlaybackDayGroup> = emptyList()
                var latestHasMoreDays = false
                _uiState.update { cur ->
                    val nextGroups = cur.playbackDayGroups.map { grp ->
                        if (grp.dateKey != dateKey) {
                            grp
                        } else {
                            val combined = (grp.videos + newVideos).distinctBy {
                                it.videoLink.ifEmpty { it.time }
                            }
                            grp.copy(
                                videos = combined,
                                hasMore = rawVideos.size >= PLAYBACK_PER_DAY_COUNT && combined.size > grp.videos.size,
                                isLoadingMore = false
                            )
                        }
                    }
                    updatedGroups = nextGroups
                    latestHasMoreDays = cur.hasMorePlaybackDays
                    cur.copy(playbackDayGroups = nextGroups)
                }

                putPlaybackSessionCache(
                    cameraId = camera.id,
                    filter = filter,
                    channel = channel,
                    dayGroups = updatedGroups,
                    hasMoreDays = latestHasMoreDays
                )
                prefetchPlaybackThumbnails(camera.id, newVideos)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update { cur ->
                    cur.copy(
                        playbackDayGroups = cur.playbackDayGroups.map { grp ->
                            if (grp.dateKey == dateKey) grp.copy(isLoadingMore = false) else grp
                        }
                    )
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Sự kiện theo UUID của Camera đang chọn (`GET Data/api/Data?cameraUUID=...`)
    // ---------------------------------------------------------------------------------------------

    fun applyCameraEventFilters(filter: TimeFilter, aiType: String?) {
        _uiState.update {
            it.copy(
                cameraEventTimeFilter = filter,
                cameraEventAiType = aiType,
                isRefreshingCameraEvents = false
            )
        }
        fetchCameraEvents(isUserRefresh = false)
    }

    fun resetCameraEventFilters() {
        applyCameraEventFilters(filter = TimeFilter.TODAY, aiType = null)
    }

    fun refreshCameraEvents() {
        _uiState.update { it.copy(isRefreshingCameraEvents = true) }
        fetchCameraEvents(isUserRefresh = true)
    }

    fun cacheClickedCameraEvent(event: EventData) {
        val cameras = treeIndex.allCameras.map { it.camera }
        val services = aiServices.ifEmpty { EventCacheManager.getAiServicesInMemory() }
        EventCacheManager.cacheClickedEvent(
            event = event,
            cameras = cameras,
            aiServices = services
        )
    }

    fun fetchCameraEvents(isUserRefresh: Boolean = false) {
        val camera = _uiState.value.selectedCameras.getOrNull(0)?.camera ?: return
        val cameraUuid = camera.extra?.uuid?.trim().orEmpty()

        cameraEventFetchJob?.cancel()
        cameraEventPaginateJob?.cancel()
        lastCameraEventRawId = null

        // Nếu camera chưa có UUID thì không gọi API sự kiện (tránh trả về sự kiện của toàn bộ camera)
        if (cameraUuid.isEmpty()) {
            eventsLoadedForCameraId = camera.id
            _uiState.update {
                it.copy(
                    cameraEvents = emptyList(),
                    isLoadingCameraEvents = false,
                    isRefreshingCameraEvents = false,
                    isPaginatingCameraEvents = false,
                    hasMoreCameraEvents = false
                )
            }
            return
        }

        val state = _uiState.value
        val filter = state.cameraEventTimeFilter
        val aiType = state.cameraEventAiType

        _uiState.update {
            it.copy(
                isLoadingCameraEvents = !isUserRefresh,
                isPaginatingCameraEvents = false,
                hasMoreCameraEvents = true
            )
        }

        cameraEventFetchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val (fromMs, toMs) = TimeFilter.resolveTimeRangeMillis(filter)
                val fromSec = fromMs / 1000
                val toSec = toMs / 1000
                val resolvedType = resolveApiEventType(aiType)

                val batchResult = fetchPermittedCameraEventBatch(
                    startLastId = null,
                    fromSec = fromSec,
                    toSec = toSec,
                    cameraUuid = cameraUuid,
                    resolvedType = resolvedType
                )
                val events = batchResult.events
                val nextLastId = batchResult.lastRawId
                val hasMore = batchResult.hasMore

                val activeCamId = _uiState.value.selectedCameras.getOrNull(0)?.camera?.id
                if (activeCamId != camera.id) return@launch

                lastCameraEventRawId = nextLastId
                eventsLoadedForCameraId = camera.id

                if (events.isNotEmpty()) {
                    EventCacheManager.cacheInMemory(
                        events = events,
                        cameras = treeIndex.allCameras.map { it.camera },
                        aiServices = aiServices
                    )
                }

                _uiState.update {
                    it.copy(
                        cameraEvents = if (batchResult.apiSucceeded || events.isNotEmpty()) events else it.cameraEvents,
                        isLoadingCameraEvents = false,
                        isRefreshingCameraEvents = false,
                        isPaginatingCameraEvents = false,
                        hasMoreCameraEvents = hasMore,
                        error = batchResult.firstError?.toUserFriendlyMessage() ?: it.error
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoadingCameraEvents = false,
                        isRefreshingCameraEvents = false,
                        isPaginatingCameraEvents = false,
                        hasMoreCameraEvents = false,
                        error = e.toUserFriendlyMessage()
                    )
                }
            }
        }
    }

    fun loadMoreCameraEvents() {
        if (cameraEventFetchJob?.isActive == true) return
        val state = _uiState.value
        val camera = state.selectedCameras.getOrNull(0)?.camera ?: return
        val cameraUuid = camera.extra?.uuid?.trim().orEmpty()
        if (cameraUuid.isEmpty()) return
        if (state.isLoadingCameraEvents || state.isPaginatingCameraEvents || !state.hasMoreCameraEvents || state.cameraEvents.isEmpty()) return

        cameraEventPaginateJob?.cancel()
        cameraEventPaginateJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isPaginatingCameraEvents = true) }
            try {
                val startId = lastCameraEventRawId ?: state.cameraEvents.last().id
                val (fromMs, toMs) = TimeFilter.resolveTimeRangeMillis(state.cameraEventTimeFilter)
                val batchResult = fetchPermittedCameraEventBatch(
                    startLastId = startId,
                    fromSec = fromMs / 1000,
                    toSec = toMs / 1000,
                    cameraUuid = cameraUuid,
                    resolvedType = resolveApiEventType(state.cameraEventAiType)
                )
                val newEvents = batchResult.events
                val nextLastId = batchResult.lastRawId
                val hasMore = batchResult.hasMore

                val activeCamId = _uiState.value.selectedCameras.getOrNull(0)?.camera?.id
                if (activeCamId != camera.id) return@launch

                lastCameraEventRawId = nextLastId
                if (newEvents.isNotEmpty()) {
                    EventCacheManager.cacheInMemory(events = newEvents)
                }

                _uiState.update { cur ->
                    val merged = (cur.cameraEvents + newEvents).distinctBy { ev -> ev.uniqueKey() }
                    cur.copy(
                        isPaginatingCameraEvents = false,
                        cameraEvents = merged,
                        hasMoreCameraEvents = hasMore && merged.size > cur.cameraEvents.size,
                        error = batchResult.firstError?.toUserFriendlyMessage() ?: cur.error
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isPaginatingCameraEvents = false,
                        hasMoreCameraEvents = false,
                        error = e.toUserFriendlyMessage()
                    )
                }
            }
        }
    }

    private data class CameraEventBatchResult(
        val events: List<EventData>,
        val lastRawId: Int?,
        val hasMore: Boolean,
        val apiSucceeded: Boolean,
        val firstError: Throwable? = null
    )

    private suspend fun fetchPermittedCameraEventBatch(
        startLastId: Int?,
        fromSec: Long,
        toSec: Long,
        cameraUuid: String,
        resolvedType: String?
    ): CameraEventBatchResult {
        if (!UserPermissionManager.canAccessEvents()) {
            return CameraEventBatchResult(
                events = emptyList(),
                lastRawId = startLastId,
                hasMore = false,
                apiSucceeded = true
            )
        }
        val validData = mutableListOf<EventData>()
        val seenKeys = mutableSetOf<String>()
        var currentLastId = startLastId
        var attempts = 0
        var reachedEnd = false
        var anySuccessful = false
        var firstError: Throwable? = null

        val allCameras = rawCameraList.ifEmpty { EventCacheManager.getCamerasInMemory() }
        while (attempts < EVENT_MAX_BATCH_ATTEMPTS) {
            attempts++
            val result = safeApiCall {
                eventApi.getDataList(
                    lastId = currentLastId,
                    count = EVENT_RAW_BATCH_SIZE,
                    from = fromSec,
                    to = toSec,
                    serviceId = null,
                    cameraUUID = cameraUuid,
                    type = resolvedType
                )
            }
            val response = result.getOrNull()
            if (response == null) {
                if (firstError == null) firstError = result.exceptionOrNull()
                reachedEnd = true
                break
            }
            anySuccessful = true

            val batchData = response.body().orEmpty()
            if (batchData.isEmpty()) {
                reachedEnd = true
                break
            }

            for (ev in batchData) {
                if (UserPermissionManager.isEventPermitted(ev, allCameras) && seenKeys.add(ev.uniqueKey())) {
                    validData.add(ev)
                }
            }

            val lastItem = batchData.lastOrNull()
            if (lastItem == null || lastItem.id <= 0 || lastItem.id == currentLastId) {
                reachedEnd = true
                break
            }
            currentLastId = lastItem.id

            if (batchData.size < EVENT_RAW_BATCH_SIZE) {
                reachedEnd = true
                break
            }
            if (validData.size >= EVENT_PAGE_TARGET_COUNT) {
                break
            }
        }

        val hasMore = !reachedEnd && validData.isNotEmpty()
        return CameraEventBatchResult(
            events = validData,
            lastRawId = currentLastId,
            hasMore = hasMore,
            apiSucceeded = anySuccessful,
            firstError = if (anySuccessful) null else firstError
        )
    }
}
