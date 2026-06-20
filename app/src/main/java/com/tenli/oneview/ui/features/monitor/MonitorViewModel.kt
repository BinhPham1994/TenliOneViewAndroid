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
    val retryCount: Int = 0
)

data class MonitorUiState(
    val isLoading: Boolean = false,
    val treeData: List<CameraTreeNode> = emptyList(),
    val selectedCameras: List<SelectedCamera?> = List(1) { null },
    val expandedNodes: Set<Any> = emptySet(), // Store IDs or object references of expanded nodes
    val error: String? = null
)

class MonitorViewModel : ViewModel() {
    private val vmsApi = LoginAuthClient.create(VmsApi::class.java)

    private val _uiState = MutableStateFlow(MonitorUiState())
    val uiState: StateFlow<MonitorUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val vmsResponse = vmsApi.getVMSServiceList()
                val groupResponse = vmsApi.getCameraGroupList()
                val cameraResponse = vmsApi.getCameraList()
                val cameraInGroupResponse = vmsApi.getCameraInGroupList()

                if (vmsResponse.isSuccessful && groupResponse.isSuccessful && cameraResponse.isSuccessful && cameraInGroupResponse.isSuccessful) {
                    val vmsList = vmsResponse.body() ?: emptyList()
                    val groupList = groupResponse.body() ?: emptyList()
                    val cameraList = cameraResponse.body() ?: emptyList()
                    val cameraInGroups = cameraInGroupResponse.body() ?: emptyList()

                    val treeData = mutableListOf<CameraTreeNode>()
                    val groupMapData = mutableMapOf<String, CameraTreeNode.GroupNode>()

                    val fullVmsList = mutableListOf(
                        VMSServiceModel(id = 0, name = "VMS Local", privateHost = "", publicHost = "", apiKey = "")
                    )
                    fullVmsList.addAll(vmsList)

                    val camerasByVms = cameraList.groupBy { it.vmsId }
                    val groupsByVms = groupList.groupBy { it.vmsId }
                    val cameraInGroupByVms = mutableMapOf<Int, MutableList<com.tenli.oneview.model.network.CameraInGroupModel>>()
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
                    _uiState.update { it.copy(isLoading = false, treeData = prunedTree.toList()) }
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

    fun addCamera(camera: CameraModel, slotIndex: Int) {
        val currentState = _uiState.value
        
        if (slotIndex != 0) return

        if (currentState.selectedCameras.any { it?.camera?.id == camera.id }) {
            return // Prevent duplicates
        }

        val updatedCameras = currentState.selectedCameras.toMutableList()
        // Placeholder SelectedCamera with streamUrl = ""
        updatedCameras[slotIndex] = SelectedCamera(camera, "")

        _uiState.update { it.copy(selectedCameras = updatedCameras) }

        // Fetch live stream URL asynchronously
        fetchLiveStreamUrl(camera.id, slotIndex)
    }

    /**
     * Re-fetches live stream URLs for all active cameras.
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
                            if (currentSelection?.camera?.id == cameraId) {
                                android.util.Log.d("MonitorVM", "fetchLiveStreamUrl: updating slot $slotIndex with url=$streamUrl codec=${liveStreamModel.videoCodecTag}")
                                cameras[slotIndex] = currentSelection.copy(
                                    streamUrl = streamUrl, 
                                    videoCodecTag = liveStreamModel.videoCodecTag
                                    // Removed retryCount = 0 to prevent infinite loop
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
                    _uiState.update { it.copy(error = "Failed to get live stream for camera") }
                    retryCameraStream(slotIndex) // Retry on API failure too
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error starting live stream: ${e.message}") }
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
            val updatedCameras = state.selectedCameras.toMutableList()
            updatedCameras[slotIndex] = null
            state.copy(selectedCameras = updatedCameras)
        }
    }

    fun retryCameraStream(slotIndex: Int) {
        val currentState = _uiState.value
        val cameras = currentState.selectedCameras.toMutableList()
        val currentSelection = cameras.getOrNull(slotIndex) ?: return
        
        if (currentSelection == null || currentSelection.retryCount >= 5) {
            if (currentSelection?.retryCount == 5) {
                // To avoid spamming, only show toast once when reaching max
                _uiState.update { it.copy(error = "Không thể phát luồng camera ${currentSelection.camera.name}") }
                cameras[slotIndex] = currentSelection.copy(retryCount = 6)
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
}
