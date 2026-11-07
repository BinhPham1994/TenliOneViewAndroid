package com.tenli.oneview.data.repository

import com.tenli.oneview.data.local.UserPermissionManager
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

                val vmsUser = UserPermissionManager.fetchVmsUserWithRetry(
                    domain = formattedDomain,
                    token = tokenModel.token,
                    userId = tokenModel.userId
                )
                val resolvedUsername = vmsUser?.username?.trim()?.takeIf { it.isNotEmpty() }
                    ?: request.username.trim()
                val effectiveIsAdmin = tokenModel.isAdmin && resolvedUsername == "admin"
                val resolvedPermissions = vmsUser?.extra?.permissions

                val loginResponseData = LoginResponseData(
                    target = UserData(
                        id = tokenModel.userId,
                        uuid = tokenModel.userId.toString(),
                        name = vmsUser?.name?.takeIf { it.isNotBlank() } ?: resolvedUsername,
                        email = resolvedUsername,
                        phoneNumber = "",
                        gender = 0,
                        address = "",
                        language = "vi",
                        avatar = null,
                        username = resolvedUsername,
                        isAdmin = effectiveIsAdmin,
                        permissions = resolvedPermissions
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
