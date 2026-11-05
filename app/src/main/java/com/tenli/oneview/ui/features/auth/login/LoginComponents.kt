package com.tenli.oneview.ui.features.auth.login

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tenli.oneview.R
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.bounceClick

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
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }

    val themeMode by ThemeManager.themeModeFlow.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }

    // Exact colors from MobileLoginPage.vue (.premium-input)
    val inputBg = if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF3F4F7)
    val leadingIconTint = when {
        isFocused -> BrandAccent // #0980EE
        isDark -> Color(0xFF94A3B8)
        else -> Color(0xFF475569)
    }
    val placeholderColor = if (isDark) Color(0xFF78879E) else Color(0xFF94A3B8)
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val actionIconTint = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(inputBg, RoundedCornerShape(14.dp))
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused },
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        ),
        visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true,
        cursorBrush = SolidColor(BrandAccent),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        end = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = leadingIconTint
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = placeholderColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onClear()
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Clear",
                                modifier = Modifier.size(16.dp),
                                tint = actionIconTint.copy(alpha = 0.55f)
                            )
                        }
                    }
                    if (isPassword && onPasswordToggle != null) {
                        IconButton(
                            onClick = onPasswordToggle,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = if (showPassword) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = "Toggle Password",
                                modifier = Modifier.size(19.dp),
                                tint = actionIconTint
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun SocialCircleButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .bounceClick { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(60.dp)
        )
    }
}

@Composable
fun OrDivider() {
    val orText = stringResource(id = R.string.login_or_divider)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = MaterialTheme.spacing.borderMedium,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = orText,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = MaterialTheme.spacing.borderMedium,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
