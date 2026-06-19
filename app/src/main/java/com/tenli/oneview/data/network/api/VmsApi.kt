package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.*
import retrofit2.Response
import retrofit2.http.*

interface VmsApi {

    // ==================== VMS Service ====================

    @GET("VMS/api/VMS")
    suspend fun getVMSServiceList(): Response<List<VMSServiceModel>>

    @POST("VMS/api/VMS")
    suspend fun createVMSService(@Body model: VMSServiceModel): Response<VMSServiceModel>

    @PUT("VMS/api/VMS/{id}")
    suspend fun updateVMSService(@Path("id") id: Int, @Body model: VMSServiceModel): Response<VMSServiceModel>

    @DELETE("VMS/api/VMS/{id}")
    suspend fun deleteVMSService(@Path("id") id: Int): Response<Unit>

    // ==================== Camera View ====================

    @GET("VMS/api/CameraView")
    suspend fun getCameraViewList(): Response<List<CameraViewModel>>

    @POST("VMS/api/CameraView")
    suspend fun createCameraView(@Body model: CameraViewModel): Response<CameraViewModel>

    @PUT("VMS/api/CameraView/{id}")
    suspend fun updateCameraView(@Path("id") id: Int, @Body model: CameraViewModel): Response<CameraViewModel>

    @DELETE("VMS/api/CameraView/{id}")
    suspend fun deleteCameraView(@Path("id") id: Int): Response<Unit>

    // ==================== User Group ====================

    @GET("VMS/api/UserGroup")
    suspend fun getUserGroupList(): Response<List<UserGroupModel>>

    @POST("VMS/api/UserGroup")
    suspend fun createUserGroup(@Body model: UserGroupModel): Response<UserGroupModel>

    @PUT("VMS/api/UserGroup/{id}")
    suspend fun updateUserGroup(@Path("id") id: Int, @Body model: UserGroupModel): Response<UserGroupModel>

    @DELETE("VMS/api/UserGroup/{id}")
    suspend fun deleteUserGroup(@Path("id") id: Int): Response<Unit>

    // ==================== User ====================

    @GET("VMS/api/User")
    suspend fun getUserList(): Response<List<UserModel>>

    @GET("VMS/api/User/{id}")
    suspend fun getUser(@Path("id") id: Int): Response<UserModel>

    @POST("VMS/api/User")
    suspend fun createUser(@Body model: UserModel): Response<UserModel>

