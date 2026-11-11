package com.tenli.oneview.ui.features.auth.login

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Size as AndroidSize
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.FlashOff
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.LuminanceSource
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.spacing
import kotlinx.coroutines.delay
import java.util.EnumMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val INVALID_QR_COOLDOWN_MS = 2800L

@Composable
fun QrScannerModal(
    isVietnamese: Boolean,
    onDismiss: () -> Unit,
    onValidDomainScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var invalidQrMessage by remember { mutableStateOf<String?>(null) }
    var lastInvalidTimestamp by remember { mutableStateOf(0L) }
    var hasScannedValid by remember { mutableStateOf(false) }

    LaunchedEffect(invalidQrMessage) {
        if (invalidQrMessage != null) {
            delay(INVALID_QR_COOLDOWN_MS)
            invalidQrMessage = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF060B14))
        ) {
            if (hasCameraPermission) {
                CameraQrPreview(
                    onQrCodeRawDetected = { rawValue ->
                        if (hasScannedValid) return@CameraQrPreview
                        val verifiedDomain = OneViewQrValidator.extractVerifiedDomain(rawValue)
                        if (verifiedDomain != null) {
                            hasScannedValid = true
                            onValidDomainScanned(verifiedDomain)
                        } else {
                            val now = System.currentTimeMillis()
                            if (now - lastInvalidTimestamp > INVALID_QR_COOLDOWN_MS) {
                                lastInvalidTimestamp = now
                                invalidQrMessage = if (isVietnamese) {
                                    "Mã QR không hợp lệ! Chỉ chấp nhận mã QR 'Đăng nhập App Mobile' từ Tenli OneView trên Web PC."
                                } else {
                                    "Invalid QR code! Only 'Mobile App Login' QR codes from Tenli OneView Web PC are accepted."
                                }
                            }
                        }
                    },
                    isVietnamese = isVietnamese,
                    invalidQrMessage = invalidQrMessage,
                    onClose = onDismiss
                )
            } else {
                PermissionRequiredContent(
                    isVietnamese = isVietnamese,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onClose = onDismiss
                )
            }
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
@Composable
private fun CameraQrPreview(
    onQrCodeRawDetected: (String) -> Unit,
    isVietnamese: Boolean,
    invalidQrMessage: String?,
    onClose: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnQrDetected by rememberUpdatedState(onQrCodeRawDetected)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val isDisposed = remember { booleanArrayOf(false) }
    val cameraProviderHolder = remember { arrayOfNulls<ProcessCameraProvider>(1) }
    val imageAnalysisHolder = remember { arrayOfNulls<ImageAnalysis>(1) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var currentZoomRatio by remember { mutableFloatStateOf(1.5f) }

    DisposableEffect(Unit) {
        onDispose {
            isDisposed[0] = true
            mainHandler.removeCallbacksAndMessages(null)
            runCatching {
                if (isTorchOn) {
                    cameraRef?.cameraControl?.enableTorch(false)
                }
            }
            runCatching { imageAnalysisHolder[0]?.clearAnalyzer() }
            runCatching { cameraProviderHolder[0]?.unbindAll() }
            imageAnalysisHolder[0] = null
            cameraProviderHolder[0] = null
            cameraRef = null
            cameraExecutor.shutdown()
        }
    }

    // Tự động lấy nét & đo sáng (AF + AE) vào đúng tâm khung quét định kỳ
    // để chống cháy sáng (overexposure) khi quét mã QR nền trắng trên màn hình PC nền tối.
    LaunchedEffect(cameraRef) {
        val cam = cameraRef ?: return@LaunchedEffect
        val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
        while (true) {
            try {
                val centerPoint = factory.createPoint(0.5f, 0.5f, 0.16f)
                val action = FocusMeteringAction.Builder(
                    centerPoint,
                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                )
                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                    .build()
                cam.cameraControl.startFocusAndMetering(action)
            } catch (_: Exception) {
                // Ignore if camera is closing
            }
            delay(3500L)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                // Hỗ trợ pinch-to-zoom & chạm để lấy nét/đo sáng trực tiếp vào vùng mã QR trên màn hình
                val scaleGestureDetector = ScaleGestureDetector(
                    ctx,
                    object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                        override fun onScale(detector: ScaleGestureDetector): Boolean {
                            val cam = cameraRef ?: return false
                            val zoomState = cam.cameraInfo.zoomState.value ?: return false
                            val nextZoom = (zoomState.zoomRatio * detector.scaleFactor)
                                .coerceIn(zoomState.minZoomRatio, minOf(zoomState.maxZoomRatio, 4.0f))
                            cam.cameraControl.setZoomRatio(nextZoom)
                            currentZoomRatio = nextZoom
                            return true
                        }
                    }
                )

                previewView.setOnTouchListener { view, event ->
                    scaleGestureDetector.onTouchEvent(event)
                    if (event.action == MotionEvent.ACTION_UP && !scaleGestureDetector.isInProgress) {
                        val cam = cameraRef
                        if (cam != null && view.width > 0 && view.height > 0) {
                            try {
                                val point = previewView.meteringPointFactory.createPoint(
                                    event.x,
                                    event.y,
                                    0.15f
                                )
                                val action = FocusMeteringAction.Builder(
                                    point,
                                    FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
                                )
                                    .setAutoCancelDuration(3, TimeUnit.SECONDS)
                                    .build()
                                cam.cameraControl.startFocusAndMetering(action)
                            } catch (_: Exception) {
                                // Ignore metering error
                            }
                        }
                    }
                    true
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = runCatching { cameraProviderFuture.get() }.getOrNull() ?: return@addListener
                    if (isDisposed[0]) {
                        runCatching { cameraProvider.unbindAll() }
                        return@addListener
                    }
                    cameraProviderHolder[0] = cameraProvider

                    // Tăng độ phân giải phân tích lên Full HD (1920x1080) thay vì mặc định 640x480
                    // để đọc rõ các mã QR mật độ cao (Version 7-9, ~49x49 modules) trên màn hình PC.
                    val resolutionSelector = ResolutionSelector.Builder()
                        .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
                        .setResolutionStrategy(
                            ResolutionStrategy(
                                AndroidSize(1920, 1080),
                                ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                            )
                        )
                        .build()

                    val preview = Preview.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .build()
                        .also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }

                    val qrAnalyzer = ZxingQrCodeAnalyzer { qrText ->
                        mainHandler.post {
                            if (!isDisposed[0]) {
                                currentOnQrDetected(qrText)
                            }
                        }
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setResolutionSelector(resolutionSelector)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            imageAnalysisHolder[0] = it
                            it.setAnalyzer(cameraExecutor, qrAnalyzer)
                        }

                    try {
                        cameraProvider.unbindAll()
                        val boundCamera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                        cameraRef = boundCamera

                        // Đặt mức zoom mặc định 1.5x để người dùng để điện thoại cách màn hình PC 20-30cm
                        // (ngoài khoảng lấy nét tối thiểu của ống kính) mà mã QR 200px vẫn đủ lớn và sắc nét.
                        val zoomState = boundCamera.cameraInfo.zoomState.value
                        val targetZoom = if (zoomState != null) {
                            1.5f.coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
                        } else {
                            1.5f
                        }
                        boundCamera.cameraControl.setZoomRatio(targetZoom)
                        currentZoomRatio = targetZoom
                    } catch (_: Exception) {
                        // Ignore camera bind failure on unsupported emulators
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Viewfinder Overlay
        ScannerViewfinderOverlay()

        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }

            Text(
                text = if (isVietnamese) "Quét mã QR dự án" else "Scan Project QR",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 17.sp
                )
            )

            val hasFlash = cameraRef?.cameraInfo?.hasFlashUnit() == true
            if (hasFlash) {
                IconButton(
                    onClick = {
                        val nextState = !isTorchOn
                        isTorchOn = nextState
                        cameraRef?.cameraControl?.enableTorch(nextState)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isTorchOn) BrandPrimary.copy(alpha = 0.85f)
                            else Color.Black.copy(alpha = 0.55f)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isTorchOn) Icons.Outlined.FlashOn else Icons.Outlined.FlashOff,
                        contentDescription = "Flash",
                        tint = Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(42.dp))
            }
        }

        // Bottom Guidance, Quick Zoom Selector & Validation Error Banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quick Zoom Selector (1x / 1.5x / 2x)
            Row(
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.78f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1.0f to "1x", 1.5f to "1.5x", 2.0f to "2x").forEach { (zoomVal, label) ->
                    val isSelected = kotlin.math.abs(currentZoomRatio - zoomVal) < 0.22f
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) BrandPrimary else Color.Transparent
                            )
                            .clickable {
                                val cam = cameraRef ?: return@clickable
                                val zoomState = cam.cameraInfo.zoomState.value
                                val clamped = if (zoomState != null) {
                                    zoomVal.coerceIn(zoomState.minZoomRatio, zoomState.maxZoomRatio)
                                } else {
                                    zoomVal
                                }
                                cam.cameraControl.setZoomRatio(clamped)
                                currentZoomRatio = clamped
                            }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = invalidQrMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val errorShape = RoundedCornerShape(MaterialTheme.spacing.radiusCompact)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clip(errorShape)
                        .background(Color(0xFFDC2626).copy(alpha = 0.92f))
                        .border(1.dp, Color(0xFFF87171).copy(alpha = 0.5f), errorShape)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WarningAmber,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = invalidQrMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            val infoCardShape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(infoCardShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.86f))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), infoCardShape)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isVietnamese) {
                            "Xác thực độc quyền Tenli OneView PC"
                        } else {
                            "Verified Tenli OneView PC QR Only"
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isVietnamese) {
                        "Trên phần mềm Tenli OneView Web PC, mở menu tài khoản → chọn \"Đăng nhập App Mobile\" và đưa mã QR vào khung quét."
                    } else {
                        "On Tenli OneView Web PC, open the account menu → select \"Mobile App Login\" and align the QR code inside the frame."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFCBD5E1),
                        lineHeight = 18.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ScannerViewfinderOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "QrScanLine")
    val scanProgress by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.92f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ScanLineOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cutoutSize = size.minDimension * 0.66f
        val left = (size.width - cutoutSize) / 2f
        val top = (size.height - cutoutSize) / 2f - 28.dp.toPx()
        val right = left + cutoutSize
        val bottom = top + cutoutSize
        val cornerRadius = 22.dp.toPx()

        // Dim background outside cutout
        val dimPath = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(
                RoundRect(
                    rect = Rect(left, top, right, bottom),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }
        drawPath(
            path = dimPath,
            color = Color.Black.copy(alpha = 0.62f),
            blendMode = BlendMode.SrcOver
        )

        // Subtle border around cutout
        drawRoundRect(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(left, top),
            size = Size(cutoutSize, cutoutSize),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // Corner brackets
        val bracketLen = 30.dp.toPx()
        val strokeWidth = 4.dp.toPx()
        val accentColor = Color(0xFF38BDF8)

        val cornersPath = Path().apply {
            // Top-left
            moveTo(left, top + bracketLen)
            lineTo(left, top + cornerRadius)
            quadraticTo(left, top, left + cornerRadius, top)
            lineTo(left + bracketLen, top)

            // Top-right
            moveTo(right - bracketLen, top)
            lineTo(right - cornerRadius, top)
            quadraticTo(right, top, right, top + cornerRadius)
            lineTo(right, top + bracketLen)

            // Bottom-right
            moveTo(right, bottom - bracketLen)
            lineTo(right, bottom - cornerRadius)
            quadraticTo(right, bottom, right - cornerRadius, bottom)
            lineTo(right - bracketLen, bottom)

            // Bottom-left
            moveTo(left + bracketLen, bottom)
            lineTo(left + cornerRadius, bottom)
            quadraticTo(left, bottom, left, bottom - cornerRadius)
            lineTo(left, bottom - bracketLen)
        }

        drawPath(
            path = cornersPath,
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Animated laser line
        val lineY = top + cutoutSize * scanProgress
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.95f),
                    Color.Transparent
                ),
                startX = left + 12.dp.toPx(),
                endX = right - 12.dp.toPx()
            ),
            start = Offset(left + 12.dp.toPx(), lineY),
            end = Offset(right - 12.dp.toPx(), lineY),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PermissionRequiredContent(
    isVietnamese: Boolean,
    onRequestPermission: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.QrCodeScanner,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isVietnamese) "Cấp quyền truy cập Camera" else "Camera Permission Required",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (isVietnamese) {
                "Vui lòng cho phép ứng dụng sử dụng Camera để quét mã QR đăng nhập dự án từ phần mềm Tenli OneView trên PC."
            } else {
                "Please allow camera access to scan the project login QR code from Tenli OneView on PC."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF94A3B8)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusCompact)
            ) {
                Text(text = if (isVietnamese) "Đóng" else "Close")
            }
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(MaterialTheme.spacing.radiusCompact)
            ) {
                Text(text = if (isVietnamese) "Cấp quyền Camera" else "Grant Permission")
            }
        }
    }
}

