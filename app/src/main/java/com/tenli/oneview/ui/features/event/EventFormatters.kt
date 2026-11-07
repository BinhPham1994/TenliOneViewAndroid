package com.tenli.oneview.ui.features.event

import com.tenli.oneview.data.local.EventCacheManager
import com.tenli.oneview.data.local.UserPermissionManager
import com.tenli.oneview.model.network.BsProfileModel
import com.tenli.oneview.model.network.CameraModel
import com.tenli.oneview.model.network.EventData
import com.tenli.oneview.ui.utils.AiTypeHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 * Tiện ích dùng chung cho màn Sự kiện (danh sách + chi tiết) và các ViewModel liên quan:
 * khóa định danh, quy đổi / định dạng thời gian, trích xuất camera / biển số / profile từ [EventData].
 * Toàn bộ là hàm thuần (không phụ thuộc Compose) để gọi được cả ở ViewModel lẫn UI.
 */

// ---------------------------------------------------------------------------------------------
// Định danh & lọc
// ---------------------------------------------------------------------------------------------

/** Khóa duy nhất của 1 sự kiện – dùng cho distinctBy và `key` của LazyColumn. */
fun EventData.uniqueKey(): String = "${serviceId}_${id}_${time}"

/** Giữ các sự kiện thuộc module AI và camera được phân quyền, đồng thời loại bỏ bản ghi trùng (theo [uniqueKey]). */
fun List<EventData>.permittedDistinct(
    allCameras: List<CameraModel> = EventCacheManager.getCamerasInMemory()
): List<EventData> =
    UserPermissionManager.filterEventsByPermissions(this, allCameras).distinctBy { it.uniqueKey() }

/** Map loại AI (bộ lọc UI) sang `type` gửi lên API. */
fun resolveApiEventType(aiType: String?): String? {
    if (aiType.isNullOrBlank()) return null
    return AiTypeHelper.getAiEventTypes(aiType).firstOrNull() ?: aiType
}

// ---------------------------------------------------------------------------------------------
// Thời gian
// ---------------------------------------------------------------------------------------------

/** Timestamp nhỏ hơn ngưỡng này được hiểu là giây, ngược lại là mili-giây. */
private const val SECONDS_TIMESTAMP_THRESHOLD = 100_000_000_000.0

/** Quy đổi `EventData.time` (giây hoặc mili-giây) sang mili-giây. */
fun eventTimeToMillis(timestamp: Double): Long =
    if (timestamp < SECONDS_TIMESTAMP_THRESHOLD) (timestamp * 1000).toLong() else timestamp.toLong()

/** Quy đổi `EventData.time` (giây hoặc mili-giây) sang giây. */
fun eventTimeToSeconds(timestamp: Double): Long = eventTimeToMillis(timestamp) / 1000

/** SimpleDateFormat không thread-safe → mỗi thread giữ 1 instance, tránh tạo mới mỗi lần định dạng. */
private fun threadLocalFormat(pattern: String): ThreadLocal<SimpleDateFormat> =
    ThreadLocal.withInitial { SimpleDateFormat(pattern, Locale.getDefault()) }

private val timeOnlyFormat = threadLocalFormat("HH:mm:ss")
private val dateOnlyFormat = threadLocalFormat("dd/MM/yyyy")
private val videoQueryFormat = threadLocalFormat("yyyy-MM-dd-HH-mm-ss")

/** `HH:mm:ss` của sự kiện, `--:--:--` nếu không có thời gian. */
fun formatEventTimeOnly(timestamp: Double): String {
    if (timestamp <= 0) return "--:--:--"
    return timeOnlyFormat.get()!!.format(Date(eventTimeToMillis(timestamp)))
}

/**
 * Matches `formattedTimeObj` in `BaseAiEventDetailModal.vue` lines 3035-3046:
 * `{ time: 'HH:mm:ss', date: 'Hôm nay' | 'DD/MM/YYYY' }`
 */
