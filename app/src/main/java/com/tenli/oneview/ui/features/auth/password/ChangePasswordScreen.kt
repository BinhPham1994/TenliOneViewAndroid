package com.tenli.oneview.ui.features.auth.password

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.ui.features.auth.login.LoginInputField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onPasswordChangedSuccess: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel(factory = ChangePasswordViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val backgroundColor = if (isDark) MaterialTheme.colorScheme.background else Color(0xFFF2F4F8)
    val surfaceColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
    val textColor = MaterialTheme.colorScheme.onBackground
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is ChangePasswordEvent.Success -> {
                    Toast.makeText(context, context.getString(R.string.msg_change_password_success), Toast.LENGTH_SHORT).show()
                    onPasswordChangedSuccess()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.acc_password_security),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 24.dp).align(Alignment.Start)
            )
                
                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    )
                }

                LoginInputField(
                    value = uiState.oldPassword,
                    onValueChange = viewModel::onOldPasswordChanged,
                    label = stringResource(id = R.string.pwd_label_current),
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    showPassword = uiState.showOldPassword,
                    onPasswordToggle = viewModel::toggleShowOldPassword,
                    onClear = { viewModel.onOldPasswordChanged("") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(16.dp))

                LoginInputField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onNewPasswordChanged,
                    label = stringResource(id = R.string.pwd_label_new),
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    showPassword = uiState.showNewPassword,
                    onPasswordToggle = viewModel::toggleShowNewPassword,
                    onClear = { viewModel.onNewPasswordChanged("") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Text(
                    text = "Mật khẩu từ 6-20 ký tự, gồm chữ và số",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, start = 16.dp, bottom = 12.dp).align(Alignment.Start)
                )

                LoginInputField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChanged,
                    label = stringResource(id = R.string.pwd_label_confirm),
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    showPassword = uiState.showConfirmPassword,
                    onPasswordToggle = viewModel::toggleShowConfirmPassword,
                    onClear = { viewModel.onConfirmPasswordChanged("") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        viewModel.changePassword()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = uiState.isSaveActive && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    elevation = if (uiState.isSaveActive) ButtonDefaults.buttonElevation() else ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.pwd_btn_agree),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }
    }
