package com.tenli.oneview.data.local

import android.content.Context
import com.google.gson.Gson
import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.VMSServiceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class CachedMonitorData(
    val vmsList: List<VMSServiceModel> = emptyList(),
    val groupList: List<CameraGroupModel> = emptyList(),
    val cameraList: List<CameraModel> = emptyList(),
    val cameraInGroups: List<CameraInGroupModel> = emptyList()
)

object MonitorCacheManager {

    private val gson = Gson()

    private fun getCacheFile(context: Context): File {
        return File(context.cacheDir, "monitor_cache_raw_data.json")
    }

    suspend fun saveMonitorData(context: Context, data: CachedMonitorData) {
        withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context)
                val json = gson.toJson(data)
                file.writeText(json)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getMonitorData(context: Context): CachedMonitorData? {
        return withContext(Dispatchers.IO) {
            try {
                val file = getCacheFile(context)
                if (file.exists()) {
                    val json = file.readText()
                    gson.fromJson(json, CachedMonitorData::class.java)
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
            val file = getCacheFile(context)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
