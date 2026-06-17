package com.tenli.oneview.data.local

import android.content.SharedPreferences
import com.google.gson.Gson

object GlobalData {
    lateinit var preferences: SharedPreferences
    lateinit var gson: Gson
}