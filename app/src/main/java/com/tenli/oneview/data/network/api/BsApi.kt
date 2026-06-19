package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface BsApi {

    // ==================== Plate ====================

    @GET("ts/api/v1/plates")
    suspend fun getPlateList(): Response<List<PlateModel>>

    @POST("ts/api/v1/plates/create")
    suspend fun createPlate(@Body model: PlateModel): Response<PlateModel>

    @PUT("ts/api/v1/plates/update/{id}")
    suspend fun updatePlate(@Path("id") id: Int, @Body model: PlateModel): Response<PlateModel>

    @DELETE("ts/api/v1/plates/delete/{id}")
    suspend fun deletePlate(@Path("id") id: Int): Response<Unit>

    // ==================== Contact ====================

    @GET("ts/api/v1/contacts")
    suspend fun getContactList(): Response<List<ContractModel>>

    @POST("ts/api/v1/contacts")
    suspend fun createContact(@Body model: ContractModel): Response<ContractModel>

    @PUT("ts/api/v1/contacts/{id}")
    suspend fun updateContact(@Path("id") id: Int, @Body model: ContractModel): Response<ContractModel>

    @DELETE("ts/api/v1/contacts/{id}")
    suspend fun deleteContact(@Path("id") id: Int): Response<Unit>

    // ==================== Profile Group ====================

    @GET("ts/api/v1/profile-groups")
    suspend fun getProfileGroupList(@Query("mode") mode: String = "detail"): Response<List<BsProfileGroupModel>>

    @POST("ts/api/v1/profile-groups")
    suspend fun createProfileGroup(@Body model: BsProfileGroupModel): Response<BsProfileGroupModel>

    @PUT("ts/api/v1/profile-groups/{id}")
    suspend fun updateProfileGroup(@Path("id") id: Int, @Body model: BsProfileGroupModel): Response<BsProfileGroupModel>

    @DELETE("ts/api/v1/profile-groups/{id}")
    suspend fun deleteProfileGroup(@Path("id") id: Int): Response<Unit>

    // ==================== Profile ====================

    @GET("ts/api/v1/profiles")
    suspend fun getProfileList(@Query("mode") mode: String = "detail"): Response<List<BsProfileModel>>

    @POST("ts/api/v1/profiles")
    suspend fun createProfile(@Body model: BsProfileModel): Response<BsProfileModel>

    @PUT("ts/api/v1/profiles/{id}")
    suspend fun updateProfile(@Path("id") id: Int, @Body model: BsProfileModel): Response<BsProfileModel>

    @DELETE("ts/api/v1/profiles/{id}")
    suspend fun deleteProfile(@Path("id") id: Int): Response<Unit>

    // ==================== Profile Face ====================

    @GET("ts/api/v1/profiles/{profileId}/faces")
    suspend fun getProfileFaces(@Path("profileId") profileId: Int): Response<List<ImageProfileModel>>

    @POST("ts/api/v1/profiles/{profileId}/faces")
    suspend fun createProfileFace(
        @Path("profileId") profileId: Int,
        @Body model: ImageProfileModel
    ): Response<ImageProfileModel>

    @DELETE("ts/api/v1/profiles/{profileId}/faces/{faceId}")
    suspend fun deleteProfileFace(
        @Path("profileId") profileId: Int,
        @Path("faceId") faceId: Int
    ): Response<Unit>

    // ==================== Task ====================

    @GET("ts/api/v1/tasks")
    suspend fun getTaskList(): Response<List<TaskModel>>

    @POST("ts/api/v1/tasks")
    suspend fun createTask(@Body model: TaskModel): Response<TaskModel>

    @PUT("ts/api/v1/tasks/{id}")
    suspend fun updateTask(@Path("id") id: Int, @Body model: TaskModel): Response<TaskModel>

    @DELETE("ts/api/v1/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: Int): Response<Unit>

    // ==================== Statistics & Counts ====================

    @GET("ts/api/v1/e/vehical/summary-by-time")
    suspend fun getVehicleStatsOverTime(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long
    ): Response<List<VehicleStatisticalOverTimeModel>>

    @GET("ts/api/v1/e/vehical/count-by-label")
    suspend fun getVehicleQuickCount(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long
    ): Response<List<VehicleQuickCountModel>>

    @GET("ts/api/v1/e/vehical/count-by-prefix")
    suspend fun getVehicleCountByPrefix(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long
    ): Response<List<VehicleCountByPrefixModel>>

    @GET("ts/api/v1/e/uniform/count-by-label")
    suspend fun getUniformCountByLabel(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = null
    ): Response<List<UniformCountByLabelModel>>

    @GET("ts/api/v1/e/vms-event/summary-by-time")
    suspend fun getVmsEventStatsOverTime(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("eventType") eventType: String? = null,
        @Query("dataEventType") dataEventType: String? = null
    ): Response<List<VmsEventStatisticalOverTimeModel>>

    @GET("ts/api/v1/e/vms-core/count-overview")
    suspend fun getVmsCountOverview(
        @Query("serviceId") serviceId: String = "-1",
        @Query("fromTime") fromTime: Long? = null,
        @Query("toTime") toTime: Long? = null,
        @Query("eventType") eventType: String? = null,
        @Query("dataEventType") dataEventType: String? = null
    ): Response<List<VmsCountOverviewModel>>

    @GET("ts/api/v1/e/vms-event/count-by-type")
    suspend fun getVmsEventCountByType(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long
    ): Response<List<VmsEventCountByTypeModel>>

    @GET("ts/api/v1/e/vms-event/count-by-camera")
    suspend fun getVmsEventCountByCamera(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("eventType") eventType: String? = null,
        @Query("dataEventType") dataEventType: String? = null
    ): Response<List<VmsEventCountByCameraModel>>

    // ==================== File Upload ====================

    @Multipart
    @POST("ts/api/v1/files/upload")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part,
        @Part("collection") collection: okhttp3.RequestBody
    ): Response<FileUploadResponse>

    // ==================== Food Traceability ====================

    @GET("qltp/api/Data")
    suspend fun getFoodList(): Response<List<FoodModel>>

    @POST("qltp/api/Data")
    suspend fun createFoodTraceability(@Body model: FoodTraceability): Response<FoodTraceability>

    @DELETE("qltp/api/Data/{id}")
    suspend fun deleteFoodItem(@Path("id") id: Int): Response<Unit>
}
