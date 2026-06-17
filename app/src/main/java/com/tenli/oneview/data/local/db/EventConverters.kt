package com.tenli.oneview.data.local.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tenli.oneview.model.network.EventFileItem
import com.tenli.oneview.model.network.SData

class EventConverters {
    private val gson = Gson()

    // Chuyển đổi Map<String, Any?>
    @TypeConverter
    fun fromMap(value: Map<String, Any?>?): String? = gson.toJson(value)

    @TypeConverter
    fun toMap(value: String?): Map<String, Any?>? {
        val type = object : TypeToken<Map<String, Any?>>() {}.type
        return gson.fromJson(value, type)
    }

    // Chuyển đổi SData
    @TypeConverter
    fun fromSData(value: SData?): String? = gson.toJson(value)

    @TypeConverter
    fun toSData(value: String?): SData? = gson.fromJson(value, SData::class.java)

    // Chuyển đổi List<EventFileItem>
    @TypeConverter
    fun fromFileList(value: List<EventFileItem>?): String? = gson.toJson(value)

    @TypeConverter
    fun toFileList(value: String?): List<EventFileItem>? {
        val type = object : TypeToken<List<EventFileItem>>() {}.type
        return gson.fromJson(value, type)
    }
}