fun formatEventTimeAndDate(timestamp: Double, isEn: Boolean): Pair<String, String> {
    if (timestamp <= 0) return Pair("--", "")
    val timeMs = eventTimeToMillis(timestamp)
    val date = Date(timeMs)

    val calNow = Calendar.getInstance()
    val calEv = Calendar.getInstance().apply { timeInMillis = timeMs }
    val isToday = calNow.get(Calendar.YEAR) == calEv.get(Calendar.YEAR) &&
        calNow.get(Calendar.DAY_OF_YEAR) == calEv.get(Calendar.DAY_OF_YEAR)

    val dateStr = if (isToday) {
        if (isEn) "Today" else "Hôm nay"
    } else {
        dateOnlyFormat.get()!!.format(date)
    }
    return Pair(timeOnlyFormat.get()!!.format(date), dateStr)
}

/** Định dạng thời gian dùng cho API danh sách video gốc (`yyyy-MM-dd-HH-mm-ss`). */
fun formatVideoQueryTime(millis: Long): String = videoQueryFormat.get()!!.format(Date(millis))

/** Parse thời gian trả về từ API danh sách video gốc (`yyyy-MM-dd-HH-mm-ss`). */
fun parseVideoQueryTime(text: String): Long? = runCatching { videoQueryFormat.get()!!.parse(text)?.time }.getOrNull()

/** Đưa [cal] về 00:00:00.000 của ngày hiện tại trong lịch và trả về mili-giây. */
fun Calendar.moveToStartOfDay(): Long {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
    return timeInMillis
}

// ---------------------------------------------------------------------------------------------
// Trích xuất dữ liệu từ EventData
// ---------------------------------------------------------------------------------------------

/** UUID camera của sự kiện (API trả về cả `cameraUUID` lẫn `cameraUuid`). */
val EventData.cameraUuidOrNull: String?
    get() = data?.cameraUUID ?: data?.cameraUuidLower

/** Biển số xe hiển thị (đã trim), chuỗi rỗng nếu không có. */
fun EventData.displayPlateNumber(): String =
    (data?.plate?.plate ?: data?.plate?.number ?: data?.vehicle?.plate ?: plate)?.trim().orEmpty()

/** Biển số thuộc danh sách đen. */
val EventData.isPlateBlacklisted: Boolean
    get() = data?.plate?.isBlacklist == true || data?.plate?.isBlacklistSnake == true

/** UUID profile (khuôn mặt / đồng phục) gắn trực tiếp trên sự kiện, chuỗi rỗng nếu không có. */
fun EventData.directProfileUuid(): String =
    (data?.face?.profileUUID ?: data?.face?.profileUUIDUpper ?: data?.profileUUID ?: data?.profileUUIDUpper)
        ?.trim().orEmpty()

/** ID profile gắn trực tiếp trên sự kiện. */
val EventData.directProfileId: Int?
    get() = data?.face?.profileId ?: data?.profileId

/** Tìm profile tương ứng với sự kiện: ưu tiên UUID → ID → tên. */
fun findMatchedProfile(event: EventData, profiles: List<BsProfileModel>, isEn: Boolean): BsProfileModel? {
    if (profiles.isEmpty()) return null
    val profileUuid = event.directProfileUuid()
    val profileId = event.directProfileId
    return when {
        profileUuid.isNotEmpty() -> profiles.find { it.uuid.equals(profileUuid, ignoreCase = true) }
        profileId != null -> profiles.find { it.id == profileId }
        else -> {
            val directName = AiTypeHelper.extractFacePersonName(event, isEn).trim()
            if (directName.isNotEmpty()) {
                profiles.find { it.name.trim().equals(directName, ignoreCase = true) }
            } else null
        }
    }
}

/** Camera của sự kiện: khớp theo UUID, nếu không có thì theo `originId`. */
fun resolveCameraForEvent(event: EventData, cameras: List<CameraModel>): CameraModel? {
    if (cameras.isEmpty()) return null
    val camUuid = event.cameraUuidOrNull
    if (!camUuid.isNullOrBlank()) {
        cameras.find { it.extra?.uuid == camUuid }?.let { return it }
    }
    if (event.originId > 0) {
        cameras.find { it.id == event.originId }?.let { return it }
    }
    return null
}

/** Map UUID camera → tên camera (bỏ qua camera không có UUID). */
fun buildCameraNameMap(cameras: List<CameraModel>): Map<String, String> {
    if (cameras.isEmpty()) return emptyMap()
    val result = HashMap<String, String>(cameras.size)
    cameras.forEach { cam ->
        val uuid = cam.extra?.uuid
        if (!uuid.isNullOrBlank()) result[uuid] = cam.name
    }
    return result
}
