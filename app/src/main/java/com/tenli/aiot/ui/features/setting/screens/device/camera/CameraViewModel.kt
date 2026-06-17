package com.tenli.aiot.ui.features.setting.screens.device.camera


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tenli.aiot.data.repository.AppRepository
import com.tenli.aiot.data.repository.BoxRepository
import com.tenli.aiot.model.network.CameraInfo
import com.tenli.aiot.model.network.DeviceItem
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CameraViewModel(
    private val appRepository: AppRepository,
    private val boxRepository: BoxRepository,
    private val device: DeviceItem,
    private val accessKey: String,
    val onNavigateBack: () -> Unit,
    val onNavigateTo: (SettingScreenType, String) -> Unit,
    val onShowSnackbar: (String) -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState(cameras = device.info?.cameraInfo ?: emptyList()))
    val uiState = _uiState.asStateFlow()

    init {
        refreshCameras()
    }

    fun getSnapshotUrl(camera: CameraInfo): String {
        return camera.snapshot?.publicURL ?: ""
    }

    fun getAccessKey(): String = accessKey

    fun toggleMenu(expanded: Boolean) {
        _uiState.update { it.copy(isMenuExpanded = expanded) }
    }

    fun refreshCameras() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val boxDeferred = async { boxRepository.getCameras(device.publicTargetURI, device.key) }
                val cloudDeferred = async { appRepository.getDeviceDetail(device.id) }

                val boxRes = boxDeferred.await()
                val cloudRes = cloudDeferred.await()

                if (boxRes.isSuccess && cloudRes.isSuccess) {
                    val boxCams = boxRes.getOrNull() ?: emptyList()
                    val cloudDetail = cloudRes.getOrNull()
                    val cloudCams = cloudDetail?.info?.cameraInfo ?: emptyList()

                    val mergedCams = boxCams.map { bCam ->
                        val cCam = cloudCams.find { it.id == bCam.id }
                        CameraInfo(
                            id = bCam.id,
                            name = bCam.name,
                            type = bCam.type,
                            url = bCam.url,
                            state = cCam?.state ?: "active",
                            snapshot = cCam?.snapshot
                        )
                    }
                    _uiState.update {
                        it.copy(cameras = mergedCams, isLoading = false)
                    }
                } else {
                    onShowSnackbar("Lỗi: Không thể đồng bộ đầy đủ dữ liệu")
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                onShowSnackbar("Lỗi kết nối: ${e.message}")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addCameraByBrand(name: String, url: String, decodingMode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val streamType = when (decodingMode) {
                "Phần mềm" -> "stream-software"
                "Phần cứng" -> "stream-hardware"
                else -> "stream"
            }

            try {
                val request = com.tenli.aiot.model.network.AddCameraRequest(
                    id = 0,
                    name = name,
                    url = url,
                    type = streamType,
                    uuid = UUID.randomUUID().toString(),
                    extra = null
                )

                val result = boxRepository.addCamera(device.publicTargetURI, device.key, request)
                if (result.isSuccess) {
                    // 1. TỰ THÊM CAM VÀO LIST (Cập nhật ảo) [cite: 2026-03-13]
                    val fakeId = -(System.currentTimeMillis() % 10000).toInt()
                    val newFakeCamera = CameraInfo(
                        id = fakeId,
                        name = name,
                        type = if (decodingMode == "Phần mềm") "stream-software" else "stream",
                        state = "active",
                        snapshot = null,
                        url = url
                    )

                    _uiState.update { state ->
                        state.copy(
                            cameras = state.cameras + newFakeCamera,
                            isLoading = false // Tắt loading ngay tại đây
                        )
                    }

                    onShowSnackbar("Thêm camera thành công")
                    onSuccess() // Quay về màn hình danh sách ngay lập tức

                    // KHÔNG gọi refreshCameras() ở đây nữa để tránh ghi đè list cũ [cite: 2026-03-13]
                }
            } catch (_: Exception) {
                onShowSnackbar("Lỗi kết nối tới Box")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectCameraForEdit(camera: CameraInfo) {
        _uiState.update { it.copy(selectedCamera = camera) }
        onNavigateTo(SettingScreenType.EditCamera, "Sửa thông tin camera")
    }

    fun editCamera(id: Int, name: String, url: String, decodingMode: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val streamType = when (decodingMode) {
                "Phần mềm" -> "stream-software"
                "Phần cứng" -> "stream-hardware"
                else -> "stream"
            }
            try {
                val request = com.tenli.aiot.model.network.AddCameraRequest(
                    id = id,
                    name = name,
                    url = url,
                    type = streamType,
                    uuid = "", // uuid might be preserved but it's optional for edit
                    extra = null
                )
                val result = boxRepository.editCamera(device.publicTargetURI, device.key, id, request)
                if (result.isSuccess) {
                    // CẬP NHẬT ẢO: Map lại danh sách để đổi thông tin [cite: 2026-03-13]
                    _uiState.update { state ->
                        val updatedList = state.cameras.map {
                            if (it.id == id) it.copy(name = name, url = url, type = streamType)
                            else it
                        }
                        state.copy(cameras = updatedList)
                    }
                    onShowSnackbar("Cập nhật thành công")
                    onSuccess()
                }
            } catch (e: Exception) {
                onShowSnackbar("Lỗi cập nhật")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // 3. XÓA: Lọc bỏ cam khỏi list ngay lập tức
    fun deleteCamera(id: Int, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val result = boxRepository.deleteCamera(device.publicTargetURI, device.key, id)
                if (result.isSuccess) {
                    // CẬP NHẬT ẢO: Filter bỏ camera vừa xóa [cite: 2026-03-13]
                    _uiState.update { state ->
                        val updatedList = state.cameras.filter { it.id != id }
                        state.copy(cameras = updatedList)
                    }
                    onShowSnackbar("Đã xóa camera thành công")
                    onSuccess()
                }
            } catch (e: Exception) {
                onShowSnackbar("Lỗi kết nối")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

class CameraViewModelFactory(
    private val appRepository: AppRepository,
    private val boxRepository: BoxRepository,
    private val device: DeviceItem,
    private val accessKey: String,
    private val onShowSnackbar: (String) -> Unit,
    private val onNavigateBack: () -> Unit,
    private val onNavigateTo: (SettingScreenType, String) -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CameraViewModel(appRepository, boxRepository, device, accessKey, onNavigateBack, onNavigateTo, onShowSnackbar) as T
    }
}