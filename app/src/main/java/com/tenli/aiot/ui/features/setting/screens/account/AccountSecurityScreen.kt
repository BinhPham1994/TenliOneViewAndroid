package com.tenli.aiot.ui.features.setting.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.setting.components.SettingMenuItem
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.fetchClientDevices
import com.tenli.aiot.ui.features.setting.core.prepareEditProfile
import com.tenli.aiot.ui.theme.spacing

@Composable
fun AccountSecurityScreen(viewModel: SettingViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(MaterialTheme.spacing.medium)
    ) {
        item {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                SettingMenuItem(R.drawable.person_icon_green, "Thông tin cá nhân", MaterialTheme.colorScheme.primary) {
                    viewModel.prepareEditProfile() // Gán dữ liệu hiện tại vào tempState
                    viewModel.navigateTo(SettingScreenType.UserDetail, "Thông tin cá nhân")
                }
                SettingMenuItem(R.drawable.setting, "Mật khẩu bảo mật", Color(0xFF2196F3), showDivider = false) {
                    viewModel.navigateTo(SettingScreenType.ChangePassword, "Mật khẩu bảo mật")
                }

            }
        }

        item { Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium)) }

        item {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                SettingMenuItem(R.drawable.device_login, "Quản lý thiết bị đăng nhập", Color(0xFF2196F3)) {
                    viewModel.fetchClientDevices()
                    viewModel.navigateTo(SettingScreenType.ClientManagement, "Quản lý thiết bị đăng nhập")
                }
                SettingMenuItem(R.drawable.logout_icon, "Lịch sử đăng nhập", Color(0xFFE37238), showDivider = false) { /* Chức năng đang phát triển */ }
            }
        }

        item { Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge)) }

        item {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                SettingMenuItem(
                    iconRes = R.drawable.person_icon_green,
                    title = "Xóa tài khoản",
                    iconTint = Color(0xFFE02B00),
                    textColor = Color.Red,
                    showDivider = false
                ) { /* Logic xác nhận xóa tài khoản */ }
            }
        }
    }
}