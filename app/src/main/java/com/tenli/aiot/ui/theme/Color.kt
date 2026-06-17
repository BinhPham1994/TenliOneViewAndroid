package com.tenli.aiot.ui.theme

import androidx.compose.ui.graphics.Color

val GreenPrimary = Color(0xFF4AA541)       // Thương hiệu Tenli
val GreenOnPrimary = Color(0xFFFFFFFF)     // Chữ trên nền xanh
val GreenContainer = Color(0xFFD1E8CF)    // Nền nhẹ cho Badge hoặc Nút phụ

// Không dùng xám xanh F2F2F7 nữa, dùng màu trắng sứ pha xanh lá cực nhẹ
val BackgroundLight = Color(0xFFEDF2EC)   // Nền hệ thống (Android M3 Surface)
val IoSBackgroundLight = Color(0xFFF2F2F7)
val SurfaceLight = Color(0xFFFFFFFF)      // Nền Card, Dialog (Trắng tinh)
val SurfaceVariant = Color(0xFFDEE5D8)    // Màu xám lá nhạt cho Input/Divider

// --- TEXT & CONTENT (Phân cấp rõ ràng theo Android) ---
val TextPrimary = Color(0xFF191C19)       // OnSurface: Gần như đen nhưng có ánh xanh đậm
val TextSecondary = Color(0xFF424940)     // OnSurfaceVariant: Xám lá (thay cho 8E8E93)
val TextDisabled = Color(0xFFC2C9BD)      // Trạng thái vô hiệu hóa (Outline)

// --- ERROR ---
val ErrorRed = Color(0xFFBA1A1A)          // Màu đỏ Error chuẩn Android (Trầm hơn iOS)