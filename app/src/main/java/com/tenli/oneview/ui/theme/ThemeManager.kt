package com.tenli.oneview.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME_MODE = "theme_mode"

    private lateinit var prefs: SharedPreferences

    // 0: Light, 1: Dark, 2: System
    private val _themeModeFlow = MutableStateFlow(2)
    val themeModeFlow: StateFlow<Int> = _themeModeFlow.asStateFlow()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _themeModeFlow.value = prefs.getInt(KEY_THEME_MODE, 2)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
        _themeModeFlow.value = mode
    }
}
