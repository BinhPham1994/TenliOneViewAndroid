package com.tenli.oneview.ui.features.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.VMSServiceModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tenli.oneview.data.local.CachedMonitorData
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.model.network.CameraInGroupModel
sealed class CameraTreeNode {
    abstract val key: String
    data class VMSNode(val vms: VMSServiceModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class GroupNode(val group: CameraGroupModel, val children: MutableList<CameraTreeNode>, override val key: String) : CameraTreeNode()
    data class CameraLeaf(val camera: CameraModel, override val key: String) : CameraTreeNode()
}

data class SelectedCamera(
    val camera: CameraModel,
    val streamUrl: String,
    val videoCodecTag: String = "",
    val retryCount: Int = 0,
    val streamType: String = "LIVE", // "LIVE", "EVENT", "PLAYBACK"
    val fallbackImageUrl: String = "",
    val vmsName: String = ""
)

enum class MonitorTab {
    EVENTS, PLAYBACK
}

enum class MonitorTimeFilter(val title: String) {
    TODAY("Hôm nay"),
    YESTERDAY("Hôm qua"),
    LAST_7_DAYS("7 ngày qua"),
    LAST_30_DAYS("30 ngày qua")
}


data class MonitorUiState(
    val isLoading: Boolean = false,
    val treeData: List<CameraTreeNode> = emptyList(),
    val selectedCameras: List<SelectedCamera?> = List(1) { null },
    val expandedNodes: Set<Any> = emptySet(),
    val error: String? = null,
    val aiServices: List<com.tenli.oneview.model.network.AIServiceModel> = emptyList(),
    val selectedTab: MonitorTab = MonitorTab.EVENTS,
    val selectedTimeFilter: MonitorTimeFilter = MonitorTimeFilter.TODAY,
    val events: List<com.tenli.oneview.model.network.EventData> = emptyList(),
    val playbacks: List<com.tenli.oneview.model.network.VideoModel> = emptyList(),
    val isPaginating: Boolean = false,
    val hasMoreEvents: Boolean = true,
    val hasMorePlaybacks: Boolean = true
)


class MonitorViewModel(application: Application) : AndroidViewModel(application) {
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)
    private val eventApi = LoginAuthClient.create(com.tenli.oneview.data.network.api.EventApi::class.java)

    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    init {
        loadData()
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

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val cachedData = MonitorCacheManager.getMonitorData(getApplication())
            if (cachedData != null && cachedData.vmsList.isNotEmpty()) {
                val finalTree = buildTree(cachedData.vmsList, cachedData.groupList, cachedData.cameraList, cachedData.cameraInGroups)
                _uiState.update { it.copy(treeData = finalTree) }
                
                val prefs = com.tenli.oneview.data.local.GlobalData.preferences
                val savedCameraId = prefs.getInt("saved_monitor_camera_id", -1)
                var targetCamera: CameraModel? = null

                if (savedCameraId != -1) {
                    targetCamera = findCameraById(finalTree, savedCameraId)
                }
                if (targetCamera == null) {
                    targetCamera = findFirstCamera(finalTree)
                }

                if (targetCamera != null && _uiState.value.selectedCameras[0] == null) {
                    addCamera(targetCamera, 0)
                }
            }

            try {
                val vmsResponse = vmsApi.getVMSServiceList()
                val groupResponse = vmsApi.getCameraGroupList()
                val cameraResponse = vmsApi.getCameraList()
                val cameraInGroupResponse = vmsApi.getCameraInGroupList()
                val aiServiceResponse = eventApi.getAIServiceList()

                if (vmsResponse.isSuccessful && groupResponse.isSuccessful && cameraResponse.isSuccessful && cameraInGroupResponse.isSuccessful) {
                    val vmsList = vmsResponse.body() ?: emptyList()
                    val groupList = groupResponse.body() ?: emptyList()
                    val cameraList = cameraResponse.body() ?: emptyList()
                    val cameraInGroups = cameraInGroupResponse.body() ?: emptyList()
                    val aiServices = if (aiServiceResponse.isSuccessful) aiServiceResponse.body() ?: emptyList() else emptyList()

                    val finalTree = buildTree(vmsList, groupList, cameraList, cameraInGroups)

                    _uiState.update { it.copy(isLoading = false, treeData = finalTree, aiServices = aiServices) }

                    val prefs = com.tenli.oneview.data.local.GlobalData.preferences
                    val savedCameraId = prefs.getInt("saved_monitor_camera_id", -1)
                    var targetCamera: CameraModel? = null

                    if (savedCameraId != -1) {
                        targetCamera = findCameraById(finalTree, savedCameraId)
                    }
                    if (targetCamera == null) {
                        targetCamera = findFirstCamera(finalTree)
                    }

                    if (targetCamera != null && _uiState.value.selectedCameras[0] == null) {
                        addCamera(targetCamera, 0)
                    }

                    MonitorCacheManager.saveMonitorData(
                        getApplication(),
                        CachedMonitorData(vmsList, groupList, cameraList, cameraInGroups)
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to load data") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
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

    fun toggleNodeExpansion(nodeId: Any) {
        _uiState.update { state ->
            val newExpanded = state.expandedNodes.toMutableSet()
            if (newExpanded.contains(nodeId)) {
                newExpanded.remove(nodeId)
            } else {
                newExpanded.add(nodeId)
            }
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

    fun addCamera(camera: CameraModel, slotIndex: Int) {
        val currentState = _uiState.value
        
        if (slotIndex != 0) return

        val existing = currentState.selectedCameras.find { it?.camera?.id == camera.id }
        if (existing != null && existing.streamUrl.startsWith("ws")) {
            return
        }

        val prefs = com.tenli.oneview.data.local.GlobalData.preferences
        prefs.edit().putInt("saved_monitor_camera_id", camera.id).apply()

        val vmsName = getVmsNameForCamera(camera.vmsId)

        val updatedCameras = currentState.selectedCameras.toMutableList()
        updatedCameras[slotIndex] = SelectedCamera(camera, "", vmsName = vmsName)

        _uiState.update { it.copy(selectedCameras = updatedCameras) }

        fetchLiveStreamUrl(camera.id, slotIndex)
        fetchListData()
    }

    fun playVideo(videoUrl: String, streamType: String, fallbackImageUrl: String = "") {
        if (videoUrl.isBlank()) return
        
        _uiState.update { state ->
            val cameras = state.selectedCameras.toMutableList()
            val currentSelection = cameras[0]
            if (currentSelection != null) {
                var fullUrl = videoUrl
                if (fullUrl.startsWith("/")) {
                    val domain = com.tenli.oneview.data.local.UserSession.domain.trimEnd('/')
                    fullUrl = "$domain$fullUrl"
                }
                
                cameras[0] = currentSelection.copy(streamUrl = fullUrl, streamType = streamType, fallbackImageUrl = fallbackImageUrl)
            }
            state.copy(selectedCameras = cameras)
        }
    }

    /**
     * Called when returning to the Monitor tab or resuming from background,
     * since WebSocket sessions expire when the screen is not visible.
     */
    fun refreshActiveStreams() {
        val currentState = _uiState.value
        val cameras = currentState.selectedCameras.toMutableList()
        var hasActive = false

        cameras.forEachIndexed { index, selectedCamera ->
            if (selectedCamera != null) {
                android.util.Log.d("MonitorVM", "refreshActiveStreams: clearing slot $index for camera ${selectedCamera.camera.id}")
                cameras[index] = selectedCamera.copy(streamUrl = "")
                hasActive = true
            }
        }

        if (hasActive) {
            _uiState.update { it.copy(selectedCameras = cameras) }

            // Re-fetch fresh stream URLs for each camera
            currentState.selectedCameras.forEachIndexed { index, selectedCamera ->
                if (selectedCamera != null) {
                    fetchLiveStreamUrl(selectedCamera.camera.id, index)
                }
            }
        }
    }

    private fun fetchLiveStreamUrl(cameraId: Int, slotIndex: Int) {
        viewModelScope.launch {
            try {
                // We construct a LiveStreamModel. Some fields might not be required for the request
                val requestModel = LiveStreamModel(
                    cameraId = cameraId,
                    channel = 1,
                    link = "",
                    videoCodec = "",
                    audioCodec = "",
                    videoCodecTag = ""
                )
                val response = vmsApi.createLiveStream(requestModel)
                android.util.Log.d("MonitorVM", "fetchLiveStreamUrl: camera=$cameraId slot=$slotIndex response=${response.code()}")
                if (response.isSuccessful) {
                    val liveStreamModel = response.body()
                    if (liveStreamModel != null) {
                        var streamUrl = liveStreamModel.link
                        if (streamUrl.startsWith("/")) {
                            val domain = com.tenli.oneview.data.local.UserSession.domain.trimEnd('/')
                            streamUrl = "$domain$streamUrl"
                        }
                        if (streamUrl.startsWith("http://")) streamUrl = streamUrl.replaceFirst("http://", "ws://")
                        if (streamUrl.startsWith("https://")) streamUrl = streamUrl.replaceFirst("https://", "wss://")
                        
                        _uiState.update { state ->
                            val cameras = state.selectedCameras.toMutableList()
                            val currentSelection = cameras[slotIndex]
                            if (currentSelection != null && currentSelection.camera.id == cameraId) {
                                cameras[slotIndex] = currentSelection.copy(
                                    streamUrl = streamUrl,
                                    videoCodecTag = liveStreamModel.videoCodecTag,
                                    retryCount = 0,
                                    streamType = "LIVE"
                                )
                            } else {
                                android.util.Log.w("MonitorVM", "fetchLiveStreamUrl: camera id mismatch at slot $slotIndex. Expected=$cameraId, found=${currentSelection?.camera?.id}")
                            }
                            state.copy(
                                selectedCameras = cameras,
                                error = null
                            )
                        }
                    }
                } else {
                    retryCameraStream(slotIndex) // Retry on API failure too
                }
            } catch (e: Exception) {
                retryCameraStream(slotIndex) // Retry on network failure
            }
        }
    }

    fun resetRetryCount(slotIndex: Int) {
        val currentState = _uiState.value
        val cameras = currentState.selectedCameras.toMutableList()
        val currentSelection = cameras.getOrNull(slotIndex) ?: return
        if (currentSelection != null) {
            cameras[slotIndex] = currentSelection.copy(retryCount = 0)
            _uiState.update { it.copy(selectedCameras = cameras) }
        }
    }

    fun removeCamera(slotIndex: Int) {
        if (slotIndex != 0) return
        _uiState.update { state ->
            val cameras = state.selectedCameras.toMutableList()
            cameras[slotIndex] = null
            
            // Clear saved preference if the first slot is removed
            if (slotIndex == 0) {
                val prefs = com.tenli.oneview.data.local.GlobalData.preferences
                prefs.edit().remove("saved_monitor_camera_id").apply()
            }
            
            state.copy(selectedCameras = cameras)
        }
    }

    fun retryCameraStream(slotIndex: Int) {
        val currentState = _uiState.value
        val cameras = currentState.selectedCameras.toMutableList()
        val currentSelection = cameras.getOrNull(slotIndex) ?: return
        
        if (currentSelection == null || currentSelection.retryCount >= 5) {
            if (currentSelection?.retryCount == 5) {
                cameras[slotIndex] = currentSelection.copy(
                    retryCount = 6,
                    streamUrl = "error://failed"
                )
                _uiState.update { it.copy(selectedCameras = cameras) }
            }
            return
        }

        // Increment retry count and clear URL to show loading
        cameras[slotIndex] = currentSelection.copy(
            streamUrl = "",
            retryCount = currentSelection.retryCount + 1
        )
        _uiState.update { it.copy(selectedCameras = cameras) }

        // Exponential backoff like the web client
        viewModelScope.launch {
            val delayMs = Math.min(1000 * Math.pow(2.0, currentSelection.retryCount.toDouble()).toLong(), 10000L)
            kotlinx.coroutines.delay(delayMs)
            fetchLiveStreamUrl(currentSelection.camera.id, slotIndex)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setTab(tab: MonitorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        fetchListData()
    }

    fun setTimeFilter(filter: MonitorTimeFilter) {
        _uiState.update { it.copy(selectedTimeFilter = filter) }
        fetchListData()
    }

    private fun fetchListData() {
        val selectedCam = _uiState.value.selectedCameras.firstOrNull()?.camera ?: return
        
        viewModelScope.launch {
            try {
                val calendar = java.util.Calendar.getInstance()
                val toTime: Long
                val fromTime: Long
                
                when (_uiState.value.selectedTimeFilter) {
                    MonitorTimeFilter.TODAY -> {
                        toTime = calendar.timeInMillis
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)
                        fromTime = calendar.timeInMillis
                    }
                    MonitorTimeFilter.YESTERDAY -> {
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)
                        toTime = calendar.timeInMillis - 1
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        fromTime = calendar.timeInMillis
                    }
                    MonitorTimeFilter.LAST_7_DAYS -> {
                        toTime = calendar.timeInMillis
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                        fromTime = calendar.timeInMillis
                    }
                    MonitorTimeFilter.LAST_30_DAYS -> {
                        toTime = calendar.timeInMillis
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
                        fromTime = calendar.timeInMillis
                    }
                }

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
                val calendar = java.util.Calendar.getInstance()
                val toTime: Long
                val fromTime: Long
                
                when (currentState.selectedTimeFilter) {
                    MonitorTimeFilter.TODAY -> {
                        toTime = calendar.timeInMillis
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)
                        fromTime = calendar.timeInMillis
                    }
                    MonitorTimeFilter.YESTERDAY -> {
                        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                        calendar.set(java.util.Calendar.MINUTE, 0)
                        calendar.set(java.util.Calendar.SECOND, 0)
                        calendar.set(java.util.Calendar.MILLISECOND, 0)
                        toTime = calendar.timeInMillis - 1
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
                        fromTime = calendar.timeInMillis
                    }
                    MonitorTimeFilter.LAST_7_DAYS -> {
                        toTime = calendar.timeInMillis
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -7)
                        fromTime = calendar.timeInMillis
                    }
                    MonitorTimeFilter.LAST_30_DAYS -> {
                        toTime = calendar.timeInMillis
                        calendar.add(java.util.Calendar.DAY_OF_YEAR, -30)
                        fromTime = calendar.timeInMillis
                    }
                }

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
                    // For playback, we use the time of the last item as `to` time
                    val lastVideoTimeStr = currentState.playbacks.last().time
                    // However time string from api is often without 'Z', we need to pass what API expects.
                    // Just pass it directly as toStr, or reformat. We'll pass it directly as `to` parameter
                    
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                MonitorViewModel(application)
            }
        }
    }
}
