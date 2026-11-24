package com.tenli.oneview.data.local

import android.content.SharedPreferences
import com.google.gson.Gson

object GlobalData {
    @Volatile
    lateinit var preferences: SharedPreferences
    @Volatile
    lateinit var gson: Gson
}