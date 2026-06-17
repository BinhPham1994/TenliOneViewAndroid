package com.tenli.oneview.ui.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

object DateTimeUtils {

    /**
     * Tính số giây từ hiện tại cho đến 8h sáng ngày hôm sau.
     */
    fun getSecondsUntil8AMNextDay(): Long {
        val now = Calendar.getInstance()
        val nextDay8AM = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return (nextDay8AM.timeInMillis - now.timeInMillis) / 1000
    }

    fun formatIsoTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "---"
        return try {
            // Parse format ISO 8601 từ server
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoString)

            // Format lại theo ý muốn
            val formatter = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
            date?.let { formatter.format(it) } ?: "---"
        } catch (e: Exception) {
            isoString // Trả về gốc nếu lỗi parse
        }
    }

    fun formatTimeInput(input: String): String {
        // 1. Chỉ lấy các chữ số
        val digits = input.filter { it.isDigit() }.take(4)

        // 2. Kiểm tra tính hợp lệ của Giờ (00-23)
        var hour = digits.take(2)
        if (hour.length == 2 && hour.toInt() > 23) hour = "23"

        // 3. Kiểm tra tính hợp lệ của Phút (00-59)
        var minute = if (digits.length > 2) digits.substring(2) else ""
        if (minute.length == 2 && minute.toInt() > 59) minute = "59"

        // 4. Trả về format HH:mm
        return when {
            digits.length > 2 -> "$hour:$minute"
            else -> hour
        }
    }

    fun formatMuteRemain(seconds: Double): String {
        if (seconds <= 0) return "0 phút"

        val totalMinutes = (seconds / 60).roundToInt()

        return when {
            totalMinutes < 1 -> "vài giây"
            totalMinutes < 60 -> "$totalMinutes phút"
            else -> {
                val hours = totalMinutes / 60
                val remainingMinutes = totalMinutes % 60
                if (remainingMinutes == 0) "${hours} giờ"
                else "${hours} giờ ${remainingMinutes} phút"
            }
        }
    }
}