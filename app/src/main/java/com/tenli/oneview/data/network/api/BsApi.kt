package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.AttendanceAssignmentModel
import com.tenli.oneview.model.network.AttendanceDataModel
import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.ContractModel
import com.tenli.oneview.model.network.EventConfigGroupModel
import com.tenli.oneview.model.network.EventConfigModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.EventProfileGroupCountModel
import com.tenli.oneview.model.network.FaceRecognitionCountByTimeModel
import com.tenli.oneview.model.network.FileUploadResponse
import com.tenli.oneview.model.network.FoodModel
import com.tenli.oneview.model.network.FoodTraceability
import com.tenli.oneview.model.network.HolidayModel
import com.tenli.oneview.model.network.ImageProfileModel
import com.tenli.oneview.model.network.NotificationTokenRegisterModel
import com.tenli.oneview.model.network.OneviewConfigModel
import com.tenli.oneview.model.network.PlateGroupModel
import com.tenli.oneview.model.network.PlateModel
import com.tenli.oneview.model.network.ProfileShiftConfigModel
import com.tenli.oneview.model.network.PublicConfigModel
import com.tenli.oneview.model.network.ShiftModel
import com.tenli.oneview.model.network.TaskModel
import com.tenli.oneview.model.network.UniformAssignmentModel
import com.tenli.oneview.model.network.UniformComplianceEvaluationItem
import com.tenli.oneview.model.network.UniformConfigModel
import com.tenli.oneview.model.network.UniformCountByLabelModel
import com.tenli.oneview.model.network.UniformLabelModel
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.model.network.VehicleCountByPrefixModel
import com.tenli.oneview.model.network.VehicleQuickCountModel
import com.tenli.oneview.model.network.VehicleStatisticalOverTimeModel
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByCameraAndTypeModel
import com.tenli.oneview.model.network.VmsEventCountByCameraModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import com.tenli.oneview.model.network.VmsEventStatisticalOverTimeModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Streaming
import retrofit2.http.Url

interface BsApi {

    // ==================== Plate ====================

    @GET("ts/api/v1/plates")
    suspend fun getPlateList(
        @Query("withGroups") withGroups: Boolean = true
    ): Response<List<PlateModel>>

    @POST("ts/api/v1/plates/create")
    suspend fun createPlate(@Body model: PlateModel): Response<PlateModel>

    @PUT("ts/api/v1/plates/update/{id}")
    suspend fun updatePlate(@Path("id") id: Int, @Body model: PlateModel): Response<PlateModel>

    @DELETE("ts/api/v1/plates/delete/{id}")
    suspend fun deletePlate(@Path("id") id: Int): Response<Unit>

    // ==================== Plate Group ====================

    @GET("ts/api/v1/plate-groups")
    suspend fun getPlateGroupList(
        @Query("mode") mode: String = "detail"
    ): Response<List<PlateGroupModel>>

    @POST("ts/api/v1/plate-groups")
    suspend fun createPlateGroup(@Body model: PlateGroupModel): Response<PlateGroupModel>

    @PUT("ts/api/v1/plate-groups/{id}")
    suspend fun updatePlateGroup(
        @Path("id") id: Int,
        @Body model: PlateGroupModel
    ): Response<PlateGroupModel>

    @PUT("ts/api/v1/plate-groups/{id}")
    suspend fun updatePlateGroup(
        @Path("id") idOrUuid: String,
        @Body model: PlateGroupModel
    ): Response<PlateGroupModel>

    @DELETE("ts/api/v1/plate-groups/{id}")
    suspend fun deletePlateGroup(@Path("id") id: Int): Response<Unit>

