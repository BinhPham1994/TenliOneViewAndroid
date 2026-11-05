package com.tenli.oneview.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.tenli.oneview.ui.component.MobileFullScreenOverlay
import com.tenli.oneview.ui.component.ProvideAppLanguage
import com.tenli.oneview.ui.component.enableAppEdgeToEdge
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.features.auth.login.LoginScreen
import com.tenli.oneview.ui.features.splash.SplashDestination
import com.tenli.oneview.ui.features.splash.SplashViewModel
import com.tenli.oneview.ui.navigation.AppNavigation
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.TenliAIoTTheme
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.ui.utils.PermissionUtils
import kotlinx.coroutines.delay

private const val TAG = "MainActivity"

/** Extra mở chi tiết sự kiện (FCM notification / màn cuộc gọi gửi "EVENT_ID") */
private const val EXTRA_EVENT_ID = "EVENT_ID"
private const val EXTRA_OPEN_EVENT_DETAIL = "OPEN_EVENT_DETAIL"

/** Thời gian giữ eventId để AppNavigation kịp điều hướng trước khi xoá khỏi intent */
private const val EVENT_ID_CONSUME_DELAY_MS = 1000L

private const val PERMISSION_ICON_BG_ALPHA = 0.14f

private val CHINESE_OEM_KEYWORDS = listOf("xiaomi", "oppo", "vivo", "huawei")

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels { SplashViewModel.Factory }
    private var startEventId by mutableStateOf<String?>(null)

    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(this) }

    /** Kết quả flow cập nhật IMMEDIATE: nếu người dùng huỷ thì kiểm tra & yêu cầu lại (bắt buộc cập nhật). */
    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) checkUpdate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        // Màu icon system bar theo giao diện app ngay từ frame đầu (ThemeManager đã init trong TenliApp.onCreate)
        enableAppEdgeToEdge()

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value.destination is SplashDestination.Loading
        }

        // Chỉ kiểm tra cập nhật ở lần tạo đầu (không lặp lại khi recreate do đổi ngôn ngữ)
        if (savedInstanceState == null) checkUpdate()
        startEventId = intent.extractEventId()

        setContent {
            TenliAIoTTheme {
                ProvideAppLanguage {
                    val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()

                    when (uiState.destination) {
                        is SplashDestination.Login -> {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.background
                            ) {
                                LoginScreen(onLoginSuccess = splashViewModel::navigateToMain)
                            }
                        }

                        is SplashDestination.Main -> {
                            AppNavigation(
                                onLogoutRequest = splashViewModel::logout,
                                initialEventId = startEventId
                            )

                            // Vẽ sau AppNavigation để các dialog xin quyền nằm trên cùng (và BackHandler được ưu tiên)
                            StartupPermissionDialogs()

                            LaunchedEffect(startEventId) {
                                if (startEventId != null) {
                                    delay(EVENT_ID_CONSUME_DELAY_MS)
                                    startEventId = null
                                    intent.removeExtra(EXTRA_EVENT_ID)
                                    intent.removeExtra(EXTRA_OPEN_EVENT_DETAIL)
                                }
                            }
                        }

                        is SplashDestination.Loading -> { /* Keep Splash */ }
                    }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.updateResources(newBase))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.extractEventId()?.let { startEventId = it }
    }

    override fun onResume() {
        super.onResume()
        // Tiếp tục flow cập nhật IMMEDIATE nếu đang dở (vd. người dùng thoát app giữa chừng)
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startImmediateUpdate(info)
            }
        }
    }

    private fun checkUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                startImmediateUpdate(info)
            }
        }
    }

    private fun startImmediateUpdate(info: AppUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                info,
                updateLauncher,
                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Cannot start in-app update flow", e)
        }
    }
}

private fun Intent.extractEventId(): String? =
    (getStringExtra(EXTRA_OPEN_EVENT_DETAIL) ?: getStringExtra(EXTRA_EVENT_ID))?.takeIf { it.isNotBlank() }

private enum class StartupPermissionDialog { NONE, NOTIFICATION, OVERLAY, BATTERY }

