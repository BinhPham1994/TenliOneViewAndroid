package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.*
import retrofit2.Response
import retrofit2.http.*
import okhttp3.MultipartBody

interface AiApi {
    @GET("api/Camera")
    suspend fun getCameraMonitorList(): Response<List<CameraMonitor>>

    @POST("api/Camera")
    suspend fun createCameraMonitor(@Body model: CameraMonitor): Response<CameraMonitor>

    @GET("api/Monitor")
    suspend fun getMonitorList(): Response<List<Monitor>>

    @GET("api/MonitorType")
    suspend fun getMonitorTypeList(): Response<List<MonitorType>>

    @GET("api/MonitorState")
    suspend fun getMonitorStateList(): Response<List<MonitorState>>

    @GET("api/MonitorParam/{type}")
    suspend fun getMonitorParam(@Path("type") type: String): Response<Any>

    @POST("api/Monitor")
    suspend fun createMonitor(@Body model: Monitor): Response<Monitor>

    @PUT("api/Monitor/{id}")
    suspend fun updateMonitor(@Path("id") id: Int, @Body model: Monitor): Response<Monitor>

    @DELETE("api/Monitor/{id}")
    suspend fun deleteMonitor(@Path("id") id: Int): Response<Unit>

    @GET("api/ProfileGroup/GetList")
    suspend fun getProfileGroupList(): Response<List<ProfileGroup>>

    @POST("api/ProfileGroup/Create")
    suspend fun createProfileGroup(@Body model: ProfileGroup): Response<ProfileGroup>

    @PUT("api/ProfileGroup/Update/{id}")
    suspend fun updateProfileGroup(@Path("id") id: Int, @Body model: ProfileGroup): Response<ProfileGroup>

    @DELETE("api/ProfileGroup/Delete/{id}")
    suspend fun deleteProfileGroup(@Path("id") id: Int): Response<Unit>

    @GET("api/Profile")
    suspend fun getProfileList(): Response<List<Profile>>

    @POST("api/Profile")
    suspend fun createProfile(@Body model: Profile): Response<Profile>

    @PUT("api/Profile/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body model: Profile): Response<Profile>

    @DELETE("api/Profile/{id}")
    suspend fun deleteProfile(@Path("id") id: Int): Response<Unit>

    @Multipart
    @POST("api/FaceCrop")
    suspend fun faceCrop(@Part file: MultipartBody.Part): Response<Any>

    @Multipart
    @POST("api/FaceCheck")
    suspend fun faceCheck(@Part file: MultipartBody.Part): Response<Any>
}
