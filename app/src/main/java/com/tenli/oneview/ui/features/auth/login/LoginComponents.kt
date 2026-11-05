package com.tenli.oneview.ui.features.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.tenli.oneview.ui.theme.BrandPrimary
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing

/** Màu cuối gradient nút chính (bản PWA BaseButton.vue .premium-btn: #2563EB → #0761C4) */
private val LoginButtonGradientEnd = Color(0xFF0761C4)

private const val INACTIVE_ALPHA = 0.5f
private const val BUTTON_SHADOW_AMBIENT_ALPHA = 0.4f
private const val BUTTON_SHADOW_SPOT_ALPHA = 0.5f
private const val BUTTON_DISABLED_CONTENT_ALPHA = 0.85f
private const val CLEAR_ICON_ALPHA = 0.55f
private const val MENU_SELECTED_ALPHA_DARK = 0.18f
private const val MENU_SELECTED_ALPHA_LIGHT = 0.10f
private const val MENU_SHADOW_AMBIENT_DARK = 0.5f
private const val MENU_SHADOW_AMBIENT_LIGHT = 0.15f
private const val MENU_SHADOW_SPOT_DARK = 0.6f
private const val MENU_SHADOW_SPOT_LIGHT = 0.2f

/** Chiều cao ô nhập & nút chính trên màn đăng nhập (52dp @ baseline 365dp) */
@Composable
@ReadOnlyComposable
internal fun loginControlHeight(): Dp = MaterialTheme.spacing.buttonHeight + MaterialTheme.spacing.space4

/** Ô nhập liệu kiểu `.premium-input` của MobileLoginPage.vue: icon đầu dòng, nút xoá, nút ẩn/hiện mật khẩu. */
@Composable
fun LoginInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    onClear: () -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester = remember { FocusRequester() }
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }

    val shape = RoundedCornerShape(spacing.radiusRegular)
    val leadingIconTint = if (isFocused) colors.accent else colors.textSubtle
    val textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textTitle)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(loginControlHeight())
            .clip(shape)
            .background(colors.inputBg, shape)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        textStyle = textStyle,
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        cursorBrush = SolidColor(colors.accent),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = spacing.space16, end = spacing.space8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(spacing.iconSmallMedium),
                        tint = leadingIconTint
                    )
                    Spacer(modifier = Modifier.width(spacing.space10))
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPlaceholder,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }

                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            onClear()
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        },
                        modifier = Modifier.size(spacing.space32)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Clear",
                            modifier = Modifier.size(spacing.iconSmall),
                            tint = colors.textSubtle.copy(alpha = CLEAR_ICON_ALPHA)
                        )
                    }
                }
                if (isPassword && onPasswordToggle != null) {
                    IconButton(
                        onClick = onPasswordToggle,
                        modifier = Modifier.size(spacing.iconButton)
                    ) {
                        Icon(
                            imageVector = if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "Toggle Password",
                            modifier = Modifier.size(spacing.iconRegular),
                            tint = colors.textSubtle
                        )
                    }
                }
            }
        }
    )
}

/**
 * Nút chính dạng gradient (BaseButton.vue .premium-btn).
 * @param active trạng thái hiển thị (mờ + mất bóng khi false), tách biệt với [enabled].
 */
@Composable
internal fun LoginGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(spacing.radiusMedium)
    val brush = remember { Brush.linearGradient(listOf(BrandPrimary, LoginButtonGradientEnd)) }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(loginControlHeight())
            .alpha(if (active) 1f else INACTIVE_ALPHA)
            .shadow(
                elevation = if (active) spacing.space12 else 0.dp,
                shape = shape,
                ambientColor = BrandPrimary.copy(alpha = BUTTON_SHADOW_AMBIENT_ALPHA),
                spotColor = BrandPrimary.copy(alpha = BUTTON_SHADOW_SPOT_ALPHA)
            )
            .clip(shape)
            .background(brush, shape),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = colors.onAccent,
            disabledContentColor = colors.onAccent.copy(alpha = BUTTON_DISABLED_CONTENT_ALPHA)
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(spacing.iconSmallMedium),
                color = colors.onAccent,
                strokeWidth = spacing.progressStroke
            )
            Spacer(modifier = Modifier.width(spacing.space8))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onAccent
        )
    }
}

/**
 * Nút phụ viền mảnh (dùng cho nút "Quét mã QR" ở bước nhập domain).
 */
@Composable
internal fun LoginSecondaryButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val shape = RoundedCornerShape(spacing.radiusMedium)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(loginControlHeight())
            .clip(shape)
            .background(colors.inputBg, shape),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = colors.textTitle
        ),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(spacing.iconRegular),
            tint = colors.accent
        )
        Spacer(modifier = Modifier.width(spacing.space8))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textTitle
        )
    }
}

/** Ô check vuông bo góc kiểu `.premium-checkbox` (không tự xử lý click – gắn qua [modifier] hoặc hàng cha). */
@Composable
internal fun LoginCheckbox(
    checked: Boolean,
    size: Dp,
    cornerRadius: Dp,
    checkIconSize: Dp,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(if (checked) colors.accent else colors.controlBg)
            .border(
                width = MaterialTheme.spacing.borderMedium,
                color = if (checked) colors.accent else colors.borderStrong,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(checkIconSize)
            )
        }
    }
}

/** Nút dạng viên nhỏ ở góc trên (chọn ngôn ngữ / giao diện). */
@Composable
internal fun LoginTopPill(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val spacing = MaterialTheme.spacing
    Row(
        modifier = modifier
            .height(spacing.space32)
            .clip(RoundedCornerShape(spacing.radiusSmallMedium))
            .background(MaterialTheme.appColors.buttonSecondaryBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}

/** Menu popup thả xuống bên dưới [LoginTopPill], căn phải. */
@Composable
internal fun LoginDropdownPopup(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isDark = colors.isDark
    val offsetY = with(LocalDensity.current) { (spacing.space32 + spacing.space6).roundToPx() }
    val shape = RoundedCornerShape(spacing.radiusRegular)

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, offsetY),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        Column(
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .shadow(
                    elevation = spacing.space16,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = if (isDark) MENU_SHADOW_AMBIENT_DARK else MENU_SHADOW_AMBIENT_LIGHT),
                    spotColor = Color.Black.copy(alpha = if (isDark) MENU_SHADOW_SPOT_DARK else MENU_SHADOW_SPOT_LIGHT)
                )
                .clip(shape)
                .background(colors.surfaceMenu)
                .border(spacing.borderMedium, colors.border, shape)
                .padding(spacing.space5),
            verticalArrangement = Arrangement.spacedBy(spacing.space2),
            content = content
        )
    }
}

/** Một lựa chọn trong [LoginDropdownPopup]; [leading] nhận màu tint theo trạng thái chọn. */
@Composable
internal fun LoginMenuOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    leading: @Composable (tint: Color) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val selectedBg = colors.accent.copy(
        alpha = if (colors.isDark) MENU_SELECTED_ALPHA_DARK else MENU_SELECTED_ALPHA_LIGHT
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(spacing.radiusSmallMedium))
            .background(if (selected) selectedBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = spacing.space12, vertical = spacing.space8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading(if (selected) colors.accent else colors.textSubtle)
        Spacer(modifier = Modifier.width(spacing.space8))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colors.accent else colors.textTitle
        )
    }
}
