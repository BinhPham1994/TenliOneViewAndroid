package com.tenli.oneview.ui.features.auth.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.ui.component.MobileTopPopupHost
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.LocaleManager
import kotlinx.coroutines.delay

private const val STEP_DOMAIN = 1
private const val STEP_CREDENTIALS = 2

/** Chờ hiển thị rõ giao diện (và hoàn tất hiệu ứng chuyển bước) rồi mới focus + bật bàn phím */
private const val FOCUS_DELAY_MS = 700L
private const val KEYBOARD_DELAY_MS = 100L
private const val SLIDE_DURATION_MS = 320
private const val FADE_IN_DURATION_MS = 260
private const val FADE_OUT_DURATION_MS = 200

private const val TERMS_URL = "https://tenli.ai/terms-of-use"
private const val PRIVACY_URL = "https://tenli.ai/chinh-sach-bao-mat"
private const val LANG_VI = "vi"
private const val LANG_EN = "en"

private val LoginLogoSize = 84.dp
private val TitleLetterSpacing = (-0.5).sp
private val FooterLetterSpacing = 0.5.sp
private const val FOOTER_TEXT_ALPHA = 0.65f
private const val CARD_SHADOW_AMBIENT_DARK = 0.45f
private const val CARD_SHADOW_AMBIENT_LIGHT = 0.18f
private const val CARD_SHADOW_SPOT_DARK = 0.55f
private const val CARD_SHADOW_SPOT_LIGHT = 0.22f

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    val onSetLanguage: (String) -> Unit = remember(context) {
        { newLang ->
            if (LocaleManager.getLocale(context) != newLang) {
                LocaleManager.setLocale(context, newLang)
                (context as? Activity)?.recreate()
            }
        }
    }

    LaunchedEffect(viewModel) {
        // Nạp lại thông tin đã lưu mỗi lần màn đăng nhập hiển thị (vd. sau khi đăng xuất – ViewModel sống theo Activity)
        viewModel.loadSavedCredentials()
        viewModel.event.collect { event ->
            when (event) {
                LoginEvent.LoginSuccess -> currentOnLoginSuccess()
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onDomainChange = viewModel::onDomainChanged,
        onEmailChange = viewModel::onEmailChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onRememberMeChange = viewModel::onRememberMeChanged,
        onTogglePassword = viewModel::toggleShowPassword,
        onLoginClick = viewModel::login,
        onSetLanguage = onSetLanguage
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onDomainChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onTogglePassword: () -> Unit,
    onLoginClick: () -> Unit,
    onSetLanguage: (String) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val domainFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    var currentStep by remember { mutableIntStateOf(STEP_DOMAIN) }
    var isTermsAccepted by remember { mutableStateOf(false) }
    val isVietnamese = !rememberIsEnglish()

    BackHandler(enabled = currentStep == STEP_CREDENTIALS) {
        focusManager.clearFocus()
        keyboardController?.hide()
        currentStep = STEP_DOMAIN
    }

    LaunchedEffect(currentStep) {
        delay(FOCUS_DELAY_MS)
        try {
            if (currentStep == STEP_DOMAIN) {
                domainFocusRequester.requestFocus()
            } else {
                emailFocusRequester.requestFocus()
            }
            delay(KEYBOARD_DELAY_MS)
            keyboardController?.show()
        } catch (_: IllegalStateException) {
            // FocusRequester chưa gắn vào layout (đang chuyển bước) – bỏ qua
        }
    }

    val onNextStep: () -> Unit = {
        if (uiState.domain.isBlank()) {
            MobileTopPopupManager.error(
                if (isVietnamese) "Vui lòng nhập domain dự án" else "Please enter project domain"
            )
        } else {
            focusManager.clearFocus()
            keyboardController?.hide()
            currentStep = STEP_CREDENTIALS
        }
    }
    val canSubmit = !uiState.isLoading && isTermsAccepted
    val onSubmit: () -> Unit = {
        focusManager.clearFocus()
        if (canSubmit) onLoginClick()
    }

    Scaffold(containerColor = colors.pageBg) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
        ) {
            // 1. Footer cố định ở dưới cùng màn hình (.mobile-login-footer "🇻🇳 MAKE IN VIETNAM")
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = padding.calculateBottomPadding() + spacing.space16)
            ) {
                LoginFooter()
            }

            // 2. Thẻ đăng nhập căn giữa màn hình (tự đẩy lên mượt mà khi bàn phím bật)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.space24, vertical = spacing.space56),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                LoginCard(modifier = Modifier.widthIn(max = spacing.dialogMaxWidth)) {
                    LoginHeader(isVietnamese = isVietnamese)
                    Spacer(modifier = Modifier.height(spacing.space32))

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = { loginStepTransition(forward = targetState > initialState) },
                        label = "LoginStepTransition"
                    ) { step ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (step == STEP_DOMAIN) {
                                // BƯỚC 1: Chỉ nhập Domain dự án + Nút Tiếp tục
                                LoginInputField(
                                    value = uiState.domain,
                                    onValueChange = onDomainChange,
                                    label = if (isVietnamese) "Nhập domain dự án (VD: https://app.tenli.ai)" else "Enter project domain (e.g. https://app.tenli.ai)",
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
                                Spacer(modifier = Modifier.height(spacing.space20))
                                LoginGradientButton(
                                    text = if (isVietnamese) "Tiếp tục" else "Continue",
                                    onClick = onNextStep,
                                    active = uiState.domain.isNotBlank()
                                )
                            } else {
                                // BƯỚC 2: Nhập Tài khoản & Mật khẩu (không hiển thị lại dòng domain)
                                CredentialsStep(
                                    uiState = uiState,
                                    isVietnamese = isVietnamese,
                                    isTermsAccepted = isTermsAccepted,
                                    canSubmit = canSubmit,
                                    emailFocusRequester = emailFocusRequester,
                                    onEmailChange = onEmailChange,
                                    onPasswordChange = onPasswordChange,
                                    onTogglePassword = onTogglePassword,
                                    onRememberMeChange = onRememberMeChange,
                                    onToggleTerms = { isTermsAccepted = !isTermsAccepted },
                                    onMoveFocusDown = { focusManager.moveFocus(FocusDirection.Down) },
                                    onSubmit = onSubmit
                                )
                            }
                        }
                    }
                }
            }

            // 3. Cụm đổi Ngôn ngữ & Giao diện cố định ở góc trên bên phải màn hình
            LoginSettingsBar(
                isVietnamese = isVietnamese,
                onSetLanguage = onSetLanguage,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        top = padding.calculateTopPadding() + spacing.space12,
                        end = spacing.space24
                    )
                    .zIndex(10f)
            )

            // Global Top Popup Notification Host (chuẩn hóa hiển thị lỗi ở popup top)
            MobileTopPopupHost(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
            )
        }
    }
}

