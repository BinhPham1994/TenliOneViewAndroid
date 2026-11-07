package com.tenli.oneview.ui.features.auth.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.spacing
import kotlinx.coroutines.delay
import java.util.EnumMap
import java.util.concurrent.Executors

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

@Composable
private fun CameraQrPreview(
    onQrCodeRawDetected: (String) -> Unit,
    isVietnamese: Boolean,
    invalidQrMessage: String?,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
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

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                    val qrAnalyzer = ZxingQrCodeAnalyzer { qrText ->
                        mainHandler.post {
                            onQrCodeRawDetected(qrText)
                        }
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor, qrAnalyzer)
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraRef = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
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

        // Bottom Guidance & Validation Error Banner
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
        }
        setHints(hints)
    }

    override fun analyze(image: ImageProxy) {
        try {
            val plane = image.planes.firstOrNull() ?: return
            val width = image.width
            val height = image.height
            val yBytes = extractLuminanceBytes(plane, width, height)

            val source = PlanarYUVLuminanceSource(
                yBytes,
                width,
                height,
                0,
                0,
                width,
                height,
                false
            )

            val result = try {
                reader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
            } catch (_: Exception) {
                try {
                    reader.decodeWithState(BinaryBitmap(HybridBinarizer(source.invert())))
                } catch (_: Exception) {
                    null
                }
            } finally {
                reader.reset()
            }

            val rawText = result?.text
            if (!rawText.isNullOrBlank()) {
                onQrDetected(rawText)
            }
        } catch (_: Exception) {
            // Ignore frame decode error
        } finally {
            image.close()
        }
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

        if (pixelStride == 1 && rowStride == width) {
            val data = ByteArray(buffer.remaining())
            buffer.get(data)
            return data
        }

        val output = ByteArray(width * height)
        val rowBuffer = ByteArray(rowStride)
        for (row in 0 until height) {
            val bytesToRead = if (row == height - 1) {
                minOf(rowStride, buffer.remaining())
            } else {
                rowStride
            }
            buffer.get(rowBuffer, 0, bytesToRead)
            val outputOffset = row * width
            if (pixelStride == 1) {
                System.arraycopy(rowBuffer, 0, output, outputOffset, width)
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
