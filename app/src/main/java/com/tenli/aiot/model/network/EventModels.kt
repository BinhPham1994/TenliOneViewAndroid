package com.tenli.aiot.model.network

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.Instant

@Entity(tableName = "events")
data class EventItem(
    @PrimaryKey val id: Long,
    val eType: String,
    val eTypeGroup: String,
    val eTime: String,
    val eTimestamp: Long,
    val isNew: Boolean = false,
    val eCameraId: Int? = null,
    val deviceId: Int? = null,

    var eImages: String? = "",
    var eVideos: String? = "",

    var eProcessId: Int? = null,

    val eValues: Map<String, Any?>? = null,
    val sData: SData? = null,
    val fileItems: List<EventFileItem> = emptyList(),

    val displayTitle: String = "",
    val timeAgo: String = "",
    val deviceUri: String? = null,
    val deviceKey: String? = null,

    val aiTitle: String = "",
    val actionPart: String = "",
    val cameraName: String = "",
    val deviceName: String = "",
    val foundConnector: String = ""
) {
    val localImageUrl: String?
        get() = if (!deviceUri.isNullOrBlank() && !eImages.isNullOrBlank()) {
            "${deviceUri.removeSuffix("/")}/api/MonitorImage/$eImages"
        } else null

    val localVideoUrl: String?
        get() = if (!deviceUri.isNullOrBlank() && !eVideos.isNullOrBlank()) {
            "${deviceUri.removeSuffix("/")}/api/MonitorVideo/$eVideos"
        } else null

    val monitorLiveUrl: String?
        get() = if (!deviceUri.isNullOrBlank() && sData?.monitorId != null) {
            "${deviceUri.removeSuffix("/")}/api/MonitorStream/${sData.monitorId}"
        } else null
}

data class SData(
    val type: String = "",
    val image: String? = "",
    val video: String? = "",
    val name: String? = null,
    val faceCrop: String? = null,
    val cropImage: String? = null,
    val cameraId: Long? = null,
    val monitorId: Int? = null,
    val sensors: List<SensorItem>? = null,
    val confirm: ConfirmInfo? = null
)

data class ConfirmInfo(
    val tag: String? = "",
    val enabled: Boolean = false,
    val expiredIn: Int? = null
)

data class SensorItem(
    val id: Int? = null,
    val type: String? = "",
    val score: Double = 0.0,
    val cameraId: Int? = null,
    val monitorId: Int? = null,
    val imageName: String? = "",
    val image: String? = "",
    val videoName: String? = "",
    val video: String? = "",
    val cropImage: String? = ""
)

data class EventFileItem(
    val id: Int,
    val type: Int,
    val publicURL: String
)


data class EventTypeDef(
    val key: String,
    val eventTypeGroup: String,
    val display: EventDisplay
)

data class EventGroupDef(
    val key: String,
    val display: EventDisplay
)

data class EventDisplay(
    val text: String,
    val texts: List<LangText> = emptyList()
) {
    fun getLocalText(): String {
        return texts.find { it.lang == "vi" }?.text ?: text
    }
}

data class LangText(val lang: String, val text: String)

fun formatTimeAgo(timestamp: Long): String {
    val now = Instant.now()
    val eventTime = Instant.ofEpochMilli(timestamp)
    val duration = Duration.between(eventTime, now)

    val seconds = duration.seconds
    val minutes = duration.toMinutes()
    val hours = duration.toHours()
    val days = duration.toDays()

    return when {
        seconds < 60 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        days < 30 -> "$days ngày trước"
        else -> {
            val months = days / 30
            if (months < 12) "$months tháng trước"
            else "${days / 365} năm trước"
        }
    }
}