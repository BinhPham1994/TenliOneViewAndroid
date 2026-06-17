package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.BaseResponse
import com.tenli.oneview.model.network.DeviceItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DeviceApi {

    @FormUrlEncoded
    @POST("device/create")
    suspend fun addDevice(
        @Field("name") name: String,
        @Field("deviceCode") deviceCode: String,
        @Field("key") key: String,
        @Field("userGroupId") userGroupId: Int
    ): Response<Any?>

    @GET("device/get-list")
    suspend fun getListDevice(): Response<BaseResponse<List<DeviceItem>>>

    @DELETE("device/delete/{id}")
    suspend fun deleteDevice(
        @Path("id") deviceId: Int
    ): Response<Any?>

    @GET("device/get/{id}")
    suspend fun getDeviceDetail(
        @Path("id") deviceId: Int
    ): Response<BaseResponse<DeviceItem>>

    @PUT("device/update/{id}")
    suspend fun editDevice(
        @Path("id") id: Int,
        @Body raw: Any
    ): Response<Any?>

    @GET("device/get-alarm-options/{id}")
    suspend fun getAlarm(
        @Path("id") deviceId: Int
    ): Response<Any?>

    @FormUrlEncoded
    @POST("device/update-alarm-options/{id}")
    suspend fun setAlarm(
        @Path("id") deviceId: Int,
        @Field("pauseUtil") pauseUtil: String
    ): Response<Any?>
}