    @DELETE("ts/api/v1/plate-groups/{id}")
    suspend fun deletePlateGroup(@Path("id") idOrUuid: String): Response<Unit>

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
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = null
    ): Response<List<VehicleStatisticalOverTimeModel>>

    @GET("ts/api/v1/e/vehical/count-by-label")
    suspend fun getVehicleQuickCount(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = null
    ): Response<List<VehicleQuickCountModel>>

    @GET("ts/api/v1/e/vehical/count-by-prefix")
    suspend fun getVehicleCountByPrefix(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = null
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
        @Query("dataEventType") dataEventType: String? = null,
        @Query("serviceId") serviceId: String? = null
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
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = null
    ): Response<List<VmsEventCountByTypeModel>>

    @GET("ts/api/v1/e/vms-event/count-by-camera")
    suspend fun getVmsEventCountByCamera(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("eventType") eventType: String? = null,
        @Query("dataEventType") dataEventType: String? = null,
        @Query("serviceId") serviceId: String? = null
    ): Response<List<VmsEventCountByCameraModel>>

    @GET("ts/api/v1/e/vms-event/count-by-camera-and-type")
    suspend fun getVmsEventCountByCameraAndType(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = null,
        @Query("eventType") eventType: String? = null,
        @Query("dataEventType") dataEventType: String? = null
    ): Response<List<VmsEventCountByCameraAndTypeModel>>

    // ==================== Profile Async Import/Export & Tasks ====================

    @Multipart
    @POST("ts/api/v1/profiles/import-async")
    suspend fun importProfilesAsync(
        @Part file: MultipartBody.Part
    ): Response<Map<String, Any>>

    @POST("ts/api/v1/profiles/export-async")
    suspend fun exportProfilesAsync(@Body body: Map<String, Any> = emptyMap()): Response<Map<String, Any>>

    @GET("ts/api/v1/tasks/history/{historyId}")
    suspend fun getTaskHistory(@Path("historyId") historyId: Int): Response<Map<String, Any>>

    @GET("ts/api/v1/tasks/history/{historyId}")
    suspend fun getTaskHistory(@Path("historyId") historyId: String): Response<Map<String, Any>>

    @POST("ts/api/v1/files/link")
    suspend fun createFileLink(@Body body: Map<String, Any>): Response<Map<String, Any>>

    /** Tải file nhị phân (ví dụ file Excel export hồ sơ từ downloadUrl) — tương đương api.getExtBinary trên web */
    @Streaming
    @GET
    suspend fun downloadFileExt(
        @Url url: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<ResponseBody>

    // ==================== Attendance & Shifts & Holidays ====================

    @GET("ts/api/v1/attendance/shifts")
    suspend fun getShiftList(): Response<List<ShiftModel>>

    @POST("ts/api/v1/attendance/shifts")
    suspend fun createShift(@Body model: ShiftModel): Response<ShiftModel>

    @PUT("ts/api/v1/attendance/shifts/{id}")
    suspend fun updateShift(@Path("id") id: Int, @Body model: ShiftModel): Response<ShiftModel>

    @PUT("ts/api/v1/attendance/shifts/{id}")
    suspend fun updateShift(@Path("id") idOrUuid: String, @Body model: ShiftModel): Response<ShiftModel>

    @DELETE("ts/api/v1/attendance/shifts/{id}")
    suspend fun deleteShift(@Path("id") id: Int): Response<Unit>

    @DELETE("ts/api/v1/attendance/shifts/{id}")
    suspend fun deleteShift(@Path("id") idOrUuid: String): Response<Unit>

    @GET("ts/api/v1/attendance/holidays")
    suspend fun getHolidayList(): Response<List<HolidayModel>>

    @POST("ts/api/v1/attendance/holidays")
    suspend fun createHoliday(@Body model: HolidayModel): Response<HolidayModel>

    @PUT("ts/api/v1/attendance/holidays/{id}")
    suspend fun updateHoliday(@Path("id") id: Int, @Body model: HolidayModel): Response<HolidayModel>

    @PUT("ts/api/v1/attendance/holidays/{id}")
    suspend fun updateHoliday(@Path("id") idOrUuid: String, @Body model: HolidayModel): Response<HolidayModel>

    @DELETE("ts/api/v1/attendance/holidays/{id}")
    suspend fun deleteHoliday(@Path("id") id: Int): Response<Unit>

    @DELETE("ts/api/v1/attendance/holidays/{id}")
    suspend fun deleteHoliday(@Path("id") idOrUuid: String): Response<Unit>

    @GET("ts/api/v1/attendance/assignments")
    suspend fun getAttendanceAssignmentList(): Response<List<AttendanceAssignmentModel>>

    @POST("ts/api/v1/attendance/assignments")
    suspend fun createAttendanceAssignment(@Body model: AttendanceAssignmentModel): Response<AttendanceAssignmentModel>

    @PUT("ts/api/v1/attendance/assignments/{id}")
    suspend fun updateAttendanceAssignment(@Path("id") id: Int, @Body model: AttendanceAssignmentModel): Response<AttendanceAssignmentModel>

    @PUT("ts/api/v1/attendance/assignments/{id}")
    suspend fun updateAttendanceAssignment(@Path("id") idOrUuid: String, @Body model: AttendanceAssignmentModel): Response<AttendanceAssignmentModel>

    @DELETE("ts/api/v1/attendance/assignments/{id}")
    suspend fun deleteAttendanceAssignment(@Path("id") id: Int): Response<Unit>

    @DELETE("ts/api/v1/attendance/assignments/{id}")
    suspend fun deleteAttendanceAssignment(@Path("id") idOrUuid: String): Response<Unit>

    @GET("ts/api/v1/e/attendance/dataWithShift")
    suspend fun getAttendanceDataWithShift(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = null,
        @Query("timezone") timezone: String? = null,
        @Query("eventType") eventType: String? = null
    ): Response<List<AttendanceDataModel>>

    @GET("ts/api/v1/e/attendance/eventProfileGroupCount")
    suspend fun getEventProfileGroupCount(
        @Query("fromTime") fromTime: Long? = null,
        @Query("toTime") toTime: Long? = null,
        @Query("serviceId") serviceId: String? = null,
        @Query("timezone") timezone: String? = null,
        @Query("eventType") eventType: String? = null,
        @Query("recursive") recursive: Boolean = true
    ): Response<List<EventProfileGroupCountModel>>

    @GET("ts/api/v1/e/attendance/profileShiftConfig")
    suspend fun getProfileShiftConfig(
        @Query("fromTime") fromTime: Long? = null,
        @Query("toTime") toTime: Long? = null,
        @Query("serviceId") serviceId: String? = null,
        @Query("timezone") timezone: String? = null
    ): Response<List<ProfileShiftConfigModel>>

    @GET("ts/api/v1/e/attendance/count-profile-by-time")
    suspend fun getFaceRecognitionCountByTime(
        @Query("fromTime") fromTime: Long,
        @Query("toTime") toTime: Long,
        @Query("serviceId") serviceId: String? = "-1",
        @Query("timezone") timezone: String? = null,
        @Query("eventType") eventType: String? = "logic-face"
    ): Response<List<FaceRecognitionCountByTimeModel>>

    // ==================== Uniform Data & Config ====================

    @GET("ts/api/v1/e/uniform/get-data")
    suspend fun getUniformData(
        @QueryMap params: Map<String, String> = emptyMap(),
        @Query("excludeUniformLabel") excludeUniformLabel: List<String>? = null
    ): Response<List<EventData>>

    @GET("ts/api/v1/e/uniform/complianceEvaluation")
    suspend fun getUniformComplianceEvaluation(
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<List<UniformComplianceEvaluationItem>>

    @GET("ts/api/v1/uniform-configs/available-labels")
    suspend fun getUniformAvailableLabels(): Response<Any>

    @GET("ts/api/v1/uniform-configs")
    suspend fun getUniformConfigList(): Response<List<UniformConfigModel>>

    @POST("ts/api/v1/uniform-configs")
    suspend fun createUniformConfig(@Body model: UniformConfigModel): Response<UniformConfigModel>

    @PUT("ts/api/v1/uniform-configs/{id}")
    suspend fun updateUniformConfig(@Path("id") id: Int, @Body model: UniformConfigModel): Response<UniformConfigModel>

    @PUT("ts/api/v1/uniform-configs/{id}")
    suspend fun updateUniformConfig(@Path("id") idOrUuid: String, @Body model: UniformConfigModel): Response<UniformConfigModel>

    @DELETE("ts/api/v1/uniform-configs/{id}")
    suspend fun deleteUniformConfig(@Path("id") id: Int): Response<Unit>

    @DELETE("ts/api/v1/uniform-configs/{id}")
    suspend fun deleteUniformConfig(@Path("id") idOrUuid: String): Response<Unit>

    @GET("ts/api/v1/uniform-assignments")
    suspend fun getUniformAssignmentList(): Response<List<UniformAssignmentModel>>

    @POST("ts/api/v1/uniform-assignments")
    suspend fun createUniformAssignment(@Body model: UniformAssignmentModel): Response<UniformAssignmentModel>

    @PUT("ts/api/v1/uniform-assignments/{id}")
    suspend fun updateUniformAssignment(@Path("id") id: Int, @Body model: UniformAssignmentModel): Response<UniformAssignmentModel>

    @PUT("ts/api/v1/uniform-assignments/{id}")
    suspend fun updateUniformAssignment(@Path("id") idOrUuid: String, @Body model: UniformAssignmentModel): Response<UniformAssignmentModel>

    @DELETE("ts/api/v1/uniform-assignments/{id}")
    suspend fun deleteUniformAssignment(@Path("id") id: Int): Response<Unit>

    @DELETE("ts/api/v1/uniform-assignments/{id}")
    suspend fun deleteUniformAssignment(@Path("id") idOrUuid: String): Response<Unit>

    @GET("ts/api/v1/uniform-labels")
    suspend fun getUniformLabelList(
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<List<UniformLabelModel>>

    @POST("ts/api/v1/uniform-labels")
    suspend fun createUniformLabel(@Body model: UniformLabelModel): Response<UniformLabelModel>

    @PUT("ts/api/v1/uniform-labels/{id}")
    suspend fun updateUniformLabel(@Path("id") id: Int, @Body model: UniformLabelModel): Response<UniformLabelModel>

    @PUT("ts/api/v1/uniform-labels/{id}")
    suspend fun updateUniformLabel(@Path("id") idOrUuid: String, @Body model: UniformLabelModel): Response<UniformLabelModel>

    @DELETE("ts/api/v1/uniform-labels/{id}")
    suspend fun deleteUniformLabel(@Path("id") id: Int): Response<Unit>

    @DELETE("ts/api/v1/uniform-labels/{id}")
    suspend fun deleteUniformLabel(@Path("id") idOrUuid: String): Response<Unit>

    // ==================== Event Config & Notification Settings ====================

    @GET("ts/api/v1/event-configs/groups")
    suspend fun getEventConfigGroupList(): Response<List<EventConfigGroupModel>>

    @GET("ts/api/v1/event-configs")
    suspend fun getEventConfigList(): Response<List<EventConfigModel>>

    @GET("ts/api/v1/user/settings/notification_settings")
    suspend fun getUserNotificationSettings(): Response<UserNotificationSettingsModel>

    @PUT("ts/api/v1/user/settings/notification_settings")
    suspend fun updateUserNotificationSettings(@Body model: UserNotificationSettingsModel): Response<UserNotificationSettingsModel>

    @GET("ts/api/v1/config/public")
    suspend fun getPublicConfig(
        @Query("client_id") clientId: String = "oneview-web"
    ): Response<PublicConfigModel>

    @GET("ts/api/v1/e/oneview-config")
    suspend fun getOneviewConfig(): Response<OneviewConfigModel>

    @POST("ts/api/v1/notification-tokens")
    suspend fun registerNotificationToken(@Body model: NotificationTokenRegisterModel): Response<Any>

    @DELETE("ts/api/v1/notification-tokens/{token}")
    suspend fun unregisterNotificationToken(@Path("token") token: String): Response<Any>

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
