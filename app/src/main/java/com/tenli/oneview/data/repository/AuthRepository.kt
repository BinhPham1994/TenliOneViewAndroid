package com.tenli.oneview.data.repository

import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.data.network.ApiProvider
import com.tenli.oneview.model.network.LogInModel
import com.tenli.oneview.model.network.LoginResponseData
import com.tenli.oneview.model.network.UserCredential
import com.tenli.oneview.model.network.UserData

class AuthRepository {

    suspend fun loginVms(domain: String, request: LogInModel): Result<LoginResponseData?> {
        val candidates = buildCandidateDomains(domain)
        if (candidates.isEmpty()) {
            return Result.failure(IllegalArgumentException("Domain is empty"))
        }

        var lastError: Exception? = null
        for (formattedDomain in candidates) {
            try {
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
                        ),
                        resolvedDomain = formattedDomain
                    )

                    return Result.success(loginResponseData)
                } else {
                    val code = response.code()
                    val httpError = Exception("Login failed with code $code")
                    // Nếu máy chủ đã phản hồi 400/401 (sai tài khoản/mật khẩu), dừng ngay không thử giao thức còn lại
                    if (code == 400 || code == 401) {
                        return Result.failure(httpError)
                    }
                    lastError = httpError
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                lastError = e
            }
        }

        return Result.failure(lastError ?: Exception("Login failed"))
    }

    /**
     * Tự động suy luận giao thức từ domain người dùng nhập:
     * - Người dùng chỉ cần nhập domain (vd: `app.tenli.ai` hoặc `192.168.1.10:8000`).
     * - Mặc định ưu tiên `https://<domain>`, nếu không kết nối được (ví dụ máy chủ nội bộ dùng HTTP)
     *   sẽ tự động thử tiếp `http://<domain>`.
     */
    private fun buildCandidateDomains(rawDomain: String): List<String> {
        val trimmed = rawDomain.trim()
        if (trimmed.isEmpty()) return emptyList()

        val explicitHttp = trimmed.startsWith("http://", ignoreCase = true)
        val explicitHttps = trimmed.startsWith("https://", ignoreCase = true)
        val hostPart = trimmed
            .replace(Regex("^https?://", RegexOption.IGNORE_CASE), "")
            .replace(Regex("/m/?$", RegexOption.IGNORE_CASE), "")
            .trimEnd('/')
        if (hostPart.isEmpty()) return emptyList()

        return when {
            explicitHttp -> listOf("http://$hostPart", "https://$hostPart")
            explicitHttps -> listOf("https://$hostPart", "http://$hostPart")
            else -> listOf("https://$hostPart", "http://$hostPart")
        }
    }
}
