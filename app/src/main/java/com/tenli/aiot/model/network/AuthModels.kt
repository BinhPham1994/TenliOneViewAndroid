package com.tenli.aiot.model.network

import com.google.gson.annotations.SerializedName
import com.tenli.aiot.data.local.UserSession

data class LoginResponseData(
    val target: UserData,
    val credential: UserCredential
)

data class UserCredential(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class UserData(
    val id: Int,
    var uuid: String,
    var name: String,
    val email: String,
    var phoneNumber: String,

    var gender: Int = 0,               // 0: Khác, 1: Nam, 2: Nữ
    var address: String? = "",
    var language: String? = "vi",

    var avatar: Avatar?,
)

data class Avatar(
    val id: Int,
    val uuid: String,
    val publicURL: String
) {
    fun getAvatarImage(): String {
        val token = UserSession.accessToken
        return "$publicURL?authValue=$token"
    }
}

data class UpdateUserRequest(
    val name: String,
    val gender: Int,
    val language: String = "vi",
    val address: String,
    val phoneNumber1: String,
    val email: String,
    val newVerifyAuthCode: String = "",
    val firebaseIdToken: String = "",
    val timeZone: String = "Asia/Ho_Chi_Minh",
    val timeZoneOffSet: String = "+07:00"
)

data class LoginRequest(
    val email: String,
    val password: String,
    val phoneNumber: String = "",
    val userIdToken: String = "",
    val googleIdToken: String = "",
    val appleTokenId: String = "",
    val firebaseIdToken: String = "",
    val verifyAuthCode: String = "",
    val emailVerifyAuthCode: String = "",
    val phoneVerifyAuthCode: String = "",
    val notifyConfig: LoginNotifyConfig,
    val device: LoginDeviceConfig,
    val saveLogin: Boolean = true,
    val createIfNotExist: Boolean = true,
    val createUserOptions: CreateUserOptions = CreateUserOptions()
)

data class LoginNotifyConfig(
    val token: String
)

data class LoginDeviceConfig(
    val uuid: String,
    val information: DeviceInfo,
    val model: String,
    val name: String
)

data class DeviceInfo(
    val description: String = "Android Device",
    val systemName: String = "Android",
    val systemVersion: String = android.os.Build.VERSION.RELEASE
)

data class CreateUserOptions(
    val group: UserGroupOptions = UserGroupOptions()
)

data class UserGroupOptions(
    val joinToDeviceSample: Boolean = false
)


data class OTPData(
    val authCode: String,
)

data class RefreshTokenData(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String,

    @SerializedName("grant_type")
    val grantType: String,

    @SerializedName("expire_in")
    val expireIn: Long,

    @SerializedName("token_version")
    val tokenVersion: Int,

    @SerializedName("authMethod")
    val authMethod: String
)
