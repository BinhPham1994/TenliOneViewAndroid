package com.tenli.aiot.model.network

data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)