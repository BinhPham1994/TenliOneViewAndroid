package com.tenli.oneview.ui.features.auth.login

import android.net.Uri
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Bộ giải mã & xác thực mã QR Đăng nhập App Mobile độc quyền của Tenli OneView Web PC.
 *
 * Bảo mật 2 chiều (Encrypt-then-MAC với AES-256-CBC + HMAC-SHA256):
 * 1. Chỉ App Tenli OneView có khóa bí mật mới giải mã được nội dung mã QR (Camera điện thoại,
 *    Zalo hoặc app bên thứ 3 khi quét sẽ chỉ thấy chuỗi mã hóa `TOV1...`, hoàn toàn không lộ URL domain).
 * 2. Chỉ mã QR được tạo từ phần mềm Tenli OneView trên Web PC mới có chữ ký HMAC-SHA256 hợp lệ.
 */
object OneViewQrValidator {
    private const val TOKEN_PREFIX = "TOV1"
    private const val PLAINTEXT_MAGIC = "TENLI_OV_V1|"
    private const val AES_SECRET = "TenliOneView_AES_Key_v1_2026_v9TaVZo2ZrdjtSwHCWvXM6Nt31m3bEef"
    private const val HMAC_SECRET = "TenliOneView_HMAC_Key_v1_2026_ysJhBedR7PDrVRpE"
    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"

    /**
     * Kiểm tra chuỗi QR quét được, xác thực chữ ký HMAC-SHA256 và giải mã AES-256-CBC.
     * Trả về URL domain dự án đã chuẩn hóa nếu hợp lệ, hoặc `null` nếu không hợp lệ.
     */
    fun extractVerifiedDomain(rawQr: String): String? {
        val trimmed = rawQr.trim()
        if (trimmed.isEmpty()) return null

        val parts = trimmed.split('.')
        if (parts.size != 4) return null

        val prefix = parts[0]
        val rawIvHex = parts[1].trim()
        val ivHex = rawIvHex.lowercase()
        val rawCipher = parts[2].trim()
        val cipherBase64Url = rawCipher
            .replace('+', '-')
            .replace('/', '_')
            .trimEnd('=')
        val signature = parts[3].trim().lowercase()

        if (prefix != TOKEN_PREFIX || ivHex.length != 32 || cipherBase64Url.isEmpty() || signature.length != 24) {
            return null
        }

        // 1. Xác thực chữ ký số HMAC-SHA256 trước khi giải mã (Encrypt-then-MAC)
        val payloadCoreNormalized = "$TOKEN_PREFIX.$ivHex.$cipherBase64Url"
        val payloadCoreRaw = "$TOKEN_PREFIX.$rawIvHex.$rawCipher"
        val expectedSigNormalized = computeHmacSha256Hex(payloadCoreNormalized, HMAC_SECRET)?.take(24)
        val expectedSigRaw = computeHmacSha256Hex(payloadCoreRaw, HMAC_SECRET)?.take(24)

        val sigBytes = signature.toByteArray(StandardCharsets.UTF_8)
        val isSigValid = (expectedSigNormalized != null &&
            MessageDigest.isEqual(sigBytes, expectedSigNormalized.toByteArray(StandardCharsets.UTF_8))) ||
            (expectedSigRaw != null &&
                MessageDigest.isEqual(sigBytes, expectedSigRaw.toByteArray(StandardCharsets.UTF_8)))
        if (!isSigValid) return null

        // 2. Giải mã AES-256-CBC để lấy lại domain dự án
        val ivBytes = hexToBytes(ivHex) ?: return null
        if (ivBytes.size != 16) return null

        val cipherBytes = try {
            Base64.decode(cipherBase64Url, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        } catch (_: Exception) {
            return null
        }

        val decryptedText = decryptAesCbc(cipherBytes, ivBytes) ?: return null
        if (!decryptedText.startsWith(PLAINTEXT_MAGIC)) return null

        val rawDomain = decryptedText.removePrefix(PLAINTEXT_MAGIC).trim()
        val normalizedDomain = normalizeDomain(rawDomain)
        return if (isValidHttpUrl(normalizedDomain)) normalizedDomain else null
    }

    private fun decryptAesCbc(cipherBytes: ByteArray, ivBytes: ByteArray): String? {
        return try {
            val keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(AES_SECRET.toByteArray(StandardCharsets.UTF_8))
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(keyBytes, "AES"),
                IvParameterSpec(ivBytes)
            )
            val plainBytes = cipher.doFinal(cipherBytes)
            String(plainBytes, StandardCharsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun hexToBytes(hex: String): ByteArray? {
        if (hex.length % 2 != 0) return null
        val result = ByteArray(hex.length / 2)
        for (i in result.indices) {
            val hi = Character.digit(hex[i * 2], 16)
            val lo = Character.digit(hex[i * 2 + 1], 16)
            if (hi == -1 || lo == -1) return null
            result[i] = ((hi shl 4) or lo).toByte()
        }
        return result
    }

    private fun normalizeDomain(rawUrl: String): String {
        var cleaned = rawUrl.trim()
        if (!cleaned.startsWith("http://", ignoreCase = true) &&
            !cleaned.startsWith("https://", ignoreCase = true)
        ) {
            cleaned = "https://$cleaned"
        }
        cleaned = cleaned
            .replace(Regex("/m/?$", RegexOption.IGNORE_CASE), "")
            .trimEnd('/')
        return cleaned
    }

    private fun isValidHttpUrl(url: String): Boolean {
        val parsed = try {
            Uri.parse(url)
        } catch (_: Exception) {
            return false
        }
        val scheme = parsed.scheme?.lowercase()
        val host = parsed.host
        return (scheme == "http" || scheme == "https") && !host.isNullOrBlank()
    }

    private fun computeHmacSha256Hex(message: String, secret: String): String? {
        return try {
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            val keySpec = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), HMAC_ALGORITHM)
            mac.init(keySpec)
            val bytes = mac.doFinal(message.toByteArray(StandardCharsets.UTF_8))
            buildString(bytes.size * 2) {
                for (b in bytes) {
                    append(String.format("%02x", b))
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
