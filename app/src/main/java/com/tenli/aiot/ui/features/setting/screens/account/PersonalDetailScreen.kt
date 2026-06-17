package com.tenli.aiot.ui.features.setting.screens.account

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.tenli.aiot.ui.features.setting.components.ProfileGenderDropdown
import com.tenli.aiot.ui.features.setting.components.SettingInputField
import com.tenli.aiot.ui.features.setting.components.SettingPrimaryButton
import com.tenli.aiot.ui.features.setting.components.UserAvatarBox
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.onAccountFieldChange
import com.tenli.aiot.ui.features.setting.core.onGenderSelect
import com.tenli.aiot.ui.features.setting.core.updateUserProfile
import com.tenli.aiot.ui.features.setting.core.uploadAvatar
import com.tenli.aiot.ui.theme.spacing

@Composable
fun PersonalDetailScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) result.uriContent?.let { viewModel.uploadAvatar(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. CHỈNH SỬA AVATAR
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.clickable {
            cropLauncher.launch(
                CropImageContractOptions(
                    null, CropImageOptions(
                        activityTitle = "Cắt ảnh đại diện", cropMenuCropButtonTitle = "Xong",
                        cropShape = CropImageView.CropShape.OVAL, fixAspectRatio = true, aspectRatioX = 1, aspectRatioY = 1
                    )
                )
            )
        }) {
            UserAvatarBox(uiState.userData?.avatar?.getAvatarImage(), size = 140.dp)
            if (uiState.account.isUpdating) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            }
            Surface(
                modifier = Modifier.fillMaxWidth(0.35f).height(30.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Sửa", color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // 2. CÁC TRƯỜNG NHẬP LIỆU
        SettingInputField("Tên hiển thị", uiState.account.tempName, placeholder = "Nhập tên", onValueChange = { viewModel.onAccountFieldChange(name = it) }, onClear = { viewModel.onAccountFieldChange(name = "") })
        SettingInputField("Email", uiState.account.tempEmail, enabled = false, onValueChange = {}, onClear = {})

        ProfileGenderDropdown(uiState.account.tempGender) { viewModel.onGenderSelect(it) }

        SettingInputField("Số điện thoại", uiState.account.tempPhone, placeholder = "Nhập SĐT", onValueChange = { viewModel.onAccountFieldChange(phone = it) }, onClear = { viewModel.onAccountFieldChange(phone = "") })
        SettingInputField("Địa chỉ", uiState.account.tempAddress, placeholder = "Nhập địa chỉ", onValueChange = { viewModel.onAccountFieldChange(address = it) }, onClear = { viewModel.onAccountFieldChange(address = "") })

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

        // 3. NÚT LƯU
        SettingPrimaryButton("Lưu", isLoading = uiState.account.isUpdating, onClick = {
            focusManager.clearFocus()
            viewModel.updateUserProfile { viewModel.navigateBack() }
        })
    }
}