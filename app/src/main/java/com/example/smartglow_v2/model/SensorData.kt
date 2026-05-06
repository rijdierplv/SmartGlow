package com.example.smartglow_v2.model

data class SensorData(
    val status: String = "",
    val motion: Boolean = false,
    val lightOn: Boolean = false,
    val brightness: Int = 0,
    val mode: String = "AUTO",
    val ldrValue: Int = 0,
    val lightLevel: String = "UNKNOWN",
    val lastUpdate: String = ""
) {
    val ldrPercent: Int
        get() = ((ldrValue / 4095.0) * 100).toInt().coerceIn(0, 100)

    val isManualMode: Boolean
        get() = mode == "MANUAL"

    val isMotionDetected: Boolean
        get() = status == "DETECTED"

    val isDark: Boolean
        get() = lightLevel == "DARK"
}