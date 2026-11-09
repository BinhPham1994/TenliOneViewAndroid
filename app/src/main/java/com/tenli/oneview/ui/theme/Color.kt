package com.tenli.oneview.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =====================================================================================
// 1. RAW PALETTE (chỉ dùng bên trong package theme hoặc khi cần màu "tuyệt đối" như trên video đen)
//    Ở tầng giao diện, ưu tiên dùng MaterialTheme.colorScheme.* hoặc MaterialTheme.appColors.*
// =====================================================================================

// --- BRAND & PRIMARY (Cloned from tenli-oneview tokens.css & mobile.css) ---
val BrandPrimary = Color(0xFF2563EB)       // --color-primary
val BrandPrimaryButton = Color(0xFF255EC2) // --color-btn-primary
val BrandAccent = Color(0xFF0980EE)        // Accent blue in mobile cards/icons
val BrandActiveNavDark = Color(0xFF3B82F6) // Active bottom nav pill in dark mode
val BrandOnPrimary = Color(0xFFFFFFFF)     // Chữ trên nền chính
val BrandContainer = Color(0xFFDBEAFE)     // Nền nhẹ cho Badge hoặc Nút phụ (Blue 100)
val BrandContainerDark = Color(0xFF1E3A8A) // Nền nhẹ cho Dark mode
val BrandLight = Color(0xFF60A5FA)         // Blue 400 - primary trên nền tối
val BrandLighter = Color(0xFF93C5FD)       // Blue 300

// --- SEMANTIC COLORS ---
val SuccessGreen = Color(0xFF10B981)       // #10b981 / #77c512
val SuccessGreenDeep = Color(0xFF15803D)   // Green 700 - chữ thành công trên nền sáng
val WarningOrange = Color(0xFFF59E0B)      // #f59e0b / #ff9119
val WarningAmberDeep = Color(0xFFD97706)   // Amber 600
val ErrorRed = Color(0xFFEF4444)           // --color-error (#ef4444)
val ErrorRedDeep = Color(0xFFDC2626)       // Red 600 - nút nguy hiểm / chữ lỗi đậm
val InfoCyan = Color(0xFF0EA5E9)           // #0ea5e9 / #00bac7
val InfoSkyDeep = Color(0xFF0284C7)        // Sky 600
val PurpleAccent = Color(0xFFA855F7)       // #a855f7
val VioletAccent = Color(0xFF8B5CF6)       // Violet 500
val IndigoAccent = Color(0xFF6366F1)       // Indigo 500
val PinkAccent = Color(0xFFEC4899)         // Pink 500
val OrangeUserAccent = Color(0xFFF97316)   // #f97316
val OrangeDeep = Color(0xFFEA580C)         // Orange 600
val IosGreen = Color(0xFF34C759)           // iOS system green (switch on)
val IosRed = Color(0xFFFF3B30)             // iOS system red

// --- SLATE SCALE (Tailwind Slate – nền tảng chung của bản PWA) ---
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)

// --- NỀN (BACKGROUND) LIGHT (Cloned from tenli-oneview html:not(.dark)) ---
val BackgroundLight = Color(0xFFE8E8ED)    // --color-bg / --color-bg-layout (#E8E8ED)
val SurfaceLight = Color(0xFFFFFFFF)       // --color-bg-elevated / --color-bg-surface (#FFFFFF)
val SurfaceSubtleLight = Color(0xFFEDF0F5) // --color-bg-subtle (#EDF0F5)
val SurfaceVariant = Color(0xFFF2F2F7)     // --color-bg-hover (#F2F2F7)
val InputFieldLight = Color(0xFFF3F4F7)    // Nền ô nhập liệu trên nền trắng

// --- TEXT & CONTENT LIGHT ---
val TextTitleLight = Slate800              // --color-text-title (#1E293B)
val TextPrimary = Slate700                 // --color-text-primary (#334155)
val TextSecondary = Slate500               // --color-text-secondary (#64748B)
val TextTertiaryLight = Color(0xFF78859B)  // --color-text-tertiary (#78859B)
val TextDisabled = Slate300                // Border / Disabled

// --- NỀN (BACKGROUND) DARK (Cloned from tenli-oneview html.dark) ---
val BackgroundDark = Slate900              // --color-bg / --color-bg-layout (#0F172A)
val SurfaceDark = Color(0xFF172033)        // --color-bg-elevated / --color-bg-surface (#172033)
val SurfaceSubtleDark = Color(0xFF242F41)  // --color-bg-subtle (#242F41)
val SurfaceVariantDark = Color(0xFF263449) // --color-bg-hover (#263449)

// --- TEXT & CONTENT DARK ---
val TextPrimaryDark = Color(0xFFFFFFFF)    // --color-text-primary (#FFFFFF)
val TextSecondaryDark = Slate300           // --color-text-secondary (rgba(255,255,255,0.82))
val TextTertiaryDark = Slate400            // --color-text-tertiary (rgba(255,255,255,0.65))
val TextDisabledDark = Slate700            // Outline / Divider in Dark mode

// =====================================================================================
// 2. SEMANTIC TOKENS – tự đổi theo Light/Dark. Dùng qua `MaterialTheme.appColors.xxx`
//    => Không cần `if (isDark) Color(..) else Color(..)` ở tầng giao diện nữa.
// =====================================================================================

