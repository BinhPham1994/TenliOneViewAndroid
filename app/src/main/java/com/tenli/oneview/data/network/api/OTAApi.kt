package com.tenli.oneview.data.network.api

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface OTAApi {
    @GET("ota/get-last")
    fun getLastOTA(
    ): Call<Any?>?

    @GET("/ota/default/quick-status")
    fun getStatus(
    ): Call<Any?>?

    @POST("/ota/default/quick-install")
    fun install(
        @Body raw: JsonObject
    ): Call<Any?>?
}