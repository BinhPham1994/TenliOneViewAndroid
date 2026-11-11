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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    onTimeUpdate: ((Long) -> Unit)? = null
) {
    var isError by remember(videoUrl) { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isVideoReady by remember(videoUrl) { mutableStateOf(false) }
    var isActuallyPlaying by remember(videoUrl) { mutableStateOf(false) }
    val playerViewHolder = remember(videoUrl) { arrayOfNulls<PlayerView>(1) }
    val exoPlayerHolder = remember(videoUrl) { arrayOfNulls<ExoPlayer>(1) }
    val currentTokenHolder = remember(videoUrl) { arrayOf(deviceKey) }
    val hasRetriedAuth = remember(videoUrl) { booleanArrayOf(false) }
    val savedResumePositionMs = remember(videoUrl) { longArrayOf(C.TIME_UNSET) }
    val wasStoppedForBackground = remember(videoUrl) { booleanArrayOf(false) }
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
                    coroutineScope.launch {
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
                    val resumePos = savedResumePositionMs[0]
                    exoPlayer.prepare()
                    if (resumePos != C.TIME_UNSET && resumePos > 0L) {
                        exoPlayer.seekTo(resumePos)
                        savedResumePositionMs[0] = C.TIME_UNSET
                    }
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
            playerViewHolder[0]?.player = null
            playerViewHolder[0] = null
            exoPlayer.clearVideoSurface()
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
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
                        if (exoPlayer.playbackState != Player.STATE_IDLE && exoPlayer.playbackState != Player.STATE_ENDED) {
                            savedResumePositionMs[0] = exoPlayer.currentPosition.coerceAtLeast(0L)
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
                    if (currentPlayWhenReady && exoPlayer.playbackState != Player.STATE_ENDED) {
                        suppressPlayWhenReadyCallback[0] = true
                        try {
                            if (exoPlayer.playbackState == Player.STATE_IDLE) {
                                val resumePos = savedResumePositionMs[0]
                                exoPlayer.prepare()
                                if (resumePos != C.TIME_UNSET && resumePos > 0L) {
                                    exoPlayer.seekTo(resumePos)
                                    savedResumePositionMs[0] = C.TIME_UNSET
                                }
                            }
                            exoPlayer.play()
                        } finally {
                            suppressPlayWhenReadyCallback[0] = false
                        }
                    } else if (stoppedForBg && !currentPlayWhenReady) {
                        isVideoReady = false
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
            while (isActive && isActuallyPlaying) {
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
                    playerViewHolder[0] = this
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
                playerViewHolder[0] = it
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
            if (playerViewHolder[0] === view) {
                playerViewHolder[0] = null
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