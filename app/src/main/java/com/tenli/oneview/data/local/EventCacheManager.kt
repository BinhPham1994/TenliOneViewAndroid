package com.tenli.oneview.data.local

import android.content.Context
import com.tenli.oneview.data.network.retrofit.LoginAuthClient
import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.features.home.TimeFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

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

    // ==================== In-Memory Instant Cache (0ms lookup when tapping an event or tab) ====================
    private val inMemoryEvents = ConcurrentHashMap<Int, EventData>()
    private val inMemoryDatasetCache = ConcurrentHashMap<String, CachedEventData>()
    private val inMemoryRelatedEvents = ConcurrentHashMap<String, List<EventData>>()

    @Volatile
    private var lastClickedEvent: EventData? = null

    @Volatile
    private var inMemoryCameras: List<CameraModel> = emptyList()

    @Volatile
    private var inMemoryAiServices: List<AIServiceModel> = emptyList()

    @Volatile
    private var inMemoryProfiles: List<BsProfileModel> = emptyList()

    @Volatile
    private var inMemoryProfileGroups: List<BsProfileGroupModel> = emptyList()

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
        aiServices: List<AIServiceModel> = emptyList()
    ) {
        events.forEach { ev ->
            if (ev.id > 0) {
                inMemoryEvents[ev.id] = ev
            }
        }
        if (cameras.isNotEmpty()) {
            inMemoryCameras = cameras
        }
        if (aiServices.isNotEmpty()) {
            inMemoryAiServices = aiServices
        }
    }

    fun cacheClickedEvent(
        event: EventData,
        cameras: List<CameraModel> = emptyList(),
        aiServices: List<AIServiceModel> = emptyList()
    ) {
        lastClickedEvent = event
        if (event.id > 0) {
            inMemoryEvents[event.id] = event
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
        cacheProfilesInMemory(profiles, groups)
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "bs_profiles_cache.json")
                val data = CachedProfilesData(
                    profiles = inMemoryProfiles,
                    groups = inMemoryProfileGroups,
                    updatedAt = System.currentTimeMillis()
                )
                file.writeText(gson.toJson(data))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun cacheRelatedEventsInMemory(eventId: Int, periodCode: String, events: List<EventData>) {
        if (eventId > 0) {
            inMemoryRelatedEvents["${eventId}_${periodCode}"] = events
            cacheInMemory(events = events)
        }
    }

    fun getRelatedEventsInMemory(eventId: Int, periodCode: String): List<EventData>? {
        return inMemoryRelatedEvents["${eventId}_${periodCode}"]
    }

    fun getEventInMemory(eventId: Int): EventData? {
        val clicked = lastClickedEvent
        if (clicked != null && clicked.id == eventId) {
            return clicked
        }
        return inMemoryEvents[eventId]
    }

    fun getCamerasInMemory(): List<CameraModel> = inMemoryCameras

    fun getAiServicesInMemory(): List<AIServiceModel> = inMemoryAiServices

    fun getProfilesInMemory(): List<BsProfileModel> = inMemoryProfiles

    fun getProfileGroupsInMemory(): List<BsProfileGroupModel> = inMemoryProfileGroups

    fun getEventDataInMemory(
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        cameraUuid: String? = null
    ): CachedEventData? {
        return inMemoryDatasetCache[datasetCacheKey(filter, serviceId, aiType, cameraUuid)]
    }

    suspend fun preloadDefaultCache(context: Context) {
        getEventData(context, TimeFilter.TODAY, null, null, null)
        withContext(Dispatchers.IO) {
            try {
                val profileFile = File(context.cacheDir, "bs_profiles_cache.json")
                if (profileFile.exists()) {
                    val parsed = gson.fromJson(profileFile.readText(), CachedProfilesData::class.java)
                    if (parsed != null) {
                        cacheProfilesInMemory(parsed.profiles, parsed.groups)
                    }
                }
            } catch (_: Exception) {
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
        return File(context.cacheDir, "event_cache_${datasetCacheKey(filter, serviceId, aiType, cameraUuid)}.json")
    }

    suspend fun saveEventData(
        context: Context,
        filter: TimeFilter,
        serviceId: Int?,
        aiType: String?,
        data: CachedEventData,
        cameraUuid: String? = null
    ) {
        val stamped = if (data.updatedAt > 0L) data else data.copy(updatedAt = System.currentTimeMillis())
        inMemoryDatasetCache[datasetCacheKey(filter, serviceId, aiType, cameraUuid)] = stamped
        cacheInMemory(stamped.events, stamped.cameraList, stamped.aiServices)
        withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter, serviceId, aiType, cameraUuid)
                val json = gson.toJson(stamped)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
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
        inMemoryDatasetCache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context, filter, serviceId, aiType, cameraUuid)
                // Tương thích ngược với file cache cũ không có hậu tố cameraPart
                val legacyFile = if (!file.exists() && cameraUuid == null) {
                    val sPart = serviceId?.toString() ?: "all"
                    val aPart = aiType ?: "all"
                    File(context.cacheDir, "event_cache_${filter.name}_${sPart}_${aPart}.json")
                } else null

                val targetFile = when {
                    file.exists() -> file
                    legacyFile?.exists() == true -> legacyFile
                    else -> null
                }

                if (targetFile != null) {
                    val json = targetFile.readText()
                    val parsed = gson.fromJson(json, CachedEventData::class.java)
                    if (parsed != null) {
                        inMemoryDatasetCache[key] = parsed
                        cacheInMemory(parsed.events, parsed.cameraList, parsed.aiServices)
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
        inMemoryEvents.clear()
        inMemoryDatasetCache.clear()
        inMemoryRelatedEvents.clear()
        lastClickedEvent = null
        try {
            val files = context.cacheDir.listFiles() ?: return
            for (file in files) {
                if (file.name.startsWith("event_cache_") || file.name == "bs_profiles_cache.json") {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun findEventById(context: Context, eventId: Int): Pair<EventData, List<CameraModel>>? {
        getEventInMemory(eventId)?.let { memEvent ->
            return Pair(memEvent, inMemoryCameras)
        }
        return withContext(Dispatchers.IO) {
            try {
                val files = context.cacheDir.listFiles() ?: return@withContext null
                for (file in files) {
                    if (file.name.startsWith("event_cache_")) {
                        val json = file.readText()
                        val data = gson.fromJson(json, CachedEventData::class.java)
                        val event = data?.events?.find { it.id == eventId }
                        if (event != null) {
                            cacheInMemory(data.events, data.cameraList, data.aiServices)
                            return@withContext Pair(event, data.cameraList)
                        }
                    } else if (file.name.startsWith("home_cache_")) {
                        val json = file.readText()
                        val data = gson.fromJson(json, CachedHomeData::class.java)
                        val event = data?.recentEvents?.find { it.id == eventId }
                        if (event != null) {
                            cacheInMemory(data.recentEvents, data.cameraList, data.aiServices)
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

