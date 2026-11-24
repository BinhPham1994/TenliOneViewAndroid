package com.tenli.oneview.data.local

import android.content.Context
import android.util.LruCache
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

data class CachedEventData(
    val events: List<EventData> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val aiServices: List<AIServiceModel> = emptyList(),
    val updatedAt: Long = 0L
)

data class CachedProfilesData(
    val profiles: List<BsProfileModel> = emptyList(),
    val groups: List<BsProfileGroupModel> = emptyList(),
    val updatedAt: Long = 0L
)

object EventCacheManager {

    private val gson get() = LoginAuthClient.gson

    private const val EVENT_CACHE_PREFIX = "event_cache_"
    private const val PROFILES_CACHE_FILE = "bs_profiles_cache.json"

    // Giới hạn kích thước cache RAM (LRU) & số file đĩa để bộ nhớ và thời gian quét không phình mãi
    private const val MAX_EVENTS_IN_MEMORY = 2_000
    private const val MAX_DATASETS_IN_MEMORY = 40
    private const val MAX_RELATED_IN_MEMORY = 60
    private const val MAX_DISK_CACHE_FILES = 20
    private const val MAX_REPORTED_FALSE_IDS = 200

    // ==================== In-Memory Instant Cache (0ms lookup when tapping an event or tab) ====================
    // android.util.LruCache là thread-safe và tự loại bỏ phần tử ít dùng nhất khi vượt giới hạn
    private val inMemoryEvents = LruCache<Int, EventData>(MAX_EVENTS_IN_MEMORY)
    private val inMemoryDatasetCache = LruCache<String, CachedEventData>(MAX_DATASETS_IN_MEMORY)
    private val inMemoryRelatedEvents = LruCache<String, List<EventData>>(MAX_RELATED_IN_MEMORY)

