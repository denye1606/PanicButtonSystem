package com.example.panicbutton

expect class KmmSettings() {
    fun putInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int): Int
    fun putLong(key: String, value: Long)
    fun getLong(key: String, defaultValue: Long): Long
    fun putString(key: String, value: String)
    fun getString(key: String): String?
}
