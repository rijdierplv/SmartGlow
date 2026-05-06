package com.example.smartglow_v2.utils

object Constants {
    const val FIREBASE_DB_URL = "https://eldroidproject-94df3-default-rtdb.firebaseio.com"
    const val PREFS_NAME = "SmartGlowPrefs"
    const val KEY_CURRENT_USER = "current_user"
    const val KEY_USERNAME = "username"

    // Firebase Paths
    const val PATH_USERS = "users"
    const val PATH_STREET_1 = "sensors_street1"
    const val PATH_STREET_2 = "sensors_street2"
    const val PATH_LOGS_1 = "Logs_street1"
    const val PATH_LOGS_2 = "Logs_street2"

    // Colors
    const val COLOR_PRIMARY_DARK = "#042739"
    const val COLOR_ACCENT = "#F77F00"
    const val COLOR_ACCENT_LIGHT = "#FE9D16"
    const val COLOR_TEXT_PRIMARY = "#FFFFFF"
    const val COLOR_TEXT_SECONDARY = "#A6A3A3"
    const val COLOR_SUCCESS = "#90EE90"
    const val COLOR_DANGER = "#FF4444"
    const val COLOR_CARD_BG = "#0A3D56"
    const val COLOR_DIVIDER = "#1A3A50"

    // Sensor thresholds (from ESP32)
    const val LDR_THRESHOLD = 2000
    const val DIM_BRIGHTNESS = 40
    const val FULL_BRIGHTNESS = 100
}