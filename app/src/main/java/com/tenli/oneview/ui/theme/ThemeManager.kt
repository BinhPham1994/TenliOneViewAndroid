package com.tenli.oneview.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    const val MODE_LIGHT = 0
    const val MODE_DARK = 1
    const val MODE_SYSTEM = 2

    private var prefs: SharedPreferences? = null

    private val _themeModeFlow = MutableStateFlow(MODE_SYSTEM)
    val themeModeFlow: StateFlow<Int> = _themeModeFlow.asStateFlow()

    fun init(context: Context) {
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        _themeModeFlow.value = p.getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun setThemeMode(mode: Int) {
        prefs?.edit { putInt(KEY_THEME_MODE, mode) }
        _themeModeFlow.value = mode
    }
}
