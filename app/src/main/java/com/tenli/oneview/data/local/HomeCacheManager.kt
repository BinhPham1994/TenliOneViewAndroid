package com.tenli.oneview.data.local

import android.content.Context
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.ui.features.home.TimeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

data class CachedHomeData(
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
    val runningAiTasks: Int = 0,
    val totalAiTasks: Int = 0,
    val updatedAt: Long = 0L
)

object HomeCacheManager {

    internal const val HOME_CACHE_PREFIX = "home_cache_"

    private val gson get() = LoginAuthClient.gson

    // RAM Cache giúp hiển thị tức thì (0ms) ngay từ frame đầu tiên khi mở app hoặc chuyển bộ lọc
    private val inMemoryHomeCache = ConcurrentHashMap<String, CachedHomeData>()
    private val diskReadMutex = Mutex()

    private fun cacheKey(filter: TimeFilter, serviceId: Int?): String {
        val servicePart = serviceId?.toString() ?: "all"
        return "${TimeFilter.cacheKeyToken(filter)}_${servicePart}"
    }

    private fun getCacheFile(context: Context, filter: TimeFilter, serviceId: Int?): File {
        return File(context.cacheDir, "$HOME_CACHE_PREFIX${cacheKey(filter, serviceId)}.json")
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
            JsonFileStore.write(gson, getCacheFile(context, filter, serviceId), stamped)
        }
    }

    suspend fun getHomeData(context: Context, filter: TimeFilter, serviceId: Int?): CachedHomeData? {
        val key = cacheKey(filter, serviceId)
        inMemoryHomeCache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            diskReadMutex.withLock {
                inMemoryHomeCache[key]?.let { return@withContext it }
                JsonFileStore.read(gson, getCacheFile(context, filter, serviceId), CachedHomeData::class.java)?.also { parsed ->
                    inMemoryHomeCache[key] = parsed
                    EventCacheManager.cacheInMemory(parsed.recentEvents, parsed.cameraList, parsed.aiServices)
                }
            }
        }
    }

    fun clearCache(context: Context) {
        inMemoryHomeCache.clear()
        JsonFileStore.deleteMatching(context.cacheDir) { it.startsWith(HOME_CACHE_PREFIX) }
    }
}
