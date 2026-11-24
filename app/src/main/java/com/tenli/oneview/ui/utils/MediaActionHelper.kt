package com.tenli.oneview.ui.utils

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import coil.Coil
import coil.request.ImageRequest
import coil.size.Precision
import com.tenli.oneview.data.local.MonitorCacheManager
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.ui.component.MobileTopPopupManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response

/**
 * Tiện ích tải trực tiếp Ảnh/Video sự kiện vào Thư viện thiết bị (MediaStore)
 * và chia sẻ Ảnh sự kiện thực tế (kèm khung nhận diện AI) qua FileProvider.
 */
object MediaActionHelper {

    private data class LoadedBitmap(
        val bitmap: Bitmap,
        val isFreshCopy: Boolean
    )

    private const val GALLERY_SUBFOLDER = "TenliOneView"
    private const val SHARE_CACHE_DIR = "shared_media"
    private const val MAX_BITMAP_DIMENSION_PX = 2560
    private const val JPEG_QUALITY = 94
    private const val STALE_SHARE_FILE_MAX_AGE_MS = 60L * 60L * 1000L

    /** Palette màu bounding box đồng bộ 100% với EventDetailScreen & PWA */
    private val BOUNDING_BOX_COLORS = intArrayOf(
        0xFFFF4D4F.toInt(),
        0xFF38DD0F.toInt(),
        0xFF1890FF.toInt(),
        0xFFFAAD14.toInt(),
        0xFF13C2C2.toInt(),
        0xFFEB2F96.toInt()
    )

    /** Kế thừa connection pool, ImageAuthInterceptor & TokenAuthenticator từ LoginAuthClient */
    private val mediaDownloadClient by lazy {
        LoginAuthClient.imageClient.newBuilder()
            .connectTimeout(20L, TimeUnit.SECONDS)
            .readTimeout(90L, TimeUnit.SECONDS)
            .build()
    }

