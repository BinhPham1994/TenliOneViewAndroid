package com.tenli.oneview.data.local

import android.content.Context
import com.google.gson.Gson
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CachedEventData(
    val events: List<EventData> = emptyList(),
    val cameraList: List<CameraModel> = emptyList()
)

object EventCacheManager {

    private val gson = Gson()

    private fun getCacheFile(context: Context, filter: TimeFilter): File {
        return File(context.cacheDir, "event_cache_${filter.name}.json")
    }

    suspend fun saveEventData(context: Context, filter: TimeFilter, data: CachedEventData) {
        withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter)
                val json = gson.toJson(data)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getEventData(context: Context, filter: TimeFilter): CachedEventData? {
        return withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter)
                if (file.exists()) {
                    val json = file.readText()
                    gson.fromJson(json, CachedEventData::class.java)
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun clearCache(context: Context) {
        try {
            val files = context.cacheDir.listFiles() ?: return
            for (file in files) {
                if (file.name.startsWith("event_cache_")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
