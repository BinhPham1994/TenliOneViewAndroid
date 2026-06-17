package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.BaseResponse
import com.tenli.oneview.model.network.GroupDetailData
import com.tenli.oneview.model.network.HomeGroup
import com.tenli.oneview.model.network.ShareCodeData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface GroupApi {
    @GET("user/userItemGroup/get-list")
    suspend fun getListGroup(): Response<BaseResponse<List<HomeGroup>>>

    @GET("user/userItemGroup/get/{id}")
    suspend fun getGroupDetail(
        @Path("id") id: Int
    ): Response<BaseResponse<GroupDetailData>>

    @FormUrlEncoded
    @POST("user/userItemGroup/leave/{id}")
    suspend fun leaveGroup(
        @Path("id") id: Int,
        @Field("force") force: Boolean = true
    ): Response<Any?>

    @FormUrlEncoded
    @POST("user/userItemGroup/create")
    suspend fun createGroup(
        @Field("name") name: String,
        @Field("itemType") itemType: String = "device"
    ): Response<Any?>

    @PUT("user/userItemGroup/update/{id}")
    suspend fun editGroup(
        @Path("id") id: Int,
        @Body raw: Any
    ): Response<Any?>

    @DELETE("user/userItemGroup/delete/{id}")
    suspend fun deleteGroup(
        @Path("id") id: Int
    ): Response<Any?>

    @FormUrlEncoded
    @POST("user/userItemGroup/create-share-code/{id}")
    suspend fun createShareCode(
        @Path("id") id: Int,
        @Field("role") role: String,
        @Field("createNew") createNew: Boolean = false,
        @Field("expiredIn") expiredIn: Int? = null
    ): Response<BaseResponse<ShareCodeData>>

    @FormUrlEncoded
    @POST("user/userItemGroup/join")
    suspend fun joinGroup(
        @Field("shareCode") shareCode: String
    ): Response<Any?>

    @FormUrlEncoded
    @POST("user/userItemGroup/remove-user/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int,
        @Field("userId") userId: Int
    ): Response<Any?>
}