@Immutable
data class AppColors(
    val isDark: Boolean,

    // --- Nền ---
    /** Nền trang (layout) */
    val pageBg: Color,
    /** Nền panel / thẻ / sheet chính (White | #172033) */
    val surface: Color,
    /** Thẻ nổi trên nền surface, ô nhập liệu trên sheet (White | #242F41) */
    val surfaceRaised: Color,
    /** Nền sheet kiểu iOS grouped (#F2F2F7 | #172033) */
    val surfaceGrouped: Color,
    /** Nền menu / dropdown / popup (White | Slate800) */
    val surfaceMenu: Color,
    /** Nền control nhẹ: chip, nút icon, badge đếm (Slate100 | White 7%) */
    val controlBg: Color,
    /** Nền nút phụ / nút Hủy (White | #263449) */
    val buttonSecondaryBg: Color,
    /** Nền nút đóng tròn trên sheet / header (Black 7% | #263449) */
    val closeButtonBg: Color,
    /** Nền ô nhập liệu (#F3F4F7 | White 4%) */
    val inputBg: Color,
    /** Nền placeholder ảnh / thumbnail (Slate200 | Slate900 45%) */
    val thumbnailBg: Color,
    /** Lớp phủ tối phía sau dialog / overlay */
    val scrim: Color,

    // --- Chữ & Icon ---
    /** Chữ đậm nhất – tiêu đề sheet, giá trị (Slate900 | Slate50) */
    val textStrong: Color,
    /** Tiêu đề thẻ / mục (Slate800 | Slate50) */
    val textTitle: Color,
    /** Nội dung thường (Slate700 | Slate300) */
    val textBody: Color,
    /** Chữ phụ / mô tả (Slate500 | Slate400) */
    val textMuted: Color,
    /** Chữ phụ đậm hơn – icon hành động, phụ đề (Slate600 | Slate400) */
    val textSubtle: Color,
    /** Placeholder / gợi ý (Slate400 | Slate500) */
    val textPlaceholder: Color,
    /** Icon mặc định (Slate700 | Slate300) */
    val iconDefault: Color,

    // --- Đường kẻ ---
    /** Divider mảnh giữa các dòng (Black 6% | White 6%) */
    val divider: Color,
    /** Viền thẻ / menu (Black 8% | White 8%) */
    val border: Color,
    /** Viền / divider đậm (Slate300 | Slate700) */
    val borderStrong: Color,
    /** Thanh kéo (drag handle) của bottom sheet */
    val handle: Color,

    // --- Thương hiệu & trạng thái ---
    val accent: Color,
    /** Màu nội dung đang active (tab, filter chọn) */
    val activeContent: Color,
    val onAccent: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val errorStrong: Color,
    val info: Color,
    val purple: Color,
    val orange: Color,
    val pink: Color,
    val indigo: Color,
    /** Nền nổi trên video / ảnh (luôn tối) */
    val mediaOverlay: Color,
    /** Nền khung video */
    val mediaBg: Color,
    /** Chữ/icon trên video (luôn sáng) */
    val onMedia: Color
)

val LightAppColors = AppColors(
    isDark = false,
    pageBg = BackgroundLight,
    surface = SurfaceLight,
    surfaceRaised = SurfaceLight,
    surfaceGrouped = SurfaceVariant,
    surfaceMenu = SurfaceLight,
    controlBg = Slate100,
    buttonSecondaryBg = SurfaceLight,
    closeButtonBg = Color.Black.copy(alpha = 0.07f),
    inputBg = InputFieldLight,
    thumbnailBg = Slate200,
    scrim = Slate900.copy(alpha = 0.45f),
    textStrong = Slate900,
    textTitle = Slate800,
    textBody = Slate700,
    textMuted = Slate500,
    textSubtle = Slate600,
    textPlaceholder = Slate400,
    iconDefault = Slate700,
    divider = Color.Black.copy(alpha = 0.06f),
    border = Color.Black.copy(alpha = 0.08f),
    borderStrong = Slate300,
    handle = Slate400.copy(alpha = 0.38f),
    accent = BrandAccent,
    activeContent = BrandPrimary,
    onAccent = Color.White,
    success = SuccessGreen,
    warning = WarningOrange,
    error = ErrorRed,
    errorStrong = ErrorRedDeep,
    info = InfoCyan,
    purple = PurpleAccent,
    orange = OrangeUserAccent,
    pink = PinkAccent,
    indigo = IndigoAccent,
    mediaOverlay = Slate900.copy(alpha = 0.65f),
    mediaBg = Color.Black,
    onMedia = Color.White
)

val DarkAppColors = AppColors(
    isDark = true,
    pageBg = BackgroundDark,
    surface = SurfaceDark,
    surfaceRaised = SurfaceSubtleDark,
    surfaceGrouped = SurfaceDark,
    surfaceMenu = Slate800,
    controlBg = Color.White.copy(alpha = 0.07f),
    buttonSecondaryBg = SurfaceVariantDark,
    closeButtonBg = SurfaceVariantDark,
    inputBg = Color.White.copy(alpha = 0.04f),
    thumbnailBg = Slate900.copy(alpha = 0.45f),
    scrim = Color.Black.copy(alpha = 0.6f),
    textStrong = Slate50,
    textTitle = Slate50,
    textBody = Slate300,
    textMuted = Slate400,
    textSubtle = Slate400,
    textPlaceholder = Slate500,
    iconDefault = Slate300,
    divider = Color.White.copy(alpha = 0.06f),
    border = Color.White.copy(alpha = 0.08f),
    borderStrong = Slate700,
    handle = Slate400.copy(alpha = 0.38f),
    accent = BrandAccent,
    activeContent = BrandLight,
    onAccent = Color.White,
    success = SuccessGreen,
    warning = WarningOrange,
    error = ErrorRed,
    errorStrong = ErrorRedDeep,
    info = InfoCyan,
    purple = PurpleAccent,
    orange = OrangeUserAccent,
    pink = PinkAccent,
    indigo = IndigoAccent,
    mediaOverlay = Slate900.copy(alpha = 0.65f),
    mediaBg = Color.Black,
    onMedia = Color.White
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
