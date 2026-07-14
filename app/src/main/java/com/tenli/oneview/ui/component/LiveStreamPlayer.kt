package com.tenli.oneview.ui.component

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorInput
import androidx.media3.extractor.ExtractorOutput
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.PositionHolder
import androidx.media3.extractor.mp4.FragmentedMp4Extractor
import androidx.media3.ui.PlayerView
import com.tenli.oneview.data.local.UserSession
import okhttp3.OkHttpClient

/**
 * Wrapper around FragmentedMp4Extractor that skips format sniffing.
 *
 * WebSocket fMP4 streams often don't start with a standard 'ftyp' box,
 * causing ExoPlayer's normal sniffing to fail with "NoDeclaredBrand".
 * This wrapper forces the use of FragmentedMp4Extractor regardless.
 */
@UnstableApi
private class NoSniffFragmentedMp4Extractor : Extractor {
    private val delegate = FragmentedMp4Extractor()

    override fun sniff(input: ExtractorInput): Boolean = true
    override fun init(output: ExtractorOutput) = delegate.init(output)
    override fun read(input: ExtractorInput, seekPosition: PositionHolder): Int =
        delegate.read(input, seekPosition)
    override fun seek(position: Long, timeUs: Long) = delegate.seek(position, timeUs)
    override fun release() = delegate.release()
}

/**
 * Native live stream player for WebSocket fMP4 streams (H.264 / H.265).
 *
 * Uses ExoPlayer with a custom [WebSocketDataSource] for transport
 * and a forced [FragmentedMp4Extractor] (no sniff) for demuxing.
 * Android's hardware HEVC decoder handles H.265 natively.
 */
@OptIn(UnstableApi::class)
@Composable
fun LiveStreamPlayer(
    wsUrl: String,
    videoCodecTag: String,
    onPlayingChange: (Boolean) -> Unit = {},
    onErrorChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isLifecycleActive by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START || event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isLifecycleActive = true
            } else if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                isLifecycleActive = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (!isLifecycleActive) {
        return // Component will be destroyed, saving battery and network
    }

    val exoPlayer = remember(wsUrl) {
        val wsClient = OkHttpClient.Builder()
            .protocols(listOf(okhttp3.Protocol.HTTP_1_1))
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(0, java.util.concurrent.TimeUnit.SECONDS)
            .build()
            
        val dataSourceFactory = DataSource.Factory {
            WebSocketDataSource(wsClient, UserSession.accessToken)
        }

        val extractorsFactory = ExtractorsFactory {
            arrayOf(NoSniffFragmentedMp4Extractor())
        }

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory, extractorsFactory)
            .setLoadErrorHandlingPolicy(object : androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy {
                override fun getRetryDelayMsFor(loadErrorInfo: androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy.LoadErrorInfo): Long {
                    return C.TIME_UNSET // Fail fast: do not retry internally with the same burned token
                }
                override fun getMinimumLoadableRetryCount(dataType: Int): Int = 0
                override fun getFallbackSelectionFor(
                    fallbackOptions: androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy.FallbackOptions,
                    loadErrorInfo: androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy.LoadErrorInfo
                ): androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy.FallbackSelection? = null
            })
            .createMediaSource(MediaItem.fromUri(wsUrl))

        ExoPlayer.Builder(context).build().apply {
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            volume = 0f // Muted – same as web player
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    isPlaying = true
                    isError = false
                    onPlayingChange(true)
                    onErrorChange(false)
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("LiveStreamPlayer", "ExoPlayer error: ${error.message}", error)
                    isError = true
                    isPlaying = false
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

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                android.view.LayoutInflater.from(ctx).inflate(com.tenli.oneview.R.layout.view_exo_texture, null).apply {
                    (this as PlayerView).apply {
                        player = exoPlayer
                        useController = false
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
                    }
                }
            },
            update = { view -> (view as PlayerView).player = exoPlayer },
            onRelease = { view -> (view as? PlayerView)?.player = null },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = if (isPlaying) 1f else 0f }
        )

        if (!isPlaying) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                if (isError) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.Videocam,
                            contentDescription = "Lỗi phát",
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                        androidx.compose.material3.Text(
                            text = "Không thể phát video",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
