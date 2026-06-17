package com.tenli.aiot.model.network

data class HomeGroup(
    val id: Int,
    val name: String,
    val userCreateId: Int,
    val isDefault: Boolean = false,
    val isOwner: Boolean = false,
    val userRequestRole: String = "",
    val ownerUserName: String = ""
)

data class HomeGroupDisplay(
    val group: HomeGroup,
    val displayName: String
)

data class GroupDetailData(
    val id: Int,
    val name: String,
    val users: List<GroupUser> = emptyList(),
    val devices: List<Any> = emptyList()
)

data class GroupUser(
    val id: Int,
    val name: String,
    val role: String
)

data class ShareCodeData(
    val target: Int,
    val code: String,
    val role: String,
    val requestExpiredAt: Long
)

fun HomeGroup?.getDisplayTitle(currentUserId: Int): String {
    val group = this ?: return "Chưa có nhà"
    return when {
        group.name == "Default Device Group" && group.isOwner && group.userCreateId == currentUserId -> "Nhà của tôi"
        group.name == "Default Device Group" -> {
            if (group.ownerUserName.isNotBlank()) {
                "Nhà của ${group.ownerUserName}"
            } else {
                group.name
            }
        }

        else -> group.name
    }
}

fun HomeGroup.getRoleDisplay(): String {
    return when (this.userRequestRole) {
        "owner" -> "Chủ nhà"
        "member" -> "Thành viên"
        "technical" -> "Hỗ trợ kĩ thuật"
        else -> "Thành viên"
    }
}