/**
 * Xin quyền thông báo (Android 13+) và lần lượt nhắc bật: thông báo → hiển thị trên ứng dụng khác → bỏ tối ưu pin.
 * Trạng thái được kiểm tra lại mỗi lần Activity resume (người dùng quay về từ màn Cài đặt).
 */
@Composable
private fun StartupPermissionDialogs() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isEn = rememberIsEnglish()
    val manufacturer = remember { Build.MANUFACTURER.lowercase() }
    val isChinesePhone = remember(manufacturer) { CHINESE_OEM_KEYWORDS.any { manufacturer.contains(it) } }

    var activeDialog by remember { mutableStateOf(StartupPermissionDialog.NONE) }
    // Không hiện cảnh báo phía sau hộp thoại xin quyền hệ thống đang mở
    var isRequestingNotification by remember { mutableStateOf(false) }

    fun refreshDialogs() {
        if (isRequestingNotification) return
        activeDialog = when {
            !PermissionUtils.isNotificationEnabled(context) -> StartupPermissionDialog.NOTIFICATION
            !Settings.canDrawOverlays(context) -> StartupPermissionDialog.OVERLAY
            !PermissionUtils.isIgnoringBattery(context) -> StartupPermissionDialog.BATTERY
            else -> StartupPermissionDialog.NONE
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        isRequestingNotification = false
        refreshDialogs()
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            isRequestingNotification = true
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshDialogs()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val dismiss = { activeDialog = StartupPermissionDialog.NONE }
    val openAppDetails = {
        dismiss()
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
        )
    }

    when (activeDialog) {
        StartupPermissionDialog.NOTIFICATION -> PermissionExplanationDialog(
            title = if (isEn) "Enable Alert Notifications" else "Bật thông báo cảnh báo",
            description = remember(isEn) { notificationDescription(isEn) },
            onConfirm = openAppDetails,
            onDismiss = dismiss
        )

        StartupPermissionDialog.OVERLAY -> PermissionExplanationDialog(
            title = when {
                isEn && isChinesePhone -> "Special Background Permissions"
                isEn -> "Display Over Other Apps"
                isChinesePhone -> "Quyền chạy nền đặc biệt"
                else -> "Quyền hiển thị cuộc gọi"
            },
            description = remember(isEn, isChinesePhone) { overlayDescription(isEn, isChinesePhone, manufacturer) },
            onConfirm = {
                dismiss()
                if (isChinesePhone) {
                    PermissionUtils.openManufacturerPermissions(context)
                } else {
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri())
                    )
                }
            },
            onDismiss = dismiss
        )

        StartupPermissionDialog.BATTERY -> PermissionExplanationDialog(
            title = if (isEn) "Keep Connection Active" else "Duy trì kết nối",
            description = remember(isEn) { batteryDescription(isEn) },
            onConfirm = openAppDetails,
            onDismiss = dismiss
        )

        StartupPermissionDialog.NONE -> Unit
    }
}

/** Ghép chuỗi có các đoạn in đậm: phần tử ở vị trí lẻ (1, 3, 5...) được in đậm. */
private fun boldSegments(vararg segments: String): AnnotatedString = buildAnnotatedString {
    segments.forEachIndexed { index, text ->
        if (index % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text) }
        } else {
            append(text)
        }
    }
}

private fun notificationDescription(isEn: Boolean): AnnotatedString = if (isEn) {
    boldSegments(
        "Notification permission is disabled. You ",
        "will not receive emergency alert calls and notifications",
        " from the AI system.\n\nPlease tap ",
        "Set up now",
        " and enable ",
        "Allow notifications",
        " to stay protected."
    )
} else {
    boldSegments(
        "Quyền thông báo đang bị tắt. Điều này sẽ khiến bạn ",
        "không thể nhận được cuộc gọi và tin nhắn cảnh báo khẩn cấp",
        " từ hệ thống AI.\n\nVui lòng nhấn ",
        "Thiết lập ngay",
        " và bật mục ",
        "Cho phép thông báo",
        " để bảo vệ an toàn cho ngôi nhà."
    )
}

