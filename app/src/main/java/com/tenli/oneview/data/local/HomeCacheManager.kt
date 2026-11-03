package com.tenli.oneview.data.local

import android.content.Context
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
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
import java.util.concurrent.ConcurrentHashMap

data class CachedHomeData(
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsOverTime: List<VmsEventStatisticalOverTimeModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val eventsByCamera: List<VmsEventCountByCameraModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<com.tenli.oneview.model.network.AIServiceModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
    val runningAiTasks: Int = 0,
    val totalAiTasks: Int = 0,
    val updatedAt: Long = 0L
)

object HomeCacheManager {

    private val gson get() = LoginAuthClient.gson

    // RAM Cache giúp hiển thị tức thì (0ms) ngay từ frame đầu tiên khi mở app hoặc chuyển bộ lọc
    private val inMemoryHomeCache = ConcurrentHashMap<String, CachedHomeData>()

    private fun cacheKey(filter: TimeFilter, serviceId: Int?): String {
        val servicePart = serviceId?.toString() ?: "all"
        return "${filter.name}_${servicePart}"
    }

    private fun getCacheFile(context: Context, filter: TimeFilter, serviceId: Int?): File {
        return File(context.cacheDir, "home_cache_${cacheKey(filter, serviceId)}.json")
    }

    fun getHomeDataInMemory(filter: TimeFilter, serviceId: Int?): CachedHomeData? {
        return inMemoryHomeCache[cacheKey(filter, serviceId)]
    }

    suspend fun preloadDefaultCache(context: Context) {
        getHomeData(context, TimeFilter.TODAY, null)
    }

    suspend fun saveHomeData(context: Context, filter: TimeFilter, serviceId: Int?, data: CachedHomeData) {
        val stamped = if (data.updatedAt > 0L) data else data.copy(updatedAt = System.currentTimeMillis())
        inMemoryHomeCache[cacheKey(filter, serviceId)] = stamped
        EventCacheManager.cacheInMemory(stamped.recentEvents, stamped.cameraList, stamped.aiServices)
        withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter, serviceId)
                val json = gson.toJson(stamped)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getHomeData(context: Context, filter: TimeFilter, serviceId: Int?): CachedHomeData? {
        val key = cacheKey(filter, serviceId)
        inMemoryHomeCache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter, serviceId)
                if (file.exists()) {
                    val json = file.readText()
                    val parsed = gson.fromJson(json, CachedHomeData::class.java)
                    if (parsed != null) {
                        inMemoryHomeCache[key] = parsed
                        EventCacheManager.cacheInMemory(parsed.recentEvents, parsed.cameraList, parsed.aiServices)
                    }
                    parsed
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
        inMemoryHomeCache.clear()
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


