package com.tenli.oneview.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Hệ thống kích thước dùng chung cho toàn app (spacing, icon, radius, kích cỡ component).
 *
 * - Tất cả giá trị được nhân với [scaleFactor] (tính theo độ rộng nhỏ nhất của màn hình, baseline 365dp)
 *   để giao diện cân đối trên mọi kích thước điện thoại Android.
 * - Truy cập qua `MaterialTheme.spacing.xxx`. KHÔNG hard-code `xx.dp` ở tầng giao diện.
 * - Thang spacing đánh số theo giá trị thiết kế gốc (space12 = 12dp @365dp) để map 1-1 với bản thiết kế/PWA.
 */
@Immutable
class Dimensions(val scaleFactor: Float = 1f) {
    private fun s(value: Float): Dp = (value * scaleFactor).dp

    // ---------------- Spacing scale ----------------
    val space1: Dp = s(1f)
    val space2: Dp = s(2f)
    val space3: Dp = s(3f)
    val space4: Dp = s(4f)
    val space5: Dp = s(5f)
    val space6: Dp = s(6f)
    val space8: Dp = s(8f)
    val space10: Dp = s(10f)
    val space12: Dp = s(12f)
    val space14: Dp = s(14f)
    val space16: Dp = s(16f)
    val space18: Dp = s(18f)
    val space20: Dp = s(20f)
    val space24: Dp = s(24f)
    val space28: Dp = s(28f)
    val space32: Dp = s(32f)
    val space40: Dp = s(40f)
    val space48: Dp = s(48f)
    val space56: Dp = s(56f)
    val space64: Dp = s(64f)

    // Alias ngữ nghĩa (giữ tương thích code cũ)
    val extraSmall: Dp get() = space4
    val small: Dp get() = space8
    val medium: Dp get() = space16
    val large: Dp get() = space24
    val extraLarge: Dp get() = space32
    /** Padding ngang mặc định của màn hình */
    val paddingScreen: Dp get() = space16
    /** Padding bên trong thẻ */
    val paddingCard: Dp get() = space12

    // ---------------- Icon sizes ----------------
    val iconTiny: Dp = s(12f)
    val iconXSmall: Dp = s(14f)
    val iconSmall: Dp = s(16f)
    val iconSmallMedium: Dp = s(18f)
    val iconRegular: Dp = s(20f)
    val iconMedium: Dp = s(24f)
    val iconLarge: Dp = s(32f)
    val iconExtraLarge: Dp = s(48f)
    val iconHuge: Dp = s(64f)

    // ---------------- Corner radius ----------------
    val radiusXSmall: Dp = s(4f)
    val radiusTiny: Dp = s(6f)
    val radiusSmall: Dp = s(8f)
    val radiusSmallMedium: Dp = s(10f)
    val radiusCompact: Dp = s(12f)
    val radiusRegular: Dp = s(14f)
    val radiusMedium: Dp = s(16f)
    val radiusLarge: Dp = s(20f)
    val radiusExtraLarge: Dp = s(24f)
    val radiusSheet: Dp = s(28f)

    // ---------------- Borders / dividers (không scale để luôn sắc nét) ----------------
    val borderHairline: Dp = 0.5.dp
    val borderThin: Dp = 0.5.dp
    val borderMedium: Dp = 1.dp
    val borderThick: Dp = 1.5.dp
    val borderFocus: Dp = 2.dp

    // ---------------- Component sizes ----------------
    /** Vùng chạm tối thiểu theo Material guideline */
    val touchTarget: Dp = s(48f)
    val buttonHeight: Dp = s(48f)
    val buttonHeightSmall: Dp = s(36f)
    val inputHeight: Dp = s(48f)
    val chipHeight: Dp = s(28f)
    val iconButtonSmall: Dp = s(28f)
    val iconButton: Dp = s(36f)
    val iconButtonLarge: Dp = s(44f)
    val avatarSmall: Dp = s(30f)
    val avatarMedium: Dp = s(40f)
    val avatarLarge: Dp = s(56f)
    val avatarExtraLarge: Dp = s(72f)
    val logoAbout: Dp = s(140f)
    val aboutTopPadding: Dp = s(96f)
    val topBarHeight: Dp = s(52f)
    val listItemHeight: Dp = s(52f)
    val sheetHandleWidth: Dp = s(36f)
    val sheetHandleHeight: Dp = s(5f)
    val statusDot: Dp = s(8f)
    val badgeMinSize: Dp = s(18f)
    val progressStroke: Dp = s(2f)
    val bottomNavHeight: Dp = s(64f)
    /** Độ rộng tối đa của nội dung dạng form/dialog trên màn hình lớn (tablet, foldable) */
    val contentMaxWidth: Dp = 560.dp
    val dialogMaxWidth: Dp = 420.dp

    companion object {
        /** Giới hạn hệ số co giãn để không vỡ bố cục trên máy quá nhỏ / quá lớn */
        const val MIN_SCALE = 0.95f
        const val MAX_SCALE = 1.15f

        fun scaled(scale: Float): Dimensions = Dimensions(scale.coerceIn(MIN_SCALE, MAX_SCALE))
    }
}

val LocalSpacing = staticCompositionLocalOf { Dimensions() }
