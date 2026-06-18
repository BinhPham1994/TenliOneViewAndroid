package com.tenli.oneview.ui.features.auth.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.main.LoginActivity
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.ui.utils.bounceClick
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val onToggleLanguage: () -> Unit = {
        val currentLang = LocaleManager.getLocale(context)
        val newLang = if (currentLang == "vi") "en" else "vi"

        LocaleManager.setLocale(context, newLang)

        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)

        (context as? Activity)?.finish()
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess()
                is LoginEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message, duration = SnackbarDuration.Short)
                }
            }
        }
    }

    LoginContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onDomainChange = { viewModel.onDomainChanged(it) },
        onEmailChange = { viewModel.onEmailChanged(it) },
        onPasswordChange = { viewModel.onPasswordChanged(it) },
        onTogglePassword = { viewModel.toggleShowPassword() },
        onLoginClick = { viewModel.login() },
        onToggleLanguage = onToggleLanguage
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onDomainChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onLoginClick: () -> Unit,
    onToggleLanguage: () -> Unit
) {

    val domainLabel = stringResource(id = R.string.login_domain_label)
    val domainHint = stringResource(id = R.string.login_domain_hint)
    val emailLabel = stringResource(id = R.string.login_email_label)
    val emailHint = stringResource(id = R.string.login_email_hint)
    val passwordLabel = stringResource(id = R.string.login_password_label)
    val passwordHint = stringResource(id = R.string.login_password_hint)
    val loginButtonText = stringResource(id = R.string.login_title)

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val currentLang = remember { LocaleManager.getLocale(context) }
    val isVietnamese = currentLang == "vi"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = MaterialTheme.spacing.large)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.medium),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .bounceClick { onToggleLanguage() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = if (isVietnamese) R.drawable.vietnam else R.drawable.kingdom),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isVietnamese) "VI" else "EN",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo_app),
                    contentDescription = null,
                    modifier = Modifier.height(120.dp)
                )
                Spacer(modifier = Modifier.height(40.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = domainLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    LoginInputField(
                        value = uiState.domain,
                        onValueChange = onDomainChange,
                        hint = domainHint,
                        onClear = { onDomainChange("") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = emailLabel,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    LoginInputField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        hint = emailHint,
                        onClear = { onEmailChange("") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = passwordLabel,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    LoginInputField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        hint = passwordHint,
                        isPassword = true,
                        showPassword = uiState.showPassword,
                        onPasswordToggle = onTogglePassword,
                        onClear = { onPasswordChange("") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            if (uiState.isLoginActive && !uiState.isLoading) onLoginClick()
                        })
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    enabled = uiState.isLoginActive && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.extraLarge)
                        .height(52.dp),
                    shape = RoundedCornerShape(MaterialTheme.spacing.radiusMedium),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary,

                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    elevation = if (uiState.isLoginActive) ButtonDefaults.buttonElevation() else ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(MaterialTheme.spacing.iconMedium),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = loginButtonText,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

            }
        }
    }
}