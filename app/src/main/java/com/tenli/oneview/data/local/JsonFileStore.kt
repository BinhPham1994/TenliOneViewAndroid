package com.tenli.oneview.data.local

import android.util.Log
import com.google.gson.Gson
import com.tenli.oneview.BuildConfig
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

/**
 * Đọc/ghi cache JSON ra đĩa dùng chung cho Home/Event/Monitor cache managers.
 *
 * - Streaming (Reader/Writer) thay vì `readText()/writeText(toJson())` → không tạo chuỗi JSON khổng lồ
 *   trong RAM (giảm GC, giảm giật).
 * - Ghi nguyên tử + khóa đồng bộ theo từng đường dẫn file: ghi ra file tạm rồi rename → không bao giờ
 *   bị xung đột `.tmp` khi nhiều luồng ghi cùng lúc hoặc để lại file hỏng nếu app bị kill giữa chừng.
 * - Phải gọi trên Dispatchers.IO.
 */
internal object JsonFileStore {
    private const val TAG = "JsonFileStore"
    private val fileLocks = ConcurrentHashMap<String, Any>()

    private fun lockFor(file: File): Any =
        fileLocks.computeIfAbsent(file.absolutePath) { Any() }

    fun <T> read(gson: Gson, file: File, type: Type): T? = synchronized(lockFor(file)) {
        if (!file.exists()) return null
        return try {
            file.bufferedReader().use { reader -> gson.fromJson<T>(reader, type) }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Read ${file.name} failed: ${e.message}")
            null
        }
    }

    fun <T> read(gson: Gson, file: File, clazz: Class<T>): T? = read(gson, file, clazz as Type)

    fun write(gson: Gson, file: File, data: Any) = synchronized(lockFor(file)) {
        file.parentFile?.let { parent -> if (!parent.exists()) parent.mkdirs() }
        val tmp = File(file.parentFile, "${file.name}.tmp")
        try {
            tmp.bufferedWriter().use { writer -> gson.toJson(data, writer) }
            if (!tmp.renameTo(file)) {
                // Một số hệ thống file không cho rename đè → xóa rồi rename lại
                file.delete()
                tmp.renameTo(file)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.w(TAG, "Write ${file.name} failed: ${e.message}")
            tmp.delete()
        }
    }

    /** Xóa các file trong [dir] có tên thỏa [predicate] */
    fun deleteMatching(dir: File, predicate: (String) -> Boolean) {
        runCatching {
            dir.listFiles()?.forEach { if (predicate(it.name)) it.delete() }
        }
    }
}

