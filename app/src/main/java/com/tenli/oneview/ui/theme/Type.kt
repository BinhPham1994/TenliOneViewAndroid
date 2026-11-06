package com.tenli.oneview.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

val compactLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center, trim = LineHeightStyle.Trim.Both
)

val defaultPlatformStyle = PlatformTextStyle(includeFontPadding = false)

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 18.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 18.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    )
)

// --- Kiểu chữ mở rộng (ngoài bộ Material 3) – dùng qua MaterialTheme.typography.caption / .micro ---
private val CaptionTextStyle = TextStyle(
    fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
)
private val MicroTextStyle = TextStyle(
    fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 12.sp, letterSpacing = 0.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
)

/** Chú thích nhỏ: timestamp, badge, meta (11sp) */
val Typography.caption: TextStyle get() = CaptionTextStyle

/** Chữ siêu nhỏ: nhãn trên ảnh, badge số (10sp) */
val Typography.micro: TextStyle get() = MicroTextStyle
