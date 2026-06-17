package com.tenli.aiot.ui.features.setting

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.aiot.R
import com.tenli.aiot.data.local.UserSession
import com.tenli.aiot.ui.component.AppConfirmDialog
import com.tenli.aiot.ui.features.setting.components.SettingTopHeader
import com.tenli.aiot.ui.features.setting.core.SettingScreenType
import com.tenli.aiot.ui.features.setting.core.SettingUiEvent
import com.tenli.aiot.ui.features.setting.core.SettingUiState
import com.tenli.aiot.ui.features.setting.core.SettingViewModel
import com.tenli.aiot.ui.features.setting.core.exitAiWizard
import com.tenli.aiot.ui.features.setting.core.selectDevice
import com.tenli.aiot.ui.features.setting.screens.SettingMainList
import com.tenli.aiot.ui.features.setting.screens.account.AccountSecurityScreen
import com.tenli.aiot.ui.features.setting.screens.account.ChangePasswordScreen
import com.tenli.aiot.ui.features.setting.screens.account.ClientDetailScreen
import com.tenli.aiot.ui.features.setting.screens.account.LoggedDevicesScreen
import com.tenli.aiot.ui.features.setting.screens.account.PersonalDetailScreen
import com.tenli.aiot.ui.features.setting.screens.device.DeviceDetailScreen
import com.tenli.aiot.ui.features.setting.screens.device.DevicesManagementScreen
import com.tenli.aiot.ui.features.setting.screens.device.ai.AiLogicWizardScreen
import com.tenli.aiot.ui.features.setting.screens.device.ai.AiSensorViewModel
import com.tenli.aiot.ui.features.setting.screens.device.ai.AiSensorViewModelFactory
import com.tenli.aiot.ui.features.setting.screens.device.ai.AiSensorWizardScreen
import com.tenli.aiot.ui.features.setting.screens.device.ai.AiSettingScreen
import com.tenli.aiot.ui.features.setting.screens.device.ai.SelectAiTaskScreen
import com.tenli.aiot.ui.features.setting.screens.device.camera.AddCameraBrandScreen
import com.tenli.aiot.ui.features.setting.screens.device.camera.AddCameraManualScreen
import com.tenli.aiot.ui.features.setting.screens.device.camera.CameraManagementScreen
import com.tenli.aiot.ui.features.setting.screens.device.camera.CameraViewModel
import com.tenli.aiot.ui.features.setting.screens.device.camera.CameraViewModelFactory
import com.tenli.aiot.ui.features.setting.screens.device.camera.EditCameraScreen
import com.tenli.aiot.ui.features.setting.screens.device.info.DeviceInfoScreen
import com.tenli.aiot.ui.features.setting.screens.device.scan.AddDeviceScreen
import com.tenli.aiot.ui.features.setting.screens.device.scan.LanScanScreen
import com.tenli.aiot.ui.features.setting.screens.device.scan.ManualAddDeviceScreen
import com.tenli.aiot.ui.features.setting.screens.device.script.ScriptFormScreen
import com.tenli.aiot.ui.features.setting.screens.device.script.ScriptManagementScreen
import com.tenli.aiot.ui.features.setting.screens.device.script.ScriptViewModel
import com.tenli.aiot.ui.features.setting.screens.device.setting.AlarmConfigScreen
import com.tenli.aiot.ui.features.setting.screens.device.setting.DeviceSettingsMainScreen
import com.tenli.aiot.ui.features.setting.screens.device.setting.MqttConfigScreen
import com.tenli.aiot.ui.features.setting.screens.device.setting.StorageConfigScreen
import com.tenli.aiot.ui.features.setting.screens.device.setting.VoiceConfigScreen
import com.tenli.aiot.ui.features.setting.screens.device.sound.AlarmSoundScreen
import com.tenli.aiot.ui.features.setting.screens.device.zigbee.ZigbeeManagementScreen
import com.tenli.aiot.ui.features.setting.screens.device.zigbee.ZigbeeViewModel
import com.tenli.aiot.ui.features.setting.screens.device.zigbee.ZigbeeViewModelFactory
import com.tenli.aiot.ui.features.setting.screens.group.CreateGroupScreen
import com.tenli.aiot.ui.features.setting.screens.group.EditGroupNameScreen
import com.tenli.aiot.ui.features.setting.screens.group.GroupDetailScreen
import com.tenli.aiot.ui.features.setting.screens.group.GroupManagementScreen
import com.tenli.aiot.ui.features.setting.screens.group.JoinGroupScreen
import com.tenli.aiot.ui.features.setting.screens.introduce.IntroduceScreen
import com.tenli.aiot.ui.features.setting.screens.language.LanguageScreen
import com.tenli.aiot.ui.features.setting.screens.notification.NotifyConfigScreen
import com.tenli.aiot.ui.features.setting.screens.notification.NotifyConfigViewModel
import com.tenli.aiot.ui.features.setting.screens.notification.detail.NotifyGroupDetailScreen
import com.tenli.aiot.ui.theme.spacing