private fun overlayDescription(isEn: Boolean, isChinesePhone: Boolean, manufacturer: String): AnnotatedString {
    val brand = manufacturer.uppercase()
    return when {
        isEn && isChinesePhone -> boldSegments(
            "To receive reliable alert calls on ", brand, " devices, please enable:\n\n• ",
            "Show on Lock screen", "\n\n• ",
            "Autostart", "\n\n• ",
            "Display pop-up windows"
        )
        isEn -> boldSegments(
            "To display alert screens immediately during emergencies, please allow:\n\n• ",
            "Display over other apps"
        )
        isChinesePhone -> boldSegments(
            "Để nhận cuộc gọi cảnh báo ổn định trên máy ", brand, ", vui lòng bật các mục sau:\n\n• ",
            "Hiển thị trên màn hình khóa", "\n\n• ",
            "Tự khởi chạy", "\n\n• ",
            "Hiển thị cửa sổ pop-up"
        )
        else -> boldSegments(
            "Để màn hình cảnh báo hiển thị ngay lập tức khi có sự kiện khẩn cấp, vui lòng cho phép mục:\n\n• ",
            "Hiển thị trên các ứng dụng khác"
        )
    }
}

private fun batteryDescription(isEn: Boolean): AnnotatedString = if (isEn) {
    boldSegments(
        "To avoid missing alerts while your phone is asleep, please:\n\n• Select ",
        "App battery usage",
        " (or Battery).\n\n• Choose ",
        "Unrestricted",
        " so the app runs reliably."
    )
} else {
    boldSegments(
        "Để không bỏ lỡ cảnh báo khi điện thoại đang ở chế độ ngủ, vui lòng thực hiện:\n\n• Chọn mục ",
        "Mức sử dụng Pin của ứng dụng",
        " (hoặc Pin).\n\n• Tích chọn chế độ ",
        "Không hạn chế (Unrestricted)",
        " để App luôn chạy ổn định."
    )
}

/** Bottom sheet giải thích lý do cần quyền, kiểu iOS grouped sheet của bản PWA. */
@Composable
fun PermissionExplanationDialog(
    title: String,
    description: AnnotatedString,
    icon: ImageVector = Icons.Default.Notifications,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = rememberIsEnglish()
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val noRipple = remember { MutableInteractionSource() }
    val buttonShape = RoundedCornerShape(spacing.radiusCompact)

    MobileFullScreenOverlay(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(interactionSource = noRipple, indication = null, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = spacing.contentMaxWidth)
                    // Chặn click xuyên xuống lớp nền (không đóng sheet khi chạm vào nội dung)
                    .clickable(interactionSource = noRipple, indication = null, onClick = {}),
                shape = RoundedCornerShape(topStart = spacing.radiusLarge, topEnd = spacing.radiusLarge),
                color = colors.surfaceGrouped
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = spacing.space16)
                        .padding(top = spacing.space10, bottom = spacing.space20)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(spacing.sheetHandleWidth)
                            .height(spacing.sheetHandleHeight)
                            .clip(CircleShape)
                            .background(colors.handle)
                    )
                    Spacer(modifier = Modifier.height(spacing.space14))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = spacing.space14),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textTitle,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .size(spacing.avatarSmall)
                                .clip(CircleShape)
                                .background(colors.closeButtonBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = LucideIcons.X,
                                contentDescription = if (isEn) "Close" else "Đóng",
                                tint = colors.textMuted,
                                modifier = Modifier.size(spacing.iconSmall)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = spacing.space12, vertical = spacing.space8),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(spacing.avatarLarge)
                                .clip(RoundedCornerShape(spacing.radiusMedium))
                                .background(colors.accent.copy(alpha = PERMISSION_ICON_BG_ALPHA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(spacing.space28)
                            )
                        }
                        Spacer(modifier = Modifier.height(spacing.space14))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(spacing.space16))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(spacing.space10)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.buttonHeight)
                                .clip(buttonShape)
                                .background(colors.buttonSecondaryBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Later" else "Để sau",
                                color = colors.textTitle,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(spacing.buttonHeight)
                                .clip(buttonShape)
                                .background(colors.accent)
                                .clickable(onClick = onConfirm),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Set up now" else "Thiết lập ngay",
                                color = colors.onAccent,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
