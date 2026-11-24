package com.tenli.oneview.data.local

import android.content.Context
import android.util.LruCache
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

data class CachedHomeData(
    val overviewStats: List<VmsCountOverviewModel> = emptyList(),
    val eventsByType: List<VmsEventCountByTypeModel> = emptyList(),
    val hasLoadedEventsByType: Boolean = false,
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val recentEvents: List<EventData> = emptyList(),
    val aiModuleEvents: Map<String, List<EventData>> = emptyMap(),
    val runningAiTasks: Int = 0,
    val totalAiTasks: Int = 0,
    val updatedAt: Long = 0L
)

object HomeCacheManager {

    internal const val HOME_CACHE_PREFIX = "home_cache_"
    private const val MAX_HOME_CACHE_ENTRIES = 12
    private const val MAX_DISK_CACHE_FILES = 20

    private val gson get() = LoginAuthClient.gson

    // RAM Cache LRU giới hạn số bản ghi giúp hiển thị tức thì (0ms) mà không phình bộ nhớ
    private val inMemoryHomeCache = LruCache<String, CachedHomeData>(MAX_HOME_CACHE_ENTRIES)
    private val diskReadMutex = Mutex()

    private fun isCustomTimeFilter(filter: TimeFilter): Boolean =
        filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE

    private fun cacheKey(filter: TimeFilter, serviceId: Int?): String {
        val servicePart = serviceId?.toString() ?: "all"
        return "${TimeFilter.cacheKeyToken(filter)}_${servicePart}"
    }

    private fun getCacheFile(context: Context, filter: TimeFilter, serviceId: Int?): File {
        return File(context.cacheDir, "$HOME_CACHE_PREFIX${cacheKey(filter, serviceId)}.json")
    }

    private fun filterReportedFalseEvents(data: CachedHomeData): CachedHomeData {
        val falseIds = EventCacheManager.snapshotReportedFalseIds()
        if (falseIds.isEmpty()) return data
        val moduleMap = data.aiModuleEvents.orEmpty()
        val hasInRecent = data.recentEvents.any { it.id in falseIds }
        val hasInModules = moduleMap.values.any { list -> list.any { it.id in falseIds } }
        if (!hasInRecent && !hasInModules) return data
        return data.copy(
            recentEvents = if (hasInRecent) data.recentEvents.filter { it.id !in falseIds } else data.recentEvents,
            aiModuleEvents = if (hasInModules) {
                moduleMap.mapValues { (_, list) -> list.filter { it.id !in falseIds } }
            } else {
                moduleMap
            }
        )
    }

    fun getHomeDataInMemory(filter: TimeFilter, serviceId: Int?): CachedHomeData? {
        return inMemoryHomeCache.get(cacheKey(filter, serviceId))
    }

    suspend fun preloadDefaultCache(context: Context) {
        getHomeData(context, TimeFilter.TODAY, null)
    }

    suspend fun saveHomeData(context: Context, filter: TimeFilter, serviceId: Int?, data: CachedHomeData) {
        if (UserSession.accessToken.isEmpty()) return
        val stamped = if (data.updatedAt > 0L) data else data.copy(updatedAt = System.currentTimeMillis())
        inMemoryHomeCache.put(cacheKey(filter, serviceId), stamped)
        EventCacheManager.cacheInMemory(stamped.recentEvents, stamped.cameraList, stamped.aiServices)
        if (isCustomTimeFilter(filter)) return
        withContext(Dispatchers.IO) {
            if (UserSession.accessToken.isEmpty()) return@withContext
            JsonFileStore.write(gson, getCacheFile(context, filter, serviceId), stamped)
            JsonFileStore.pruneOldestMatching(context.cacheDir, MAX_DISK_CACHE_FILES) {
                it.startsWith(HOME_CACHE_PREFIX)
            }
        }
    }

    /**
     * Xoá một sự kiện đã bị báo sai khỏi toàn bộ RAM cache và Disk cache của màn Trang chủ.
     */
    suspend fun removeEvent(context: Context, eventId: Int) {
        EventCacheManager.markEventReportedFalse(eventId)
        val updatedEntries = mutableListOf<Pair<String, CachedHomeData>>()
        val snapshot = inMemoryHomeCache.snapshot()
        for ((key, cached) in snapshot) {
            val moduleMap = cached.aiModuleEvents.orEmpty()
            val hasInRecent = cached.recentEvents.any { it.id == eventId }
            val hasInModules = moduleMap.values.any { list -> list.any { it.id == eventId } }
            if (hasInRecent || hasInModules) {
                val updated = cached.copy(
                    recentEvents = if (hasInRecent) cached.recentEvents.filter { it.id != eventId } else cached.recentEvents,
                    aiModuleEvents = if (hasInModules) {
                        moduleMap.mapValues { (_, list) -> list.filter { it.id != eventId } }
                    } else {
                        moduleMap
                    }
                )
                inMemoryHomeCache.put(key, updated)
                if (!key.startsWith("${TimeFilter.CUSTOM_DATE.name}_") &&
                    !key.startsWith("${TimeFilter.CUSTOM_RANGE.name}_")
                ) {
                    updatedEntries.add(key to updated)
                }
            }
        }
        if (updatedEntries.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                if (UserSession.accessToken.isEmpty()) return@withContext
                for ((key, updated) in updatedEntries) {
                    val file = File(context.cacheDir, "$HOME_CACHE_PREFIX$key.json")
                    if (file.exists()) {
                        JsonFileStore.write(gson, file, updated)
                    }
                }
            }
        }
    }

    suspend fun getHomeData(context: Context, filter: TimeFilter, serviceId: Int?): CachedHomeData? {
        val key = cacheKey(filter, serviceId)
        inMemoryHomeCache.get(key)?.let { return it }
        if (isCustomTimeFilter(filter)) return null

        return withContext(Dispatchers.IO) {
            diskReadMutex.withLock {
                inMemoryHomeCache.get(key)?.let { return@withContext it }
                JsonFileStore.read(gson, getCacheFile(context, filter, serviceId), CachedHomeData::class.java)?.let { raw ->
                    val cleaned = filterReportedFalseEvents(raw)
                    inMemoryHomeCache.put(key, cleaned)
                    EventCacheManager.cacheInMemory(cleaned.recentEvents, cleaned.cameraList, cleaned.aiServices)
                    cleaned
                }
            }
        }
    }

    fun trimMemory() {
        inMemoryHomeCache.evictAll()
    }

    fun clearInMemorySync() {
        inMemoryHomeCache.evictAll()
    }

    fun clearCache(context: Context) {
        clearInMemorySync()
        JsonFileStore.deleteMatching(context.cacheDir) { it.startsWith(HOME_CACHE_PREFIX) }
    }
}
