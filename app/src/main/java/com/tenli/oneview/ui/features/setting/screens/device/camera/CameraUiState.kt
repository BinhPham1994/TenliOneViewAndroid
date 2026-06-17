package com.tenli.oneview.ui.features.setting.screens.device.camera

import com.tenli.oneview.model.network.CameraInfo

data class CameraUiState(
    val cameras: List<CameraInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isMenuExpanded: Boolean = false,
    val selectedCamera: CameraInfo? = null,
)