    private val reportedFalseLock = Any()
    private val reportedFalseEventIds = object : LinkedHashMap<Int, Boolean>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Int, Boolean>?): Boolean =
            size > MAX_REPORTED_FALSE_IDS
    }

    @Volatile
    private var lastClickedEvent: EventData? = null

    @Volatile
    private var inMemoryCameras: List<CameraModel> = emptyList()

    @Volatile
    private var cameraNameMapCache: Pair<List<CameraModel>, Map<String, String>>? = null

    @Volatile
    private var inMemoryAiServices: List<AIServiceModel> = emptyList()

    @Volatile
    private var inMemoryProfiles: List<BsProfileModel> = emptyList()

    @Volatile
    private var inMemoryProfileGroups: List<BsProfileGroupModel> = emptyList()

    @Volatile
    private var sessionMetadataUpdatedAtMs: Long = 0L

    private val diskReadMutex = Mutex()

    private fun isCustomTimeFilter(filter: TimeFilter): Boolean =
        filter == TimeFilter.CUSTOM_DATE || filter == TimeFilter.CUSTOM_RANGE

    internal fun markEventReportedFalse(eventId: Int) {
        if (eventId <= 0) return
        synchronized(reportedFalseLock) {
            reportedFalseEventIds[eventId] = java.lang.Boolean.TRUE
        }
    }

    internal fun isEventReportedFalse(eventId: Int): Boolean =
        synchronized(reportedFalseLock) { reportedFalseEventIds.containsKey(eventId) }

    internal fun snapshotReportedFalseIds(): Set<Int> =
        synchronized(reportedFalseLock) {
            if (reportedFalseEventIds.isEmpty()) emptySet() else HashSet(reportedFalseEventIds.keys)
        }

    private fun filterReportedFalseEvents(events: List<EventData>): List<EventData> {
        val falseIds = snapshotReportedFalseIds()
        if (falseIds.isEmpty() || events.isEmpty()) return events
        return if (events.any { it.id in falseIds }) {
            events.filter { it.id !in falseIds }
        } else {
            events
        }
    }

    private fun datasetCacheKey(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = null
    ): String {
        val servicePart = serviceId?.toString() ?: "all"
        val aiTypePart = aiType ?: "all"
        val cameraPart = cameraUuid ?: "all"
        return "${TimeFilter.cacheKeyToken(filter)}_${servicePart}_${aiTypePart}_${cameraPart}"
    }

    fun cacheInMemory(
        events: List<EventData> = emptyList(),
        cameras: List<CameraModel> = emptyList(),
        aiServices: List<AIServiceModel> = emptyList(),
        fromNetwork: Boolean = false
    ) {
        if (UserSession.accessToken.isEmpty()) return
        val falseIds = snapshotReportedFalseIds()
        events.forEach { ev ->
            if (ev.id > 0 && ev.id !in falseIds) {
                inMemoryEvents.put(ev.id, ev)
            }
        }
        if (cameras.isNotEmpty()) {
            inMemoryCameras = cameras
        }
        if (aiServices.isNotEmpty()) {
            inMemoryAiServices = aiServices
        }
        if (fromNetwork && cameras.isNotEmpty()) {
            sessionMetadataUpdatedAtMs = System.currentTimeMillis()
        }
    }

    fun hasFreshSessionMetadata(maxAgeMs: Long = 30_000L): Boolean {
        val updatedAt = sessionMetadataUpdatedAtMs
        if (updatedAt <= 0L || inMemoryCameras.isEmpty()) return false
        return (System.currentTimeMillis() - updatedAt) < maxAgeMs
    }

    fun cacheClickedEvent(
        event: EventData,
        cameras: List<CameraModel> = emptyList(),
        aiServices: List<AIServiceModel> = emptyList()
    ) {
        lastClickedEvent = event
        if (event.id > 0) {
            inMemoryEvents.put(event.id, event)
        }
        if (cameras.isNotEmpty()) {
            inMemoryCameras = cameras
        }
        if (aiServices.isNotEmpty()) {
            inMemoryAiServices = aiServices
        }
    }

    fun cacheProfilesInMemory(
        profiles: List<BsProfileModel>,
        groups: List<BsProfileGroupModel>
    ) {
        if (profiles.isNotEmpty()) {
            inMemoryProfiles = profiles
        }
        if (groups.isNotEmpty()) {
            inMemoryProfileGroups = groups
        }
    }

    suspend fun saveProfilesCache(
        context: Context,
        profiles: List<BsProfileModel>,
        groups: List<BsProfileGroupModel>
    ) {
        if (UserSession.accessToken.isEmpty()) return
        cacheProfilesInMemory(profiles, groups)
        withContext(Dispatchers.IO) {
            if (UserSession.accessToken.isEmpty()) return@withContext
            val data = CachedProfilesData(
                profiles = inMemoryProfiles,
                groups = inMemoryProfileGroups,
                updatedAt = System.currentTimeMillis()
            )
            JsonFileStore.write(gson, File(context.cacheDir, PROFILES_CACHE_FILE), data)
        }
    }

    fun cacheRelatedEventsInMemory(eventId: Int, periodCode: String, events: List<EventData>) {
        if (eventId > 0) {
            val cleaned = filterReportedFalseEvents(events)
            inMemoryRelatedEvents.put("${eventId}_${periodCode}", cleaned)
            cacheInMemory(events = cleaned)
        }
    }

    fun getRelatedEventsInMemory(eventId: Int, periodCode: String): List<EventData>? {
        return inMemoryRelatedEvents.get("${eventId}_${periodCode}")
    }

    fun getEventInMemory(eventId: Int): EventData? {
        if (isEventReportedFalse(eventId)) return null
        val clicked = lastClickedEvent
        if (clicked != null && clicked.id == eventId) {
            return clicked
        }
        return inMemoryEvents.get(eventId)
    }

    fun getCamerasInMemory(): List<CameraModel> = inMemoryCameras

    fun getCameraNameMapInMemory(): Map<String, String> {
        val cameras = inMemoryCameras
        if (cameras.isEmpty()) return emptyMap()
        val cached = cameraNameMapCache
        if (cached != null && cached.first === cameras) {
            return cached.second
        }
        val map = HashMap<String, String>(cameras.size)
        for (cam in cameras) {
            val uuid = cam.extra?.uuid
            if (!uuid.isNullOrBlank()) {
                map[uuid] = cam.name
            }
        }
        cameraNameMapCache = Pair(cameras, map)
        return map
    }

    fun getAiServicesInMemory(): List<AIServiceModel> = inMemoryAiServices

    fun getProfilesInMemory(): List<BsProfileModel> = inMemoryProfiles

    fun getProfileGroupsInMemory(): List<BsProfileGroupModel> = inMemoryProfileGroups

    fun getEventDataInMemory(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = null
    ): CachedEventData? {
        return inMemoryDatasetCache.get(datasetCacheKey(filter, serviceId, aiType, cameraUuid))
    }

    suspend fun preloadDefaultCache(context: Context) {
        getEventData(context, TimeFilter.TODAY, null, null, null)
        withContext(Dispatchers.IO) {
            JsonFileStore.read(gson, File(context.cacheDir, PROFILES_CACHE_FILE), CachedProfilesData::class.java)?.let { parsed ->
                cacheProfilesInMemory(parsed.profiles.orEmpty(), parsed.groups.orEmpty())
            }
        }
    }

    // ==================== Disk Cache ====================

    private fun getCacheFile(
        context: Context,
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = null
    ): File {
        return File(context.cacheDir, "$EVENT_CACHE_PREFIX${datasetCacheKey(filter, serviceId, aiType, cameraUuid)}.json")
    }

    suspend fun saveEventData(
        context: Context,
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        data: CachedEventData,
        cameraUuid: String? = null
    ) {
        if (UserSession.accessToken.isEmpty()) return
        val stamped = if (data.updatedAt > 0L) data else data.copy(updatedAt = System.currentTimeMillis())
        inMemoryDatasetCache.put(datasetCacheKey(filter, serviceId, aiType, cameraUuid), stamped)
        cacheInMemory(stamped.events, stamped.cameraList, stamped.aiServices)
        if (isCustomTimeFilter(filter)) return
        withContext(Dispatchers.IO) {
            if (UserSession.accessToken.isEmpty()) return@withContext
            JsonFileStore.write(gson, getCacheFile(context, filter, serviceId, aiType, cameraUuid), stamped)
            JsonFileStore.pruneOldestMatching(context.cacheDir, MAX_DISK_CACHE_FILES) {
                it.startsWith(EVENT_CACHE_PREFIX)
            }
        }
    }

    /**
     * Xoá một sự kiện đã bị báo sai khỏi toàn bộ RAM cache và Disk cache của màn Sự kiện.
     */
    suspend fun removeEvent(context: Context, eventId: Int) {
        markEventReportedFalse(eventId)
        if (lastClickedEvent?.id == eventId) {
            lastClickedEvent = null
        }
        inMemoryEvents.remove(eventId)

        val relatedSnapshot = inMemoryRelatedEvents.snapshot()
        for ((key, list) in relatedSnapshot) {
            if (key.startsWith("${eventId}_")) {
                inMemoryRelatedEvents.remove(key)
            } else if (list.any { it.id == eventId }) {
                inMemoryRelatedEvents.put(key, list.filter { it.id != eventId })
            }
        }

        val updatedDatasets = mutableListOf<Pair<String, CachedEventData>>()
        val datasetSnapshot = inMemoryDatasetCache.snapshot()
        for ((key, cached) in datasetSnapshot) {
            if (cached.events.any { it.id == eventId }) {
                val updated = cached.copy(events = cached.events.filter { it.id != eventId })
                inMemoryDatasetCache.put(key, updated)
                if (!key.startsWith("${TimeFilter.CUSTOM_DATE.name}_") &&
                    !key.startsWith("${TimeFilter.CUSTOM_RANGE.name}_")
                ) {
                    updatedDatasets.add(key to updated)
                }
            }
        }
        if (updatedDatasets.isNotEmpty()) {
            withContext(Dispatchers.IO) {
                if (UserSession.accessToken.isEmpty()) return@withContext
                for ((key, updated) in updatedDatasets) {
                    val file = File(context.cacheDir, "$EVENT_CACHE_PREFIX$key.json")
                    if (file.exists()) {
                        JsonFileStore.write(gson, file, updated)
                    }
                }
            }
        }
    }

    suspend fun getEventData(
        context: Context,
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = null
    ): CachedEventData? {
        val key = datasetCacheKey(filter, serviceId, aiType, cameraUuid)
        inMemoryDatasetCache.get(key)?.let { return it }
        if (isCustomTimeFilter(filter)) return null

        return withContext(Dispatchers.IO) {
            diskReadMutex.withLock {
                inMemoryDatasetCache.get(key)?.let { return@withContext it }
                val file = getCacheFile(context, filter, serviceId, aiType, cameraUuid)
                // Tương thích ngược với file cache cũ không có hậu tố cameraPart
                val targetFile = if (!file.exists() && cameraUuid == null) {
                    val sPart = serviceId?.toString() ?: "all"
                    val aPart = aiType ?: "all"
                    File(context.cacheDir, "$EVENT_CACHE_PREFIX${filter.name}_${sPart}_${aPart}.json")
                } else {
                    file
                }

                JsonFileStore.read(gson, targetFile, CachedEventData::class.java)?.let { parsed ->
                    val rawEvents = parsed.events.orEmpty()
                    val filteredEvents = filterReportedFalseEvents(rawEvents)
                    val cleaned = if (filteredEvents === rawEvents) parsed else parsed.copy(events = filteredEvents)
                    inMemoryDatasetCache.put(key, cleaned)
                    cacheInMemory(filteredEvents, cleaned.cameraList.orEmpty(), cleaned.aiServices.orEmpty())
                    cleaned
                }
            }
        }
    }

    fun trimMemory() {
        inMemoryEvents.evictAll()
        inMemoryDatasetCache.evictAll()
        inMemoryRelatedEvents.evictAll()
        lastClickedEvent = null
    }

    fun clearInMemorySync() {
        inMemoryEvents.evictAll()
        inMemoryDatasetCache.evictAll()
        inMemoryRelatedEvents.evictAll()
        inMemoryCameras = emptyList()
        cameraNameMapCache = null
        inMemoryAiServices = emptyList()
        inMemoryProfiles = emptyList()
        inMemoryProfileGroups = emptyList()
        sessionMetadataUpdatedAtMs = 0L
        lastClickedEvent = null
        synchronized(reportedFalseLock) {
            reportedFalseEventIds.clear()
        }
    }

    fun clearCache(context: Context) {
        clearInMemorySync()
        JsonFileStore.deleteMatching(context.cacheDir) { it.startsWith(EVENT_CACHE_PREFIX) || it == PROFILES_CACHE_FILE }
    }

    suspend fun findEventById(context: Context, eventId: Int): Pair<EventData, List<CameraModel>>? {
        if (isEventReportedFalse(eventId)) return null
        getEventInMemory(eventId)?.let { memEvent ->
            return Pair(memEvent, inMemoryCameras)
        }
        return withContext(Dispatchers.IO) {
            val files = context.cacheDir.listFiles() ?: return@withContext null
            for (file in files) {
                if (file.name.endsWith(".tmp")) continue
                when {
                    file.name.startsWith(EVENT_CACHE_PREFIX) -> {
                        val data = JsonFileStore.read(gson, file, CachedEventData::class.java) ?: continue
                        val events = filterReportedFalseEvents(data.events.orEmpty())
                        val event = events.find { it.id == eventId } ?: continue
                        cacheInMemory(events, data.cameraList.orEmpty(), data.aiServices.orEmpty())
                        return@withContext Pair(event, data.cameraList.orEmpty())
                    }
                    file.name.startsWith(HomeCacheManager.HOME_CACHE_PREFIX) -> {
                        val data = JsonFileStore.read(gson, file, CachedHomeData::class.java) ?: continue
                        val events = filterReportedFalseEvents(data.recentEvents.orEmpty())
                        val event = events.find { it.id == eventId } ?: continue
                        cacheInMemory(events, data.cameraList.orEmpty(), data.aiServices.orEmpty())
                        return@withContext Pair(event, data.cameraList.orEmpty())
                    }
                }
            }
            null
        }
    }
}

