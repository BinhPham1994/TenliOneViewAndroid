package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.CameraMonitor
import com.tenli.oneview.model.network.Face
import com.tenli.oneview.model.network.FaceImageCrop
import com.tenli.oneview.model.network.LicensePlate
import com.tenli.oneview.model.network.ModelRunImageResponse
import com.tenli.oneview.model.network.ModelRunTextResponse
import com.tenli.oneview.model.network.Monitor
import com.tenli.oneview.model.network.MonitorState
import com.tenli.oneview.model.network.MonitorType
import com.tenli.oneview.model.network.PauseAlarmRequest
import com.tenli.oneview.model.network.Profile
import com.tenli.oneview.model.network.ProfileGroup
import com.tenli.oneview.model.network.ProfileInProfileGroup
import com.tenli.oneview.model.network.UpdateProfileInProfileGroup
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface AiApi {

    // ==================== Camera Monitor ====================

    @GET("api/Camera")
    suspend fun getCameraMonitorList(): Response<List<CameraMonitor>>

    @GET
    suspend fun getCameraMonitorListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<CameraMonitor>>

    @POST("api/Camera")
    suspend fun createCameraMonitor(@Body model: CameraMonitor): Response<CameraMonitor>

    @POST
    suspend fun createCameraMonitorExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: CameraMonitor
    ): Response<CameraMonitor>

    // ==================== Monitor ====================

    @GET("api/Monitor")
    suspend fun getMonitorList(): Response<List<Monitor>>

    @GET
    suspend fun getMonitorListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Monitor>>

    @GET("api/MonitorType")
    suspend fun getMonitorTypeList(): Response<List<MonitorType>>

    @GET
    suspend fun getMonitorTypeListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<MonitorType>>

    @GET("api/MonitorState")
    suspend fun getMonitorStateList(): Response<List<MonitorState>>

    @GET
    suspend fun getMonitorStateListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<MonitorState>>

    @GET("api/MonitorParam/{type}")
    suspend fun getMonitorParam(@Path("type") type: String): Response<Any>

    @GET
    suspend fun getMonitorParamExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<Any>

    @POST("api/Monitor")
    suspend fun createMonitor(@Body model: Monitor): Response<Monitor>

    @POST
    suspend fun createMonitorExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: Monitor
    ): Response<Monitor>

    @PUT("api/Monitor/{id}")
    suspend fun updateMonitor(@Path("id") id: Int, @Body model: Monitor): Response<Monitor>

    @PUT
    suspend fun updateMonitorExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: Monitor
    ): Response<Monitor>

    @DELETE("api/Monitor/{id}")
    suspend fun deleteMonitor(@Path("id") id: Int): Response<Unit>

    @DELETE
    suspend fun deleteMonitorExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    // ==================== Profile Group ====================

    @GET("api/ProfileGroup/GetList")
    suspend fun getProfileGroupList(): Response<List<ProfileGroup>>

    @GET
    suspend fun getProfileGroupListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<ProfileGroup>>

    @POST("api/ProfileGroup/Create")
    suspend fun createProfileGroup(@Body model: ProfileGroup): Response<ProfileGroup>

    @POST
    suspend fun createProfileGroupExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: ProfileGroup
    ): Response<ProfileGroup>

    @PUT("api/ProfileGroup/Update/{id}")
    suspend fun updateProfileGroup(@Path("id") id: Int, @Body model: ProfileGroup): Response<ProfileGroup>

    @PUT
    suspend fun updateProfileGroupExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: ProfileGroup
    ): Response<ProfileGroup>

    @DELETE("api/ProfileGroup/Delete/{id}")
    suspend fun deleteProfileGroup(@Path("id") id: Int): Response<Unit>

    @DELETE
    suspend fun deleteProfileGroupExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    @POST("api/ProfileGroup/AddProfile")
    suspend fun createProfileInProfileGroup(@Body model: ProfileInProfileGroup): Response<ProfileInProfileGroup>

    @POST
    suspend fun createProfileInProfileGroupExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: ProfileInProfileGroup
    ): Response<ProfileInProfileGroup>

    @GET("api/ProfileGroup/ProfileMap")
    suspend fun getProfileMap(@Query("profileId") profileId: Int): Response<List<ProfileInProfileGroup>>

    @GET
    suspend fun getProfileMapExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Query("profileId") profileId: Int? = null
    ): Response<List<ProfileInProfileGroup>>

    @HTTP(method = "DELETE", path = "api/ProfileGroup/DeleteProfile", hasBody = false)
    suspend fun deleteProfileFromProfileGroup(@Query("id") mapId: Int): Response<Unit>

    @DELETE
    suspend fun deleteProfileFromProfileGroupExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Query("id") mapId: Int? = null
    ): Response<Unit>

    @POST("api/ProfileGroup/UpdateProfile")
    suspend fun updateProfileInProfileGroup(@Body model: UpdateProfileInProfileGroup): Response<UpdateProfileInProfileGroup>

    @POST
    suspend fun updateProfileInProfileGroupExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: UpdateProfileInProfileGroup
    ): Response<UpdateProfileInProfileGroup>

    // ==================== Profile ====================

    @GET("api/Profile")
    suspend fun getProfileList(): Response<List<Profile>>

    @GET
    suspend fun getProfileListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Profile>>

    @POST("api/Profile")
    suspend fun createProfile(@Body model: Profile): Response<Profile>

    @POST
    suspend fun createProfileExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: Profile
    ): Response<Profile>

    @PUT("api/Profile/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body model: Profile): Response<Profile>

    @PUT
    suspend fun updateProfileExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: Profile
    ): Response<Profile>

    @DELETE("api/Profile/{id}")
    suspend fun deleteProfile(@Path("id") id: Int): Response<Unit>

    @DELETE
    suspend fun deleteProfileExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    // ==================== Face ====================

    @GET("api/Face")
    suspend fun getFaceList(@Query("profileId") profileId: Int): Response<List<Face>>

    @GET
    suspend fun getFaceListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Query("profileId") profileId: Int? = null
    ): Response<List<Face>>

    @DELETE("api/Face/{faceId}")
    suspend fun deleteFace(@Path("faceId") faceId: Int): Response<Unit>

    @DELETE
    suspend fun deleteFaceExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<Unit>

    /** Raw binary upload (application/octet-stream) — tương đương postFileBinary trên web */
    @POST("api/FaceCrop")
    suspend fun faceCrop(@Body body: RequestBody): Response<Any>

    @POST
    suspend fun faceCropExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body body: RequestBody
    ): Response<Any>

    /** Raw binary upload (application/octet-stream) — tương đương postFileBinary trên web */
    @POST("api/FaceCheck")
    suspend fun faceCheck(@Body body: RequestBody): Response<Any>

    @POST
    suspend fun faceCheckExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body body: RequestBody
    ): Response<Any>

    @POST("api/FaceImageCrop")
    suspend fun faceImageCrop(
        @Query("profileId") profileId: Int,
        @Body model: FaceImageCrop
    ): Response<FaceImageCrop>

    @POST
    suspend fun faceImageCropExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Query("profileId") profileId: Int? = null,
        @Body model: FaceImageCrop
    ): Response<FaceImageCrop>

    @GET("api/FaceEnroll/{cameraId}")
    suspend fun faceEnroll(@Path("cameraId") cameraId: Int): Response<List<String>>

    @GET("api/FaceEnroll/{cameraId}")
    suspend fun faceEnroll(@Path("cameraId") cameraId: String): Response<List<String>>

    @GET
    suspend fun faceEnrollExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<String>>

    // ==================== Model Run ====================

    /** Extract text feature — web gửi POST với query params, body rỗng */
    @POST("api/ModelRun")
    suspend fun extractFeatureText(
        @Query("q") q: String,
        @Query("f") f: String
    ): Response<ModelRunTextResponse>

    @POST
    suspend fun extractFeatureTextExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Query("q") q: String? = null,
        @Query("f") f: String? = null,
        @Body body: Map<String, Any> = emptyMap()
    ): Response<ModelRunTextResponse>

    /** Extract image feature — raw binary upload với query params */
    @POST("api/ModelRun")
    suspend fun extractFeatureImage(
        @Query("f") f: String,
        @Query("raw") raw: String,
        @Body body: RequestBody
    ): Response<ModelRunImageResponse>

    @POST
    suspend fun extractFeatureImageExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Query("f") f: String? = null,
        @Query("raw") raw: String? = null,
        @Body body: RequestBody
    ): Response<ModelRunImageResponse>

    // ==================== License Plate ====================

    @GET("api/LicensePlate")
    suspend fun getLicensePlateList(): Response<List<LicensePlate>>

    @GET
    suspend fun getLicensePlateListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<LicensePlate>>

    @POST("api/LicensePlate")
    suspend fun createLicensePlate(@Body model: LicensePlate): Response<LicensePlate>

    @POST
    suspend fun createLicensePlateExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: LicensePlate
    ): Response<LicensePlate>

    @PUT("api/LicensePlate/{id}")
    suspend fun updateLicensePlate(@Path("id") id: Int, @Body model: LicensePlate): Response<LicensePlate>

    @PUT
    suspend fun updateLicensePlateExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body model: LicensePlate
    ): Response<LicensePlate>

    @HTTP(method = "DELETE", path = "api/LicensePlate", hasBody = false)
    suspend fun deleteLicensePlate(@Query("id") mapId: Int): Response<Unit>

    @DELETE
    suspend fun deleteLicensePlateExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Query("id") mapId: Int? = null
    ): Response<Unit>

    // ==================== System & Alarm ====================

    @POST("api/SystemControl")
    suspend fun restartService(@Body command: Map<String, String>): Response<Any>

    @POST
    suspend fun restartServiceExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body command: Map<String, String>
    ): Response<Any>

    @POST("nr/pause-alarm")
    suspend fun pauseAlarm(@Body request: PauseAlarmRequest): Response<Any>

    @POST
    suspend fun pauseAlarmExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body request: PauseAlarmRequest = PauseAlarmRequest()
    ): Response<Any>

    @POST
    suspend fun pauseAlarmMapExt(
        @Url url: String,
        @Header("Authorization") authHeader: String,
        @Body body: Map<String, Any> = emptyMap()
    ): Response<Any>
}
