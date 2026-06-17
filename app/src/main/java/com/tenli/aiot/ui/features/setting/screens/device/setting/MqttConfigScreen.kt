package com.tenli.aiot.ui.features.setting.screens.device.setting

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.features.setting.components.ConfigInputField
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.updateMqttConfig
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoGroup
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoSectionTitle
import com.tenli.aiot.ui.theme.spacing

@Composable
fun MqttConfigScreen(viewModel: SettingViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val data = uiState.box.systemSetting?.settings
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // State lưu trữ dữ liệu nhập
    var isEnabled by remember(data) { mutableStateOf(data?.mqttEnabled ?: false) }
    var host by remember(data) { mutableStateOf(data?.mqttHost ?: "") }
    var port by remember(data) { mutableStateOf(data?.mqttPort?.toString() ?: "") }
    var clientId by remember(data) { mutableStateOf(data?.mqttClientId ?: "") }
    var user by remember(data) { mutableStateOf(data?.mqttUser ?: "") }
    var password by remember(data) { mutableStateOf(data?.mqttPassword ?: "") }
    var rootTopic by remember(data) { mutableStateOf(data?.mqttRootTopic ?: "") }
    var subQos by remember(data) { mutableStateOf(data?.mqttSubQoS?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(bottom = MaterialTheme.spacing.large)
        ) {
            // Hiển thị cảnh báo nếu thiết bị offline
            if (data == null) {
                OfflineBanner()
            }

            DeviceInfoSectionTitle("Trạng thái dịch vụ")
            DeviceInfoGroup {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Kích hoạt MQTT", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        enabled = data != null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            DeviceInfoSectionTitle("Thông số kết nối")
            DeviceInfoGroup {
                ConfigInputField("Địa chỉ máy chủ", host, { host = it }, onClear = { host = "" }, "localhost")
                MqttDivider()
                ConfigInputField("Cổng dịch vụ", port, { port = it }, onClear = { port = "" }, "1883", keyboardType = KeyboardType.Number)
                MqttDivider()
                ConfigInputField("Client ID", clientId, { clientId = it }, onClear = { clientId = "" }, "aiot_box_01")
            }

            DeviceInfoSectionTitle("Xác thực")
            DeviceInfoGroup {
                ConfigInputField("Tài khoản", user, { user = it }, onClear = { user = "" }, "username")
                MqttDivider()
                ConfigInputField("Mật khẩu", password, { password = it }, onClear = { password = "" }, "password", isPassword = true)
            }

            DeviceInfoSectionTitle("Cấu hình Topic")
            DeviceInfoGroup {
                ConfigInputField("Root Topic", rootTopic, { rootTopic = it }, onClear = { rootTopic = "" }, "tenli/box/data")
                MqttDivider()
                ConfigInputField("Sub QoS", subQos, { subQos = it }, onClear = { subQos = "" }, "0, 1, 2", keyboardType = KeyboardType.Number)
            }
        }

        // Nút lưu cố định ở phía dưới
        Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.updateMqttConfig(
                        enabled = isEnabled, host = host, port = port,
                        clientId = clientId, user = user, pass = password,
                        topic = rootTopic, qos = subQos,
                        onSuccess = { viewModel.navigateBack() }
                    )
                },
                enabled = data != null && !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(MaterialTheme.spacing.iconMedium), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Lưu cấu hình", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
fun MqttDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
        thickness = MaterialTheme.spacing.borderThin,
        color = Color.LightGray.copy(alpha = 0.3f)
    )
}

@Composable
fun OfflineBanner() {
    Surface(
        modifier = Modifier.padding(MaterialTheme.spacing.medium).fillMaxWidth(),
        color = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.reset_icon), // Thay bằng icon cảnh báo nếu có
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = "Thiết bị ngoại tuyến. Vui lòng kiểm tra lại kết nối.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}