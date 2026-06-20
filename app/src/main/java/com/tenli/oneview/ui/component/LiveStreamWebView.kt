package com.tenli.oneview.ui.component

import android.annotation.SuppressLint
import android.graphics.Color
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveStreamWebView(
    wsUrl: String,
    videoCodecTag: String,
    onPlayingChange: (Boolean) -> Unit = {},
    onErrorChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    val htmlContent = remember(wsUrl, videoCodecTag) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                body, html {
                    margin: 0; padding: 0; width: 100%; height: 100%; background-color: transparent; overflow: hidden;
                }
                .video-container {
                    position: relative; width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;
                }
                video {
                    width: 100%; height: 100%; object-fit: contain; display: block;
                }
                /* Hide controls */
                video::-webkit-media-controls-play-button { display: none; }
                video::-webkit-media-controls-timeline { display: none; }
                video::-webkit-media-controls-current-time-display { display: none; }
                video::-webkit-media-controls-mute-button { display: none; }
            </style>
        </head>
        <body>
            <div class="video-container">
                <video id="video" muted autoplay playsinline></video>
            </div>
            <script>
                const video = document.getElementById('video');
                let ws;
                
                class Player {
                    constructor(node, wsUrl, codecTag) {
                        this.node = node;
                        this.wsUrl = wsUrl;
                        this.codecTag = codecTag;
                        this.mediaSource = new window.MediaSource();
                        this.url = URL.createObjectURL(this.mediaSource);
                        this.node.src = this.url;
                        this.sourceBuffer = null;
                        this.cache = [];
                        this.MAX_CACHE_SIZE = 100;

                        this.mediaSource.addEventListener('sourceopen', () => {
                            try {
                                this.sourceBuffer = this.mediaSource.addSourceBuffer(`video/mp4; codecs="${'$'}{this.codecTag}"`);
                                this.sourceBuffer.addEventListener('updateend', () => {
                                    this.processCache();
                                    this.manageBuffer();
                                    this.syncTimeline();
                                });
                            } catch (e) {
                                console.error('Add SourceBuffer error:', e);
                            }
                        });
                    }

                    syncTimeline() {
                        if (this.node.buffered.length === 0) return;
                        const end = this.node.buffered.end(this.node.buffered.length - 1);
                        const drift = end - this.node.currentTime;
                        if (drift > 4) {
                            this.node.currentTime = end - 0.5;
                            this.node.playbackRate = 1.0;
                        } else if (drift > 1.5) {
                            this.node.playbackRate = 1.1;
                        } else if (drift < 1.0) {
                            this.node.playbackRate = 1.0;
                        }
                    }

                    manageBuffer() {
                        if (this.sourceBuffer && !this.sourceBuffer.updating) {
                            const buffered = this.sourceBuffer.buffered;
                            if (buffered.length > 0) {
                                const start = buffered.start(0);
                                const end = buffered.end(0);
                                if (end - start > 40) {
                                    const removeEnd = this.node.currentTime - 10;
                                    if (removeEnd > start) {
                                        try {
                                            this.sourceBuffer.remove(start, removeEnd);
                                        } catch (e) { }
                                    }
                                }
                            }
                        }
                    }

                    processCache() {
                        if (this.sourceBuffer && !this.sourceBuffer.updating && this.cache.length > 0) {
                            const nextBatch = this.cache.shift();
                            if (nextBatch) {
                                try {
                                    this.sourceBuffer.appendBuffer(nextBatch);
                                } catch (e) {
                                    if (e.name === 'QuotaExceededError') {
                                        this.manageBuffer();
                                        this.cache.unshift(nextBatch);
                                    }
                                }
                            }
                        }
                    }

                    feed(fragment) {
                        if (this.sourceBuffer && this.mediaSource.readyState === 'open') {
                            const cleanFragment = new Uint8Array(fragment);
                            if (this.sourceBuffer.updating || this.cache.length > 0) {
                                if (this.cache.length > this.MAX_CACHE_SIZE) {
                                    this.cache.splice(0, 50);
                                }
                                this.cache.push(cleanFragment);
                            } else {
                                try {
                                    this.sourceBuffer.appendBuffer(cleanFragment);
                                } catch (e) {
                                    this.cache.push(cleanFragment);
                                }
                            }
                        }
                    }

                    destroy() {
                        this.node.pause();
                        if (this.sourceBuffer) {
                            try {
                                this.sourceBuffer.abort();
                                if (this.sourceBuffer.buffered.length > 0) {
                                    const start = this.sourceBuffer.buffered.start(0);
                                    const end = this.sourceBuffer.buffered.end(0);
                                    this.sourceBuffer.remove(start, end);
                                }
                                this.mediaSource.removeSourceBuffer(this.sourceBuffer);
                            } catch (e) { }
                        }
                        if (this.mediaSource.readyState === 'open') {
                            try { this.mediaSource.endOfStream(); } catch (e) { }
                        }
                        URL.revokeObjectURL(this.url);
                        this.node.removeAttribute('src');
                        this.node.load();
                        this.cache = [];
                    }
                }

                let player;
                let reconnectAttempts = 0;
                let reconnectTimer = null;
                const MAX_RECONNECT = 5;
                let intentionalClose = false;

                function load() {
                    intentionalClose = false;
                    player = new Player(video, "$wsUrl", "$videoCodecTag");

                    ws = new WebSocket("$wsUrl");
                    ws.binaryType = 'arraybuffer';
                    ws.addEventListener('open', () => {
                        reconnectAttempts = 0;
                        if (window.Android) window.Android.onPlay();
                    });
                    ws.addEventListener('message', (event) => {
                        player.feed(new Uint8Array(event.data));
                    });
                    ws.addEventListener('error', () => {
                        if (window.Android) window.Android.onError();
                        if (!intentionalClose) attemptReconnect();
                    });
                    ws.addEventListener('close', () => {
                        if (!intentionalClose) attemptReconnect();
                    });
                }

                function unload() {
                    if (player) {
                        player.destroy();
                        player = null;
                    }
                    if (ws) {
                        intentionalClose = true;
                        ws.close();
                        ws = null;
                    }
                }

                function attemptReconnect() {
                    if (intentionalClose || reconnectAttempts >= MAX_RECONNECT) return;
                    reconnectAttempts++;
                    const delay = Math.min(1000 * Math.pow(2, reconnectAttempts - 1), 10000);
                    reconnectTimer = setTimeout(() => {
                        unload();
                        load();
                    }, delay);
                }

                document.addEventListener("DOMContentLoaded", () => {
                    load();
                });
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                setBackgroundColor(Color.TRANSPARENT)
                settings.apply {
                    javaScriptEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    domStorageEnabled = true
                    allowFileAccess = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onPlay() {
                        onPlayingChange(true)
                        onErrorChange(false)
                    }
                    @android.webkit.JavascriptInterface
                    fun onError() {
                        onErrorChange(true)
                        onPlayingChange(false)
                    }
                }, "Android")
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            // If URL changes, load the new content. Handled by `remember` triggering recomposition if params change.
        }
    )
}
