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

    private fun getCacheFile(context: Context, filter: TimeFilter, serviceId: Int?, aiType: String?): File {
        val servicePart = serviceId?.toString() ?: "all"
        val aiTypePart = aiType ?: "all"
        return File(context.cacheDir, "event_cache_${filter.name}_${servicePart}_${aiTypePart}.json")
    }

    suspend fun saveEventData(context: Context, filter: TimeFilter, serviceId: Int?, aiType: String?, data: CachedEventData) {
        withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter, serviceId, aiType)
                val json = gson.toJson(data)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getEventData(context: Context, filter: TimeFilter, serviceId: Int?, aiType: String?): CachedEventData? {
        return withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter, serviceId, aiType)
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

    suspend fun findEventById(context: Context, eventId: Int): Pair<EventData, List<CameraModel>>? {
        return withContext(Dispatchers.IO) {
            try {
                val files = context.cacheDir.listFiles() ?: return@withContext null
                for (file in files) {
                    if (file.name.startsWith("event_cache_")) {
                        val json = file.readText()
                        val data = gson.fromJson(json, CachedEventData::class.java)
                        val event = data.events.find { it.id == eventId }
                        if (event != null) {
                            return@withContext Pair(event, data.cameraList)
                        }
                    }
                }
                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
