package com.tenli.aiot.ui.theme

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
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 18.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 20.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 16.sp, platformStyle = defaultPlatformStyle, lineHeightStyle = compactLineHeightStyle
    )
)