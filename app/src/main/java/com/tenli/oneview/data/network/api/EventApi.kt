package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.BaseResponse
import com.tenli.oneview.model.network.EventGroupDef
import com.tenli.oneview.model.network.EventItem
import com.tenli.oneview.model.network.EventTypeDef
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface EventApi {
    @GET("event/get-list")
    suspend fun getRecentEvent(
        @Query("pageSize") pageSize: Int = 5,
        @Query("deviceId") deviceId: Int,
        @Query("orderByList") orderByList: String = "-eTime"
    ): Response<BaseResponse<List<EventItem>>>

    @GET("event/get-list")
    suspend fun getEventByMonitor(
        @Query("eProcessId") monitorID: Int,
        @Query("deviceId") deviceId: Int,
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Query("orderByList") orderByList: String = "-eTime"
    ): Response<BaseResponse<List<EventItem>>>

    @GET("event/get-list")
    suspend fun getEventByDeviceID(
        @Query("eTypeGroup") eType: ArrayList<String>,
        @Query("deviceId") deviceId: Int,
        @Query("pageSize") pageSize: Int = 100,
        @Query("orderByList") orderByList: String = "-eTime"
    ): Response<BaseResponse<List<EventItem>>>

    @GET("event/get-list")
    suspend fun getListEvents(
        @Query("pageNumber") pageNumber: Int,
        @Query("pageSize") pageSize: Int = 20,
        @Query("orderByList") orderByList: String = "-eTime"
    ): Response<BaseResponse<List<EventItem>>>

    @GET("event/get-list")
    suspend fun getListFilterEvents(
        @Query("pageNumber") pageNumber: Int,
        @Query("eType") eType: List<String>? = null, // Chuyển sang List? và mặc định null
        @Query("fromTime") fromTime: String? = null,
        @Query("toTime") toTime: String? = null,
        @Query("deviceId") deviceId: List<Int>? = null, // Chuyển sang List? và mặc định null
        @Query("pageSize") pageSize: Int = 20,
        @Query("orderByList") orderByList: String = "-eTime"
    ): Response<BaseResponse<List<EventItem>>>
    @GET("event/get/{id}")

    suspend fun getEventDetail(
        @Path("id") id: Long,
        @Query("seen") seen: Int = 1,
    ): Response<BaseResponse<EventItem>>

    @GET("event/get-event-type-defines")
    suspend fun getEventType(
    ): Response<BaseResponse<List<EventTypeDef>>>

    @GET("event/get-event-type-group-defines")
    suspend fun getEventTypeGroup(
    ): Response<BaseResponse<List<EventGroupDef>>>
}