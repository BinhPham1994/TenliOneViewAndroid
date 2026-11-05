package com.tenli.oneview.data.network

import com.tenli.oneview.data.network.api.AiApi
import com.tenli.oneview.data.network.api.BsApi
import com.tenli.oneview.data.network.api.EventApi
import com.tenli.oneview.data.network.api.VmsApi
import com.tenli.oneview.data.network.retrofit.LoginAuthClient

/**
 * Điểm truy cập duy nhất cho các Retrofit service.
 * Mỗi service chỉ được tạo 1 lần (lazy, thread-safe) và dùng chung toàn app,
 * tránh gọi `LoginAuthClient.create(...)` rải rác ở ViewModel/Composable.
 */
object ApiProvider {
    val vmsApi: VmsApi by lazy { LoginAuthClient.create(VmsApi::class.java) }
    val eventApi: EventApi by lazy { LoginAuthClient.create(EventApi::class.java) }
    val bsApi: BsApi by lazy { LoginAuthClient.create(BsApi::class.java) }
    val aiApi: AiApi by lazy { LoginAuthClient.create(AiApi::class.java) }
}
