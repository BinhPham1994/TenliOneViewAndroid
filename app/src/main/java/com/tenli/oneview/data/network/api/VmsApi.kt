package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.*
import retrofit2.Response
import retrofit2.http.*

interface VmsApi {
    @GET("VMS/api/VMS")
    suspend fun getVMSServiceList(): Response<List<VMSServiceModel>>

    @POST("VMS/api/VMS")
    suspend fun createVMSService(@Body model: VMSServiceModel): Response<VMSServiceModel>

    @PUT("VMS/api/VMS/{id}")
    suspend fun updateVMSService(@Path("id") id: Int, @Body model: VMSServiceModel): Response<VMSServiceModel>

    @DELETE("VMS/api/VMS/{id}")
    suspend fun deleteVMSService(@Path("id") id: Int): Response<Unit>

    @GET("VMS/api/CameraView")
    suspend fun getCameraViewList(): Response<List<CameraViewModel>>

    @GET("VMS/api/UserGroup")
    suspend fun getUserGroupList(): Response<List<UserGroupModel>>

    @GET("VMS/api/User")
    suspend fun getUserList(): Response<List<UserModel>>

    @GET("VMS/api/UserInGroup")
    suspend fun getUserInGroupList(): Response<List<UserInGroupModel>>

    @GET("VMS/api/Storage")
    suspend fun getStorageList(): Response<List<StorageModel>>

    @POST("VMS/api/AuthToken")
    suspend fun getAuthToken(@Body request: Map<String, String>): Response<AuthTokenModel>

    @GET("VMS/api/CameraGroup")
    suspend fun getCameraGroupList(): Response<List<CameraGroupModel>>

    @GET("VMS/api/Camera")
    suspend fun getCameraList(): Response<List<CameraModel>>

    @GET("VMS/api/CameraInGroup")
    suspend fun getCameraInGroupList(): Response<List<CameraInGroupModel>>

    @GET("VMS/api/VideoList")
    suspend fun getVideoList(
        @Query("camera") camera: String? = null,
        @Query("count") count: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<VideoModel>>

    @GET("VMS/api/PermanentLink")
    suspend fun getPermanentLinkList(): Response<List<PermanentLinkModel>>

    @GET("VMS/api/CameraGroupAccess")
    suspend fun getCameraGroupAccessList(): Response<List<CameraGroupAccessModel>>

    @GET("VMS/api/ReportLink/{vmsId}")
    suspend fun getReportLinkList(@Path("vmsId") vmsId: Int): Response<ReportLinkModel>

    @GET("VMS/api/VideoJoinLink/{cameraId}")
    suspend fun getVideoJoinLink(@Path("cameraId") cameraId: Int): Response<VideoLinkModel>

    @GET("VMS/api/CameraImageLink/{cameraId}")
    suspend fun getCameraImageLink(@Path("cameraId") cameraId: Int): Response<CameraImageLinkModel>

    @POST("VMS/api/SystemSetting")
    suspend fun saveSystemSetting(@Body model: SystemSettingModel): Response<SystemSettingModel>

    @GET("VMS/api/SystemSetting")
    suspend fun getSystemSetting(): Response<SystemSettingModel>

    @POST("VMS/api/SystemControl")
    suspend fun restartService(@Body command: Map<String, String>): Response<Unit>
}
