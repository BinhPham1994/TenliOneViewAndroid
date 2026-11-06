package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.CameraGroupModel
import com.tenli.oneview.model.network.CameraImageLinkModel
import com.tenli.oneview.model.network.CameraInGroupModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.ChangePasswordModel
import com.tenli.oneview.model.network.LiveStreamModel
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.model.network.ReportLinkModel
import com.tenli.oneview.model.network.TokenModel
import com.tenli.oneview.model.network.VMSServiceModel
import com.tenli.oneview.model.network.VideoModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface VmsApi {

    // ==================== VMS Service ====================

    @GET("VMS/api/VMS")
    suspend fun getVMSServiceList(): Response<List<VMSServiceModel>>

    // ==================== Camera Group ====================

    @GET("VMS/api/CameraGroup")
    suspend fun getCameraGroupList(): Response<List<CameraGroupModel>>

    // ==================== Camera ====================

    @GET("VMS/api/Camera")
    suspend fun getCameraList(): Response<List<CameraModel>>

    // ==================== Camera In Group ====================

    @GET("VMS/api/CameraInGroup")
    suspend fun getCameraInGroupList(): Response<List<CameraInGroupModel>>

    // ==================== Live Stream & Record Stream ====================

    @POST("VMS/api/LiveStream")
    suspend fun createLiveStream(@Body model: LiveStreamModel): Response<LiveStreamModel>

    // ==================== Video ====================

    @GET("VMS/api/VideoList")
    suspend fun getVideoList(
        @Query("camera") camera: Int? = null,
        @Query("count") count: Int? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("channel") channel: String? = null
    ): Response<List<VideoModel>>

    // ==================== Report & Links ====================

    @GET("VMS/api/ReportLink/{vmsId}")
    suspend fun getReportLink(@Path("vmsId") vmsId: Int): Response<ReportLinkModel>

    @GET("VMS/api/CameraImageLink/{cameraId}")
    suspend fun getCameraImageLink(@Path("cameraId") cameraId: Int): Response<CameraImageLinkModel>

    // ==================== Auth ====================

    @POST
    suspend fun logInExt(@Url url: String, @Body model: LogInModel): Response<TokenModel>

    @POST("VMS/api/ChangePassword")
    suspend fun changePassword(@Body model: ChangePasswordModel): Response<Unit>
}
