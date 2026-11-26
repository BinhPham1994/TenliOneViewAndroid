package com.tenli.oneview.ui.features.auth.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tenli.oneview.R
import com.tenli.oneview.ui.component.MobileTopPopupHost
import com.tenli.oneview.ui.component.MobileTopPopupManager
import com.tenli.oneview.ui.component.SheetBackButton
import com.tenli.oneview.ui.component.rememberIsEnglish
import com.tenli.oneview.ui.theme.LucideIcons
import com.tenli.oneview.ui.theme.ThemeManager
import com.tenli.oneview.ui.theme.appColors
import com.tenli.oneview.ui.theme.spacing
import com.tenli.oneview.ui.utils.LocaleManager
import com.tenli.oneview.util.NetworkMonitor
import com.tenli.oneview.util.noNetworkMessage
import kotlinx.coroutines.delay

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
private val FooterLetterSpacing = 0.sp
private const val FOOTER_TEXT_ALPHA = 0.65f
private const val CARD_SHADOW_AMBIENT_DARK = 0.45f
private const val CARD_SHADOW_AMBIENT_LIGHT = 0.18f
private const val CARD_SHADOW_SPOT_DARK = 0.55f
private const val CARD_SHADOW_SPOT_LIGHT = 0.22f

private val DomainKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.Uri,
    imeAction = ImeAction.Next
)
private val EmailKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.Text,
    imeAction = ImeAction.Next
)
private val PasswordKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.Password,
    imeAction = ImeAction.Done
)

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
        // Nạp lại thông tin đã lưu sau khi đăng xuất (tránh ghi đè bước đang nhập khi Activity recreate do đổi ngôn ngữ)
        viewModel.reloadSavedCredentialsIfNeeded()
        viewModel.event.collect { event ->
            when (event) {
                LoginEvent.LoginSuccess -> currentOnLoginSuccess()
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onDomainChange = viewModel::onDomainChanged,
        onQrDomainScanned = viewModel::onQrDomainScanned,
        onProceedToCredentialsStep = viewModel::onProceedToCredentialsStep,
        onBackFromCredentialsStep = viewModel::onBackToDomainStep,
        onEmailChange = viewModel::onEmailChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onToggleTerms = viewModel::toggleTermsAccepted,
        onTogglePassword = viewModel::toggleShowPassword,
        onLoginClick = viewModel::login,
        onSetLanguage = onSetLanguage
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onDomainChange: (String) -> Unit,
    onQrDomainScanned: (String) -> Unit,
    onProceedToCredentialsStep: () -> Unit,
    onBackFromCredentialsStep: () -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onToggleTerms: () -> Unit,
    onTogglePassword: () -> Unit,
    onLoginClick: () -> Unit,
    onSetLanguage: (String) -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val domainFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val currentStep = uiState.step
    var showQrScanner by remember { mutableStateOf(false) }
    var cardTopInRootPx by remember { mutableFloatStateOf(Float.MAX_VALUE) }
    val isVietnamese = !rememberIsEnglish()

    LaunchedEffect(Unit) {
        if (!NetworkMonitor.isNetworkAvailable()) {
            MobileTopPopupManager.error(noNetworkMessage(!isVietnamese))
        }
    }

    val onBackToDomainStep: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
        onBackFromCredentialsStep()
    }

    BackHandler(enabled = currentStep == LoginStep.CREDENTIALS) {
        onBackToDomainStep()
    }

    LaunchedEffect(currentStep, showQrScanner) {
        if (showQrScanner) return@LaunchedEffect
        delay(FOCUS_DELAY_MS)
        if (showQrScanner) return@LaunchedEffect
        try {
            if (currentStep == LoginStep.DOMAIN) {
                domainFocusRequester.requestFocus()
            } else {
                emailFocusRequester.requestFocus()
            }
            delay(KEYBOARD_DELAY_MS)
            if (!showQrScanner) {
                keyboardController?.show()
            }
        } catch (_: IllegalStateException) {
            // FocusRequester chưa gắn vào layout (đang chuyển bước) – bỏ qua
        }
    }

    val isDomainValid = remember(uiState.domain) {
        LoginViewModel.isValidProjectDomain(uiState.domain)
    }

    val onNextStep: () -> Unit = {
        if (uiState.domain.isBlank()) {
            MobileTopPopupManager.error(
                if (isVietnamese) "Vui lòng nhập domain dự án" else "Please enter project domain"
            )
        } else if (!LoginViewModel.isValidProjectDomain(uiState.domain)) {
            MobileTopPopupManager.error(
                if (isVietnamese) "Domain dự án không hợp lệ" else "Invalid project domain"
            )
        } else if (!NetworkMonitor.isNetworkAvailable()) {
            MobileTopPopupManager.error(noNetworkMessage(!isVietnamese))
        } else {
            onProceedToCredentialsStep()
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }
    val isCredentialsReady = uiState.email.isNotBlank() &&
        uiState.password.isNotEmpty() &&
        uiState.termsAccepted
    val canSubmit = !uiState.isLoading && isCredentialsReady
    val onSubmit: () -> Unit = {
        focusManager.clearFocus()
        if (canSubmit) onLoginClick()
    }
    val onKeyboardDone: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
        if (!uiState.isLoading) onLoginClick()
    }
    val currentOnNextStep by rememberUpdatedState(onNextStep)
    val domainKeyboardActions = remember {
        KeyboardActions(
            onNext = { currentOnNextStep() },
            onDone = { currentOnNextStep() }
        )
    }
    val onClearDomain = remember(onDomainChange) { { onDomainChange("") } }

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
                val hasManualDomainHeader = currentStep == LoginStep.CREDENTIALS &&
                    !uiState.isDomainFromQr &&
                    uiState.domain.isNotBlank()
                val cardTopPadding by animateDpAsState(
                    targetValue = if (hasManualDomainHeader) spacing.space48 else spacing.space32,
                    animationSpec = tween(SLIDE_DURATION_MS),
                    label = "LoginCardTopPadding"
                )
                LoginCard(
                    modifier = Modifier
                        .widthIn(max = spacing.dialogMaxWidth)
                        .onGloballyPositioned { coords ->
                            val topY = coords.positionInRoot().y
                            if (cardTopInRootPx != topY) {
                                cardTopInRootPx = topY
                            }
                        },
                    contentTopPadding = cardTopPadding,
                    topStartAction = {
                        AnimatedVisibility(
                            visible = currentStep == LoginStep.CREDENTIALS,
                            enter = fadeIn(animationSpec = tween(FADE_IN_DURATION_MS)),
                            exit = fadeOut(animationSpec = tween(FADE_OUT_DURATION_MS))
                        ) {
                            val showManualDomain = !uiState.isDomainFromQr && uiState.domain.isNotBlank()
                            Row(
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onBackToDomainStep
                                ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SheetBackButton(onClick = onBackToDomainStep)
                                if (showManualDomain) {
                                    Spacer(modifier = Modifier.width(spacing.space8))
                                    Text(
                                        text = uiState.domain,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textSubtle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }
                            }
                        }
                    }
                ) {
                    LoginHeader(isVietnamese = isVietnamese)
                    Spacer(modifier = Modifier.height(spacing.space32))

                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = { loginStepTransition(forward = targetState.ordinal > initialState.ordinal) },
                        label = "LoginStepTransition"
                    ) { step ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (step == LoginStep.DOMAIN) {
                                // BƯỚC 1: Nhập Domain dự án hoặc Quét mã QR + Nút Tiếp tục
                                LoginInputField(
                                    value = uiState.domain,
                                    onValueChange = onDomainChange,
                                    label = if (isVietnamese) "Nhập domain dự án" else "Enter project domain",
                                    leadingIcon = LucideIcons.Globe,
                                    onClear = onClearDomain,
                                    keyboardOptions = DomainKeyboardOptions,
                                    keyboardActions = domainKeyboardActions,
                                    focusRequester = domainFocusRequester
                                )
                                Spacer(modifier = Modifier.height(spacing.space20))
                                LoginGradientButton(
                                    text = if (isVietnamese) "Tiếp tục" else "Continue",
                                    onClick = onNextStep,
                                    active = isDomainValid
                                )
                                Spacer(modifier = Modifier.height(spacing.space12))
                                LoginSecondaryButton(
                                    text = if (isVietnamese) "Quét mã QR" else "Scan QR Code",
                                    icon = LucideIcons.QrCode,
                                    onClick = {
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        showQrScanner = true
                                    }
                                )
                            } else {
                                // BƯỚC 2: Nhập Tài khoản & Mật khẩu (hiển thị domain cạnh nút quay lại nếu nhập tay, ẩn nếu quét QR)
                                CredentialsStep(
                                    uiState = uiState,
                                    isVietnamese = isVietnamese,
                                    isTermsAccepted = uiState.termsAccepted,
                                    isFormReady = isCredentialsReady,
                                    canSubmit = canSubmit,
                                    emailFocusRequester = emailFocusRequester,
                                    onEmailChange = onEmailChange,
                                    onPasswordChange = onPasswordChange,
                                    onTogglePassword = onTogglePassword,
                                    onToggleTerms = onToggleTerms,
                                    onMoveFocusDown = { focusManager.moveFocus(FocusDirection.Down) },
                                    onKeyboardDone = onKeyboardDone,
                                    onSubmit = onSubmit
                                )
                            }
                        }
                    }
                }
            }

            // 3. Cụm đổi Ngôn ngữ & Giao diện: cố định ở góc trên phải, khi cuộn form lên gần chạm thì đẩy lên cùng thành 1 khối và cắt ẩn ngay tại mép thanh trạng thái
            val settingsBaseTopPx = with(density) { (padding.calculateTopPadding() + spacing.space12).toPx() }
            val settingsBarHeightPx = with(density) { spacing.space32.toPx() }
            val settingsMinGapPx = with(density) { spacing.space12.toPx() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = padding.calculateTopPadding())
                    .clipToBounds()
                    .zIndex(10f)
            ) {
                LoginSettingsBar(
                    isVietnamese = isVietnamese,
                    onSetLanguage = onSetLanguage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(
                            top = spacing.space12,
                            end = spacing.space24
                        )
                        .graphicsLayer {
                            val maxAllowedTopPx = cardTopInRootPx - settingsMinGapPx - settingsBarHeightPx
                            translationY = (maxAllowedTopPx - settingsBaseTopPx).coerceAtMost(0f)
                        }
                )
            }

            // Global Top Popup Notification Host (chuẩn hóa hiển thị lỗi ở popup top)
            MobileTopPopupHost(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(100f)
            )

            if (showQrScanner) {
                QrScannerModal(
                    isVietnamese = isVietnamese,
                    onDismiss = { showQrScanner = false },
                    onValidDomainScanned = { scannedDomain ->
                        showQrScanner = false
                        onQrDomainScanned(scannedDomain)
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                )
            }
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
    contentTopPadding: Dp = MaterialTheme.spacing.space32,
    topStartAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.appColors
    val spacing = MaterialTheme.spacing
    val isDark = colors.isDark
    val shape = remember(spacing.radiusExtraLarge) { RoundedCornerShape(spacing.radiusExtraLarge) }
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
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = spacing.space24,
                        end = spacing.space24,
                        top = contentTopPadding,
                        bottom = spacing.space32
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                content()
            }

            if (topStartAction != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            top = spacing.space16,
                            start = spacing.space16,
                            end = spacing.space16
                        )
                ) {
                    topStartAction()
                }
            }
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
    isFormReady: Boolean,
    canSubmit: Boolean,
    emailFocusRequester: FocusRequester,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    onToggleTerms: () -> Unit,
    onMoveFocusDown: () -> Unit,
    onKeyboardDone: () -> Unit,
    onSubmit: () -> Unit
) {
    val spacing = MaterialTheme.spacing
    val currentOnMoveFocusDown by rememberUpdatedState(onMoveFocusDown)
    val currentOnKeyboardDone by rememberUpdatedState(onKeyboardDone)
    val emailKeyboardActions = remember {
        KeyboardActions(onNext = { currentOnMoveFocusDown() })
    }
    val passwordKeyboardActions = remember {
        KeyboardActions(onDone = { currentOnKeyboardDone() })
    }
    val onClearEmail = remember(onEmailChange) { { onEmailChange("") } }
    val onClearPassword = remember(onPasswordChange) { { onPasswordChange("") } }

    LoginInputField(
        value = uiState.email,
        onValueChange = onEmailChange,
        label = if (isVietnamese) "Nhập tên đăng nhập" else "Enter username",
        leadingIcon = LucideIcons.User,
        onClear = onClearEmail,
        keyboardOptions = EmailKeyboardOptions,
        keyboardActions = emailKeyboardActions,
        focusRequester = emailFocusRequester
    )

    Spacer(modifier = Modifier.height(spacing.space16))

    LoginInputField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        label = if (isVietnamese) "Nhập mật khẩu" else "Enter password",
        leadingIcon = LucideIcons.Lock,
        isPassword = true,
        showPassword = uiState.showPassword,
        onPasswordToggle = onTogglePassword,
        onClear = onClearPassword,
        keyboardOptions = PasswordKeyboardOptions,
        keyboardActions = passwordKeyboardActions
    )

    Spacer(modifier = Modifier.height(spacing.space20))

    LoginGradientButton(
        text = when {
            uiState.isLoading -> if (isVietnamese) "Đang xác thực..." else "Authenticating..."
            isVietnamese -> "Đăng nhập"
            else -> "Sign In"
        },
        onClick = onSubmit,
        active = isFormReady,
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
    val checkboxNoRipple = remember { MutableInteractionSource() }
    val textNoRipple = remember { MutableInteractionSource() }

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
                .clickable(interactionSource = checkboxNoRipple, indication = null, onClick = onToggle)
        )
        Spacer(modifier = Modifier.width(spacing.space8))
        Text(
            text = termsText,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSubtle,
            modifier = Modifier
                .weight(1f)
                .clickable(interactionSource = textNoRipple, indication = null, onClick = onToggle)
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
