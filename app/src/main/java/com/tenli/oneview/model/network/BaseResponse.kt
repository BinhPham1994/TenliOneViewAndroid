package com.tenli.oneview.model.network

data class BaseResponse<T>(
    val code: Int,
    val message: String,
    val data: T? = null
)