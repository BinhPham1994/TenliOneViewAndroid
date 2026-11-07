package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName
import com.tenli.oneview.data.local.UserSession

data class LoginResponseData(
    val target: UserData = UserData(),
    val credential: UserCredential = UserCredential(),
    val resolvedDomain: String = ""
)

data class UserCredential(
    @SerializedName("access_token") val accessToken: String = "",
    @SerializedName("refresh_token") val refreshToken: String = ""
)

data class UserData(
    val id: Int = 0,
    var uuid: String = "",
    var name: String = "",
    val email: String = "",
    var phoneNumber: String = "",

    var gender: Int = 0,               // 0: Khác, 1: Nam, 2: Nữ
    var address: String? = "",
    var language: String? = "vi",

    var avatar: Avatar? = null,
    var username: String? = null,
    var isAdmin: Boolean? = null,
    var permissions: UserPermissions? = null
)

data class Avatar(
    val id: Int = 0,
    val uuid: String = "",
    val publicURL: String = ""
) {
    fun getAvatarImage(): String {
        val token = UserSession.accessToken
        return "$publicURL?authValue=$token"
    }
}