    @PUT("VMS/api/User/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body model: UserModel): Response<UserModel>

    @DELETE("VMS/api/User/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    // ==================== User In Group ====================

    @GET("VMS/api/UserInGroup")
    suspend fun getUserInGroupList(): Response<List<UserInGroupModel>>

    @POST("VMS/api/UserInGroup")
    suspend fun createUserInGroup(@Body model: UserInGroupModel): Response<UserInGroupModel>

    @PUT("VMS/api/UserInGroup/{id}")
    suspend fun updateUserInGroup(@Path("id") id: Int, @Body model: UserInGroupModel): Response<UserInGroupModel>

    @DELETE("VMS/api/UserInGroup/{id}")
    suspend fun deleteUserInGroup(@Path("id") id: Int): Response<Unit>

    // ==================== Storage ====================

    @GET("VMS/api/Storage")
    suspend fun getStorageList(): Response<List<StorageModel>>

    @POST("VMS/api/Storage")
    suspend fun createStorage(@Body model: StorageModel): Response<StorageModel>

    @PUT("VMS/api/Storage/{id}")
    suspend fun updateStorage(@Path("id") id: Int, @Body model: StorageModel): Response<StorageModel>

    @DELETE("VMS/api/Storage/{id}")
    suspend fun deleteStorage(@Path("id") id: Int): Response<Unit>

    // ==================== Auth Token ====================

    @POST("VMS/api/AuthToken")
    suspend fun getAuthToken(@Body request: AuthTokenRequest): Response<AuthTokenModel>

    /** AuthToken với URL tùy chỉnh (tương đương postExt trên web) */
    @POST
    suspend fun getAuthTokenExt(@Url url: String, @Body request: AuthTokenRequest): Response<AuthTokenModel>

    // ==================== Camera Group ====================

    @GET("VMS/api/CameraGroup")
    suspend fun getCameraGroupList(): Response<List<CameraGroupModel>>

    @POST("VMS/api/CameraGroup")
    suspend fun createCameraGroup(@Body model: CameraGroupModel): Response<CameraGroupModel>

    @PUT("VMS/api/CameraGroup/{id}")
    suspend fun updateCameraGroup(@Path("id") id: Int, @Body model: CameraGroupModel): Response<CameraGroupModel>

    @DELETE("VMS/api/CameraGroup/{id}")
    suspend fun deleteCameraGroup(@Path("id") id: Int): Response<Unit>

    // ==================== Camera ====================

    @GET("VMS/api/Camera")
    suspend fun getCameraList(): Response<List<CameraModel>>

    @POST("VMS/api/Camera")
    suspend fun createCamera(@Body model: CameraModel): Response<CameraModel>

    @PUT("VMS/api/Camera/{id}")
    suspend fun updateCamera(@Path("id") id: Int, @Body model: CameraModel): Response<CameraModel>

    @DELETE("VMS/api/Camera/{id}")
    suspend fun deleteCamera(@Path("id") id: Int): Response<Unit>

    // ==================== Camera In Group ====================

    @GET("VMS/api/CameraInGroup")
    suspend fun getCameraInGroupList(): Response<List<CameraInGroupModel>>

    @POST("VMS/api/CameraInGroup")
    suspend fun createCameraInGroup(@Body model: CameraInGroupModel): Response<CameraInGroupModel>

    @PUT("VMS/api/CameraInGroup/{id}")
    suspend fun updateCameraInGroup(@Path("id") id: Int, @Body model: CameraInGroupModel): Response<CameraInGroupModel>

    @DELETE("VMS/api/CameraInGroup/{id}")
    suspend fun deleteCameraInGroup(@Path("id") id: Int): Response<Unit>

    // ==================== Live Stream ====================

    @POST("VMS/api/LiveStream")
    suspend fun createLiveStream(@Body model: LiveStreamModel): Response<LiveStreamModel>

    // ==================== Video ====================

    @GET("VMS/api/VideoList")
    suspend fun getVideoList(
        @Query("camera") camera: Int? = null,
        @Query("count") count: Int? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<List<VideoModel>>

    // ==================== Permanent Link ====================

    @GET("VMS/api/PermanentLink")
    suspend fun getPermanentLinkList(): Response<List<PermanentLinkModel>>

    @POST("VMS/api/PermanentLink")
    suspend fun createPermanentLink(@Body model: PermanentLinkModel): Response<PermanentLinkModel>

    @DELETE("VMS/api/PermanentLink/{id}")
    suspend fun deletePermanentLink(@Path("id") id: Int): Response<Unit>

    // ==================== Camera Group Access ====================

    @GET("VMS/api/CameraGroupAccess")
    suspend fun getCameraGroupAccessList(): Response<List<CameraGroupAccessModel>>

    @POST("VMS/api/CameraGroupAccess")
    suspend fun createCameraGroupAccess(@Body model: CameraGroupAccessModel): Response<CameraGroupAccessModel>

    @PUT("VMS/api/CameraGroupAccess/{id}")
    suspend fun updateCameraGroupAccess(@Path("id") id: Int, @Body model: CameraGroupAccessModel): Response<CameraGroupAccessModel>

    @DELETE("VMS/api/CameraGroupAccess/{id}")
    suspend fun deleteCameraGroupAccess(@Path("id") id: Int): Response<Unit>

    // ==================== Report & Links ====================

    @GET("VMS/api/ReportLink/{vmsId}")
    suspend fun getReportLink(@Path("vmsId") vmsId: Int): Response<ReportLinkModel>

    @GET("VMS/api/VideoJoinLink/{cameraId}")
    suspend fun getVideoJoinLink(@Path("cameraId") cameraId: Int): Response<VideoLinkModel>

    @GET("VMS/api/CameraImageLink/{cameraId}")
    suspend fun getCameraImageLink(@Path("cameraId") cameraId: Int): Response<CameraImageLinkModel>

    // ==================== System ====================

    @POST("VMS/api/SystemSetting")
    suspend fun saveSystemSetting(@Body model: SystemSettingModel): Response<SystemSettingModel>

    @GET("VMS/api/SystemSetting")
    suspend fun getSystemSetting(): Response<SystemSettingModel>

    @POST("VMS/api/SystemControl")
    suspend fun restartService(@Body command: Map<String, String>): Response<Unit>

    /** Restart service với URL tùy chỉnh (tương đương restartServiceExt trên web) */
    @POST
    suspend fun restartServiceExt(@Url url: String, @Body command: Map<String, String>): Response<Unit>

    // ==================== Storage Status (External Host) ====================

    /** StorageStatus dùng host riêng của VMS service (tương đương getExt trên web) */
    @GET
    suspend fun getStorageStatus(@Url url: String): Response<StorageStatusModel>

    // ==================== Auth ====================

    @POST("VMS/api/LogIn")
    suspend fun logIn(@Body model: LogInModel): Response<TokenModel>

    @POST("VMS/api/LogOut")
    suspend fun logOut(@Body model: LogOutModel): Response<Unit>

    @POST("VMS/api/ChangePassword")
    suspend fun changePassword(@Body model: ChangePasswordModel): Response<Unit>

    @POST("VMS/api/ResetPassword")
    suspend fun resetPassword(@Body model: ResetPasswordModel): Response<Unit>
}
