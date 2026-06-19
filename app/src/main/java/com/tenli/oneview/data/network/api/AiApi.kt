package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.*
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface AiApi {

    // ==================== Camera Monitor ====================

    @GET("api/Camera")
    suspend fun getCameraMonitorList(): Response<List<CameraMonitor>>

    @POST("api/Camera")
    suspend fun createCameraMonitor(@Body model: CameraMonitor): Response<CameraMonitor>

    // ==================== Monitor ====================

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

    // ==================== Profile Group ====================

    @GET("api/ProfileGroup/GetList")
    suspend fun getProfileGroupList(): Response<List<ProfileGroup>>

    @POST("api/ProfileGroup/Create")
    suspend fun createProfileGroup(@Body model: ProfileGroup): Response<ProfileGroup>

    @PUT("api/ProfileGroup/Update/{id}")
    suspend fun updateProfileGroup(@Path("id") id: Int, @Body model: ProfileGroup): Response<ProfileGroup>

    @DELETE("api/ProfileGroup/Delete/{id}")
    suspend fun deleteProfileGroup(@Path("id") id: Int): Response<Unit>

    @POST("api/ProfileGroup/AddProfile")
    suspend fun createProfileInProfileGroup(@Body model: ProfileInProfileGroup): Response<ProfileInProfileGroup>

    @GET("api/ProfileGroup/ProfileMap")
    suspend fun getProfileMap(@Query("profileId") profileId: Int): Response<List<ProfileInProfileGroup>>

    @HTTP(method = "DELETE", path = "api/ProfileGroup/DeleteProfile", hasBody = false)
    suspend fun deleteProfileFromProfileGroup(@Query("id") mapId: Int): Response<Unit>

    @POST("api/ProfileGroup/UpdateProfile")
    suspend fun updateProfileInProfileGroup(@Body model: UpdateProfileInProfileGroup): Response<UpdateProfileInProfileGroup>

    // ==================== Profile ====================

    @GET("api/Profile")
    suspend fun getProfileList(): Response<List<Profile>>

    @POST("api/Profile")
    suspend fun createProfile(@Body model: Profile): Response<Profile>

    @PUT("api/Profile/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body model: Profile): Response<Profile>

    @DELETE("api/Profile/{id}")
    suspend fun deleteProfile(@Path("id") id: Int): Response<Unit>

    // ==================== Face ====================

    @GET("api/Face")
    suspend fun getFaceList(@Query("profileId") profileId: Int): Response<List<Face>>

    @DELETE("api/Face/{faceId}")
    suspend fun deleteFace(@Path("faceId") faceId: Int): Response<Unit>

    /** Raw binary upload (application/octet-stream) — tương đương postFileBinary trên web */
    @POST("api/FaceCrop")
    suspend fun faceCrop(@Body body: RequestBody): Response<Any>

    /** Raw binary upload (application/octet-stream) — tương đương postFileBinary trên web */
    @POST("api/FaceCheck")
    suspend fun faceCheck(@Body body: RequestBody): Response<Any>

    @POST("api/FaceImageCrop")
    suspend fun faceImageCrop(
        @Query("profileId") profileId: Int,
        @Body model: FaceImageCrop
    ): Response<FaceImageCrop>

    // ==================== Model Run ====================

    /** Extract text feature — web gửi POST với query params, body rỗng */
    @POST("api/ModelRun")
    suspend fun extractFeatureText(
        @Query("q") q: String,
        @Query("f") f: String
    ): Response<ModelRunTextResponse>

    /** Extract image feature — raw binary upload với query params */
    @POST("api/ModelRun")
    suspend fun extractFeatureImage(
        @Query("f") f: String,
        @Query("raw") raw: String,
        @Body body: RequestBody
    ): Response<ModelRunImageResponse>

    // ==================== License Plate ====================

    @GET("api/LicensePlate")
    suspend fun getLicensePlateList(): Response<List<LicensePlate>>

    @POST("api/LicensePlate")
    suspend fun createLicensePlate(@Body model: LicensePlate): Response<LicensePlate>

    @PUT("api/LicensePlate/{id}")
    suspend fun updateLicensePlate(@Path("id") id: Int, @Body model: LicensePlate): Response<LicensePlate>

    @HTTP(method = "DELETE", path = "api/LicensePlate", hasBody = false)
    suspend fun deleteLicensePlate(@Query("id") mapId: Int): Response<Unit>

    // ==================== System ====================

    @POST("api/SystemControl")
    suspend fun restartService(@Body command: Map<String, String>): Response<Any>
}
