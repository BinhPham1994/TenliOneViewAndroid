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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.ui.utils.bounceClick

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

        val intent = Intent(context, MainActivity::class.java)
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
    
    var isTermsAccepted by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 30.dp)
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
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .bounceClick { onToggleLanguage() }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                    modifier = Modifier.height(100.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AI OneView",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hệ thống giám sát, vận hành thông minh",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(40.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    LoginInputField(
                        value = uiState.domain,
                        onValueChange = onDomainChange,
                        label = domainHint,
                        leadingIcon = Icons.Outlined.Public,
                        onClear = { onDomainChange("") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    LoginInputField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = emailLabel,
                        leadingIcon = Icons.Outlined.Email,
                        onClear = { onEmailChange("") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    LoginInputField(
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        label = passwordLabel,
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        showPassword = uiState.showPassword,
                        onPasswordToggle = onTogglePassword,
                        onClear = { onPasswordChange("") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                        })
                    )
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    enabled = uiState.isLoginActive && !uiState.isLoading && isTermsAccepted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
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

            val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {
                Icon(
                    imageVector = if (isTermsAccepted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Accept terms",
                    tint = if (isTermsAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .bounceClick { isTermsAccepted = !isTermsAccepted }
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                val annotatedString = buildAnnotatedString {
                    append("Tôi xác nhận đã đọc và đồng ý với ")
                    pushStringAnnotation(tag = "terms", annotation = "https://tenli.ai/terms-of-use")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Điều khoản sử dụng")
                    }
                    pop()
                    append(" và ")
                    pushStringAnnotation(tag = "privacy", annotation = "https://tenli.ai/chinh-sach-bao-mat")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                        append("Chính sách Bảo mật")
                    }
                    pop()
                    append(" của Tenli")
                }

                androidx.compose.foundation.text.ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start
                    ),
                    onClick = { offset ->
                        val termsAnnotation = annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset).firstOrNull()
                        val privacyAnnotation = annotatedString.getStringAnnotations(tag = "privacy", start = offset, end = offset).firstOrNull()

                        if (termsAnnotation != null) {
                            uriHandler.openUri(termsAnnotation.item)
                        } else if (privacyAnnotation != null) {
                            uriHandler.openUri(privacyAnnotation.item)
                        } else {
                            isTermsAccepted = !isTermsAccepted
                        }
                    }
                )
            }
        }
    }
}