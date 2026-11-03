package com.tenli.oneview.ui.features.auth.login

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.main.MainActivity
import com.tenli.oneview.ui.theme.BrandAccent
import com.tenli.oneview.ui.theme.ErrorRed
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
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            (context as? Activity)?.finish()
        }
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
    onTogglePassword: () -> Unit,
    onClearError: () -> Unit,
    onLoginClick: () -> Unit,
    onSetLanguage: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val currentLang = remember { LocaleManager.getLocale(context) }
    val isVietnamese = currentLang == "vi"

    val themeMode by ThemeManager.themeModeFlow.collectAsState()
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        0 -> false
        1 -> true
        else -> isSystemDark
    }

    var rememberMe by remember { mutableStateOf(true) }
    var showLangMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    val pageBg = if (isDark) Color(0xFF0F172A) else Color(0xFFE8E8ED)
    val cardBg = if (isDark) Color(0xFF151E32) else Color(0xFFF6F7FA)
    val cardBorder = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
    val titleColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    val domainPlaceholder = if (isVietnamese) "Nhập domain dự án (VD: https://app.tenli.ai)" else "Enter project domain (e.g. https://app.tenli.ai)"
    val usernamePlaceholder = if (isVietnamese) "Nhập tên đăng nhập" else "Enter username"
    val passwordPlaceholder = if (isVietnamese) "Nhập mật khẩu" else "Enter password"
    val loginTitle = if (isVietnamese) "Đăng nhập" else "Sign In"
    val loginSubtitle = if (isVietnamese) "Hệ thống giám sát, vận hành thông minh" else "Smart monitoring & operation system"
    val rememberMeText = if (isVietnamese) "Ghi nhớ đăng nhập" else "Remember me"
    val loginBtnText = if (uiState.isLoading) {
        if (isVietnamese) "Đang xác thực..." else "Authenticating..."
    } else {
        if (isVietnamese) "Đăng nhập" else "Sign In"
    }
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    Scaffold(
        containerColor = pageBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
                .padding(padding)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // 1. Top-Right Language & Theme Switchers OUTSIDE the card (matching MobileLoginPage.vue .mobile-login-top-actions)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 12.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Language Switcher (`🇻🇳 VN` / `🇺🇸 EN`)
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { showLangMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isVietnamese) "🇻🇳" else "🇺🇸",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isVietnamese) "VN" else "EN",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = titleColor
                            )
                        )
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false },
                        modifier = Modifier.background(if (isDark) Color(0xFF172033) else Color.White)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "🇻🇳  Tiếng Việt",
                                    fontSize = 13.sp,
                                    fontWeight = if (isVietnamese) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isVietnamese) BrandAccent else titleColor
                                )
                            },
                            onClick = {
                                showLangMenu = false
                                onSetLanguage("vi")
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "🇺🇸  English",
                                    fontSize = 13.sp,
                                    fontWeight = if (!isVietnamese) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (!isVietnamese) BrandAccent else titleColor
                                )
                            },
                            onClick = {
                                showLangMenu = false
                                onSetLanguage("en")
                            }
                        )
                    }
                }

                // Theme Switcher (Sun / Moon / Monitor matching ThemeSwitcher.vue)
                Box {
                    val themeIcon = when (themeMode) {
                        0 -> Icons.Outlined.LightMode
                        1 -> Icons.Outlined.DarkMode
                        else -> Icons.Outlined.DesktopWindows
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showThemeMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = themeIcon,
                            contentDescription = "Theme",
                            tint = titleColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showThemeMenu,
                        onDismissRequest = { showThemeMenu = false },
                        modifier = Modifier.background(if (isDark) Color(0xFF172033) else Color.White)
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (themeMode == 0) BrandAccent else titleColor
                                )
                            },
                            text = {
                                Text(
                                    if (isVietnamese) "Sáng" else "Light",
                                    fontSize = 13.sp,
                                    fontWeight = if (themeMode == 0) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (themeMode == 0) BrandAccent else titleColor
                                )
                            },
                            onClick = {
                                showThemeMenu = false
                                ThemeManager.setThemeMode(0)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (themeMode == 1) BrandAccent else titleColor
                                )
                            },
                            text = {
                                Text(
                                    if (isVietnamese) "Tối" else "Dark",
                                    fontSize = 13.sp,
                                    fontWeight = if (themeMode == 1) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (themeMode == 1) BrandAccent else titleColor
                                )
                            },
                            onClick = {
                                showThemeMenu = false
                                ThemeManager.setThemeMode(1)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.DesktopWindows,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (themeMode == 2) BrandAccent else titleColor
                                )
                            },
                            text = {
                                Text(
                                    if (isVietnamese) "Hệ thống" else "System",
                                    fontSize = 13.sp,
                                    fontWeight = if (themeMode == 2) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (themeMode == 2) BrandAccent else titleColor
                                )
                            },
                            onClick = {
                                showThemeMenu = false
                                ThemeManager.setThemeMode(2)
                            }
                        )
                    }
                }
            }

            // 2. Centered Glass Login Card (matching MobileLoginPage.vue .glass-card)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 400.dp)
                        .shadow(
                            elevation = if (isDark) 28.dp else 24.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.18f),
                            spotColor = Color.Black.copy(alpha = if (isDark) 0.55f else 0.22f)
                        )
                        .border(1.dp, cardBorder, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    color = cardBg
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 4-Bar Colorful Logo (`logo-auth.svg`)
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_auth),
                            contentDescription = "Tenli",
                            modifier = Modifier.height(44.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Title ("Đăng nhập")
                        Text(
                            text = loginTitle,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp,
                                letterSpacing = (-0.5).sp
                            ),
                            color = titleColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subtitle ("Hệ thống giám sát, vận hành thông minh")
                        Text(
                            text = loginSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = subtitleColor,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Input 1: Domain Input (Preserved for Android, no label above, matching PWA input style)
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
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input 2: Username Input
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
                            )
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
                                    if (!uiState.isLoading) {
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
                                .clickable { rememberMe = !rememberMe }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (rememberMe) BrandAccent
                                        else if (isDark) Color.White.copy(alpha = 0.06f)
                                        else Color.Black.copy(alpha = 0.03f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (rememberMe) BrandAccent
                                        else if (isDark) Color.White.copy(alpha = 0.12f)
                                        else Color.Black.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
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
                                    fontWeight = FontWeight.Normal
                                ),
                                color = subtitleColor
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Inline Error Capsule Alert (matching .login-error in MobileLoginPage.vue)
                        if (!uiState.errorMessage.isNullOrEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(10.dp),
                                        spotColor = ErrorRed.copy(alpha = 0.4f)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ErrorRed)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = uiState.errorMessage,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onClearError,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White.copy(alpha = 0.85f),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        // Primary Gradient Submit Button (matching BaseButton.vue .premium-btn)
                        val buttonBrush = Brush.linearGradient(
                            colors = listOf(Color(0xFF2563EB), Color(0xFF0761C4))
                        )
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (!uiState.isLoading) {
                                    onLoginClick()
                                }
                            },
                            enabled = !uiState.isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .shadow(
                                    elevation = 12.dp,
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

                        Spacer(modifier = Modifier.height(24.dp))

                        // Copyright inside card (matching .form-copyright: "© 2026 AI Solutions")
                        Text(
                            text = "© $currentYear AI Solutions",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            color = subtitleColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. Bottom-of-Screen Footer OUTSIDE the card (matching .mobile-login-footer "🇻🇳 MAKE IN VIETNAM")
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
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
    }
}
