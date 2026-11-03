package com.tenli.oneview.model.network

import com.google.gson.annotations.SerializedName
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

// ==================== ONVIF Core SOAP & Discovery ====================

data class SOAPModel(
    val xml: String,
    @SerializedName("addr", alternate = ["url"])
    val addr: String,
    val username: String = "",
    val password: String = ""
)

data class SOAPResponseModel(
    val xml: String? = null,
    val status: Int? = null
)

// ==================== ONVIF Profiles & RTSP ====================

data class OnvifProfileModel(
    val name: String = "",
    val token: String,
    val sourceToken: String = "",
    val videoSourceToken: String = "",
    val encoderToken: String = "",
    val videoEncoderToken: String = "",
    val analyticsToken: String = "",
    val ptzToken: String = "",
    val ptzNodeToken: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val encoding: String = "",
    val quality: Double = 0.0,
    val fps: Double = 0.0,
    val frameRateLimit: Int = 0,
    val bitrate: Int = 0,
    val bitrateLimit: Int = 0,
    val govLength: Int = 0,
    val profile: String = ""
)

typealias ProfileModel = OnvifProfileModel

data class RtspModel(
    val uri: String = "",
    val main: String = "",
    val sub: String = ""
)

// ==================== ONVIF PTZ ====================

data class PtzStatusModel(
    val pan: Double? = 0.0,
    val tilt: Double? = 0.0,
    val zoom: Double? = 0.0,
    val panTiltSpace: String? = null,
    val zoomSpace: String? = null,
    val moveStatusPanTilt: String = "IDLE",
    val moveStatusZoom: String = "IDLE",
    val error: String = "",
    val utcTime: String = "",
    val readAt: Long = System.currentTimeMillis()
)

data class OnvifPresetModel(
    val token: String,
    val name: String,
    val pan: Double? = null,
    val tilt: Double? = null,
    val zoom: Double? = null
)

// ==================== ONVIF Imaging Settings ====================

data class ImagingExposure(
    val mode: String = "AUTO",
    val minExposureTime: Double? = null,
    val maxExposureTime: Double? = null,
    val exposureTime: Double? = null,
    val gain: Double? = null,
    val minGain: Double? = null,
    val maxGain: Double? = null,
    val iris: Double? = null
)

data class ImagingBacklightCompensation(
    val mode: String = "OFF",
    val level: Double = 10.0
)

data class ImagingWideDynamicRange(
    val mode: String = "OFF",
    val level: Double = 50.0
)

data class ImagingWhiteBalance(
    val mode: String = "AUTO",
    val crGain: Double = 0.0,
    val cbGain: Double = 0.0
)

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
    val whiteBalanceMode: String? = null,
    val exposure: ImagingExposure? = null,
    val backlightCompensation: ImagingBacklightCompensation? = null,
    val wideDynamicRange: ImagingWideDynamicRange? = null,
    val whiteBalance: ImagingWhiteBalance? = null
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
    val ipAddress: String = "",
    val subnetMask: String = "255.255.255.0",
    val prefixLength: Int = 24,
    val gateway: String? = null,
    val dns: List<String>? = null
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
    val profile: String = "Main",
    val sessionTimeout: String? = null,
    val multicastXml: String? = null,
    val isMedia2: Boolean? = null,
    val debugXml: String? = null
)

data class VideoEncoderSaveResult(
    val success: Boolean,
    val verified: Boolean? = null,
    val appliedConfig: VideoEncoderConfigModel? = null,
    val message: String = ""
)

// ==================== ONVIF Users & Motion ====================

data class OnvifUserModel(
    val username: String,
    val password: String? = null,
    val userLevel: String = "Operator" // 'Administrator' | 'Operator' | 'User' | 'Anonymous'
)

data class OnvifMotionConfigModel(
    val supported: Boolean = false,
    val enabled: Boolean = false,
    val sensitivity: Int = 50,
    val columns: Int = 22,
    val rows: Int = 18,
    val activeCells: String = "",
    val analyticsUrl: String? = null,
    val eventsUrl: String? = null,
    val recordingUrl: String? = null,
    val configurationToken: String? = null,
    val recordingJobToken: String? = null,
    val recordingToken: String? = null,
    val recordingJobMode: String? = null, // 'Active' | 'Idle'
    val recordingJobState: String? = null, // 'Active' | 'Idle' | 'Error'
    val hasRecordingJobControl: Boolean? = null,
    val hasRecordedData: Boolean? = null,
    val earliestRecording: String? = null,
    val latestRecording: String? = null,
    val ruleName: String = "",
    val ruleType: String? = null,
    val moduleName: String = "",
    val moduleType: String? = null,
    val rawRuleXml: String? = null,
    val rawModuleXml: String? = null,
    val detectionSource: String? = null
)

// ==================== ONVIF WS-Security & SOAP Builder Helpers ====================

object OnvifSoapHelper {

    const val DEVICE_DISCOVERY_XML = """<?xml version="1.0" encoding="UTF-8"?>
<e:Envelope xmlns:e="http://www.w3.org/2003/05/soap-envelope" xmlns:w="http://schemas.xmlsoap.org/ws/2004/08/addressing" xmlns:d="http://schemas.xmlsoap.org/ws/2005/04/discovery" xmlns:dn="http://www.onvif.org/ver10/network/wsdl">
  <e:Header>
    <w:MessageID>uuid:037EDAA1-2F29-4C82-9EE9-28A6E6FCBDAD</w:MessageID>
    <w:To>urn:schemas-xmlsoap-org:ws:2005/04/discovery</w:To>
    <w:Action>http://schemas.xmlsoap.org/ws/2005/04/discovery/Probe</w:Action>
  </e:Header>
  <e:Body>
    <d:Probe>
      <d:Types>dn:NetworkVideoTransmitter</d:Types>
    </d:Probe>
  </e:Body>
</e:Envelope>"""