@Composable
fun SettingScreen(
    listState: LazyListState,
    viewModel: SettingViewModel = viewModel(factory = SettingViewModel.Factory),
    onLogoutRequest: () -> Unit,
    onShowBottomBar: (Boolean) -> Unit,
    initialTarget: String? = null,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentContext = LocalContext.current
    var onDeleteAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var onBackAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(initialTarget) {
        viewModel.handleInitialTarget(initialTarget)
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is SettingUiEvent.ShowSnackbar) snackbarHostState.showSnackbar(event.message)
        }
    }

    LaunchedEffect(uiState.currentScreen) {
        onShowBottomBar(uiState.currentScreen == SettingScreenType.Main)
    }

    BackHandler {
        if (onBackAction != null) {
            onBackAction?.invoke()
        } else {
            when {
                uiState.currentScreen == SettingScreenType.AiSensorWizard ||
                        uiState.currentScreen == SettingScreenType.AiLogicWizard -> {
                    viewModel.exitAiWizard()
                }

                uiState.currentScreen != SettingScreenType.Main -> viewModel.navigateBack()
                else -> (currentContext as? Activity)?.moveTaskToBack(true)
            }
        }
    }

    if (uiState.isLogoutDialogOpen) {
        AppConfirmDialog(
            title = "Đăng xuất",
            message = "Bạn có chắc chắn muốn đăng xuất khỏi tài khoản không? Mọi phiên làm việc hiện tại trên thiết bị này sẽ kết thúc.",
            confirmText = "Đăng xuất",
            confirmColor = MaterialTheme.colorScheme.error,
            iconRes = R.drawable.logout_icon,
            onDismiss = { viewModel.showLogoutDialog(false) },
            onConfirm = {
                viewModel.showLogoutDialog(false)
                viewModel.performLogout(onSuccess = onLogoutRequest)
            }
        )
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        SettingTopHeader(
            uiState = uiState, viewModel = viewModel, onBack = {
                if (onBackAction != null) {
                    onBackAction?.invoke()
                } else {
                    if (uiState.currentScreen == SettingScreenType.AiSensorWizard ||
                        uiState.currentScreen == SettingScreenType.AiLogicWizard
                    ) {
                        viewModel.exitAiWizard()
                    } else {
                        viewModel.navigateBack()
                    }
                }
            }, onDeleteClick = onDeleteAction
        )
    }) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            SettingRouter(
                screen = uiState.currentScreen,
                listState = listState,
                uiState = uiState,
                viewModel = viewModel,
                onLogoutRequest = onLogoutRequest,
                onSetupDelete = { action -> onDeleteAction = action },
                onSetupBack = { action -> onBackAction = action })
            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}

