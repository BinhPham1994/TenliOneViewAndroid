package com.tenli.aiot.model.network

data class ClientDevice(
    val id: Int,
    val name: String,
    val model: String,
    val information: DeviceInformation?, // Trường lồng dữ liệu chi tiết
    val status: Int,
    val type: Int,
    val updatedAt: String,
    val isCurrent: Boolean
)

data class DeviceInformation(
    val extend: DeviceExtend?,
    val lastAccess: String?,
    val remoteInfo: RemoteInfo?
)

data class DeviceExtend(
    val systemName: String?,
    val systemVersion: String?
)

data class RemoteInfo(
    val remoteIP: String?
)