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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.tenli.oneview.ui.navigation.AppNavigation
import com.tenli.oneview.ui.theme.TenliAIoTTheme
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.ui.utils.PermissionUtils
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenli.oneview.ui.features.splash.SplashDestination
import com.tenli.oneview.ui.features.splash.SplashViewModel
import com.tenli.oneview.ui.features.auth.login.LoginScreen

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

                if (showNotificationWarningDialog) {
                    val notificationDescription = buildAnnotatedString {
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

                    PermissionExplanationDialog(
                        title = "Bật thông báo cảnh báo",
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
                    val overlayTitle = if (isChinesePhone) "Quyền chạy nền đặc biệt" else "Quyền hiển thị cuộc gọi"

                    val overlayDescription = buildAnnotatedString {
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

                    PermissionExplanationDialog(
                        title = "Duy trì kết nối",
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
    description: AnnotatedString, // Đổi từ String sang AnnotatedString
    icon: ImageVector = Icons.Default.Notifications,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                // Đổi sang TextAlign.Start để các bước 1, 2, 3 thẳng hàng, dễ đọc hơn
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Thiết lập ngay",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Để sau",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    )
}