package com.tenli.aiot.data.repository

import com.tenli.aiot.data.network.api.EventApi
import com.tenli.aiot.data.network.retrofit.CloudAuthClient
import com.tenli.aiot.model.network.BaseResponse
import com.tenli.aiot.model.network.EventItem

class EventRepository {
    suspend fun getListEvents(pageNumber: Int, pageSize: Int): Result<BaseResponse<List<EventItem>>> {
        return try {
            val response = CloudAuthClient.create(EventApi::class.java).getListEvents(pageNumber, pageSize)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get events: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListFilterEvents(
        pageNumber: Int,
        pageSize: Int,
        eType: List<String>?,
        fromTime: String?,
        toTime: String?,
        deviceId: List<Int>?
    ): Result<BaseResponse<List<EventItem>>> {
        return try {
            val response = CloudAuthClient.create(EventApi::class.java).getListFilterEvents(
                pageNumber = pageNumber,
                pageSize = pageSize,
                eType = eType,
                fromTime = fromTime,
                toTime = toTime,
                deviceId = deviceId
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get filter events: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventDetail(eventId: Long): Result<EventItem?> {
        return try {
            val response = CloudAuthClient.create(EventApi::class.java).getEventDetail(eventId)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception("Failed to fetch event detail: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventByMonitor(monitorID: Int, deviceId: Int, pageNumber: Int): Result<BaseResponse<List<EventItem>>> {
        return try {
            val response = CloudAuthClient.create(EventApi::class.java).getEventByMonitor(
                monitorID = monitorID,
                deviceId = deviceId,
                pageNumber = pageNumber
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch monitor events: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentEvent(deviceId: Int, pageSize: Int = 5): Result<BaseResponse<List<EventItem>>> {
        return try {
            val response = CloudAuthClient.create(EventApi::class.java).getRecentEvent(pageSize = pageSize, deviceId = deviceId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch recent events: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEventByDeviceID(eType: ArrayList<String>, deviceId: Int, pageSize: Int = 100): Result<BaseResponse<List<EventItem>>> {
        return try {
            val response = CloudAuthClient.create(EventApi::class.java).getEventByDeviceID(eType = eType, deviceId = deviceId, pageSize = pageSize)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch device events: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
