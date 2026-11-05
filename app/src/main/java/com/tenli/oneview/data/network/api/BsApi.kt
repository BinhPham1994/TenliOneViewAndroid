package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.BsProfileGroupModel
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.model.network.UserNotificationSettingsModel
import com.tenli.oneview.model.network.VmsCountOverviewModel
import com.tenli.oneview.model.network.VmsEventCountByTypeModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface BsApi {

    // ==================== Profile Group ====================

    @GET("ts/api/v1/profile-groups")
    suspend fun getProfileGroupList(@Query("mode") mode: String = "detail"): Response<List<BsProfileGroupModel>>

    // ==================== Profile ====================

    @GET("ts/api/v1/profiles")
    suspend fun getProfileList(@Query("mode") mode: String = "detail"): Response<List<BsProfileModel>>

    // ==================== Statistics & Counts ====================

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

    // ==================== Uniform Data & Config ====================

    @GET("ts/api/v1/e/uniform/get-data")
    suspend fun getUniformData(
        @QueryMap params: Map<String, String> = emptyMap(),
        @Query("excludeUniformLabel") excludeUniformLabel: List<String>? = null
    ): Response<List<EventData>>

    // ==================== Event Config & Notification Settings ====================

    @GET("ts/api/v1/user/settings/notification_settings")
    suspend fun getUserNotificationSettings(): Response<UserNotificationSettingsModel>

    @PUT("ts/api/v1/user/settings/notification_settings")
    suspend fun updateUserNotificationSettings(@Body model: UserNotificationSettingsModel): Response<UserNotificationSettingsModel>
}
