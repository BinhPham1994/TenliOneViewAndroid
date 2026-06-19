package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.*
import retrofit2.Response
import retrofit2.http.*

interface EventApi {

    // ==================== AI Service ====================

    @GET("Data/api/Service")
    suspend fun getAIServiceList(): Response<List<AIServiceModel>>

    @POST("Data/api/Service")
    suspend fun createAIService(@Body model: AIServiceModel): Response<AIServiceModel>

    @PUT("Data/api/Service/{id}")
    suspend fun updateAIService(@Path("id") id: Int, @Body model: AIServiceModel): Response<AIServiceModel>

    @DELETE("Data/api/Service/{id}")
    suspend fun deleteAIService(@Path("id") id: Int): Response<Unit>

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
        @Query("cameraUUID") cameraUUID: String? = null
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

    // ==================== Rule ====================

    @GET("Data/api/Rule")
    suspend fun getRuleList(): Response<List<RuleModel>>

    @POST("Data/api/Rule")
    suspend fun createRule(@Body model: RuleModel): Response<RuleModel>

    @PUT("Data/api/Rule/{id}")
    suspend fun updateRule(@Path("id") id: Int, @Body model: RuleModel): Response<RuleModel>

    @DELETE("Data/api/Rule/{id}")
    suspend fun deleteRule(@Path("id") id: Int): Response<Unit>
}
