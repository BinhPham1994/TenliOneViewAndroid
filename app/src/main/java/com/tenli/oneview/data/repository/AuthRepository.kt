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
