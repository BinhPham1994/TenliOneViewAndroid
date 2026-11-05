package com.tenli.oneview.data.repository

import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.model.network.LoginResponseData
import com.tenli.oneview.model.network.UserCredential
import com.tenli.oneview.model.network.UserData

class AuthRepository {

    suspend fun loginVms(domain: String, request: LogInModel): Result<LoginResponseData?> {
        return try {
            val formattedDomain = if (!domain.startsWith("http://") && !domain.startsWith("https://")) "http://$domain" else domain
            val loginUrl = "$formattedDomain/VMS/api/LogIn"

            val response = ApiProvider.vmsApi.logInExt(loginUrl, request)
            if (response.isSuccessful) {
                val tokenModel = response.body() ?: throw Exception("Token response is null")

                // Tạo UserData tối thiểu từ username; quyền chi tiết được nạp sau
                // (giống fetchAndApplyPermissions() của bản web).
                val loginResponseData = LoginResponseData(
                    target = UserData(
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
                    credential = UserCredential(
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
}
