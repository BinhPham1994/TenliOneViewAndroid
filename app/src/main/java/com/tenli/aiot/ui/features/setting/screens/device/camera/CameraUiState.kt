package com.tenli.aiot.ui.features.setting.screens.device.camera

import com.tenli.aiot.model.network.CameraInfo

data class CameraUiState(
    val cameras: List<CameraInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isMenuExpanded: Boolean = false,
    val selectedCamera: CameraInfo? = null,
)