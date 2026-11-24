package com.tenli.oneview.ui.component


import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.abs
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.Coil
import coil.compose.AsyncImage
import coil.memory.MemoryCache
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.R
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.network.retrofit.TokenAuthenticator
import com.tenli.oneview.ui.theme.appColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive as isCoroutineActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("InflateParams")
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    thumbnailUrl: String?,
    deviceKey: String,
    isOriginalRatio: Boolean = false,
    isFullscreen: Boolean = false,
    playWhenReady: Boolean = true,
    showLoadingOverlay: Boolean = true,
    showControllerInitially: Boolean = false,
    initialSeekPositionMs: Long? = null,
    errorText: String = "Không có video sự kiện",
    onPlayingChange: (Boolean) -> Unit = {},
    onPlayWhenReadyChange: (Boolean) -> Unit = {},
    onErrorChange: (Boolean) -> Unit = {},
    onControllerVisibilityChange: (Boolean) -> Unit = {},
    onFullscreenToggle: (() -> Unit)? = null,
    onTimeUpdate: ((Long) -> Unit)? = null,
    enableZoom: Boolean = false,
    smoothResetTrigger: Int = 0,
    isExternallyTransformed: Boolean = false,
    onZoomChanged: (Boolean) -> Unit = {},
    isActive: Boolean = true
) {
    var isError by remember(videoUrl) { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isVideoReady by remember(videoUrl) { mutableStateOf(false) }
    var isActuallyPlaying by remember(videoUrl) { mutableStateOf(false) }
    var isControllerShown by remember(videoUrl) { mutableStateOf(showControllerInitially) }
    var zoomScale by remember(videoUrl) { mutableFloatStateOf(1f) }
    var zoomOffsetX by remember(videoUrl) { mutableFloatStateOf(0f) }
    var zoomOffsetY by remember(videoUrl) { mutableFloatStateOf(0f) }
    val zoomValuesHolder = remember(videoUrl) { floatArrayOf(1f, 0f, 0f) }
    val zoomAnimJobHolder = remember(videoUrl) { arrayOfNulls<Job>(1) }
    val authRefreshJobHolder = remember(videoUrl) { arrayOfNulls<Job>(1) }
    val viewportSizeHolder = remember(videoUrl) { arrayOf(IntSize.Zero) }
    val playerViewHolder = remember(videoUrl) { arrayOfNulls<PlayerView>(1) }
    val exoPlayerHolder = remember(videoUrl) { arrayOfNulls<ExoPlayer>(1) }
    val currentTokenHolder = remember(videoUrl) { arrayOf(deviceKey) }
    val hasRetriedAuth = remember(videoUrl) { booleanArrayOf(false) }
    val savedResumePositionMs = remember(videoUrl) { longArrayOf(C.TIME_UNSET) }
    val wasStoppedForBackground = remember(videoUrl) { booleanArrayOf(false) }
    val wasStoppedForInactive = remember(videoUrl) { booleanArrayOf(false) }
    val wasEndedBeforeStop = remember(videoUrl) { booleanArrayOf(false) }
    val suppressPlayWhenReadyCallback = remember(videoUrl) { booleanArrayOf(false) }
    val suppressFullscreenCallback = remember(videoUrl) { booleanArrayOf(false) }
    val colors = MaterialTheme.appColors
    val containerBg = colors.surface
    val stageBg = when {
        isVideoReady && !isError -> colors.mediaBg
        showLoadingOverlay -> containerBg
        else -> Color.Transparent
    }
    val currentPlayWhenReady by rememberUpdatedState(playWhenReady)
    val currentIsActive by rememberUpdatedState(isActive)
    val currentIsFullscreen by rememberUpdatedState(isFullscreen)
    val currentIsExternallyTransformed by rememberUpdatedState(isExternallyTransformed)
    val currentOnPlayingChange by rememberUpdatedState(onPlayingChange)
    val currentOnPlayWhenReadyChange by rememberUpdatedState(onPlayWhenReadyChange)
    val currentOnErrorChange by rememberUpdatedState(onErrorChange)
    val currentOnControllerVisibilityChange by rememberUpdatedState(onControllerVisibilityChange)
    val currentOnFullscreenToggle by rememberUpdatedState(onFullscreenToggle)
    val currentOnZoomChanged by rememberUpdatedState(onZoomChanged)

    fun syncFullscreenButtonState(view: PlayerView?, targetFullscreen: Boolean) {
        if (view == null) return
        suppressFullscreenCallback[0] = true
        try {
            view.setFullscreenButtonState(targetFullscreen)
        } finally {
            suppressFullscreenCallback[0] = false
        }
    }

    androidx.compose.runtime.LaunchedEffect(isFullscreen) {
        if (currentOnFullscreenToggle != null) {
            syncFullscreenButtonState(playerViewHolder[0], isFullscreen)
        }
    }

    fun syncSurfaceZoom(view: PlayerView?, targetScale: Float, ox: Float, oy: Float) {
        val frame = view?.findViewById<android.view.View>(androidx.media3.ui.R.id.exo_content_frame)
            ?: view?.videoSurfaceView
        frame?.apply {
            scaleX = targetScale
            scaleY = targetScale
            translationX = ox
            translationY = oy
        }
    }

    fun cancelZoomAnimation() {
        zoomAnimJobHolder[0]?.cancel()
        zoomAnimJobHolder[0] = null
    }

    fun applyVideoZoom(targetScale: Float, rawX: Float, rawY: Float) {
        val viewportSize = viewportSizeHolder[0]
        val s = targetScale.coerceIn(1f, 5f)
        val clamped = if (s <= 1.001f || viewportSize.width <= 0 || viewportSize.height <= 0) {
            Offset.Zero
        } else {
            val maxX = (viewportSize.width * (s - 1f)) * 0.5f
            val maxY = (viewportSize.height * (s - 1f)) * 0.5f
            Offset(rawX.coerceIn(-maxX, maxX), rawY.coerceIn(-maxY, maxY))
        }
        val wasZoomed = zoomValuesHolder[0] > 1.02f
        zoomValuesHolder[0] = s
        zoomValuesHolder[1] = clamped.x
        zoomValuesHolder[2] = clamped.y
        zoomScale = s
        zoomOffsetX = clamped.x
        zoomOffsetY = clamped.y
        syncSurfaceZoom(playerViewHolder[0], s, clamped.x, clamped.y)
        val nowZoomed = s > 1.02f
        if (wasZoomed != nowZoomed) {
            currentOnZoomChanged(nowZoomed)
        }
    }

    val resetVideoZoom: () -> Unit = {
        cancelZoomAnimation()
        zoomAnimJobHolder[0] = coroutineScope.launch {
            val startScale = zoomValuesHolder[0]
            val startX = zoomValuesHolder[1]
            val startY = zoomValuesHolder[2]
            if (startScale <= 1.005f) {
                applyVideoZoom(1f, 0f, 0f)
                return@launch
            }
            val anim = Animatable(0f)
            anim.animateTo(1f, animationSpec = tween(durationMillis = 200)) {
                val f = value
                applyVideoZoom(
                    targetScale = startScale + (1f - startScale) * f,
                    rawX = startX * (1f - f),
                    rawY = startY * (1f - f)
                )
            }
            applyVideoZoom(1f, 0f, 0f)
        }
    }

    androidx.compose.runtime.LaunchedEffect(smoothResetTrigger) {
        if (smoothResetTrigger > 0) {
            resetVideoZoom()
        }
    }

    fun createAuthenticatedMediaSource(token: String) =
        androidx.media3.exoplayer.source.DefaultMediaSourceFactory(
            DefaultHttpDataSource.Factory()
                .setConnectTimeoutMs(10_000)
                .setReadTimeoutMs(15_000)
                .setAllowCrossProtocolRedirects(true)
                .apply {
                    if (token.isNotBlank() && !VideoFrameCache.isPresignedUrl(videoUrl)) {
                        setDefaultRequestProperties(mapOf("Authorization" to "Bearer $token"))
                    }
                }
        ).createMediaSource(MediaItem.fromUri(videoUrl))

    val playerListener = remember(videoUrl) {
        object : Player.Listener {
            override fun onRenderedFirstFrame() {
                isVideoReady = true
                isError = false
                currentOnPlayingChange(true)
                if (!suppressPlayWhenReadyCallback[0]) {
                    currentOnPlayWhenReadyChange(currentPlayWhenReady)
                }
                currentOnErrorChange(false)
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                isActuallyPlaying = isPlaying
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (!suppressPlayWhenReadyCallback[0]) {
                    currentOnPlayWhenReadyChange(playWhenReady)
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    isActuallyPlaying = false
                    currentOnPlayWhenReadyChange(false)
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                var cause: Throwable? = error.cause
                var isAuthHttpError = false
                while (cause != null) {
                    if (cause is HttpDataSource.InvalidResponseCodeException &&
                        (cause.responseCode == 401 || cause.responseCode == 403)
                    ) {
                        isAuthHttpError = true
                        break
                    }
                    cause = cause.cause
                }

                val player = exoPlayerHolder[0]
                if (isAuthHttpError &&
                    !hasRetriedAuth[0] &&
                    player != null &&
                    !VideoFrameCache.isPresignedUrl(videoUrl)
                ) {
                    hasRetriedAuth[0] = true
                    val failedToken = currentTokenHolder[0]
                    val resumePos = player.currentPosition.coerceAtLeast(0L)
                    authRefreshJobHolder[0]?.cancel()
                    authRefreshJobHolder[0] = coroutineScope.launch {
                        val newToken = withContext(Dispatchers.IO) {
                            TokenAuthenticator.refreshAccessToken(failedToken)
                        }
                        val activePlayer = exoPlayerHolder[0]
                        if (!newToken.isNullOrBlank() && activePlayer === player) {
                            currentTokenHolder[0] = newToken
                            activePlayer.setMediaSource(createAuthenticatedMediaSource(newToken), resumePos)
                            activePlayer.prepare()
                        } else {
                            isError = true
                            isVideoReady = false
                            isActuallyPlaying = false
                            currentOnPlayingChange(false)
                            currentOnPlayWhenReadyChange(false)
                            currentOnErrorChange(true)
                        }
                    }
                    return
                }

                android.util.Log.e("VideoPlayer", "ExoPlayer Error: ${error.message}", error)
                isError = true
                isVideoReady = false
                isActuallyPlaying = false
                currentOnPlayingChange(false)
                currentOnPlayWhenReadyChange(false)
                currentOnErrorChange(true)
            }
        }
    }

    val exoPlayer = remember(videoUrl) {
        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 2000,
                /* maxBufferMs = */ 12000,
                /* bufferForPlaybackMs = */ 250,
                /* bufferForPlaybackAfterRebufferMs = */ 500
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBackBuffer(0, false)
            .build()
        val renderersFactory = androidx.media3.exoplayer.DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)

        ExoPlayer.Builder(context, renderersFactory)
            .setLoadControl(loadControl)
            .build()
            .apply {
                exoPlayerHolder[0] = this
                setMediaSource(createAuthenticatedMediaSource(currentTokenHolder[0]))
                if (initialSeekPositionMs != null) {
                    seekTo(initialSeekPositionMs)
                }
                addListener(playerListener)
                val shouldStart = currentPlayWhenReady && currentIsActive
                suppressPlayWhenReadyCallback[0] = true
                try {
                    this.playWhenReady = shouldStart
                } finally {
                    suppressPlayWhenReadyCallback[0] = false
                }
                if (shouldStart) {
                    prepare()
                }
            }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    androidx.compose.runtime.LaunchedEffect(exoPlayer, playWhenReady, isActive) {
        suppressPlayWhenReadyCallback[0] = true
        try {
            if (!isActive) {
                if (exoPlayer.playbackState != Player.STATE_IDLE) {
                    val wasEnded = exoPlayer.playbackState == Player.STATE_ENDED
                    wasEndedBeforeStop[0] = wasEnded
                    if (!wasEnded) {
                        savedResumePositionMs[0] = exoPlayer.currentPosition.coerceAtLeast(0L)
                    } else {
                        savedResumePositionMs[0] = C.TIME_UNSET
                    }
                    wasStoppedForInactive[0] = true
                    isActuallyPlaying = false
                    exoPlayer.stop()
                } else if (exoPlayer.playWhenReady) {
                    exoPlayer.pause()
                }
                return@LaunchedEffect
            }

            val stoppedForInactive = wasStoppedForInactive[0]
            wasStoppedForInactive[0] = false
            val isLifecycleStarted = lifecycleOwner.lifecycle.currentState.isAtLeast(
                androidx.lifecycle.Lifecycle.State.STARTED
            )

            if (playWhenReady) {
                if (!isLifecycleStarted) {
                    return@LaunchedEffect
                }
                if (exoPlayer.playbackState == Player.STATE_IDLE) {
                    val resumePos = savedResumePositionMs[0]
                    if (wasEndedBeforeStop[0]) {
                        exoPlayer.seekTo(0L)
                        wasEndedBeforeStop[0] = false
                    } else if (resumePos != C.TIME_UNSET && resumePos > 0L) {
                        exoPlayer.seekTo(resumePos)
                        savedResumePositionMs[0] = C.TIME_UNSET
                    }
                    exoPlayer.prepare()
                } else if (exoPlayer.playbackState == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0L)
                }
                if (!exoPlayer.playWhenReady) {
                    exoPlayer.play()
                }
            } else {
                if (exoPlayer.playWhenReady) {
                    exoPlayer.pause()
                }
                if (stoppedForInactive && exoPlayer.playbackState == Player.STATE_IDLE) {
                    isVideoReady = false
                    currentOnPlayingChange(false)
                }
            }
        } finally {
            suppressPlayWhenReadyCallback[0] = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            authRefreshJobHolder[0]?.cancel()
            authRefreshJobHolder[0] = null
            cancelZoomAnimation()
            isActuallyPlaying = false
            exoPlayer.removeListener(playerListener)
            val pv = playerViewHolder[0]
            if (pv?.player === exoPlayer) {
                pv.player = null
            }
            playerViewHolder[0] = null
            if (exoPlayerHolder[0] === exoPlayer) {
                exoPlayerHolder[0] = null
            }
            exoPlayer.clearVideoSurface()
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> {
                    suppressPlayWhenReadyCallback[0] = true
                    try {
                        exoPlayer.pause()
                    } finally {
                        suppressPlayWhenReadyCallback[0] = false
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    suppressPlayWhenReadyCallback[0] = true
                    try {
                        if (exoPlayer.playbackState != Player.STATE_IDLE) {
                            val wasEnded = exoPlayer.playbackState == Player.STATE_ENDED
                            wasEndedBeforeStop[0] = wasEnded
                            if (!wasEnded) {
                                savedResumePositionMs[0] = exoPlayer.currentPosition.coerceAtLeast(0L)
                            } else {
                                savedResumePositionMs[0] = C.TIME_UNSET
                            }
                            wasStoppedForBackground[0] = true
                            isActuallyPlaying = false
                            exoPlayer.stop()
                        } else {
                            exoPlayer.pause()
                        }
                    } finally {
                        suppressPlayWhenReadyCallback[0] = false
                    }
                }
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> {
                    val stoppedForBg = wasStoppedForBackground[0]
                    wasStoppedForBackground[0] = false
                    if (!currentIsActive) {
                        if (stoppedForBg) {
                            isVideoReady = false
                            currentOnPlayingChange(false)
                        }
                        return@LifecycleEventObserver
                    }
                    if (currentPlayWhenReady && !wasEndedBeforeStop[0] && exoPlayer.playbackState != Player.STATE_ENDED) {
                        suppressPlayWhenReadyCallback[0] = true
                        try {
                            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                                val resumePos = savedResumePositionMs[0]
                                if (resumePos != C.TIME_UNSET && resumePos > 0L) {
                                    exoPlayer.seekTo(resumePos)
                                    savedResumePositionMs[0] = C.TIME_UNSET
                                }
                                exoPlayer.prepare()
                            }
                            exoPlayer.play()
                        } finally {
                            suppressPlayWhenReadyCallback[0] = false
                        }
                    } else if (stoppedForBg && (!currentPlayWhenReady || wasEndedBeforeStop[0])) {
                        isVideoReady = false
                        currentOnPlayingChange(false)
                    }
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentOnTimeUpdate by rememberUpdatedState(onTimeUpdate)
    if (onTimeUpdate != null) {
        androidx.compose.runtime.LaunchedEffect(exoPlayer, isActuallyPlaying) {
            if (!isActuallyPlaying) return@LaunchedEffect
            while (isCoroutineActive && isActuallyPlaying) {
                currentOnTimeUpdate?.invoke(exoPlayer.currentPosition.coerceAtLeast(0L))
                delay(250L)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(isVideoReady, isError, showControllerInitially) {
        if (isVideoReady && !isError) {
            if (showControllerInitially) {
                playerViewHolder[0]?.showController()
                currentOnControllerVisibilityChange(true)
            } else {
                playerViewHolder[0]?.hideController()
                currentOnControllerVisibilityChange(false)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(videoUrl, exoPlayer) {
        syncSurfaceZoom(
            playerViewHolder[0],
            zoomValuesHolder[0],
            zoomValuesHolder[1],
            zoomValuesHolder[2]
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .background(stageBg)
            .onSizeChanged { size ->
                viewportSizeHolder[0] = size
                if (zoomValuesHolder[0] > 1.001f) {
                    applyVideoZoom(zoomValuesHolder[0], zoomValuesHolder[1], zoomValuesHolder[2])
                }
            }
            .then(
                if (enableZoom) {
                    Modifier.pointerInput(videoUrl) {
                        val touchSlop = viewConfiguration.touchSlop
                        val controllerSafeZonePx = 64.dp.toPx()
                        awaitEachGesture {
                            val down = awaitFirstDown(
                                requireUnconsumed = false,
                                pass = PointerEventPass.Initial
                            )
                            cancelZoomAnimation()
                            val startedInBottomBar =
                                isControllerShown && down.position.y >= (size.height - controllerSafeZonePx)
                            var isTransforming = false
                            var panAccumulated = Offset.Zero

                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val pressedCount = event.changes.count { it.pressed }
                                if (pressedCount == 0) break

                                if (pressedCount >= 2) {
                                    val zoomChange = event.calculateZoom()
                                    val panChange = event.calculatePan()
                                    val centroid = event.calculateCentroid(useCurrent = false)

                                    if (!isTransforming) {
                                        panAccumulated += panChange
                                        if (abs(1f - zoomChange) > 0.015f || panAccumulated.getDistance() > touchSlop) {
                                            isTransforming = true
                                        }
                                    }

                                    if (isTransforming && centroid != Offset.Unspecified) {
                                        val oldScale = zoomValuesHolder[0]
                                        val newScale = (oldScale * zoomChange).coerceIn(1f, 5f)
                                        val effectiveZoom = if (oldScale > 0f) newScale / oldScale else 1f
                                        val center = Offset(size.width * 0.5f, size.height * 0.5f)
                                        val newOffset = (centroid - center) * (1f - effectiveZoom) +
                                            Offset(zoomValuesHolder[1], zoomValuesHolder[2]) * effectiveZoom +
                                            panChange
                                        applyVideoZoom(newScale, newOffset.x, newOffset.y)
                                    }

                                    event.changes.forEach { change ->
                                        if (change.positionChanged()) {
                                            change.consume()
                                        }
                                    }
                                } else if (zoomValuesHolder[0] > 1.02f && !startedInBottomBar) {
                                    val panChange = event.calculatePan()
                                    if (!isTransforming) {
                                        panAccumulated += panChange
                                        if (panAccumulated.getDistance() > touchSlop) {
                                            isTransforming = true
                                        }
                                    }
                                    if (isTransforming) {
                                        applyVideoZoom(
                                            targetScale = zoomValuesHolder[0],
                                            rawX = zoomValuesHolder[1] + panChange.x,
                                            rawY = zoomValuesHolder[2] + panChange.y
                                        )
                                        event.changes.forEach { change ->
                                            if (change.positionChanged()) {
                                                change.consume()
                                            }
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            if (zoomValuesHolder[0] in 1.0001f..1.03f) {
                                applyVideoZoom(1f, 0f, 0f)
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
    ) {
        val targetResizeMode = if (isOriginalRatio) {
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else {
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
        AndroidView(factory = { ctx ->
            (android.view.LayoutInflater.from(ctx).inflate(R.layout.view_exo_texture, null) as PlayerView).apply {
                playerViewHolder[0] = this
                clipChildren = true
                useController = true
                controllerAutoShow = showControllerInitially
                controllerHideOnTouch = true
                controllerShowTimeoutMs = 3000
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                resizeMode = targetResizeMode
                setControllerVisibilityListener(
                    PlayerView.ControllerVisibilityListener { visibility ->
                        val visible = visibility == android.view.View.VISIBLE
                        isControllerShown = visible
                        currentOnControllerVisibilityChange(visible)
                    }
                )
                player = exoPlayer
                if (!showControllerInitially) {
                    hideController()
                }
                if (currentOnFullscreenToggle != null) {
                    setFullscreenButtonClickListener(
                        PlayerView.FullscreenButtonClickListener { _ ->
                            if (!suppressFullscreenCallback[0]) {
                                val desiredFullscreen = !currentIsFullscreen
                                syncFullscreenButtonState(this, desiredFullscreen)
                                currentOnFullscreenToggle?.invoke()
                            }
                        }
                    )
                    syncFullscreenButtonState(this, isFullscreen)
                } else {
                    setFullscreenButtonClickListener(null as PlayerView.FullscreenButtonClickListener?)
                }
                syncSurfaceZoom(this, zoomValuesHolder[0], zoomValuesHolder[1], zoomValuesHolder[2])
            }
        }, update = { view ->
            (view as PlayerView).let { pv ->
                playerViewHolder[0] = pv
                pv.controllerAutoShow = showControllerInitially
                if (pv.player !== exoPlayer) {
                    pv.player = exoPlayer
                    syncSurfaceZoom(pv, zoomValuesHolder[0], zoomValuesHolder[1], zoomValuesHolder[2])
                    if (!showControllerInitially) {
                        pv.hideController()
                        isControllerShown = false
                        currentOnControllerVisibilityChange(false)
                    }
                }
                if (pv.resizeMode != targetResizeMode) {
                    pv.resizeMode = targetResizeMode
                }
                if (currentOnFullscreenToggle != null) {
                    pv.setFullscreenButtonClickListener(
                        PlayerView.FullscreenButtonClickListener { _ ->
                            if (!suppressFullscreenCallback[0]) {
                                val desiredFullscreen = !currentIsFullscreen
                                syncFullscreenButtonState(pv, desiredFullscreen)
                                currentOnFullscreenToggle?.invoke()
                            }
                        }
                    )
                    syncFullscreenButtonState(pv, isFullscreen)
                } else {
                    pv.setFullscreenButtonClickListener(null as PlayerView.FullscreenButtonClickListener?)
                }
            }
        }, onRelease = { view ->
            val pv = view as? PlayerView
            syncSurfaceZoom(pv, 1f, 0f, 0f)
            if (playerViewHolder[0] === pv) {
                playerViewHolder[0] = null
            }
            pv?.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?)
            pv?.setFullscreenButtonClickListener(null as PlayerView.FullscreenButtonClickListener?)
            pv?.player = null
        }, modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (isVideoReady && !isError) 1f else 0f }
            .pointerInput(videoUrl) {
                awaitEachGesture {
                    val down = awaitFirstDown(
                        requireUnconsumed = false,
                        pass = PointerEventPass.Initial
                    )
                    if (!currentIsExternallyTransformed || down.isConsumed) {
                        return@awaitEachGesture
                    }
                    if (playerViewHolder[0] == null) {
                        return@awaitEachGesture
                    }
                    val pointerId = down.id
                    val downTimeMs = down.uptimeMillis
                    var lastX = down.position.x
                    var lastY = down.position.y
                    var dispatchedDown = false

                    fun sendToPlayerView(action: Int, eventTimeMs: Long, x: Float, y: Float) {
                        val targetView = playerViewHolder[0] ?: return
                        val safeEventTime = eventTimeMs.coerceAtLeast(downTimeMs)
                        val motionEvent = android.view.MotionEvent.obtain(
                            downTimeMs,
                            safeEventTime,
                            action,
                            x,
                            y,
                            0
                        )
                        try {
                            targetView.dispatchTouchEvent(motionEvent)
                        } finally {
                            motionEvent.recycle()
                        }
                    }

                    sendToPlayerView(android.view.MotionEvent.ACTION_DOWN, downTimeMs, lastX, lastY)
                    dispatchedDown = true
                    down.consume()

                    try {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val pressedCount = event.changes.count { it.pressed }

                            // Khi chạm ngón thứ 2 (Pinch-to-Zoom) hoặc cử chỉ pan khi đang zoom đã consume
                            if (pressedCount >= 2 || event.changes.any { it.isConsumed }) {
                                if (dispatchedDown) {
                                    val cancelTime = event.changes.firstOrNull()?.uptimeMillis
                                        ?: android.os.SystemClock.uptimeMillis()
                                    sendToPlayerView(
                                        android.view.MotionEvent.ACTION_CANCEL,
                                        cancelTime,
                                        lastX,
                                        lastY
                                    )
                                    dispatchedDown = false
                                }
                                break
                            }

                            val change = event.changes.firstOrNull { it.id == pointerId }
                                ?: event.changes.firstOrNull()
                                ?: break

                            lastX = change.position.x
                            lastY = change.position.y

                            if (!change.pressed) {
                                if (dispatchedDown) {
                                    sendToPlayerView(
                                        android.view.MotionEvent.ACTION_UP,
                                        change.uptimeMillis,
                                        lastX,
                                        lastY
                                    )
                                    dispatchedDown = false
                                }
                                change.consume()
                                break
                            } else {
                                if (dispatchedDown) {
                                    sendToPlayerView(
                                        android.view.MotionEvent.ACTION_MOVE,
                                        change.uptimeMillis,
                                        lastX,
                                        lastY
                                    )
                                }
                                change.consume()
                            }
                        }
                    } finally {
                        if (dispatchedDown) {
                            sendToPlayerView(
                                android.view.MotionEvent.ACTION_CANCEL,
                                android.os.SystemClock.uptimeMillis(),
                                lastX,
                                lastY
                            )
                            dispatchedDown = false
                        }
                    }
                }
            })

        if (enableZoom) {
            VideoZoomResetBadge(
                scaleProvider = { zoomScale },
                isVisibleProvider = { zoomScale > 1.06f && isVideoReady && !isError },
                onReset = resetVideoZoom,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }

        if (isError) {
            val isEn = rememberIsEnglish()
            val resolvedErrorText = if (isEn && errorText == "Không có video sự kiện") {
                "No event video"
            } else {
                errorText
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                CommonEmptyState(
                    text = resolvedErrorText,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (!isVideoReady && showLoadingOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                if (!thumbnailUrl.isNullOrEmpty()) {
                    val thumbRequest = remember(thumbnailUrl) {
                        val memCache = Coil.imageLoader(context).memoryCache
                        val placeholderKey = if (memCache != null) {
                            listOf(
                                "${thumbnailUrl}_1280x720",
                                "${thumbnailUrl}_800x450",
                                "${thumbnailUrl}_480x270",
                                "${thumbnailUrl}_420x236",
                                thumbnailUrl
                            ).firstOrNull { key -> memCache[MemoryCache.Key(key)] != null }
                        } else {
                            null
                        }
                        ImageRequest.Builder(context)
                            .data(MonitorCacheManager.coilDataModel(thumbnailUrl))
                            .size(1280, 720)
                            .precision(Precision.INEXACT)
                            .allowHardware(true)
                            .crossfade(false)
                            .memoryCacheKey("${thumbnailUrl}_1280x720")
                            .apply {
                                if (!thumbnailUrl.startsWith("file://")) {
                                    diskCacheKey(thumbnailUrl.substringBefore('?'))
                                }
                                if (placeholderKey != null) {
                                    placeholderMemoryCacheKey(placeholderKey)
                                }
                            }
                            .build()
                    }
                    AsyncImage(
                        model = thumbRequest,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = if (isOriginalRatio) ContentScale.Fit else ContentScale.FillBounds
                    )
                }
                if (playWhenReady) {
                    WaveDotsLoading(dotColor = colors.accent)
                }
            }
        }
    }
}

@Composable
private fun VideoZoomResetBadge(
    scaleProvider: () -> Float,
    isVisibleProvider: () -> Boolean,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visible by remember {
        derivedStateOf { isVisibleProvider() }
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(160)),
        exit = fadeOut(animationSpec = tween(160)),
        modifier = modifier
    ) {
        ZoomResetPill(
            scale = scaleProvider(),
            onClick = onReset
        )
    }
}