package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ==================== ONVIF Core SOAP & Discovery ====================

data class SOAPModel(
    val xml: String,
    @SerializedName("addr", alternate = ["url"])
    val addr: String,
    val username: String = "",
    val password: String = ""
)

data class SOAPResponseModel(
    val xml: String? = null
)

// ==================== ONVIF Profiles & RTSP ====================

data class OnvifProfileModel(
    val name: String,
    val token: String,
    val videoSourceToken: String = "",
    val videoEncoderToken: String = "",
    val ptzNodeToken: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val encoding: String = "",
    val fps: Double = 0.0,
    val bitrate: Int = 0
)

data class RtspModel(
    val main: String = "",
    val sub: String = ""
)

// ==================== ONVIF PTZ ====================

data class PtzStatusModel(
    val pan: Double = 0.0,
    val tilt: Double = 0.0,
    val zoom: Double = 0.0,
    val moveStatusPanTilt: String = "IDLE",
    val moveStatusZoom: String = "IDLE",
    val error: String = "",
    val utcTime: String = ""
)

data class OnvifPresetModel(
    val token: String,
    val name: String,
    val pan: Double? = null,
    val tilt: Double? = null,
    val zoom: Double? = null
)

// ==================== ONVIF Imaging Settings ====================

data class ImagingSettingsModel(
    val brightness: Double? = null,
    val colorSaturation: Double? = null,
    val contrast: Double? = null,
    val sharpness: Double? = null,
    val irCutFilter: String? = null, // 'ON' | 'OFF' | 'AUTO'
    val wideDynamicRangeMode: String? = null, // 'ON' | 'OFF'
    val wideDynamicRangeLevel: Double? = null,
    val backlightCompensationMode: String? = null, // 'ON' | 'OFF'
    val backlightCompensationLevel: Double? = null,
    val exposureMode: String? = null,
    val whiteBalanceMode: String? = null
)

// ==================== ONVIF Device Information & Network ====================

data class DeviceInformationModel(
    val manufacturer: String = "",
    val model: String = "",
    val firmwareVersion: String = "",
    val serialNumber: String = "",
    val hardwareId: String = ""
)

data class NetworkInterfaceModel(
    val token: String = "",
    val enabled: Boolean = true,
    val name: String = "",
    val hwAddress: String = "",
    val mtu: Int = 1500,
    val dhcp: Boolean = false,
    val ipv4Address: String = "",
    val prefixLength: Int = 24
)

// ==================== ONVIF Video Encoder ====================

data class VideoEncoderConfigModel(
    val token: String = "",
    val name: String = "",
    val useCount: Int = 1,
    val encoding: String = "H264", // 'H264' | 'H265' | 'JPEG' | 'MPEG4'
    val width: Int = 1920,
    val height: Int = 1080,
    val quality: Double = 4.0,
    val frameRateLimit: Int = 25,
    val encodingInterval: Int = 1,
    val bitrateLimit: Int = 4096,
    val govLength: Int = 50,
    val profile: String = "Main"
)

data class VideoEncoderSaveResult(
    val success: Boolean,
    val message: String = ""
)

// ==================== ONVIF Users & Motion ====================

data class OnvifUserModel(
    val username: String,
    val password: String? = null,
    val userLevel: String = "Operator" // 'Administrator' | 'Operator' | 'User' | 'Anonymous'
)

data class OnvifMotionConfigModel(
    val enabled: Boolean = false,
    val sensitivity: Int = 50,
    val columns: Int = 22,
    val rows: Int = 18,
    val activeCells: String = "",
    val ruleName: String = "",
    val moduleName: String = ""
)

// ==================== ONVIF WS-Security & SOAP Builder Helpers ====================

object OnvifSoapHelper {

    fun generateWSSEHeader(username: String, password: String): String {
        if (username.isEmpty()) return ""
        return try {
            val nonceBytes = ByteArray(16)
            SecureRandom().nextBytes(nonceBytes)
            val created = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            val createdBytes = created.toByteArray(Charsets.UTF_8)
            val passwordBytes = password.toByteArray(Charsets.UTF_8)

            val combined = ByteArray(nonceBytes.size + createdBytes.size + passwordBytes.size)
            System.arraycopy(nonceBytes, 0, combined, 0, nonceBytes.size)
            System.arraycopy(createdBytes, 0, combined, nonceBytes.size, createdBytes.size)
            System.arraycopy(passwordBytes, 0, combined, nonceBytes.size + createdBytes.size, passwordBytes.size)

            val sha1 = MessageDigest.getInstance("SHA-1").digest(combined)
            val passwordDigest = Base64.getEncoder().encodeToString(sha1)
            val nonceBase64 = Base64.getEncoder().encodeToString(nonceBytes)

            """
            <s:Header>
                <Security s:mustUnderstand="1" xmlns="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
                    <UsernameToken>
                        <Username>$username</Username>
                        <Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest">$passwordDigest</Password>
                        <Nonce EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">$nonceBase64</Nonce>
                        <Created xmlns="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">$created</Created>
                    </UsernameToken>
                </Security>
            </s:Header>
            """.trimIndent()
        } catch (_: Exception) {
            ""
        }
    }

    fun buildSoapEnvelope(bodyXml: String, username: String = "", password: String = ""): String {
        val header = if (username.isNotEmpty()) generateWSSEHeader(username, password) else "<s:Header/>"
        return """
            <?xml version="1.0" encoding="utf-8"?>
            <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope"
                        xmlns:tds="http://www.onvif.org/ver10/device/wsdl"
                        xmlns:trt="http://www.onvif.org/ver10/media/wsdl"
                        xmlns:tptz="http://www.onvif.org/ver20/ptz/wsdl"
                        xmlns:timg="http://www.onvif.org/ver20/imaging/wsdl"
                        xmlns:tev="http://www.onvif.org/ver10/events/wsdl"
                        xmlns:tan="http://www.onvif.org/ver20/analytics/wsdl"
                        xmlns:tt="http://www.onvif.org/ver10/schema">
                $header
                <s:Body>
                    $bodyXml
                </s:Body>
            </s:Envelope>
        """.trimIndent()
    }
}

