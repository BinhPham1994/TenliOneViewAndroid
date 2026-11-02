package com.tenli.oneview.data.network.api

import com.tenli.oneview.model.network.DeviceDiscoveryModel
import com.tenli.oneview.model.network.DeviceServiceRequest
import com.tenli.oneview.model.network.DeviceServiceResponse
import com.tenli.oneview.model.network.SOAPModel
import com.tenli.oneview.model.network.SOAPResponseModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * ONVIF API — tương ứng 1:1 với onvifData.ts trên PWA & PC Web.
 * Hỗ trợ cả gọi trực tiếp qua VMS mặc định và gọi sang VMS service host riêng (postExt).
 */
interface OnvifApi {

    @POST("VMS/api/DeviceDiscovery")
    suspend fun deviceDiscovery(
        @Body model: DeviceDiscoveryModel
    ): Response<List<DeviceDiscoveryModel>>

    @POST
    suspend fun deviceDiscoveryExt(
        @Url url: String,
        @Header("Authorization") authHeader: String? = null,
        @Body model: DeviceDiscoveryModel
    ): Response<List<DeviceDiscoveryModel>>

    @POST("VMS/api/DeviceService")
    suspend fun deviceService(
        @Body model: SOAPModel
    ): Response<SOAPResponseModel>

    @POST
    suspend fun deviceServiceExt(
        @Url url: String,
        @Header("Authorization") authHeader: String? = null,
        @Body model: SOAPModel
    ): Response<SOAPResponseModel>

    @POST("VMS/api/DeviceService")
    suspend fun deviceServiceLegacy(
        @Body request: DeviceServiceRequest
    ): Response<DeviceServiceResponse>
}

