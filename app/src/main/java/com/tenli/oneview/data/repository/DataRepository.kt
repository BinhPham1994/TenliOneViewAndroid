package com.tenli.oneview.data.repository

import androidx.core.content.edit
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.tenli.oneview.data.local.GlobalData
import com.tenli.oneview.model.network.DeviceItem
import com.tenli.oneview.model.network.EventGroupDef
import com.tenli.oneview.model.network.EventTypeDef
import com.tenli.oneview.model.network.HomeGroupDisplay
import com.tenli.oneview.model.network.MonitorDisplayItem
import com.tenli.oneview.model.network.ScriptItem

object DataRepository {
    var deviceList = listOf<DeviceItem>()
    var scriptMap = mutableMapOf<Int, List<ScriptItem>>()
    var groupList = listOf<HomeGroupDisplay>()
        set(value) {
            field = value.sortedWith(
                compareByDescending<HomeGroupDisplay> {
                    it.displayName.contains("Nhà của tôi", ignoreCase = true)
                }.thenBy { it.displayName }
            )
        }
    var eventTypeDefs = listOf<EventTypeDef>()
    var eventGroupDefs = listOf<EventGroupDef>()
    var monitorMap = mutableMapOf<Int, List<MonitorDisplayItem>>()
    var monitorList = listOf<MonitorDisplayItem>()

    fun hasDevices() = deviceList.isNotEmpty()
    fun hasDefines() = eventTypeDefs.isNotEmpty()

    private val gson = Gson()

    fun persist() {
        GlobalData.preferences.edit {
            putString("cache_device_list", gson.toJson(deviceList))
            putString("cache_group_list", gson.toJson(groupList))
            putString("cache_type_defs", gson.toJson(eventTypeDefs))
            putString("cache_group_defs", gson.toJson(eventGroupDefs))
            putString("cache_monitor_list", gson.toJson(monitorList))
            putString("cache_script_map", gson.toJson(scriptMap))
            putString("cache_monitor_map", gson.toJson(monitorMap))
        }
    }

    fun restore() {
        val prefs = GlobalData.preferences
        prefs.getString("cache_device_list", null)?.let {
            deviceList = gson.fromJson(it, object : TypeToken<List<DeviceItem>>() {}.type)
        }
        prefs.getString("cache_group_list", null)?.let {
            groupList = gson.fromJson(it, object : TypeToken<List<HomeGroupDisplay>>() {}.type)
        }
        prefs.getString("cache_type_defs", null)?.let {
            eventTypeDefs = gson.fromJson(it, object : TypeToken<List<EventTypeDef>>() {}.type)
        }
        prefs.getString("cache_group_defs", null)?.let {
            eventGroupDefs = gson.fromJson(it, object : TypeToken<List<EventGroupDef>>() {}.type)
        }
        prefs.getString("cache_monitor_list", null)?.let {
            monitorList = gson.fromJson(it, object : TypeToken<List<MonitorDisplayItem>>() {}.type)
        }
        prefs.getString("cache_script_map", null)?.let {
            val type = object : TypeToken<MutableMap<Int, List<ScriptItem>>>() {}.type
            scriptMap = gson.fromJson(it, type)
        }
        prefs.getString("cache_monitor_map", null)?.let {
            val type = object : TypeToken<MutableMap<Int, List<MonitorDisplayItem>>>() {}.type
            monitorMap = gson.fromJson(it, type)
        }
    }

    fun clearCache() {
        deviceList = emptyList()
        groupList = emptyList()
        eventTypeDefs = emptyList()
        eventGroupDefs = emptyList()
        monitorList = emptyList()
        scriptMap.clear()
        monitorMap.clear()
        GlobalData.preferences.edit { clear() }
    }
}