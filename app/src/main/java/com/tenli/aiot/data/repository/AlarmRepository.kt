package com.tenli.aiot.data.repository

import com.tenli.aiot.data.network.api.AlarmApi
import com.tenli.aiot.data.network.retrofit.CloudAuthClient
import com.tenli.aiot.model.network.AddScriptResponse
import com.tenli.aiot.model.network.EventTypeGroupDetail
import com.tenli.aiot.model.network.NotificationSettingResponse
import com.tenli.aiot.model.network.UpdateNotificationResponse
import retrofit2.HttpException

class AlarmRepository {
    suspend fun getNotifySetting(): Result<NotificationSettingResponse?> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).getNotifySetting()
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotifySetting(body: Any): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).updateNotifySetting(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserNotifySetting(body: Any): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).updateUserNotifySetting(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateNotifySettingEventGroup(body: Any): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).updateNotifySettingEventGroup(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNotifySettingEventGroup(id: String): Result<EventTypeGroupDetail?> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).getNotifySettingEventGroup(id)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createScript(body: Any): Result<AddScriptResponse?> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).createScript(body)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editScript(body: Any): Result<AddScriptResponse?> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).editScript(body)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteScript(body: Any): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).deleteScript(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEventType(body: Any): Result<UpdateNotificationResponse?> {
        return try {
            val response = CloudAuthClient.create(AlarmApi::class.java).updateEventType(body)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
