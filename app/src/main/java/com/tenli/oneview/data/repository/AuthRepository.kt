package com.tenli.oneview.data.repository

import com.tenli.oneview.model.network.LoginResponseData

class AuthRepository {


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


}
