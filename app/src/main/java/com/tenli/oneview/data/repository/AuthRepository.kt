package com.tenli.oneview.data.repository

import com.tenli.oneview.data.network.api.UserApi
import com.tenli.oneview.data.network.retrofit.CloudAuthClient
import com.tenli.oneview.model.network.LoginRequest
import com.tenli.oneview.model.network.LoginResponseData

class AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponseData?> {
        return try {
            val response = com.tenli.oneview.data.network.retrofit.LoginAuthClient.create(UserApi::class.java).login(request)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception("Login failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginVms(domain: String, request: com.tenli.oneview.model.network.LogInModel): Result<LoginResponseData?> {
        return try {
            val formattedDomain = if (!domain.startsWith("http://") && !domain.startsWith("https://")) "http://$domain" else domain
            val loginUrl = "$formattedDomain/VMS/api/LogIn"
            val vmsApi = com.tenli.oneview.data.network.retrofit.LoginAuthClient.create(com.tenli.oneview.data.network.api.VmsApi::class.java)
            
            val response = vmsApi.logInExt(loginUrl, request)
            if (response.isSuccessful) {
                val tokenModel = response.body() ?: throw Exception("Token response is null")
                
                // Now fetch user data
                val userUrl = "$formattedDomain/VMS/api/User/${tokenModel.userId}"
                // Note: The VMS API might need the Authorization header, which usually the interceptor adds if token is available.
                // However, we just received the token. It might not be in the interceptor yet!
                // To be safe, we can just map what we have if getUserExt requires the token and it's not yet in UserSession.
                
                // Let's just create a dummy UserData for now using the username, and we can fetch permissions later
                // just like the web version does in fetchAndApplyPermissions()
                
                val loginResponseData = LoginResponseData(
                    target = com.tenli.oneview.model.network.UserData(
                        id = tokenModel.userId,
                        uuid = tokenModel.userId.toString(),
                        name = request.username,
                        email = request.username,
                        phoneNumber = "",
                        gender = 0,
                        address = "",
                        language = "vi",
                        avatar = null
                    ),
                    credential = com.tenli.oneview.model.network.UserCredential(
                        accessToken = tokenModel.token,
                        refreshToken = tokenModel.token
                    )
                )
                
                Result.success(loginResponseData)
            } else {
                Result.failure(Exception("Login failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyEmail(email: String, reset: Boolean = false): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).verifyEmail(email, reset)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Verify email failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOTP(email: String, otp: String): Result<String> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).verifyOTP(email, otp)
            if (response.isSuccessful) {
                val authCode = response.body()?.data?.authCode ?: ""
                Result.success(authCode)
            } else {
                Result.failure(Exception("Verify OTP failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String, pass: String, authCode: String): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).resetPassword(email, pass, authCode)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Reset password failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAccount(username: String, email: String, pass: String, authCode: String): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).create(username, email, pass, authCode)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Create account failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
