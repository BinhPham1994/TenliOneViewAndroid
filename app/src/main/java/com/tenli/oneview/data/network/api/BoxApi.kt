package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.BaseResponse
import com.tenli.oneview.model.network.BoxSystemInfo
import com.tenli.oneview.model.network.BoxSystemState
import com.tenli.oneview.model.network.CameraItem
import com.tenli.oneview.model.network.Monitor
import com.tenli.oneview.model.network.MonitorType
import com.tenli.oneview.model.network.ScriptItem
import com.tenli.oneview.model.network.SystemSettingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BoxApi {

    @POST("/api/Camera")
    suspend fun addCamera(
        @Body raw: Any
    ): Response<Any?>

    @PUT("/api/Camera/{id}")
    suspend fun editCamera(
        @Path("id") id: Int,
        @Body raw: Any
    ): Response<Any?>

    @DELETE("/api/Camera/{id}")
    suspend fun deleteCamera(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("/api/Camera")
    suspend fun getCameras(
    ): Response<List<CameraItem>>

    @GET("/api/CameraState")
    suspend fun getCameraStatus(
    ): Response<Any?>

    @GET("/api/Monitor")
    suspend fun getMonitors(
    ): Response<List<Monitor>>

    @GET("/api/MonitorType")
    suspend fun getMonitorType(
    ): Response<List<MonitorType>>

    @POST("/api/Monitor")
    suspend fun addMonitor(
        @Body raw: Any
    ): Response<Any?>

    @PUT("/api/Monitor/{id}")
    suspend fun updateMonitor(
        @Path("id") id: Int,
        @Body raw: Any
    ): Response<Any?>

    @DELETE("/api/Monitor/{id}")
    suspend fun deleteMonitor(
        @Path("id") id: Int
    ): Response<Unit>

    @GET("/local/tcp2mqtt/default/camera")
    suspend fun getSensor(): Response<Any?>

    @GET("/nr/schedule/get")
    suspend fun getScripts(): Response<BaseResponse<List<ScriptItem>>>

    @Headers("Content-Type: application/json")
    @POST("/nr/schedule/add")
    suspend fun addScript(
        @Body raw: Any
    ): Response<Any?>

    @Headers("Content-Type: application/json")
    @PUT("/nr/schedule/update/{id}")
    suspend fun editScript(
        @Path("id") id: String,
        @Body raw: Any
    ): Response<Any?>

    @DELETE("/nr/schedule/delete/{id}")
    suspend fun deleteScript(
        @Path("id") id: String
    ): Response<Unit>

    @Headers("Content-Type: application/json")
    @POST("/local/tcp2mqtt/default/camera/save")
    suspend fun addSensor(
        @Body raw: Any
    ): Response<Any?>

    @Headers("Content-Type: application/json")
    @POST("/local/tcp2mqtt/default/camera/save")
    suspend fun editSensor(
        @Body raw: Any
    ): Response<Any?>

    @DELETE("/local/tcp2mqtt/default/camera/{id}")
    suspend fun deleteSensor(
        @Path("id") id: String
    ): Response<Any?>

    @GET("/api/SystemState")
    suspend fun getSystemState(
    ): Response<BoxSystemState>

    @GET("/api/SystemInfo")
    suspend fun getDeviceInformation(): Response<BoxSystemInfo>

    @GET("/api/SystemSetting")
    suspend fun getSystemSetting(
    ): Response<SystemSettingResponse>

    @POST("/api/SystemSetting")
    suspend fun updateSystemSetting(
        @Body raw: SystemSettingResponse
    ): Response<SystemSettingResponse>

    @POST("/api/SystemKey")
    suspend fun configDeviceKey(
        @Body raw: Any
    ): Response<Any?>

    @POST("/api/SystemControl")
    suspend fun controlDevice(
        @Body raw: Any
    ): Response<Any?>

    @POST("/api/MonitorControl/{id}")
    suspend fun monitorControl(
        @Path("id") id: String,
        @Body raw: Any
    ): Response<Any?>

    @GET("/local/onvif/discovery")
    suspend fun discoveryDevice(
    ): Response<Any?>

    @FormUrlEncoded
    @POST("/local/onvif/listProfile")
    suspend fun getListProfile(
        @Field("xaddr") urlOnvif: String,
        @Field("user") userName: String,
        @Field("pass") pass: String,
    ): Response<Any?>

    @GET("/api/MonitorParam/{monitorType}")
    suspend fun getParamDefault(
        @Path("monitorType") id: String
    ): Response<Any?>

    @GET("/api/Profile")
    suspend fun getProfiles(
    ): Response<Any?>

    @GET("/api/Face")
    suspend fun getListFace(
        @Query("profileId") profileId: Int,
    ): Response<Any?>
}