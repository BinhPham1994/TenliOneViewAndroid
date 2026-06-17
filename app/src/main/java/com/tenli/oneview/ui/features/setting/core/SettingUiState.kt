package com.tenli.oneview.ui.features.setting.core

import com.tenli.oneview.model.network.BoxSystemInfo
import com.tenli.oneview.model.network.BoxSystemState
import com.tenli.oneview.model.network.ClientDevice
import com.tenli.oneview.model.network.DeviceItem
import com.tenli.oneview.model.network.GroupDetailData
import com.tenli.oneview.model.network.GroupUser
import com.tenli.oneview.model.network.HomeGroupDisplay
import com.tenli.oneview.model.network.LanDevice
import com.tenli.oneview.model.network.MonitorDisplayItem
import com.tenli.oneview.model.network.MonitorType
import com.tenli.oneview.model.network.SystemSettingResponse
import com.tenli.oneview.model.network.UserData

enum class SettingScreenType {
    // --- Các màn hình hệ thống ---
    Main, User, UserDetail, ClientManagement, ClientDetail, ChangePassword,
    Group, GroupDetail, Device, Notify, Introduce, Language,

    // --- Quản lý thiết bị & AI ---
    DevicesManagement,
    DeviceDetail,
    DeviceInformation,
    CreateGroup,
    JoinGroup,
    EditGroupName,

    AiSetting,
    SelectAiTask,
    SelectCameraForAi, // THÊM LẠI DÒNG NÀY [cite: 2026-03-08]
    AiSensorWizard,
    AiLogicWizard, // Thêm màn hình này

    LanScan,
    AddDeviceManual,
    QrCodeScan,
    AddDeviceScreen,

    CameraManagement,
    AddCameraBrand,
    AddCameraManual,
    EditCamera,

    // --- Quản lý cài đặt Box AI ---
    DeviceSettings,
    MqttConfig,
    AlarmConfig,
    StorageConfig,
    VoiceConfig,

    NotifyDetail,
    ZigbeeManagement,
    AlarmSound,
    ScriptManagement,
    AddScript,
    EditScript
}

// 2. TÚI CHỨA: Tài khoản & Bảo mật (Giữ nguyên)
data class AccountUiState(
    val isUpdating: Boolean = false,
    val isChangingPassword: Boolean = false,
    val tempName: String = "",
    val tempEmail: String = "",
    val tempPhone: String = "",
    val tempGender: Int = 1,
    val tempAddress: String = "",
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = ""
)

// 3. TÚI CHỨA: Quản lý Nhà (Giữ nguyên)
data class GroupUiState(
    val displayGroups: List<HomeGroupDisplay> = emptyList(),
    val selectedGroup: HomeGroupDisplay? = null,
    val groupDetail: GroupDetailData? = null,
    val isGroupMenuExpanded: Boolean = false,
    val tempGroupName: String = "",
    val tempJoinCode: String = "",
    val isMemberMenuExpanded: Boolean = false,
    val isShareCodeDialogOpen: Boolean = false,
    val shareCode: String = "",
    val shareCodeExpireTime: Int = 0,
    val isConfirmDeleteMemberOpen: Boolean = false,
    val memberToDelete: GroupUser? = null,
    val selectedExpiryMinutes: Int? = null,
    val isConfirmGroupActionOpen: Boolean = false,
)

// 4. TÚI CHỨA: Quản lý Thiết bị (Giữ nguyên)
data class ClientUiState(
    val clientDevices: List<ClientDevice> = emptyList(),
    val isLoadingDevices: Boolean = false,
    val selectedDevice: ClientDevice? = null,
    val isLoggingOutDevice: Boolean = false,
)

/**
 * BoxUiState MỚI: Chỉ giữ lại thông tin hệ thống
 */
data class BoxUiState(
    val isLoading: Boolean = false,
    val info: BoxSystemInfo? = null,
    val state: BoxSystemState? = null,
    val systemSetting: SystemSettingResponse? = null,

    val monitorTypes: List<MonitorType> = emptyList(),
    val selectedAiCategory: Int = 0,
    val editingMonitorItem: MonitorDisplayItem? = null,
    val monitorUpdateTicket: Int = 0,

    val isAddDeviceMenuExpanded: Boolean = false,
    val deviceGroups: List<DeviceGroupDisplay> = emptyList(),
    val selectedDeviceItem: DeviceItem? = null,

    val isScanningLan: Boolean = false,
    val lanDevices: List<LanDevice> = emptyList(),

    val verifiedKey: String? = null,
    val pendingKey: String? = null,
    val manualBaseUrl: String? = null,

    val isCameraMenuExpanded: Boolean = false,
    val isScriptMenuExpanded: Boolean = false,
)

data class SettingUiState(
    val title: String = "Cài đặt",
    val currentScreen: SettingScreenType = SettingScreenType.Main,
    val userData: UserData? = null,
    val isLoading: Boolean = false,
    val isLogoutDialogOpen: Boolean = false,
    val isLoggingOut: Boolean = false,
    val currentLanguage: String = "vi",
    val hasDeviceIssue: Boolean = false,

    val account: AccountUiState = AccountUiState(),
    val client: ClientUiState = ClientUiState(),
    val group: GroupUiState = GroupUiState(),
    val box: BoxUiState = BoxUiState(),
)

data class DeviceGroupDisplay(val groupName: String, val devices: List<DeviceItem>)

sealed class SettingUiEvent {
    data class ShowSnackbar(val message: String) : SettingUiEvent()
}
