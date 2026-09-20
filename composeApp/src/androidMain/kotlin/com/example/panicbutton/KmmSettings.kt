package com.example.panicbutton

import android.content.Context
import android.content.SharedPreferences

actual class KmmSettings actual constructor() {
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("PanicButtonPrefs", Context.MODE_PRIVATE)
    }

    actual fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }

    actual fun putLong(key: String, value: Long) {
        prefs.edit().putLong(key, value).apply()
    }

    actual fun getLong(key: String, defaultValue: Long): Long {
        return prefs.getLong(key, defaultValue)
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    companion object {
        lateinit var context: Context
    }
}
