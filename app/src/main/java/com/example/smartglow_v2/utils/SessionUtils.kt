package com.example.smartglow_v2.utils

import android.content.Context
import android.content.SharedPreferences

private const val PREF_NAME = "user_session"
private const val KEY_LOGGED_IN = "is_logged_in"

fun isLoggedIn(context: Context): Boolean {
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_LOGGED_IN, false)
}

fun setLoggedIn(context: Context, value: Boolean) {
    val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
}