@Composable
private fun SettingRouter(
    screen: SettingScreenType, listState: LazyListState, uiState: SettingUiState, viewModel: SettingViewModel, onLogoutRequest: () -> Unit, onSetupDelete: ((() -> Unit)?) -> Unit,
    onSetupBack: ((() -> Unit)?) -> Unit
) {
    val context = LocalContext.current
    val appContainer = (context.applicationContext as com.tenli.aiot.TenliApp).container
    
    val selectedDevice = uiState.box.selectedDeviceItem
    val cameraVmKey = "camera_vm_${selectedDevice?.id}"
    var showDeleteScriptDialog by remember { mutableStateOf(false) }
    when (screen) {
        SettingScreenType.Main -> SettingMainList(listState, uiState, viewModel)

        // --- NHÓM TÀI KHOẢN (ACCOUNT) ---
        SettingScreenType.User -> AccountSecurityScreen(viewModel)
        SettingScreenType.UserDetail -> PersonalDetailScreen(viewModel)
        SettingScreenType.ChangePassword -> ChangePasswordScreen(viewModel, onLogoutRequest)
        SettingScreenType.ClientManagement -> LoggedDevicesScreen(viewModel)
        SettingScreenType.ClientDetail -> ClientDetailScreen(viewModel)

        // --- NHÓM QUẢN LÝ NHÀ (GROUPS) ---
        SettingScreenType.Group -> GroupManagementScreen(viewModel)
        SettingScreenType.GroupDetail -> GroupDetailScreen(viewModel)
        SettingScreenType.CreateGroup -> CreateGroupScreen(viewModel)
        SettingScreenType.JoinGroup -> JoinGroupScreen(viewModel)
        SettingScreenType.EditGroupName -> EditGroupNameScreen(viewModel)

        // --- NHÓM PHIÊN ĐĂNG NHẬP (CLIENTS/SESSIONS) ---
        SettingScreenType.DevicesManagement -> {
            DevicesManagementScreen(
                viewModel = viewModel, onDeviceDetail = { device ->
                    viewModel.selectDevice(device)
                })
        }

        SettingScreenType.LanScan -> {
            LanScanScreen(viewModel = viewModel)
        }

        SettingScreenType.AddDeviceScreen -> {
            AddDeviceScreen(viewModel = viewModel)
        }

        SettingScreenType.AddDeviceManual -> {
            ManualAddDeviceScreen(viewModel = viewModel)
        }

        SettingScreenType.DeviceDetail -> {
            DeviceDetailScreen(viewModel = viewModel)
        }

        SettingScreenType.DeviceInformation -> {
            DeviceInfoScreen(viewModel = viewModel)
        }

        SettingScreenType.DeviceSettings -> {
            DeviceSettingsMainScreen(viewModel = viewModel)
        }

        SettingScreenType.MqttConfig -> {
            MqttConfigScreen(viewModel = viewModel)
        }

        SettingScreenType.AlarmConfig -> {
            AlarmConfigScreen(viewModel = viewModel)
        }

        SettingScreenType.StorageConfig -> {
            StorageConfigScreen(viewModel = viewModel)
        }

        SettingScreenType.VoiceConfig -> {
            VoiceConfigScreen(viewModel = viewModel)
        }

        SettingScreenType.AiSetting -> {
            AiSettingScreen(viewModel = viewModel)
        }

        SettingScreenType.SelectAiTask -> {
            if (selectedDevice != null) {
                val aiViewModel: AiSensorViewModel = viewModel(
                    key = "ai_wizard_${selectedDevice.id}",
                    factory = AiSensorViewModelFactory(
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateBack = { viewModel.navigateBack() },
                        onNavigateTo = { s, t -> viewModel.navigateTo(s, t) })
                )
                val currentCategory = uiState.box.selectedAiCategory
                LaunchedEffect(Unit) {
                    aiViewModel.prepareAddNewMonitor(currentCategory)
                    aiViewModel.fetchMonitorTypes()
                }
                SelectAiTaskScreen(viewModel = aiViewModel)
            }
        }

        SettingScreenType.AiSensorWizard -> {
            if (selectedDevice != null) {
                val aiViewModel: AiSensorViewModel = viewModel(
                    key = "ai_wizard_${selectedDevice.id}", factory = AiSensorViewModelFactory(
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        initialEditItem = uiState.box.editingMonitorItem,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateBack = { viewModel.exitAiWizard() }, // Cái này dùng cho nút Back của Step 1
                        onNavigateTo = { s, t -> viewModel.navigateTo(s, t) })
                )

                LaunchedEffect(aiViewModel) {
                    onSetupBack { aiViewModel.handleBackWithValidation() }
                }

                LaunchedEffect(aiViewModel) {
                    onSetupDelete { aiViewModel.askDeleteMonitor() }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        onSetupDelete(null)
                        onSetupBack(null)
                    }
                }

                LaunchedEffect(uiState.box.editingMonitorItem) {
                    uiState.box.editingMonitorItem?.let {
                        aiViewModel.setupEditMonitor(it)
                    }
                }

                AiSensorWizardScreen(
                    viewModel = aiViewModel, device = selectedDevice, onSuccess = { viewModel.exitAiWizard() })
            }
        }

        SettingScreenType.AiLogicWizard -> {
            if (selectedDevice != null) {
                val aiViewModel: AiSensorViewModel = viewModel(
                    key = "ai_wizard_${selectedDevice.id}",
                    factory = AiSensorViewModelFactory(
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        initialEditItem = uiState.box.editingMonitorItem,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateBack = { viewModel.exitAiWizard() },
                        onNavigateTo = { s, t -> viewModel.navigateTo(s, t) }
                    )
                )
                LaunchedEffect(uiState.box.editingMonitorItem) {
                    uiState.box.editingMonitorItem?.let {
                        aiViewModel.setupEditMonitor(it)
                    }
                }
                LaunchedEffect(aiViewModel) {
                    onSetupDelete { aiViewModel.askDeleteMonitor() }
                    onSetupBack { aiViewModel.handleBackWithValidation() }
                }
                LaunchedEffect(aiViewModel) {
                    onSetupDelete { aiViewModel.askDeleteMonitor() }
                }
                DisposableEffect(Unit) {
                    onDispose {
                        onSetupDelete(null)
                        onSetupBack(null)
                    }
                }
                AiLogicWizardScreen(
                    viewModel = aiViewModel,
                    device = selectedDevice,
                    onSuccess = { viewModel.exitAiWizard() }
                )
            }
        }

        SettingScreenType.CameraManagement -> {
            if (selectedDevice != null) {
                val cameraViewModel: CameraViewModel = viewModel(
                    key = cameraVmKey,
                    factory = CameraViewModelFactory(
                        appRepository = appContainer.appRepository,
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        accessKey = UserSession.accessToken,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateBack = { viewModel.navigateBack() },
                        onNavigateTo = { s, t -> viewModel.navigateTo(s, t) }
                    )
                )
                CameraManagementScreen(viewModel = cameraViewModel)
            }
        }

        SettingScreenType.AddCameraBrand -> {
            if (selectedDevice != null) {
                val cameraViewModel: CameraViewModel = viewModel(
                    key = cameraVmKey,
                    factory = CameraViewModelFactory(
                        appRepository = appContainer.appRepository,
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        accessKey = UserSession.accessToken,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateBack = { viewModel.navigateBack() },
                        onNavigateTo = { s, t -> viewModel.navigateTo(s, t) }
                    )
                )
                AddCameraBrandScreen(viewModel = cameraViewModel)
            }
        }

        SettingScreenType.AddCameraManual -> {
            if (selectedDevice != null) {
                val cameraViewModel: CameraViewModel = viewModel(
                    key = cameraVmKey,
                    factory = CameraViewModelFactory(
                        appRepository = appContainer.appRepository,
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        accessKey = UserSession.accessToken,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateBack = { viewModel.navigateBack() },
                        onNavigateTo = { s, t -> viewModel.navigateTo(s, t) }
                    )
                )
                AddCameraManualScreen(viewModel = cameraViewModel)
            }
        }

        SettingScreenType.EditCamera -> {
            if (selectedDevice != null) {
                val cameraViewModel: CameraViewModel = viewModel(
                    key = cameraVmKey,
                    factory = CameraViewModelFactory(
                        appRepository = appContainer.appRepository,
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        accessKey = UserSession.accessToken,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateBack = { viewModel.navigateBack() },
                        onNavigateTo = { s, t -> viewModel.navigateTo(s, t) }
                    )
                )

                var showDeleteDialog by remember { mutableStateOf(false) }

                LaunchedEffect(cameraViewModel) {
                    onSetupBack {
                        viewModel.navigateBack()
                    }
                    onSetupDelete {
                        showDeleteDialog = true
                    }
                }

                DisposableEffect(Unit) {
                    onDispose { onSetupDelete(null) }
                }

                if (showDeleteDialog) {
                    val cameraToDelete = cameraViewModel.uiState.collectAsStateWithLifecycle().value.selectedCamera
                    AppConfirmDialog(
                        title = "Xóa camera",
                        message = "Bạn có chắc chắn muốn xóa camera \"${cameraToDelete?.name}\" không? Hành động này không thể hoàn tác.",
                        confirmText = "Xóa ngay",
                        confirmColor = MaterialTheme.colorScheme.error, // Màu đỏ đậm
                        iconRes = R.drawable.delete,
                        onDismiss = { showDeleteDialog = false },
                        onConfirm = {
                            showDeleteDialog = false
                            cameraToDelete?.let { cam ->
                                cameraViewModel.deleteCamera(cam.id) {
                                    viewModel.navigateBack()
                                }
                            }
                        }
                    )
                }

                EditCameraScreen(viewModel = cameraViewModel)
            }
        }

        SettingScreenType.Notify -> {
            NotifyConfigScreen(
                viewModel = viewModel(factory = NotifyConfigViewModel.Factory(appContainer.alarmRepository)),
                onNavigate = { screen, title ->
                    viewModel.navigateTo(screen, title)
                }
            )
        }

        SettingScreenType.NotifyDetail -> {
            NotifyGroupDetailScreen(viewModel = viewModel(factory = NotifyConfigViewModel.Factory(appContainer.alarmRepository)))
        }

        SettingScreenType.Introduce -> {
            IntroduceScreen()
        }

        SettingScreenType.Language -> {
            LanguageScreen()
        }

        SettingScreenType.ZigbeeManagement -> {
            val device = uiState.box.selectedDeviceItem
            if (device != null) {
                val zigbeeViewModel: ZigbeeViewModel = viewModel(
                    key = "zigbee_${device.id}",
                    factory = ZigbeeViewModelFactory(device)
                )
                ZigbeeManagementScreen(viewModel = zigbeeViewModel)
            }
        }

        SettingScreenType.AlarmSound -> {
            AlarmSoundScreen(viewModel = viewModel)
        }

        SettingScreenType.ScriptManagement -> {
            if (selectedDevice != null) {
                val scriptViewModel: ScriptViewModel = viewModel(
                    key = "script_vm_${selectedDevice.id}",
                    factory = ScriptViewModel.Factory(
                        application = context.applicationContext as android.app.Application,
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateTo = { screen, title -> viewModel.navigateTo(screen, title) }
                    )
                )

                ScriptManagementScreen(
                    viewModel = scriptViewModel,
                    onEditScript = { script -> scriptViewModel.navigateToEditScript(script) }
                )
            }
        }

        SettingScreenType.AddScript -> {
            if (selectedDevice != null) {
                val scriptViewModel: ScriptViewModel = viewModel(
                    key = "script_vm_${selectedDevice.id}",
                    factory = ScriptViewModel.Factory(
                        application = context.applicationContext as android.app.Application,
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        // THÊM: Callback điều hướng [cite: 2026-03-16]
                        onNavigateTo = { screen, title -> viewModel.navigateTo(screen, title) }
                    )
                )
                ScriptFormScreen(
                    viewModel = scriptViewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }

        SettingScreenType.EditScript -> {
            if (selectedDevice != null) {
                val scriptViewModel: ScriptViewModel = viewModel(
                    key = "script_vm_${selectedDevice.id}",
                    factory = ScriptViewModel.Factory(
                        application = context.applicationContext as android.app.Application,
                        boxRepository = appContainer.boxRepository,
                        device = selectedDevice,
                        onShowSnackbar = { viewModel.showSnackbar(it) },
                        onNavigateTo = { screen, title -> viewModel.navigateTo(screen, title) }
                    )
                )

                // --- BỔ SUNG LOGIC KẾT NỐI HEADER TẠI ĐÂY --- [cite: 2026-03-16]
                LaunchedEffect(Unit) {
                    // Khi nhấn nút xóa trên Header, ta sẽ bật dialog lên
                    onSetupDelete { showDeleteScriptDialog = true }
                }

                DisposableEffect(Unit) {
                    // Khi thoát màn hình sửa, xóa callback xóa để tránh ảnh hưởng màn hình khác
                    onDispose { onSetupDelete(null) }
                }
                // ------------------------------------------

                val scriptToEdit = scriptViewModel.selectedScript
                if (scriptToEdit != null) {
                    ScriptFormScreen(
                        viewModel = scriptViewModel,
                        script = scriptToEdit,
                        showDeleteDialog = showDeleteScriptDialog,
                        onDismissDelete = { showDeleteScriptDialog = false },
                        onBack = {
                            showDeleteScriptDialog = false
                            scriptViewModel.selectedScript = null
                            viewModel.navigateBack()
                        }
                    )
                }
            }
        }

        else -> Text("Đang phát triển", Modifier.padding(MaterialTheme.spacing.extraLarge))
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {}, contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 5.dp)
    }
}