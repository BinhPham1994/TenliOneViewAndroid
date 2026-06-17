package com.tenli.aiot.ui.features.setting.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tenli.aiot.R
import com.tenli.aiot.ui.theme.spacing

@Composable
fun SettingInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
    leadingIconRes: Int? = null,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    onClear: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = MaterialTheme.spacing.medium)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = MaterialTheme.spacing.small, bottom = 5.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            enabled = enabled,
            placeholder = { Text(placeholder, color = Color.LightGray) },
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            leadingIcon = leadingIconRes?.let {
                {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = MaterialTheme.spacing.extraSmall)
                ) {
                    if (value.isNotEmpty() && enabled) {
                        IconButton(onClick = {
                            onClear()
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }) {
                            Icon(Icons.Default.Clear, null, modifier = Modifier.size(20.dp), tint = Color.Gray)
                        }
                    }
                    if (isPassword && onPasswordToggle != null) {
                        IconButton(onClick = onPasswordToggle) {
                            Icon(
                                painter = painterResource(if (showPassword) R.drawable.show_pass else R.drawable.hide_pass),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }
            },
            visualTransformation = if (isPassword && !showPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent, // Ẩn gạch chân
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledPlaceholderColor = Color.LightGray
            ),
            shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
            singleLine = true
        )
    }
}