    private suspend fun Call.awaitResponse(): Response = suspendCancellableCoroutine { cont ->
        cont.invokeOnCancellation {
            runCatching { cancel() }
        }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (cont.isCancelled) return
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (cont.isCancelled) {
                    runCatching { response.close() }
                    return
                }
                cont.resume(response) { _, value, _ ->
                    runCatching { value.close() }
                }
            }
        })
    }

    /**
     * Tải ảnh sự kiện (có vẽ sẵn bounding box AI nếu có) trực tiếp vào Album Ảnh của điện thoại.
     */
    suspend fun downloadEventImageToGallery(
        context: Context,
        imageUrl: String,
        boxes: List<List<Float>>,
        eventId: Int,
        isEn: Boolean
    ) {
        val appContext = context.applicationContext
        val saved = withContext(Dispatchers.IO) {
            try {
                val loaded = loadAnnotatedBitmap(appContext, imageUrl, boxes)
                    ?: return@withContext false
                try {
                    val fileName = "Tenli_Event_${eventId}_${System.currentTimeMillis()}.jpg"
                    saveBitmapToGallery(appContext, loaded.bitmap, fileName)
                } finally {
                    if (loaded.isFreshCopy && !loaded.bitmap.isRecycled) {
                        loaded.bitmap.recycle()
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Throwable) {
                false
            }
        }
        if (saved) {
            MobileTopPopupManager.success(
                if (isEn) "Saved photo to Gallery" else "Đã lưu ảnh vào Thư viện"
            )
        } else {
            MobileTopPopupManager.error(
                if (isEn) "Failed to save photo" else "Không thể tải ảnh về máy"
            )
        }
    }

    /**
     * Tải video sự kiện (MP4) có xác thực trực tiếp vào Thư viện Video của điện thoại.
     */
    suspend fun downloadEventVideoToGallery(
        context: Context,
        videoUrl: String,
        eventId: Int,
        isEn: Boolean
    ) {
        val appContext = context.applicationContext
        MobileTopPopupManager.info(
            if (isEn) "Downloading event video..." else "Đang tải video sự kiện..."
        )
        val saved = withContext(Dispatchers.IO) {
            val callerJob = currentCoroutineContext()[Job]
            val req = Request.Builder().url(videoUrl).get().build()
            val call = mediaDownloadClient.newCall(req)
            try {
                call.awaitResponse().use { response ->
                    if (!response.isSuccessful) {
                        error("HTTP ${response.code}")
                    }
                    val body = response.body ?: error("Empty body")
                    val fileName = "Tenli_Video_${eventId}_${System.currentTimeMillis()}.mp4"
                    saveStreamToGalleryVideo(appContext, fileName) { out ->
                        body.byteStream().use { input ->
                            val buffer = ByteArray(32 * 1024)
                            while (true) {
                                if (callerJob?.isActive == false) {
                                    runCatching { call.cancel() }
                                    throw CancellationException("Video download cancelled")
                                }
                                val read = input.read(buffer)
                                if (read == -1) break
                                out.write(buffer, 0, read)
                            }
                        }
                    }
                }
            } catch (e: CancellationException) {
                runCatching { call.cancel() }
                throw e
            } catch (_: Throwable) {
                if (callerJob?.isActive == false) {
                    throw CancellationException("Video download cancelled")
                }
                false
            }
        }
        if (saved) {
            MobileTopPopupManager.success(
                if (isEn) "Saved video to Gallery" else "Đã lưu video vào Thư viện"
            )
        } else {
            MobileTopPopupManager.error(
                if (isEn) "Failed to download video" else "Không thể tải video về máy"
            )
        }
    }

    /**
     * Chia sẻ sự kiện kèm file ảnh thật (đã vẽ khung nhận diện AI) qua Zalo/Telegram/Messenger/Email...
     * Tự động fallback về chia sẻ văn bản nếu sự kiện không có ảnh hoặc mạng lỗi.
     */
    suspend fun shareEventWithMedia(
        context: Context,
        imageUrl: String?,
        boxes: List<List<Float>>,
        eventId: Int,
        subjectText: String,
        shareText: String,
        chooserTitle: String
    ) {
        val shareUri = if (!imageUrl.isNullOrBlank()) {
            withContext(Dispatchers.IO) {
                try {
                    val loaded = loadAnnotatedBitmap(context.applicationContext, imageUrl, boxes)
                        ?: return@withContext null
                    try {
                        val shareDir = File(context.cacheDir, SHARE_CACHE_DIR).apply { mkdirs() }
                        val now = System.currentTimeMillis()
                        shareDir.listFiles()?.forEach { f ->
                            if (now - f.lastModified() > STALE_SHARE_FILE_MAX_AGE_MS) {
                                runCatching { f.delete() }
                            }
                        }
                        val outFile = File(shareDir, "tenli_event_${eventId}_$now.jpg")
                        FileOutputStream(outFile).use { out ->
                            loaded.bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                            out.flush()
                        }
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            outFile
                        )
                    } finally {
                        if (loaded.isFreshCopy && !loaded.bitmap.isRecycled) {
                            loaded.bitmap.recycle()
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Throwable) {
                    null
                }
            }
        } else {
            null
        }

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            if (shareUri != null) {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                putExtra(Intent.EXTRA_SUBJECT, subjectText)
                putExtra(Intent.EXTRA_TEXT, shareText)
                clipData = ClipData.newRawUri(subjectText, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subjectText)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
        }

        val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        runCatching {
            context.startActivity(chooser)
        }
    }

    private suspend fun loadAnnotatedBitmap(
        context: Context,
        imageUrl: String,
        boxes: List<List<Float>>
    ): LoadedBitmap? {
        val request = ImageRequest.Builder(context)
            .data(MonitorCacheManager.coilDataModel(imageUrl))
            .size(MAX_BITMAP_DIMENSION_PX, MAX_BITMAP_DIMENSION_PX)
            .precision(Precision.INEXACT)
            .allowHardware(false)
            .allowRgb565(false)
            .apply {
                if (!imageUrl.startsWith("file://")) {
                    diskCacheKey(imageUrl.substringBefore('?'))
                }
            }
            .build()

        val result = Coil.imageLoader(context).execute(request)
        val rawBitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return null
        if (rawBitmap.width <= 0 || rawBitmap.height <= 0) return null

        val validBoxes = boxes.filter { it.size == 4 }
        if (validBoxes.isEmpty() && rawBitmap.config == Bitmap.Config.ARGB_8888) {
            return LoadedBitmap(rawBitmap, isFreshCopy = false)
        }

        val mutable = rawBitmap.copy(Bitmap.Config.ARGB_8888, true)
            ?: return LoadedBitmap(rawBitmap, isFreshCopy = false)
        try {
            if (validBoxes.isNotEmpty()) {
                val canvas = Canvas(mutable)
                val w = mutable.width.toFloat()
                val h = mutable.height.toFloat()
                val strokePx = (maxOf(w, h) * 0.0038f).coerceIn(3f, 12f)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = strokePx
                }
                val rect = RectF()
                validBoxes.forEachIndexed { index, boxArr ->
                    val minX = (boxArr[0].coerceIn(0f, 1f)) * w
                    val minY = (boxArr[1].coerceIn(0f, 1f)) * h
                    val maxX = (boxArr[2].coerceIn(0f, 1f)) * w
                    val maxY = (boxArr[3].coerceIn(0f, 1f)) * h
                    if (maxX > minX && maxY > minY) {
                        paint.color = BOUNDING_BOX_COLORS[index % BOUNDING_BOX_COLORS.size]
                        rect.set(minX, minY, maxX, maxY)
                        canvas.drawRect(rect, paint)
                    }
                }
            }
            return LoadedBitmap(mutable, isFreshCopy = true)
        } catch (t: Throwable) {
            if (!mutable.isRecycled) {
                mutable.recycle()
            }
            throw t
        }
    }

    private fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_PICTURES}/$GALLERY_SUBFOLDER"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            try {
                val ok = resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                } ?: false
                if (!ok) {
                    resolver.delete(uri, null, null)
                    return false
                }
                val doneValues = ContentValues().apply {
                    put(MediaStore.Images.Media.IS_PENDING, 0)
                }
                resolver.update(uri, doneValues, null, null)
                true
            } catch (e: Throwable) {
                runCatching { resolver.delete(uri, null, null) }
                false
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                GALLERY_SUBFOLDER
            ).apply { mkdirs() }
            val outFile = File(dir, fileName)
            val ok = FileOutputStream(outFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            if (ok) {
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(outFile.absolutePath),
                    arrayOf("image/jpeg"),
                    null
                )
            }
            ok
        }
    }

    private fun saveStreamToGalleryVideo(
        context: Context,
        fileName: String,
        writer: (java.io.OutputStream) -> Unit
    ): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "${Environment.DIRECTORY_MOVIES}/$GALLERY_SUBFOLDER"
                )
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            try {
                val stream = resolver.openOutputStream(uri) ?: run {
                    resolver.delete(uri, null, null)
                    return false
                }
                stream.use { out ->
                    writer(out)
                    out.flush()
                }
                val doneValues = ContentValues().apply {
                    put(MediaStore.Video.Media.IS_PENDING, 0)
                }
                resolver.update(uri, doneValues, null, null)
                true
            } catch (e: Throwable) {
                runCatching { resolver.delete(uri, null, null) }
                false
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                GALLERY_SUBFOLDER
            ).apply { mkdirs() }
            val outFile = File(dir, fileName)
            return try {
                FileOutputStream(outFile).use { out ->
                    writer(out)
                    out.flush()
                }
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(outFile.absolutePath),
                    arrayOf("video/mp4"),
                    null
                )
                true
            } catch (e: Throwable) {
                runCatching { outFile.delete() }
                false
            }
        }
    }
}
