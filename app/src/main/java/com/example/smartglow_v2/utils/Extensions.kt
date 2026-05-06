package com.example.smartglow_v2.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.Toast
import com.example.smartglow_v2.view.auth.ChoicesActivity
import com.example.smartglow_v2.view.auth.LoginActivity
import com.example.smartglow_v2.view.main.DashboardActivity

fun String.encodeEmail(): String {
    return this.replace(".", ",")
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.getSharedPrefs() =
    getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

fun Context.getCurrentEmail(): String? =
    getSharedPrefs().getString(Constants.KEY_CURRENT_USER, null)

fun Context.getUsername(): String =
    getSharedPrefs().getString(Constants.KEY_USERNAME, "User") ?: "User"

fun Context.clearSession() {
    getSharedPrefs().edit().clear().apply()
}

fun Context.saveSession(email: String, username: String) {
    getSharedPrefs().edit()
        .putString(Constants.KEY_CURRENT_USER, email)
        .putString(Constants.KEY_USERNAME, username)
        .apply()
}

fun Context.goToLogin() {
    startActivity(Intent(this, LoginActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
}

fun Context.goToDashboard() {
    startActivity(Intent(this, DashboardActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    })
}

fun Int.toColor() = Color.parseColor(this.toString())
