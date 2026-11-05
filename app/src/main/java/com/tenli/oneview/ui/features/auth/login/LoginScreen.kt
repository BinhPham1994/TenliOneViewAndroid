package com.tenli.oneview.ui.features.auth.login

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DesktopWindows
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.ErrorRed
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.ui.utils.bounceClick
import java.util.Calendar

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val onSetLanguage: (String) -> Unit = { newLang ->
        val currentLang = LocaleManager.getLocale(context)
        if (currentLang != newLang) {
            LocaleManager.setLocale(context, newLang)
            (context as? Activity)?.recreate()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadSavedCredentials()
        viewModel.event.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess()
                is LoginEvent.ShowError -> {
                    com.tenli.oneview.ui.component.MobileTopPopupManager.error(event.message)
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
        onRememberMeChange = { viewModel.onRememberMeChanged(it) },
        onTogglePassword = { viewModel.toggleShowPassword() },
        onClearError = { viewModel.clearError() },
        onLoginClick = { viewModel.login() },
        onSetLanguage = onSetLanguage
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onDomainChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onTogglePassword: () -> Unit,
    onClearError: () -> Unit,
    onLoginClick: () -> Unit,
    onSetLanguage: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val domainFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }

    BackHandler(enabled = currentStep == 2) {
        focusManager.clearFocus()
        keyboardController?.hide()
        currentStep = 1
    }

    LaunchedEffect(currentStep) {
        LocaleManager.getLocale(context)
        // Chờ hiển thị rõ giao diện (và hoàn tất hiệu ứng chuyển bước) rồi mới focus + bật bàn phím
        kotlinx.coroutines.delay(700)
        try {
            if (currentStep == 1) {
                domainFocusRequester.requestFocus()
            } else {
                emailFocusRequester.requestFocus()
            }
            kotlinx.coroutines.delay(100)
            keyboardController?.show()
        } catch (_: Exception) {
        }
    }
    val currentLang by LocaleManager.languageFlow.collectAsState()
    val isVietnamese = currentLang == "vi"

    val themeMode by ThemeManager.themeModeFlow.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }

    var isTermsAccepted by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val pageBg = if (isDark) Color(0xFF0F172A) else Color(0xFFE8E8ED)
    val cardBg = if (isDark) Color(0xFF162033) else Color.White
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    val domainPlaceholder = if (isVietnamese) "Nhập domain dự án (VD: https://app.tenli.ai)" else "Enter project domain (e.g. https://app.tenli.ai)"
    val usernamePlaceholder = if (isVietnamese) "Nhập tên đăng nhập" else "Enter username"
    val passwordPlaceholder = if (isVietnamese) "Nhập mật khẩu" else "Enter password"
    val loginTitle = "Tenli OneView"
    val loginSubtitle = if (isVietnamese) "Hệ thống giám sát, vận hành thông minh" else "Smart monitoring & operation system"
    val rememberMeText = if (isVietnamese) "Ghi nhớ đăng nhập" else "Remember me"
    val loginBtnText = if (uiState.isLoading) {
        if (isVietnamese) "Đang xác thực..." else "Authenticating..."
    } else {
        if (isVietnamese) "Đăng nhập" else "Sign In"
    }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    LaunchedEffect(uiState.errorMessage) {
        if (!uiState.errorMessage.isNullOrEmpty()) {
            com.tenli.oneview.ui.component.MobileTopPopupManager.error(uiState.errorMessage)
            onClearError()
        }
    }

    Scaffold(
        containerColor = pageBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            // 1. Top-Right Language & Theme Switchers OUTSIDE the card (matching MobileLoginPage.vue .mobile-login-top-actions)
            val popupOffsetY = with(LocalDensity.current) { 38.dp.roundToPx() }
            val menuBg = if (isDark) Color(0xFF1E293B) else Color.White
            val menuBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
            val selectedPillBg = BrandAccent.copy(alpha = if (isDark) 0.18f else 0.10f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .padding(bottom = 12.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                // Language Switcher (`🇻🇳 VN` / `🇺🇸 EN`)
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF1E2B44) else Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showLangMenu = !showLangMenu }
                            .padding(horizontal = 10.dp)
                    ) {
                        Text(
                            text = if (isVietnamese) "🇻🇳" else "🇺🇸",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isVietnamese) "VN" else "EN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = titleColor
                            )
                        )
                    }

                    if (showLangMenu) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, popupOffsetY),
                            onDismissRequest = { showLangMenu = false },
                            properties = PopupProperties(focusable = true)
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        ambientColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.15f),
                                        spotColor = Color.Black.copy(alpha = if (isDark) 0.6f else 0.2f)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(menuBg)
                                    .border(1.dp, menuBorder, RoundedCornerShape(14.dp))
                                    .padding(5.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val langOptions = listOf(
                                    Triple("vi", "🇻🇳", "Tiếng Việt"),
                                    Triple("en", "🇺🇸", "English")
                                )
                                langOptions.forEach { (code, flag, label) ->
                                    val isSelected = (code == "vi" && isVietnamese) || (code == "en" && !isVietnamese)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) selectedPillBg else Color.Transparent)
                                            .clickable {
                                                showLangMenu = false
                                                onSetLanguage(code)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = flag, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (isSelected) BrandAccent else titleColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Theme Switcher (Sun / Moon / Monitor matching ThemeSwitcher.vue)
                Box {
                    val themeIcon = when (themeMode) {
                        0 -> LucideIcons.Sun
                        1 -> LucideIcons.Moon
                        else -> LucideIcons.Monitor
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isDark) Color(0xFF1E2B44) else Color.White)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showThemeMenu = !showThemeMenu },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = "Theme",
                            tint = titleColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (showThemeMenu) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, popupOffsetY),
                            onDismissRequest = { showThemeMenu = false },
                            properties = PopupProperties(focusable = true)
                        ) {
                            Column(
                                modifier = Modifier
                                    .width(IntrinsicSize.Max)
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        ambientColor = Color.Black.copy(alpha = if (isDark) 0.5f else 0.15f),
                                        spotColor = Color.Black.copy(alpha = if (isDark) 0.6f else 0.2f)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(menuBg)
                                    .border(1.dp, menuBorder, RoundedCornerShape(14.dp))
                                    .padding(5.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                val themeOptions = listOf(
                                    Triple(0, LucideIcons.Sun, if (isVietnamese) "Sáng" else "Light"),
                                    Triple(1, LucideIcons.Moon, if (isVietnamese) "Tối" else "Dark"),
                                    Triple(2, LucideIcons.Monitor, if (isVietnamese) "Hệ thống" else "System")
                                )
                                themeOptions.forEach { (mode, iconVec, label) ->
                                    val isSelected = themeMode == mode
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) selectedPillBg else Color.Transparent)
                                            .clickable {
                                                showThemeMenu = false
                                                ThemeManager.setThemeMode(mode)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = iconVec,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp),
                                            tint = if (isSelected) BrandAccent else subtitleColor
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            color = if (isSelected) BrandAccent else titleColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Centered Glass Login Card (matching MobileLoginPage.vue .glass-card)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 400.dp)
                    .shadow(
                        elevation = if (isDark) 28.dp else 24.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.18f),
                        spotColor = Color.Black.copy(alpha = if (isDark) 0.55f else 0.22f)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = cardBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo (`logo_pwa.png`)
                    Image(
                        painter = painterResource(id = R.drawable.logo_pwa),
                        contentDescription = "Tenli",
                        modifier = Modifier.size(84.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title ("Tenli OneView")
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = titleColor)) {
                                append("Tenli ")
                            }
                            withStyle(style = SpanStyle(color = BrandAccent)) {
                                append("OneView")
                            }
                        },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subtitle ("Hệ thống giám sát, vận hành thông minh")
                    Text(
                        text = loginSubtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = subtitleColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    val buttonBrush = Brush.linearGradient(
                        colors = listOf(Color(0xFF2563EB), Color(0xFF0761C4))
                    )
                    val onNextStep: () -> Unit = {
                        if (uiState.domain.trim().isEmpty()) {
                            com.tenli.oneview.ui.component.MobileTopPopupManager.error(
                                if (isVietnamese) "Vui lòng nhập domain dự án" else "Please enter project domain"
                            )
                        } else {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            currentStep = 2
                        }
                    }

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (targetState > initialState) {
                                // Chuyển từ Bước 1 -> Bước 2: Trượt từ phải sang trái
                                (slideInHorizontally(animationSpec = tween(320)) { fullWidth -> fullWidth } +
                                    fadeIn(animationSpec = tween(260)))
                                    .togetherWith(
                                        slideOutHorizontally(animationSpec = tween(320)) { fullWidth -> -fullWidth } +
                                            fadeOut(animationSpec = tween(200))
                                    )
                            } else {
                                // Quay lại Bước 2 -> Bước 1: Trượt từ trái sang phải
                                (slideInHorizontally(animationSpec = tween(320)) { fullWidth -> -fullWidth } +
                                    fadeIn(animationSpec = tween(260)))
                                    .togetherWith(
                                        slideOutHorizontally(animationSpec = tween(320)) { fullWidth -> fullWidth } +
                                            fadeOut(animationSpec = tween(200))
                                    )
                            }.using(SizeTransform(clip = false))
                        },
                        label = "LoginStepTransition"
                    ) { step ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (step == 1) {
                                // BƯỚC 1: Chỉ nhập Domain dự án + Nút Tiếp tục
                                LoginInputField(
                                    value = uiState.domain,
                                    onValueChange = onDomainChange,
                                    label = domainPlaceholder,
                                    leadingIcon = Icons.Outlined.Public,
                                    onClear = { onDomainChange("") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Uri,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { onNextStep() },
                                        onDone = { onNextStep() }
                                    ),
                                    focusRequester = domainFocusRequester
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                val isDomainReady = uiState.domain.trim().isNotEmpty()
                                Button(
                                    onClick = { onNextStep() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .alpha(if (isDomainReady) 1f else 0.5f)
                                        .shadow(
                                            elevation = if (isDomainReady) 12.dp else 0.dp,
                                            shape = RoundedCornerShape(16.dp),
                                            ambientColor = Color(0xFF2563EB).copy(alpha = 0.4f),
                                            spotColor = Color(0xFF2563EB).copy(alpha = 0.5f)
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(buttonBrush, RoundedCornerShape(16.dp)),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White.copy(alpha = 0.85f)
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                                ) {
                                    Text(
                                        text = if (isVietnamese) "Tiếp tục" else "Continue",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            } else {
                                // BƯỚC 2: Nhập Tài khoản & Mật khẩu (không hiển thị lại dòng domain)
                                LoginInputField(
                                    value = uiState.email,
                                    onValueChange = onEmailChange,
                                    label = usernamePlaceholder,
                                    leadingIcon = Icons.Outlined.Person,
                                    onClear = { onEmailChange("") },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Next
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    ),
                                    focusRequester = emailFocusRequester
                                )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input 3: Password Input
                        LoginInputField(
                            value = uiState.password,
                            onValueChange = onPasswordChange,
                            label = passwordPlaceholder,
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            showPassword = uiState.showPassword,
                            onPasswordToggle = onTogglePassword,
                            onClear = { onPasswordChange("") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (!uiState.isLoading && isTermsAccepted) {
                                        onLoginClick()
                                    }
                                }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Remember Me Checkbox (matching .premium-checkbox in MobileLoginPage.vue)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { onRememberMeChange(!uiState.rememberMe) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (uiState.rememberMe) BrandAccent
                                        else if (isDark) Color.White.copy(alpha = 0.06f)
                                        else Color.Black.copy(alpha = 0.03f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (uiState.rememberMe) BrandAccent
                                        else if (isDark) Color.White.copy(alpha = 0.12f)
                                        else Color.Black.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (uiState.rememberMe) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rememberMeText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = subtitleColor
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Primary Gradient Submit Button (matching BaseButton.vue .premium-btn)
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (!uiState.isLoading && isTermsAccepted) {
                                    onLoginClick()
                                }
                            },
                            enabled = !uiState.isLoading && isTermsAccepted,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .alpha(if (isTermsAccepted) 1f else 0.5f)
                                .shadow(
                                    elevation = if (isTermsAccepted) 12.dp else 0.dp,
                                    shape = RoundedCornerShape(16.dp),
                                    ambientColor = Color(0xFF2563EB).copy(alpha = 0.4f),
                                    spotColor = Color(0xFF2563EB).copy(alpha = 0.5f)
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(buttonBrush, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                contentColor = Color.White,
                                disabledContentColor = Color.White.copy(alpha = 0.85f)
                            ),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = loginBtnText,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Terms of Use & Privacy Policy Checkbox (Compact below Login Button)
                        val uriHandler = LocalUriHandler.current
                        val termsAnnotatedString = buildAnnotatedString {
                            if (isVietnamese) {
                                append("Tôi đồng ý với ")
                                pushStringAnnotation(tag = "terms", annotation = "https://tenli.ai/terms-of-use")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = BrandAccent)) {
                                    append("Điều khoản sử dụng")
                                }
                                pop()
                                append(" & ")
                                pushStringAnnotation(tag = "privacy", annotation = "https://tenli.ai/chinh-sach-bao-mat")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = BrandAccent)) {
                                    append("Chính sách Bảo mật")
                                }
                                pop()
                                append(" của Tenli")
                            } else {
                                append("I agree to Tenli's ")
                                pushStringAnnotation(tag = "terms", annotation = "https://tenli.ai/terms-of-use")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = BrandAccent)) {
                                    append("Terms of Use")
                                }
                                pop()
                                append(" & ")
                                pushStringAnnotation(tag = "privacy", annotation = "https://tenli.ai/chinh-sach-bao-mat")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = BrandAccent)) {
                                    append("Privacy Policy")
                                }
                                pop()
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 1.dp)
                                    .size(17.dp)
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(
                                        if (isTermsAccepted) BrandAccent
                                        else if (isDark) Color.White.copy(alpha = 0.06f)
                                        else Color.Black.copy(alpha = 0.03f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isTermsAccepted) BrandAccent
                                        else if (isDark) Color.White.copy(alpha = 0.14f)
                                        else Color.Black.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(5.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { isTermsAccepted = !isTermsAccepted },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isTermsAccepted) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            ClickableText(
                                text = termsAnnotatedString,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = subtitleColor
                                ),
                                modifier = Modifier.weight(1f),
                                onClick = { offset ->
                                    val termsAnnotation = termsAnnotatedString
                                        .getStringAnnotations(tag = "terms", start = offset, end = offset)
                                        .firstOrNull()
                                    val privacyAnnotation = termsAnnotatedString
                                        .getStringAnnotations(tag = "privacy", start = offset, end = offset)
                                        .firstOrNull()

                                    when {
                                        termsAnnotation != null -> uriHandler.openUri(termsAnnotation.item)
                                        privacyAnnotation != null -> uriHandler.openUri(privacyAnnotation.item)
                                        else -> isTermsAccepted = !isTermsAccepted
                                    }
                                }
                            )
                        }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 3. Bottom-of-Screen Footer OUTSIDE the card (matching .mobile-login-footer "🇻🇳 MAKE IN VIETNAM")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🇻🇳",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MAKE IN VIETNAM",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = titleColor.copy(alpha = 0.65f)
                )
            }
            }

            // Global Top Popup Notification Host (chuẩn hóa hiển thị lỗi ở popup top)
            com.tenli.oneview.ui.component.MobileTopPopupHost(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
            )
        }
    }
}
