package com.tenli.oneview.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.tenli.oneview.ui.features.auth.login.LoginScreen
import com.tenli.oneview.ui.features.splash.SplashDestination
import com.tenli.oneview.ui.features.splash.SplashViewModel
import com.tenli.oneview.ui.navigation.AppNavigation
import com.tenli.oneview.ui.theme.TenliAIoTTheme
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.ui.utils.PermissionUtils

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels { SplashViewModel.Factory }
    private var startEventId by mutableStateOf<String?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        super.onCreate(savedInstanceState)

        com.tenli.oneview.ui.theme.ThemeManager.init(applicationContext)

        splashScreen.setKeepOnScreenCondition {
            splashViewModel.uiState.value.destination is SplashDestination.Loading
        }

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkUpdate()
        startEventId = intent.getStringExtra("OPEN_EVENT_DETAIL")

        setContent {
            TenliAIoTTheme {
                val uiState by splashViewModel.uiState.collectAsStateWithLifecycle()

                when (uiState.destination) {
                    is SplashDestination.Login -> {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LoginScreen(
                                onLoginSuccess = { splashViewModel.navigateToMain() }
                            )
                        }
                    }
                    is SplashDestination.Main -> {
                        val context = LocalContext.current
                        val lifecycleOwner = LocalLifecycleOwner.current

                val manufacturer = remember { Build.MANUFACTURER.lowercase() }
                val isChinesePhone = remember {
                    manufacturer.contains("xiaomi") || manufacturer.contains("oppo") ||
                            manufacturer.contains("vivo") || manufacturer.contains("huawei")
                }

                var showOverlayDialog by remember { mutableStateOf(false) }
                var showBatteryDialog by remember { mutableStateOf(false) }
                var showNotificationWarningDialog by remember { mutableStateOf(false) }

                val notificationLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasNotify = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasNotify) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            val isNotifyGranted = PermissionUtils.isNotificationEnabled(context)
                            if (!isNotifyGranted) {
                                showNotificationWarningDialog = true
                            } else {
                                showNotificationWarningDialog = false
                                val hasOverlay = Settings.canDrawOverlays(context)
                                val isBatteryReady = PermissionUtils.isIgnoringBattery(context)

                                when {
                                    !hasOverlay -> {
                                        showOverlayDialog = true
                                        showBatteryDialog = false
                                    }

                                    !isBatteryReady -> {
                                        showOverlayDialog = false
                                        showBatteryDialog = true
                                    }

                                    else -> {
                                        showOverlayDialog = false
                                        showBatteryDialog = false
                                    }
                                }
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                val isEn = com.tenli.oneview.ui.component.rememberIsEnglish()

                if (showNotificationWarningDialog) {
                    val notificationDescription = buildAnnotatedString {
                        if (isEn) {
                            append("Notification permission is disabled. You ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("will not receive emergency alert calls and notifications")
                            }
                            append(" from the AI system.\n\n")
                            append("Please tap ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Set up now")
                            }
                            append(" and enable ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Allow notifications")
                            }
                            append(" to stay protected.")
                        } else {
                            append("Quyền thông báo đang bị tắt. Điều này sẽ khiến bạn ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("không thể nhận được cuộc gọi và tin nhắn cảnh báo khẩn cấp")
                            }
                            append(" từ hệ thống AI.\n\n")

                            append("Vui lòng nhấn ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Thiết lập ngay")
                            }
                            append(" và bật mục ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Cho phép thông báo")
                            }
                            append(" để bảo vệ an toàn cho ngôi nhà.")
                        }
                    }

                    PermissionExplanationDialog(
                        title = if (isEn) "Enable Alert Notifications" else "Bật thông báo cảnh báo",
                        description = notificationDescription,
                        onConfirm = {
                            showNotificationWarningDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                            startActivity(intent)
                        },
                        onDismiss = { showNotificationWarningDialog = false }
                    )
                }

                if (showOverlayDialog) {
                    val overlayTitle = if (isEn) {
                        if (isChinesePhone) "Special Background Permissions" else "Display Over Other Apps"
                    } else {
                        if (isChinesePhone) "Quyền chạy nền đặc biệt" else "Quyền hiển thị cuộc gọi"
                    }

                    val overlayDescription = buildAnnotatedString {
                        if (isEn) {
                            if (isChinesePhone) {
                                append("To receive reliable alert calls on ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(manufacturer.uppercase())
                                }
                                append(" devices, please enable:\n\n")
                                append("• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Show on Lock screen")
                                }
                                append("\n\n• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Autostart")
                                }
                                append("\n\n• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Display pop-up windows")
                                }
                            } else {
                                append("To display alert screens immediately during emergencies, please allow:\n\n")
                                append("• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Display over other apps")
                                }
                            }
                        } else {
                            if (isChinesePhone) {
                                append("Để nhận cuộc gọi cảnh báo ổn định trên máy ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(manufacturer.uppercase())
                                }
                                append(", vui lòng bật các mục sau:\n\n")

                                append("• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Hiển thị trên màn hình khóa")
                                }
                                append("\n\n")

                                append("• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Tự khởi chạy")
                                }
                                append("\n\n")

                                append("• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Hiển thị cửa sổ pop-up")
                                }
                            } else {
                                append("Để màn hình cảnh báo hiển thị ngay lập tức khi có sự kiện khẩn cấp, vui lòng cho phép mục:\n\n")
                                append("• ")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Hiển thị trên các ứng dụng khác")
                                }
                            }
                        }
                    }

                    PermissionExplanationDialog(
                        title = overlayTitle,
                        description = overlayDescription,
                        onConfirm = {
                            showOverlayDialog = false
                            if (isChinesePhone) {
                                PermissionUtils.openManufacturerPermissions(context)
                            } else {
                                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri())
                                startActivity(intent)
                            }
                        },
                        onDismiss = { showOverlayDialog = false }
                    )
                }

                if (showBatteryDialog) {
                    val batteryDescription = buildAnnotatedString {
                        if (isEn) {
                            append("To avoid missing alerts while your phone is asleep, please:\n\n")
                            append("• Select ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("App battery usage")
                            }
                            append(" (or Battery).\n\n")
                            append("• Choose ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Unrestricted")
                            }
                            append(" so the app runs reliably.")
                        } else {
                            append("Để không bỏ lỡ cảnh báo khi điện thoại đang ở chế độ ngủ, vui lòng thực hiện:\n\n")

                            append("• Chọn mục ") // Dùng dấu chấm thay cho 1️⃣
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Mức sử dụng Pin của ứng dụng")
                            }
                            append(" (hoặc Pin).\n\n")

                            append("• Tích chọn chế độ ") // Dùng dấu chấm thay cho 2️⃣
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("Không hạn chế (Unrestricted)")
                            }
                            append(" để App luôn chạy ổn định.")
                        }
                    }

                    PermissionExplanationDialog(
                        title = if (isEn) "Keep Connection Active" else "Duy trì kết nối",
                        description = batteryDescription,
                        onConfirm = {
                            showBatteryDialog = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                            startActivity(intent)
                        },
                        onDismiss = { showBatteryDialog = false }
                    )
                }
                AppNavigation(
                    onLogoutRequest = { splashViewModel.logout() },
                    initialEventId = startEventId
                )

                LaunchedEffect(startEventId) {
                    if (startEventId != null) {
                        kotlinx.coroutines.delay(1000)
                        startEventId = null
                        intent.removeExtra("OPEN_EVENT_DETAIL")
                    }
                }
                    }
                    is SplashDestination.Loading -> { /* Keep Splash */ }
                }
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleManager.updateResources(newBase)
        super.attachBaseContext(context)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val eventId = intent.getStringExtra("OPEN_EVENT_DETAIL")
        if (!eventId.isNullOrEmpty()) {
            startEventId = eventId
        }
    }



    private lateinit var appUpdateManager: AppUpdateManager
    private val UPDATE_REQUEST_CODE = 123

    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                checkUpdate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, UPDATE_REQUEST_CODE)
            }
        }
    }
}