    fun prefixLengthToSubnetMask(prefix: Int): String {
        val mask = IntArray(4)
        var p = prefix
        for (i in 0 until 4) {
            val n = min(max(p, 0), 8)
            mask[i] = (256 - 2.0.pow((8 - n).toDouble()).toInt())
            p -= n
        }
        return mask.joinToString(".")
    }

    fun subnetMaskToPrefixLength(mask: String): Int {
        return mask.split(".")
            .mapNotNull { it.toIntOrNull() }
            .sumOf { Integer.bitCount(it and 0xFF) }
    }

    fun parseSoapFault(xml: String?): String? {
        if (xml.isNullOrBlank()) return null
        if (!xml.contains(":Fault") &&
            !xml.contains("<Fault>") &&
            !xml.contains("<s:Fault") &&
            !xml.contains("<SOAP-ENV:Fault")
        ) {
            return null
        }
        val textRegex = Regex("<(?:\\w+:)?(?:Text|faultstring|Reason)[^>]*>([^<]+)</", RegexOption.IGNORE_CASE)
        val subcodeRegex = Regex("<(?:\\w+:)?(?:Value|faultcode|Code)[^>]*>([^<]+)</", RegexOption.IGNORE_CASE)
        val text = textRegex.find(xml)?.groupValues?.getOrNull(1)?.trim().orEmpty()
        val subcode = subcodeRegex.find(xml)?.groupValues?.getOrNull(1)?.trim().orEmpty()
        return when {
            subcode.isNotEmpty() && text.isNotEmpty() -> "$subcode: $text"
            text.isNotEmpty() -> text
            subcode.isNotEmpty() -> subcode
            else -> "SOAP Fault"
        }
    }

    /**
     * Khớp hoàn toàn với generateWSSEHeader trong onvifData.ts:
     * Strict ONVIF format: YYYY-MM-DDTHH:mm:ssZ (không có milliseconds) và hỗ trợ timeOffsetMs.
     */
    fun generateWSSEHeader(
        username: String,
        password: String,
        timeOffsetMs: Long = 0L
    ): String {
        if (username.isEmpty() || password.isEmpty()) return ""
        return try {
            val nonceBytes = ByteArray(16)
            SecureRandom().nextBytes(nonceBytes)
            val created = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date(System.currentTimeMillis() + timeOffsetMs))

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
    <wsse:Security soapenv:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
      <wsse:UsernameToken>
        <wsse:Username>$username</wsse:Username>
        <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest">$passwordDigest</wsse:Password>
        <wsse:Nonce EncodingType="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary">$nonceBase64</wsse:Nonce>
        <wsu:Created>$created</wsu:Created>
      </wsse:UsernameToken>
    </wsse:Security>""".trimIndent()
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Khớp với buildSoapMessage trong onvifData.ts (hỗ trợ SOAP 1.1 & 1.2, Media2 tr2, soapAction).
     */
    fun buildSoapMessage(
        bodyContent: String,
        soapVersion: String = "1.2",
        wsseHeader: String = "",
        useMedia2: Boolean = false,
        soapAction: String = ""
    ): String {
        val soapenv = if (soapVersion == "1.1") {
            "http://schemas.xmlsoap.org/soap/envelope/"
        } else {
            "http://www.w3.org/2003/05/soap-envelope"
        }
        val actionHeader = if (soapAction.isNotEmpty()) {
            "\n    <wsa:Action soapenv:mustUnderstand=\"1\">$soapAction</wsa:Action>"
        } else {
            ""
        }
        return """<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="$soapenv" xmlns:wsa="http://www.w3.org/2005/08/addressing" xmlns:trt="http://www.onvif.org/ver10/media/wsdl" xmlns:tr2="http://www.onvif.org/ver20/media/wsdl" xmlns:tt="http://www.onvif.org/ver10/schema" xmlns:timg="http://www.onvif.org/ver20/imaging/wsdl" xmlns:tptz="http://www.onvif.org/ver20/ptz/wsdl" xmlns:tds="http://www.onvif.org/ver10/device/wsdl" xmlns:tev="http://www.onvif.org/ver10/events/wsdl" xmlns:tan="http://www.onvif.org/ver20/analytics/wsdl" xmlns:trc="http://www.onvif.org/ver10/recording/wsdl" xmlns:tse="http://www.onvif.org/ver10/search/wsdl">
  <soapenv:Header>$wsseHeader$actionHeader</soapenv:Header>
  <soapenv:Body>
    $bodyContent
  </soapenv:Body>
</soapenv:Envelope>"""
    }

    fun buildSoapEnvelope(
        bodyXml: String,
        username: String = "",
        password: String = "",
        timeOffsetMs: Long = 0L,
        soapVersion: String = "1.2",
        soapAction: String = ""
    ): String {
        val wsse = if (username.isNotEmpty()) generateWSSEHeader(username, password, timeOffsetMs) else ""
        return buildSoapMessage(
            bodyContent = bodyXml,
            soapVersion = soapVersion,
            wsseHeader = wsse,
            soapAction = soapAction
        )
    }
}

