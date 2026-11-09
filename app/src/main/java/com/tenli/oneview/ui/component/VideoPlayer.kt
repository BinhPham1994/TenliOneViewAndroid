package com.tenli.oneview.ui.component


import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.appColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

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
    onTimeUpdate: ((Long) -> Unit)? = null
) {
    var isError by remember(videoUrl) { mutableStateOf(false) }
    val context = LocalContext.current
    var isVideoReady by remember(videoUrl) { mutableStateOf(false) }
    var isActuallyPlaying by remember(videoUrl) { mutableStateOf(false) }
    var playerViewRef by remember(videoUrl) { mutableStateOf<PlayerView?>(null) }
    val suppressPlayWhenReadyCallback = remember(videoUrl) { booleanArrayOf(false) }
    val colors = MaterialTheme.appColors
    val containerBg = colors.surface
    val stageBg = when {
        isVideoReady && !isError -> colors.mediaBg
        showLoadingOverlay -> containerBg
        else -> Color.Transparent
    }
    val currentPlayWhenReady by rememberUpdatedState(playWhenReady)
    val currentOnPlayingChange by rememberUpdatedState(onPlayingChange)
    val currentOnPlayWhenReadyChange by rememberUpdatedState(onPlayWhenReadyChange)
    val currentOnErrorChange by rememberUpdatedState(onErrorChange)
    val currentOnControllerVisibilityChange by rememberUpdatedState(onControllerVisibilityChange)
    val currentOnFullscreenToggle by rememberUpdatedState(onFullscreenToggle)

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
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setConnectTimeoutMs(10_000)
                    .setReadTimeoutMs(15_000)
                    .setAllowCrossProtocolRedirects(true)
                    .setDefaultRequestProperties(mapOf("Authorization" to "Bearer $deviceKey"))
                setMediaSource(
                    androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(videoUrl))
                )
                if (initialSeekPositionMs != null) {
                    seekTo(initialSeekPositionMs)
                }
                addListener(playerListener)
                suppressPlayWhenReadyCallback[0] = true
                try {
                    this.playWhenReady = currentPlayWhenReady
                } finally {
                    suppressPlayWhenReadyCallback[0] = false
                }
                if (currentPlayWhenReady) {
                    prepare()
                }
            }
    }

    androidx.compose.runtime.LaunchedEffect(exoPlayer, playWhenReady) {
        suppressPlayWhenReadyCallback[0] = true
        try {
            if (playWhenReady) {
                if (exoPlayer.playbackState == Player.STATE_IDLE) {
                    exoPlayer.prepare()
                } else if (exoPlayer.playbackState == Player.STATE_ENDED) {
                    exoPlayer.seekTo(0)
                }
                if (!exoPlayer.playWhenReady) {
                    exoPlayer.play()
                }
            } else {
                if (exoPlayer.playWhenReady) {
                    exoPlayer.pause()
                }
            }
        } finally {
            suppressPlayWhenReadyCallback[0] = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            isActuallyPlaying = false
            exoPlayer.removeListener(playerListener)
            playerViewRef?.player = null
            playerViewRef = null
            exoPlayer.clearVideoSurface()
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                suppressPlayWhenReadyCallback[0] = true
                try {
                    exoPlayer.pause()
                } finally {
                    suppressPlayWhenReadyCallback[0] = false
                }
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                if (currentPlayWhenReady && exoPlayer.playbackState != Player.STATE_ENDED) {
                    suppressPlayWhenReadyCallback[0] = true
                    try {
                        if (exoPlayer.playbackState == Player.STATE_IDLE) {
                            exoPlayer.prepare()
                        }
                        exoPlayer.play()
                    } finally {
                        suppressPlayWhenReadyCallback[0] = false
                    }
                }
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
            while (isActive && isActuallyPlaying) {
                currentOnTimeUpdate?.invoke(exoPlayer.currentPosition.coerceAtLeast(0L))
                delay(250L)
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(isVideoReady, isError, showControllerInitially) {
        if (isVideoReady && !isError) {
            if (showControllerInitially) {
                playerViewRef?.showController()
                currentOnControllerVisibilityChange(true)
            } else {
                playerViewRef?.hideController()
                currentOnControllerVisibilityChange(false)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(stageBg)
    ) {
        val targetResizeMode = if (isOriginalRatio) {
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
        } else {
            androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
        }
        AndroidView(factory = { ctx ->
            android.view.LayoutInflater.from(ctx).inflate(R.layout.view_exo_texture, null).apply {
                (this as PlayerView).apply {
                    playerViewRef = this
                    useController = true
                    controllerAutoShow = showControllerInitially
                    controllerHideOnTouch = true
                    controllerShowTimeoutMs = 3000
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    resizeMode = targetResizeMode
                    setControllerVisibilityListener(
                        PlayerView.ControllerVisibilityListener { visibility ->
                            currentOnControllerVisibilityChange(visibility == android.view.View.VISIBLE)
                        }
                    )
                    player = exoPlayer
                    if (!showControllerInitially) {
                        hideController()
                    }
                    if (currentOnFullscreenToggle != null) {
                        setFullscreenButtonClickListener(
                            PlayerView.FullscreenButtonClickListener {
                                currentOnFullscreenToggle?.invoke()
                            }
                        )
                        setFullscreenButtonState(isFullscreen)
                    } else {
                        setFullscreenButtonClickListener(null as PlayerView.FullscreenButtonClickListener?)
                    }
                }
            }
        }, update = { view ->
            (view as PlayerView).let {
                playerViewRef = it
                it.controllerAutoShow = showControllerInitially
                if (it.player !== exoPlayer) {
                    it.player = exoPlayer
                    if (!showControllerInitially) {
                        it.hideController()
                        currentOnControllerVisibilityChange(false)
                    }
                }
                if (it.resizeMode != targetResizeMode) {
                    it.resizeMode = targetResizeMode
                }
                if (currentOnFullscreenToggle != null) {
                    it.setFullscreenButtonState(isFullscreen)
                }
            }
        }, onRelease = { view ->
            if (playerViewRef === view) {
                playerViewRef = null
            }
            (view as? PlayerView)?.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?)
            (view as? PlayerView)?.setFullscreenButtonClickListener(null as PlayerView.FullscreenButtonClickListener?)
            (view as? PlayerView)?.player = null
        }, modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (isVideoReady && !isError) 1f else 0f })

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
                        ImageRequest.Builder(context)
                            .data(thumbnailUrl)
                            .size(1280, 720)
                            .precision(Precision.INEXACT)
                            .allowHardware(true)
                            .crossfade(false)
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