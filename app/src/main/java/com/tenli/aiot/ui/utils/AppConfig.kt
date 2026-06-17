package com.tenli.aiot.ui.utils

import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Response

object AppConfig {
    const val PACKAGE_ID = "com.tenli.aiot"
    const val DEVICE_KEY_DEFAULT = "key"
    const val PORT_DEVICE_DEFAULT = 8088
    const val ENGLISH = "en"
    const val VIETNAMESE = "vi"
    const val IOS = "iOS"
    const val ANDROID = "Android"

    fun getDeviceName(context: Context): String {
        return Settings.Global.getString(context.contentResolver, "device_name")
    }

    fun convertJsonObject(expose: String): JSONObject {
        val jsonData = JSONObject()
        expose.removeSurrounding("{", "}")
            .split(",")
            .map { it.trim() }
            .filter { it.contains("=") }
            .forEach { item ->
                val (key, value) = item.split("=")
                val processedValue = when (value) {
                    "true" -> true
                    "false" -> false
                    else -> value
                }
                jsonData.put(key, processedValue)
            }
        return jsonData
    }


    // 1. Chuyển response body thành JSONObject
    inline fun <reified T> Response<T>.toJSONObject(): JSONObject? {
        return this.body()?.let { body ->
            val json = Gson().toJson(body)
            JSONObject(json)
        }
    }

    // 2. Lấy đối tượng "data" từ JSONObject
    fun JSONObject.getDataObject(): JSONObject? {
        return try {
            this.getJSONObject("data")
        } catch (e: Exception) {
            null
        }
    }

    fun JSONObject.getDataArray(): JSONArray? {
        return try {
            this.getJSONArray("data")
        } catch (e: Exception) {
            null
        }
    }

    // 3. Chuyển JSONObject thành model Kotlin
    inline fun <reified T> JSONObject.toModel(): T {
        return Gson().fromJson(this.toString(), T::class.java)
    }

    // Optional: Phiên bản an toàn hơn
    inline fun <reified T> JSONObject.toModelOrNull(): T? {
        return try {
            Gson().fromJson(this.toString(), T::class.java)
        } catch (e: Exception) {
            null
        }
    }
}