private class ZxingQrCodeAnalyzer(
    private val onQrDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.POSSIBLE_FORMATS, listOf(BarcodeFormat.QR_CODE))
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
            put(DecodeHintType.TRY_HARDER, true)
            put(DecodeHintType.ALSO_INVERTED, true)
        }
        setHints(hints)
    }

    private var luminanceBuffer = ByteArray(0)
    private var rowScratchBuffer = ByteArray(0)
    private var rotatedSquareBuffer = ByteArray(0)

    override fun analyze(image: ImageProxy) {
        try {
            val plane = image.planes.firstOrNull() ?: return
            val width = image.width
            val height = image.height
            if (width <= 0 || height <= 0) return

            val yBytes = extractLuminanceBytes(plane, width, height)
            val minDim = minOf(width, height)

            // 1. Ưu tiên vùng tâm khung quét (62% kích thước ngắn nhất):
            //    Loại bỏ viền tối của màn hình PC giúp GlobalHistogramBinarizer & HybridBinarizer
            //    không bị lệch ngưỡng sáng/tối và khử nhiễu Moiré trên tấm nền LCD/LED.
            val tightSize = (minDim * 0.62f).toInt().coerceAtLeast(120)
            val tightLeft = (width - tightSize) / 2
            val tightTop = (height - tightSize) / 2
            val tightSource = PlanarYUVLuminanceSource(
                yBytes,
                width,
                height,
                tightLeft,
                tightTop,
                tightSize,
                tightSize,
                false
            )

            var rawText = decodeSource(tightSource)

            // 2. Xoay 90 độ vùng tâm khung quét:
            //    Cảm biến camera điện thoại nằm ngang (xoay 90 độ so với màn hình dọc), trong khi
            //    FinderPatternFinder của ZXing quét ưu tiên theo hàng ngang trước. Xoay 90 độ giúp
            //    ZXing quét vuông góc với các vạch quét (scanlines) của màn hình PC.
            if (rawText.isNullOrBlank()) {
                val rotatedBytes = rotateSquare90(yBytes, width, tightLeft, tightTop, tightSize)
                val rotatedSource = PlanarYUVLuminanceSource(
                    rotatedBytes,
                    tightSize,
                    tightSize,
                    0,
                    0,
                    tightSize,
                    tightSize,
                    false
                )
                rawText = decodeSource(rotatedSource)
            }

            // 3. Mở rộng vùng quét trung tâm (84%) phòng trường hợp người dùng để lệch nhẹ hoặc gần hơn
            if (rawText.isNullOrBlank()) {
                val wideSize = (minDim * 0.84f).toInt().coerceAtLeast(tightSize)
                val wideLeft = (width - wideSize) / 2
                val wideTop = (height - wideSize) / 2
                val wideSource = PlanarYUVLuminanceSource(
                    yBytes,
                    width,
                    height,
                    wideLeft,
                    wideTop,
                    wideSize,
                    wideSize,
                    false
                )
                rawText = decodeSource(wideSource)
            }

            // 4. Fallback toàn khung hình
            if (rawText.isNullOrBlank()) {
                val fullSource = PlanarYUVLuminanceSource(
                    yBytes,
                    width,
                    height,
                    0,
                    0,
                    width,
                    height,
                    false
                )
                rawText = decodeSource(fullSource)
            }

            if (!rawText.isNullOrBlank()) {
                onQrDetected(rawText)
            }
        } catch (_: Exception) {
            // Ignore frame decode error
        } finally {
            image.close()
        }
    }

    private fun decodeSource(source: LuminanceSource): String? {
        // Thử GlobalHistogramBinarizer trước (chống nhiễu lưới điểm ảnh RGB / Moiré trên màn hình PC tốt nhất),
        // sau đó thử HybridBinarizer (cân bằng sáng cục bộ theo khối 8x8).
        try {
            val result = reader.decodeWithState(BinaryBitmap(GlobalHistogramBinarizer(source)))
            if (!result.text.isNullOrBlank()) return result.text
        } catch (_: Exception) {
            // Continue to HybridBinarizer
        } finally {
            reader.reset()
        }

        try {
            val result = reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
            if (!result.text.isNullOrBlank()) return result.text
        } catch (_: Exception) {
            // Ignore
        } finally {
            reader.reset()
        }

        return null
    }

    private fun rotateSquare90(
        src: ByteArray,
        srcStride: Int,
        left: Int,
        top: Int,
        size: Int
    ): ByteArray {
        val total = size * size
        if (rotatedSquareBuffer.size < total) {
            rotatedSquareBuffer = ByteArray(total)
        }
        val dst = rotatedSquareBuffer
        for (row in 0 until size) {
            val srcRowOffset = (top + row) * srcStride + left
            val dstCol = size - 1 - row
            for (col in 0 until size) {
                dst[col * size + dstCol] = src[srcRowOffset + col]
            }
        }
        return dst
    }

    private fun extractLuminanceBytes(
        plane: ImageProxy.PlaneProxy,
        width: Int,
        height: Int
    ): ByteArray {
        val buffer = plane.buffer
        buffer.rewind()
        val rowStride = plane.rowStride
        val pixelStride = plane.pixelStride
        val totalPixels = width * height

        if (luminanceBuffer.size < totalPixels) {
            luminanceBuffer = ByteArray(totalPixels)
        }
        val output = luminanceBuffer

        if (pixelStride == 1 && rowStride == width && buffer.remaining() >= totalPixels) {
            buffer.get(output, 0, totalPixels)
            return output
        }

        if (rowScratchBuffer.size < rowStride) {
            rowScratchBuffer = ByteArray(rowStride)
        }
        val rowBuffer = rowScratchBuffer

        for (row in 0 until height) {
            val remaining = buffer.remaining()
            if (remaining <= 0) break
            val bytesToRead = minOf(rowStride, remaining)
            buffer.get(rowBuffer, 0, bytesToRead)
            val outputOffset = row * width
            if (pixelStride == 1) {
                val copyLen = minOf(width, bytesToRead)
                System.arraycopy(rowBuffer, 0, output, outputOffset, copyLen)
            } else {
                for (col in 0 until width) {
                    val idx = col * pixelStride
                    if (idx < bytesToRead) {
                        output[outputOffset + col] = rowBuffer[idx]
                    }
                }
            }
        }
        return output
    }
}