@Composable
fun PermissionExplanationDialog(
    title: String,
    description: AnnotatedString,
    icon: ImageVector = Icons.Default.Notifications,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = com.tenli.oneview.ui.component.rememberIsEnglish()
    val isDark = com.tenli.oneview.ui.component.rememberIsDarkTheme()
    val sheetBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF172033) else androidx.compose.ui.graphics.Color(0xFFF2F2F7)
    val closeBtnBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF263449) else androidx.compose.ui.graphics.Color(0xFFE2E8F0)
    val titleColor = if (isDark) androidx.compose.ui.graphics.Color(0xFFF8FAFC) else androidx.compose.ui.graphics.Color(0xFF1E293B)
    val subTextColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF94A3B8) else androidx.compose.ui.graphics.Color(0xFF64748B)
    val cancelBtnBg = if (isDark) androidx.compose.ui.graphics.Color(0xFF263449) else androidx.compose.ui.graphics.Color.White
    val primaryColor = com.tenli.oneview.ui.theme.BrandAccent

    com.tenli.oneview.ui.component.MobileFullScreenOverlay(
        onDismissRequest = onDismiss
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = androidx.compose.ui.Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = sheetBg
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 10.dp, bottom = 20.dp)
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .align(androidx.compose.ui.Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isDark) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
                                else androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.15f)
                            )
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(14.dp))

                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = titleColor
                        )
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(closeBtnBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.tenli.oneview.ui.theme.LucideIcons.X,
                                contentDescription = if (isEn) "Close" else "Đóng",
                                tint = subTextColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(primaryColor.copy(alpha = 0.14f)),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = subTextColor,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(cancelBtnBg)
                                .clickable(onClick = onDismiss),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Later" else "Để sau",
                                color = titleColor,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryColor)
                                .clickable(onClick = onConfirm),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "Set up now" else "Thiết lập ngay",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}