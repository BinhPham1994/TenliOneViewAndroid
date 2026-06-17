package com.tenli.aiot.ui.features.setting.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.setting.components.SettingMenuItem
import com.tenli.aiot.ui.features.setting.components.UserAvatarBox
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import com.tenli.aiot.ui.features.setting.core.SettingUiState
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.fetchDevices
import com.tenli.aiot.ui.features.setting.core.fetchGroups
import com.tenli.aiot.ui.theme.spacing
import com.tenli.aiot.ui.utils.LocaleManager

@Composable
fun SettingMainList(
    listState: LazyListState,
    uiState: SettingUiState,
    viewModel: SettingViewModel
) {
    val context = LocalContext.current

    val accountSecurityTitle = stringResource(id = R.string.setting_account_security)
    val homeManageTitle = stringResource(id = R.string.setting_home_manage)
    val deviceManageTitle = stringResource(id = R.string.setting_device_manage)
    val alarmConfigTitle = stringResource(id = R.string.setting_alarm_config)
    val introduceTitle = stringResource(id = R.string.setting_introduce)
    val languageTitle = stringResource(id = R.string.setting_language)
    val logoutTitle = stringResource(id = R.string.setting_logout)

    val currentLang = LocaleManager.getLocale(context)
    val langDisplayName = if (currentLang == "en") "English" else "Tiếng Việt"
    val langFlag = if (currentLang == "en") R.drawable.kingdom else R.drawable.vietnam

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = MaterialTheme.spacing.extraLarge)
    ) {
        item {
            Surface(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .clickable { viewModel.navigateTo(SettingScreenType.User, accountSecurityTitle) }, color = Color.White
            ) {
                Row(modifier = Modifier.padding(MaterialTheme.spacing.medium), verticalAlignment = Alignment.CenterVertically) {
                    UserAvatarBox(uiState.userData?.avatar?.getAvatarImage(), size = 80.dp)
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.userData?.name ?: "Người dùng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${uiState.userData?.uuid ?: "---"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                SettingMenuItem(R.drawable.home, homeManageTitle, MaterialTheme.colorScheme.primary) {
                    viewModel.fetchGroups()
                    viewModel.navigateTo(SettingScreenType.Group, homeManageTitle)
                }
                SettingMenuItem(
                    iconRes = R.drawable.device_icon,
                    title = deviceManageTitle,
                    iconTint = Color(0xFF2196F3),
                    showBadge = uiState.hasDeviceIssue
                ) {
                    viewModel.fetchDevices()
                    viewModel.navigateTo(SettingScreenType.DevicesManagement, deviceManageTitle)
                }
                SettingMenuItem(R.drawable.alarm, alarmConfigTitle, Color(0xFFF44336), showDivider = false) {
                    viewModel.navigateTo(SettingScreenType.Notify, alarmConfigTitle)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium)) }

        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                SettingMenuItem(R.drawable.introduce_icon, introduceTitle, Color(0xFFFF9800)) {
                    viewModel.navigateTo(SettingScreenType.Introduce, introduceTitle)
                }
                SettingMenuItem(
                    iconRes = R.drawable.device_connected,
                    title = languageTitle,
                    iconTint = Color(0xFF673AB7),
                    showDivider = false,
                    extraContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(langFlag),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(
                                text = langDisplayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                ) {
                    viewModel.navigateTo(SettingScreenType.Language, langDisplayName)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium)) }
        item {
            Column(
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .clip(RoundedCornerShape(MaterialTheme.spacing.radiusMedium))
                    .background(Color.White)
            ) {
                SettingMenuItem(
                    iconRes = R.drawable.logout_icon,
                    title = logoutTitle,
                    iconTint = Color(0xFFE02B00),
                    textColor = Color.Red,
                    showDivider = false
                ) {
                    viewModel.showLogoutDialog(true)
                }
            }
        }
    }
}