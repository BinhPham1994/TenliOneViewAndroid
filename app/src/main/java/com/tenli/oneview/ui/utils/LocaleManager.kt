package com.tenli.oneview.ui.utils

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = "tenli_language_settings"
    private const val KEY_LANG = "selected_language"

    @Volatile
    private var cachedLocale: String? = null

    private val _languageFlow = MutableStateFlow("vi")
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    fun getLocale(context: Context): String {
        cachedLocale?.let {
            if (_languageFlow.value != it) _languageFlow.value = it
            return it
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lang = prefs.getString(KEY_LANG, "vi") ?: "vi"
        cachedLocale = lang
        _languageFlow.value = lang
        return lang
    }

    fun setLocale(context: Context, languageCode: String) {
        cachedLocale = languageCode
        _languageFlow.value = languageCode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit(commit = true) { putString(KEY_LANG, languageCode) }

        // Vẫn gọi dòng này để hỗ trợ Android 13+ (Hệ thống tự đổi)
        val appLocale = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun updateResources(context: Context): Context {
        val lang = getLocale(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)

        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }
}