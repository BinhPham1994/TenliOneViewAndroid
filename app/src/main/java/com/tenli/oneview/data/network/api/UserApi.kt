package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.BaseResponse
import com.tenli.oneview.model.network.ClientDevice
import com.tenli.oneview.model.network.LoginRequest
import com.tenli.oneview.model.network.LoginResponseData
import com.tenli.oneview.model.network.OTPData
import com.tenli.oneview.model.network.RefreshTokenData
import com.tenli.oneview.model.network.UpdateUserRequest
import com.tenli.oneview.model.network.UserData
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {
    @Headers("Content-Type: application/json")
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginResponseData>>

    @FormUrlEncoded
    @POST("user/create")
    suspend fun create(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("verifyAuthCode") verifyAuthCode: String
    ): Response<BaseResponse<Unit>>

    @FormUrlEncoded
    @POST("user/create-new-password")
    suspend fun resetPassword(
        @Field("email") email: String,
        @Field("new_password") newPassword: String,
        @Field("verifyAuthCode") verifyAuthCode: String
    ): Response<BaseResponse<Unit>>

    @FormUrlEncoded
    @POST("auth/token")
    fun refreshTokenSync(
        @Field("token") token: String,
        @Field("grant_type") grantType: String = "refresh_token"
    ): Call<BaseResponse<RefreshTokenData>>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>

    @GET("user")
    suspend fun getInfoUser(): Response<BaseResponse<Unit>>

    @GET("system/ping")
    suspend fun ping(): Response<BaseResponse<Unit>>

    @FormUrlEncoded
    @POST("user/change-password")
    suspend fun changePassword(
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String
    ): Response<BaseResponse<Unit>>

    @GET("auth/user-app-client/get-list")
    suspend fun getClients(
    ): Response<BaseResponse<List<ClientDevice>>>

    @FormUrlEncoded
    @POST("auth/user-app-client/remove/{id}")
    suspend fun logoutClient(
        @Path("id") id: Int,
        @Field("force") force: Boolean = true
    ): Response<BaseResponse<Any?>>

    @FormUrlEncoded
    @POST("verify/send-verify-email")
    suspend fun verifyEmail(
        @Field("email") email: String,
        @Field("existRequired") existRequired: Boolean
    ): Response<BaseResponse<Unit>>

    @FormUrlEncoded
    @POST("verify/verify-email")
    suspend fun verifyOTP(
        @Field("email") email: String,
        @Field("otpCode") otpCode: String
    ): Response<BaseResponse<OTPData>>

    @PUT("user/update")
    suspend fun updateUser(
        @Body request: UpdateUserRequest
    ): Response<BaseResponse<Unit>>

    @Multipart
    @PUT("user/update-avatar")
    suspend fun updateAvatar(
        @Part image: MultipartBody.Part
    ): Response<BaseResponse<UserData>>
}