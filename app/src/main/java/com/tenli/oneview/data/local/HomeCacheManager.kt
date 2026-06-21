package com.tenli.oneview.data.local

import android.content.Context
import com.google.gson.Gson
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByCameraModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.model.network.VmsEventStatisticalOverTimeModel
import com.tenli.oneview.ui.features.home.TimeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CachedHomeData(
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsOverTime: List<VmsEventStatisticalOverTimeModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val eventsByCamera: List<VmsEventCountByCameraModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList()
)

object HomeCacheManager {

    private val gson = Gson()

    private fun getCacheFile(context: Context, filter: TimeFilter): File {
        return File(context.cacheDir, "home_cache_${filter.name}.json")
    }

    suspend fun saveHomeData(context: Context, filter: TimeFilter, data: CachedHomeData) {
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

    suspend fun getHomeData(context: Context, filter: TimeFilter): CachedHomeData? {
        return withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter)
                if (file.exists()) {
                    val json = file.readText()
                    gson.fromJson(json, CachedHomeData::class.java)
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
                if (file.name.startsWith("home_cache_")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
