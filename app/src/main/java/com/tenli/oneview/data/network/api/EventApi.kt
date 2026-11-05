package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.AIServiceModel
import com.tenli.oneview.model.network.Confirm
import com.tenli.oneview.model.network.EventData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface EventApi {

    // ==================== AI Service ====================

    @GET("Data/api/Service")
    suspend fun getAIServiceList(): Response<List<AIServiceModel>>

    // ==================== Event Data ====================

    @GET("Data/api/Data")
    suspend fun getDataList(
        @Query("order") order: String = "desc",
        @Query("isConfirmed") isConfirmed: String = "1",
        @Query("lastId") lastId: Int? = null,
        @Query("count") count: Int? = null,
        @Query("serviceId") serviceId: Int? = null,
        @Query("type") type: String? = null,
        @Query("from") from: Long? = null,
        @Query("to") to: Long? = null,
        @Query("cameraUUID") cameraUUID: String? = null,
        @QueryMap extraParams: Map<String, String> = emptyMap()
    ): Response<List<EventData>>

    @GET("Data/api/FaceData/{profileUUID}")
    suspend fun getFaceDataByProfileUUID(
        @Path("profileUUID") profileUUID: String,
        @Query("order") order: String = "desc",
        @Query("count") count: Int? = null,
        @Query("lastId") lastId: Int? = null,
        @Query("from") from: Long? = null,
        @Query("to") to: Long? = null,
        @Query("serviceId") serviceId: Int? = null,
        @Query("cameraUUID") cameraUUID: String? = null,
        @QueryMap extraParams: Map<String, String> = emptyMap()
    ): Response<List<EventData>>

    // ==================== Data Search ====================

    @POST("Data/api/DataSearch")
    suspend fun searchAIData(
        @Query("from") from: Long? = null,
        @Query("to") to: Long? = null,
        @Query("serviceId") serviceId: Int? = null,
        @Query("count") count: Int? = null,
        @Query("fast") fast: String = "no",
        @Body searchItems: List<Any>
    ): Response<List<EventData>>

    // ==================== Verify Event ====================

    @POST("Data/api/Data/confirm/{id}")
    suspend fun verifyEvent(@Path("id") id: Int, @Body confirm: Confirm): Response<Unit>

    @POST("Data/api/Data/confirm/{id}")
    suspend fun verifyEvent(@Path("id") id: String, @Body confirm: Confirm): Response<Unit>
}
