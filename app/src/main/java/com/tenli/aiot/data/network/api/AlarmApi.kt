package com.tenli.aiot.data.network.api

import com.tenli.aiot.model.network.AddScriptResponse
import com.tenli.aiot.model.network.BaseResponse
import com.tenli.aiot.model.network.EventTypeGroupDetail
import com.tenli.aiot.model.network.NotificationSettingResponse
import com.tenli.aiot.model.network.UpdateNotificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AlarmApi {
    @GET("user/get-notification-setting")
    suspend fun getNotifySetting(): Response<BaseResponse<NotificationSettingResponse>>

    @POST("user/update-notification-setting")
    suspend fun updateNotifySetting(
        @Body raw: Any
    ): Response<BaseResponse<Unit>>

    @POST("user/update-notification-setting/user")
    suspend fun updateUserNotifySetting(
        @Body raw: Any
    ): Response<BaseResponse<Unit>>

    @POST("user/update-notification-setting/event-type-group")
    suspend fun updateNotifySettingEventGroup(
        @Body raw: Any
    ): Response<BaseResponse<Unit>>


    @GET("user/get-notification-setting/event-type-group/{id}")
    suspend fun getNotifySettingEventGroup(
        @Path("id") id: String,
    ): Response<BaseResponse<EventTypeGroupDetail>>


    @POST("user/update-notification-setting/event-type-group/add-custom-script")
    suspend fun createScript(
        @Body raw: Any
    ): Response<BaseResponse<AddScriptResponse>>


    @PUT("user/update-notification-setting/event-type-group/update-custom-script")
    suspend fun editScript(
        @Body raw: Any
    ): Response<BaseResponse<AddScriptResponse>>

    @HTTP(method = "DELETE", path = "user/update-notification-setting/event-type-group/remove-custom-script", hasBody = true)
    suspend fun deleteScript(
        @Body raw: Any
    ): Response<BaseResponse<Unit>>

    @POST("user/update-notification-setting/event-type")
    suspend fun updateEventType(
        @Body raw: Any
    ): Response<BaseResponse<UpdateNotificationResponse>>
}