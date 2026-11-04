package com.tenli.oneview.ui.component


import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.tenli.oneview.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@SuppressLint("InflateParams")
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    thumbnailUrl: String?,
    deviceKey: String,
    initialSeekPositionMs: Long? = null,
    errorText: String = "Không có video sự kiện",
    onPlayingChange: (Boolean) -> Unit = {},
    onErrorChange: (Boolean) -> Unit = {},
    onTimeUpdate: ((Long) -> Unit)? = null
) {
    var isError by remember(videoUrl) { mutableStateOf(false) }
    val context = LocalContext.current
    var isVideoReady by remember(videoUrl) { mutableStateOf(false) }
    val isDark = rememberIsDarkTheme()
    val containerBg = if (isDark) Color(0xFF1E293B) else Color(0xFFE8ECF2)

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            val dataSourceFactory = DefaultHttpDataSource.Factory().setDefaultRequestProperties(mapOf("Authorization" to "Bearer $deviceKey"))
            setMediaSource(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(dataSourceFactory).createMediaSource(MediaItem.fromUri(videoUrl))
            )
            if (initialSeekPositionMs != null) {
                seekTo(initialSeekPositionMs)
            }
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    isVideoReady = true; isError = false
                    onPlayingChange(true)
                    onErrorChange(false)
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.util.Log.e("VideoPlayer", "ExoPlayer Error: ${error.message}", error)
                    isError = true; isVideoReady = false
                    onPlayingChange(false)
                    onErrorChange(true)
                }
            })
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, exoPlayer) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                exoPlayer.pause()
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                exoPlayer.play()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (onTimeUpdate != null) {
        androidx.compose.runtime.LaunchedEffect(exoPlayer, isVideoReady, onTimeUpdate) {
            while (isActive && isVideoReady) {
                if (exoPlayer.isPlaying) {
                    onTimeUpdate(exoPlayer.currentPosition)
                }
                delay(250L)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(containerBg)
    ) {
        AndroidView(factory = { ctx ->
            android.view.LayoutInflater.from(ctx).inflate(R.layout.view_exo_texture, null).apply {
                (this as PlayerView).apply {
                    player = exoPlayer
                    useController = true
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            }
        }, update = { view ->
            (view as PlayerView).player = exoPlayer
        }, onRelease = { view ->
            (view as? PlayerView)?.player = null
        }, modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = if (isVideoReady && !isError) 1f else 0f })

        if (isError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                CommonEmptyState(
                    text = errorText,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else if (!isVideoReady) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(containerBg),
                contentAlignment = Alignment.Center
            ) {
                if (!thumbnailUrl.isNullOrBlank()) {
                    SafeAsyncImage(
                        url = thumbnailUrl,
                        deviceKey = deviceKey,
                        showLoading = false
                    )
                }
                WaveDotsLoading()
            }
        }
    }
}