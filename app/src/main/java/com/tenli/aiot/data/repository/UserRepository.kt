package com.tenli.aiot.data.repository

import com.tenli.aiot.data.network.api.UserApi
import com.tenli.aiot.data.network.retrofit.CloudAuthClient

import com.tenli.aiot.model.network.ClientDevice
import com.tenli.aiot.model.network.UpdateUserRequest
import com.tenli.aiot.model.network.UserData
import okhttp3.MultipartBody

class UserRepository {
    suspend fun logout(): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).logout()
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Logout failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(request: UpdateUserRequest): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).updateUser(request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvatar(body: MultipartBody.Part): Result<UserData?> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).updateAvatar(body)
            if (response.isSuccessful) {
                Result.success(response.body()?.data)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(oldPass: String, newPass: String): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).changePassword(oldPass, newPass)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClients(): Result<List<ClientDevice>> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).getClients()
            if (response.isSuccessful) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logoutClient(deviceId: Int): Result<Boolean> {
        return try {
            val response = CloudAuthClient.create(UserApi::class.java).logoutClient(deviceId)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