private fun loginStepTransition(forward: Boolean): ContentTransform {
    // Tiến (1 -> 2): trượt từ phải sang trái; Lùi (2 -> 1): trượt từ trái sang phải
    val direction = if (forward) 1 else -1
    return ContentTransform(
        targetContentEnter = slideInHorizontally(animationSpec = tween(SLIDE_DURATION_MS)) { fullWidth -> direction * fullWidth } +
            fadeIn(animationSpec = tween(FADE_IN_DURATION_MS)),
        initialContentExit = slideOutHorizontally(animationSpec = tween(SLIDE_DURATION_MS)) { fullWidth -> -direction * fullWidth } +
            fadeOut(animationSpec = tween(FADE_OUT_DURATION_MS)),
        sizeTransform = SizeTransform(clip = false)
    )
}

@Composable
private fun LoginSettingsBar(
    isVietnamese: Boolean,
    onSetLanguage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val themeMode by ThemeManager.themeModeFlow.collectAsStateWithLifecycle()
    var showLangMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.space8, Alignment.End)
    ) {
        // Language Switcher (`🇻🇳 VN` / `🇺🇸 EN`)
        Box {
            LoginTopPill(onClick = { showLangMenu = !showLangMenu }) {
                Spacer(modifier = Modifier.width(spacing.space10))
                Text(
                    text = if (isVietnamese) "🇻🇳" else "🇺🇸",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(spacing.space5))
                Text(
                    text = if (isVietnamese) "VN" else "EN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textTitle
                )
                Spacer(modifier = Modifier.width(spacing.space10))
            }

            if (showLangMenu) {
                LoginDropdownPopup(onDismissRequest = { showLangMenu = false }) {
                    LanguageOptions.forEach { option ->
                        LoginMenuOption(
                            label = option.label,
                            selected = (option.code == LANG_VI) == isVietnamese,
                            onClick = {
                                showLangMenu = false
                                onSetLanguage(option.code)
                            }
                        ) {
                            Text(text = option.flag, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Theme Switcher (Sun / Moon / Monitor – ThemeSwitcher.vue)
        Box {
            LoginTopPill(
                onClick = { showThemeMenu = !showThemeMenu },
                modifier = Modifier.width(spacing.space32)
            ) {
                Icon(
                    imageVector = themeIcon(themeMode),
                    contentDescription = "Theme",
                    tint = colors.textTitle,
                    modifier = Modifier.size(spacing.iconSmall)
                )
            }

            if (showThemeMenu) {
                LoginDropdownPopup(onDismissRequest = { showThemeMenu = false }) {
                    ThemeOptions.forEach { mode ->
                        LoginMenuOption(
                            label = themeLabel(mode, isVietnamese),
                            selected = themeMode == mode,
                            onClick = {
                                showThemeMenu = false
                                ThemeManager.setThemeMode(mode)
                            }
                        ) { tint ->
                            Icon(
                                imageVector = themeIcon(mode),
                                contentDescription = null,
                                modifier = Modifier.size(spacing.iconXSmall),
                                tint = tint
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class LanguageOption(val code: String, val flag: String, val label: String)

private val LanguageOptions = listOf(
    LanguageOption(LANG_VI, "🇻🇳", "Tiếng Việt"),
    LanguageOption(LANG_EN, "🇺🇸", "English")
)

private val ThemeOptions = listOf(ThemeManager.MODE_LIGHT, ThemeManager.MODE_DARK, ThemeManager.MODE_SYSTEM)

private fun themeIcon(mode: Int) = when (mode) {
    ThemeManager.MODE_LIGHT -> LucideIcons.Sun
    ThemeManager.MODE_DARK -> LucideIcons.Moon
    else -> LucideIcons.Monitor
}

private fun themeLabel(mode: Int, isVietnamese: Boolean) = when (mode) {
    ThemeManager.MODE_LIGHT -> if (isVietnamese) "Sáng" else "Light"
    ThemeManager.MODE_DARK -> if (isVietnamese) "Tối" else "Dark"
    else -> if (isVietnamese) "Hệ thống" else "System"
}

@Composable
private fun LoginCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isDark = colors.isDark
    val shape = RoundedCornerShape(spacing.radiusExtraLarge)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) spacing.space28 else spacing.space24,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (isDark) CARD_SHADOW_AMBIENT_DARK else CARD_SHADOW_AMBIENT_LIGHT),
                spotColor = Color.Black.copy(alpha = if (isDark) CARD_SHADOW_SPOT_DARK else CARD_SHADOW_SPOT_LIGHT)
            ),
        shape = shape,
        color = colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space24, vertical = spacing.space32),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
private fun LoginHeader(isVietnamese: Boolean) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    // App Logo (`logo_pwa.png`)
    Image(
        painter = painterResource(id = R.drawable.logo_pwa),
        contentDescription = "Tenli",
        modifier = Modifier.size(LoginLogoSize)
    )
    Spacer(modifier = Modifier.height(spacing.space10))

    // Title ("Tenli OneView")
    val title = remember(colors.textTitle, colors.accent) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = colors.textTitle)) { append("Tenli ") }
            withStyle(SpanStyle(color = colors.accent)) { append("OneView") }
        }
    }
    Text(
        text = title,
        style = MaterialTheme.typography.headlineLarge.copy(letterSpacing = TitleLetterSpacing),
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(spacing.space8))

    // Subtitle
    Text(
        text = if (isVietnamese) "Hệ thống giám sát, vận hành thông minh" else "Smart monitoring & operation system",
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSubtle,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun CredentialsStep(
    uiState: LoginUiState,
    isVietnamese: Boolean,
    isTermsAccepted: Boolean,
    canSubmit: Boolean,
    emailFocusRequester: FocusRequester,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onToggleTerms: () -> Unit,
    onMoveFocusDown: () -> Unit,
    onSubmit: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing

    LoginInputField(
        value = uiState.email,
        onValueChange = onEmailChange,
        label = if (isVietnamese) "Nhập tên đăng nhập" else "Enter username",
        leadingIcon = Icons.Outlined.Person,
        onClear = { onEmailChange("") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(onNext = { onMoveFocusDown() }),
        focusRequester = emailFocusRequester
    )

    Spacer(modifier = Modifier.height(spacing.space16))

    LoginInputField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        label = if (isVietnamese) "Nhập mật khẩu" else "Enter password",
        leadingIcon = Icons.Outlined.Lock,
        isPassword = true,
        showPassword = uiState.showPassword,
        onPasswordToggle = onTogglePassword,
        onClear = { onPasswordChange("") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() })
    )

    Spacer(modifier = Modifier.height(spacing.space16))

    // Remember Me (.premium-checkbox)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onRememberMeChange(!uiState.rememberMe) }
            .padding(vertical = spacing.space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LoginCheckbox(
            checked = uiState.rememberMe,
            size = spacing.iconRegular,
            cornerRadius = spacing.radiusTiny,
            checkIconSize = spacing.iconXSmall
        )
        Spacer(modifier = Modifier.width(spacing.space8))
        Text(
            text = if (isVietnamese) "Ghi nhớ đăng nhập" else "Remember me",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSubtle
        )
    }

    Spacer(modifier = Modifier.height(spacing.space20))

    LoginGradientButton(
        text = when {
            uiState.isLoading -> if (isVietnamese) "Đang xác thực..." else "Authenticating..."
            isVietnamese -> "Đăng nhập"
            else -> "Sign In"
        },
        onClick = onSubmit,
        active = isTermsAccepted,
        enabled = canSubmit,
        isLoading = uiState.isLoading
    )

    Spacer(modifier = Modifier.height(spacing.space16))

    TermsAgreementRow(
        isVietnamese = isVietnamese,
        accepted = isTermsAccepted,
        onToggle = onToggleTerms
    )
}

/** Checkbox đồng ý Điều khoản & Chính sách (link mở trình duyệt, chạm phần chữ còn lại để tick). */
@Composable
private fun TermsAgreementRow(
    isVietnamese: Boolean,
    accepted: Boolean,
    onToggle: () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val linkStyles = remember(colors.accent) {
        TextLinkStyles(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = colors.accent))
    }
    val termsText = remember(isVietnamese, linkStyles) {
        buildAnnotatedString {
            append(if (isVietnamese) "Tôi đồng ý với " else "I agree to Tenli's ")
            withLink(LinkAnnotation.Url(TERMS_URL, linkStyles)) {
                append(if (isVietnamese) "Điều khoản sử dụng" else "Terms of Use")
            }
            append(" & ")
            withLink(LinkAnnotation.Url(PRIVACY_URL, linkStyles)) {
                append(if (isVietnamese) "Chính sách Bảo mật" else "Privacy Policy")
            }
            if (isVietnamese) append(" của Tenli")
        }
    }
    val noRipple = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.space2),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        LoginCheckbox(
            checked = accepted,
            size = spacing.iconSmall,
            cornerRadius = spacing.space5,
            checkIconSize = spacing.iconTiny,
            modifier = Modifier
                .padding(top = spacing.space1)
                .clickable(interactionSource = noRipple, indication = null, onClick = onToggle)
        )
        Spacer(modifier = Modifier.width(spacing.space8))
        Text(
            text = termsText,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSubtle,
            modifier = Modifier
                .weight(1f)
                .clickable(interactionSource = noRipple, indication = null, onClick = onToggle)
        )
    }
}

@Composable
private fun LoginFooter() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "🇻🇳", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.space8))
        Text(
            text = "MAKE IN VIETNAM",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = FooterLetterSpacing),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.appColors.textTitle.copy(alpha = FOOTER_TEXT_ALPHA)
        )
    }
}
