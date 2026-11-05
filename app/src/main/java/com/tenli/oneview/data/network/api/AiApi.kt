package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.Monitor
import com.tenli.oneview.model.network.Profile
import com.tenli.oneview.model.network.ProfileGroup
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface AiApi {

    // ==================== Monitor ====================

    @GET("api/Monitor")
    suspend fun getMonitorList(): Response<List<Monitor>>

    @GET
    suspend fun getMonitorListExt(
        @Url url: String,
        @Header("Authorization") authHeader: String
    ): Response<List<Monitor>>

    // ==================== Profile Group ====================

    @GET("api/ProfileGroup/GetList")
    suspend fun getProfileGroupList(): Response<List<ProfileGroup>>

    // ==================== Profile ====================

    @GET("api/Profile")
    suspend fun getProfileList(): Response<List<Profile>>
}
