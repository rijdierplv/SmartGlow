package com.example.smartglow_v2.model

data class LogEntry(
    val datetime: String = "",
    val status: String = "",
    val motion: Boolean = false,
    val brightness: Int? = null,
    val ldrValue: Int? = null,
    val lightLevel: String? = null,
    val street: String = ""
) {
    val isMotionLog: Boolean
        get() = status == "DETECTED" || status == "CLEAR"

    val formattedTime: String
        get() {
            val parts = datetime.split(" ")
            return if (parts.size >= 3) "${parts[1].take(5)} ${parts[2]}" else datetime
        }

    val formattedDate: String
        get() {
            val parts = datetime.split(" ")
            if (parts.isNotEmpty() && parts[0].length >= 10) {
                val month = parts[0].substring(5, 7).toIntOrNull() ?: 0
                val day = parts[0].substring(8, 10)
                val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                return "${months.getOrElse(month) { "" }} $day"
            }
            return ""
        }

    val subtitleText: String
        get() = when (status) {
            "DETECTED" -> "Motion sensor triggered"
            "CLEAR" -> "No motion detected"
            "LIGHT ON" -> "Light manually turned on"
            "LIGHT OFF" -> "Light manually turned off"
            "FULL BRIGHTNESS" -> "Brightness set to maximum"
            "DIM" -> "Brightness set to dim mode"
            "BRIGHTNESS CHANGED" -> "Brightness manually adjusted"
            "MANUAL MODE" -> "Manual override enabled"
            "AUTO MODE" -> "Automatic mode enabled"
            "DARK" -> "Dark environment detected"
            "DAYTIME" -> "Daytime – lights off"
            else -> "System activity recorded"
        }
}