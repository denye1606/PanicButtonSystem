package com.example.panicbutton

import platform.Foundation.NSUserDefaults

actual class KmmSettings {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun putInt(key: String, value: Int) {
        defaults.setInteger(value.toLong(), key)
    }

    actual fun getInt(key: String, defaultValue: Int): Int {
        return if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else defaultValue
    }

    actual fun putLong(key: String, value: Long) {
        defaults.setObject(value, key)
    }

    actual fun getLong(key: String, defaultValue: Long): Long {
        return (defaults.objectForKey(key) as? Long) ?: defaultValue
    }

    actual fun putString(key: String, value: String) {
        defaults.setObject(value, key)
    }

    actual fun getString(key: String): String? {
        return defaults.stringForKey(key)
    }
}
