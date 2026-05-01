package com.example.smartglow_v2

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Dashboard : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ── Navigation ────────────────────────────────────────────────
        findViewById<ImageButton>(R.id.controlBtn).setOnClickListener {
            startActivity(Intent(this, Control::class.java))
        }
        findViewById<ImageButton>(R.id.activityLogBtn).setOnClickListener {
            startActivity(Intent(this, ActivityLog::class.java))
        }

        // ── Start listeners ───────────────────────────────────────────
        listenStreet1()
        listenStreet2()
    }

    // ═══════════════════════════════════════════════════════════════
    //  STREET 1
    // ═══════════════════════════════════════════════════════════════
    private fun listenStreet1() {
        val lightStatus        = findViewById<TextView>(R.id.lightStatus)
        val lightStatusLogo    = findViewById<ImageView>(R.id.lightStatusLogo)
        val lastUpdateText     = findViewById<TextView>(R.id.lastUpdateText)
        val lastUpdateTime     = findViewById<TextView>(R.id.lastUpdateTime)
        val motionStatus       = findViewById<TextView>(R.id.motionStatus)
        val modeType           = findViewById<TextView>(R.id.modeType)
        val brightnessLevel    = findViewById<TextView>(R.id.brightnessLevel)
        val brightnessLevelStatus = findViewById<TextView>(R.id.brightnessLevelStatus)
        val overrideTile       = findViewById<TextView>(R.id.overrideTile)
        val motionTile         = findViewById<TextView>(R.id.motionTile)
        val nightTile          = findViewById<TextView>(R.id.nightTile)
        val ldrReading         = findViewById<TextView>(R.id.ldrReading)
        val lightLevelText     = findViewById<TextView>(R.id.lightLevelText)
        val ldrMode            = findViewById<TextView>(R.id.ldrMode)
        val ldrModePercent     = findViewById<TextView>(R.id.ldrModePercent)

        FirebaseDatabase.getInstance()
            .getReference("sensors_street1")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val status     = snapshot.child("status").getValue(String::class.java)
                    val lastUpdate = snapshot.child("lastUpdate").getValue(String::class.java)
                    val isOn       = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false
                    val brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0
                    val mode       = snapshot.child("mode").getValue(String::class.java) ?: "AUTO"
                    val rawLdr     = snapshot.child("ldrValue").getValue(Int::class.java) ?: 0
                    val lightLevel = snapshot.child("lightLevel").getValue(String::class.java) ?: "UNKNOWN"

                    val ldrPercent = ((rawLdr / 4095.0) * 100).toInt().coerceIn(0, 100)

                    // LDR / ambient
                    ldrReading.text    = ldrPercent.toString()
                    lightLevelText.text = lightLevel
                    ldrModePercent.text = ldrPercent.toString()

                    if (lightLevel == "DARK") {
                        ldrMode.text = "NightTime Mode"
                        lightLevelText.setTextColor(Color.parseColor("#90EE90"))
                        nightTile.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#90EE90"))
                        nightTile.setTextColor(Color.BLACK)
                    } else {
                        ldrMode.text = "DayTime Mode"
                        lightLevelText.setTextColor(Color.parseColor("#A6A3A3"))
                        nightTile.backgroundTintList = null
                        nightTile.setTextColor(Color.WHITE)
                    }

                    // Motion
                    motionStatus.text = status ?: "CLEAR"
                    motionStatus.setTextColor(
                        if (status == "DETECTED") Color.parseColor("#90EE90")
                        else Color.parseColor("#A6A3A3")
                    )

                    // Light on/off
                    if (isOn) {
                        lightStatus.text = "Lights ON"
                        lightStatusLogo.setImageResource(R.drawable.lightingbulb_on)
                        brightnessLevel.text = brightness.toString()
                        brightnessLevelStatus.text = "ON"
                        brightnessLevelStatus.setTextColor(Color.parseColor("#FFA500"))
                    } else {
                        lightStatus.text = "Lights OFF"
                        lightStatusLogo.setImageResource(R.drawable.lightbulb)
                        brightnessLevel.text = "0"
                        brightnessLevelStatus.text = "OFF"
                        brightnessLevelStatus.setTextColor(Color.parseColor("#A6A3A3"))
                    }

                    // Mode
                    modeType.text = mode
                    modeType.setTextColor(
                        if (mode == "MANUAL") Color.parseColor("#FFA500")
                        else Color.parseColor("#A6A3A3")
                    )

                    // Override tile
                    if (mode == "MANUAL") {
                        overrideTile.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#90EE90"))
                        overrideTile.setTextColor(Color.BLACK)
                    } else {
                        overrideTile.backgroundTintList = null
                        overrideTile.setTextColor(Color.WHITE)
                    }

                    // Motion tile
                    if (status == "DETECTED") {
                        motionTile.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#90EE90"))
                        motionTile.setTextColor(Color.BLACK)
                    } else {
                        motionTile.backgroundTintList = null
                        motionTile.setTextColor(Color.WHITE)
                    }

                    // Last update
                    lastUpdateText.text = "Last Update:"
                    if (!lastUpdate.isNullOrEmpty() && lastUpdate.length >= 10) {
                        val datePart = lastUpdate.substring(0, 10)
                        val timePart = if (lastUpdate.length > 11) lastUpdate.substring(11) else ""
                        lastUpdateTime.text = "$datePart  $timePart"
                    } else {
                        lastUpdateTime.text = "No data"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    resetStreet1Ui(
                        motionStatus, lastUpdateText, lastUpdateTime, modeType,
                        brightnessLevel, brightnessLevelStatus,
                        overrideTile, motionTile, nightTile,
                        ldrReading, lightLevelText, ldrMode, ldrModePercent
                    )
                }
            })
    }

    private fun resetStreet1Ui(
        motionStatus: TextView, lastUpdateText: TextView, lastUpdateTime: TextView,
        modeType: TextView, brightnessLevel: TextView, brightnessLevelStatus: TextView,
        overrideTile: TextView, motionTile: TextView, nightTile: TextView,
        ldrReading: TextView, lightLevelText: TextView, ldrMode: TextView, ldrModePercent: TextView
    ) {
        motionStatus.text = "CLEAR"
        lastUpdateText.text = "Last Update:"
        lastUpdateTime.text = "Unavailable"
        modeType.text = "AUTO"
        modeType.setTextColor(Color.parseColor("#A6A3A3"))
        brightnessLevel.text = "0"
        brightnessLevelStatus.text = "OFF"
        brightnessLevelStatus.setTextColor(Color.parseColor("#A6A3A3"))
        overrideTile.backgroundTintList = null
        overrideTile.setTextColor(Color.WHITE)
        motionTile.backgroundTintList = null
        motionTile.setTextColor(Color.WHITE)
        nightTile.backgroundTintList = null
        nightTile.setTextColor(Color.WHITE)
        ldrReading.text = "0"
        lightLevelText.text = "UNKNOWN"
        ldrMode.text = "DayTime Mode"
        ldrModePercent.text = "0"
    }

    // ═══════════════════════════════════════════════════════════════
    //  STREET 2
    // ═══════════════════════════════════════════════════════════════
    private fun listenStreet2() {
        val lightStatus2           = findViewById<TextView>(R.id.lightStatus2)
        val lightStatusLogo2       = findViewById<ImageView>(R.id.lightStatusLogo2)
        val lastUpdateText2        = findViewById<TextView>(R.id.lastUpdateText2)
        val lastUpdateTime2        = findViewById<TextView>(R.id.lastUpdateTime2)
        val motionStatus2          = findViewById<TextView>(R.id.motionStatus2)
        val modeType2              = findViewById<TextView>(R.id.modeType2)
        val brightnessLevel2       = findViewById<TextView>(R.id.brightnessLevel2)
        val brightnessLevelStatus2 = findViewById<TextView>(R.id.brightnessLevelStatus2)
        val overrideTile2          = findViewById<TextView>(R.id.overrideTile2)
        val motionTile2            = findViewById<TextView>(R.id.motionTile2)
        val nightTile2             = findViewById<TextView>(R.id.nightTile2)
        val ldrReading2            = findViewById<TextView>(R.id.ldrReading2)
        val lightLevelText2        = findViewById<TextView>(R.id.lightLevelText2)
        val ldrMode2               = findViewById<TextView>(R.id.ldrMode2)
        val ldrModePercent2        = findViewById<TextView>(R.id.ldrModePercent2)

        FirebaseDatabase.getInstance()
            .getReference("sensors_street2")
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val status     = snapshot.child("status").getValue(String::class.java)
                    val lastUpdate = snapshot.child("lastUpdate").getValue(String::class.java)
                    val isOn       = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false
                    val brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0
                    val mode       = snapshot.child("mode").getValue(String::class.java) ?: "AUTO"
                    val rawLdr     = snapshot.child("ldrValue").getValue(Int::class.java) ?: 0
                    val lightLevel = snapshot.child("lightLevel").getValue(String::class.java) ?: "UNKNOWN"

                    val ldrPercent = ((rawLdr / 4095.0) * 100).toInt().coerceIn(0, 100)

                    // LDR / ambient
                    ldrReading2.text     = ldrPercent.toString()
                    lightLevelText2.text = lightLevel
                    ldrModePercent2.text = ldrPercent.toString()

                    if (lightLevel == "DARK") {
                        ldrMode2.text = "NightTime Mode"
                        lightLevelText2.setTextColor(Color.parseColor("#90EE90"))
                        nightTile2.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#90EE90"))
                        nightTile2.setTextColor(Color.BLACK)
                    } else {
                        ldrMode2.text = "DayTime Mode"
                        lightLevelText2.setTextColor(Color.parseColor("#A6A3A3"))
                        nightTile2.backgroundTintList = null
                        nightTile2.setTextColor(Color.WHITE)
                    }

                    // Motion
                    motionStatus2.text = status ?: "CLEAR"
                    motionStatus2.setTextColor(
                        if (status == "DETECTED") Color.parseColor("#90EE90")
                        else Color.parseColor("#A6A3A3")
                    )

                    // Light on/off
                    if (isOn) {
                        lightStatus2.text = "Lights ON"
                        lightStatusLogo2.setImageResource(R.drawable.lightingbulb_on)
                        brightnessLevel2.text = brightness.toString()
                        brightnessLevelStatus2.text = "ON"
                        brightnessLevelStatus2.setTextColor(Color.parseColor("#FFA500"))
                    } else {
                        lightStatus2.text = "Lights OFF"
                        lightStatusLogo2.setImageResource(R.drawable.lightbulb)
                        brightnessLevel2.text = "0"
                        brightnessLevelStatus2.text = "OFF"
                        brightnessLevelStatus2.setTextColor(Color.parseColor("#A6A3A3"))
                    }

                    // Mode
                    modeType2.text = mode
                    modeType2.setTextColor(
                        if (mode == "MANUAL") Color.parseColor("#FFA500")
                        else Color.parseColor("#A6A3A3")
                    )

                    // Override tile
                    if (mode == "MANUAL") {
                        overrideTile2.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#90EE90"))
                        overrideTile2.setTextColor(Color.BLACK)
                    } else {
                        overrideTile2.backgroundTintList = null
                        overrideTile2.setTextColor(Color.WHITE)
                    }

                    // Motion tile
                    if (status == "DETECTED") {
                        motionTile2.backgroundTintList =
                            ColorStateList.valueOf(Color.parseColor("#90EE90"))
                        motionTile2.setTextColor(Color.BLACK)
                    } else {
                        motionTile2.backgroundTintList = null
                        motionTile2.setTextColor(Color.WHITE)
                    }

                    // Last update
                    lastUpdateText2.text = "Last Update:"
                    if (!lastUpdate.isNullOrEmpty() && lastUpdate.length >= 10) {
                        val datePart = lastUpdate.substring(0, 10)
                        val timePart = if (lastUpdate.length > 11) lastUpdate.substring(11) else ""
                        lastUpdateTime2.text = "$datePart  $timePart"
                    } else {
                        lastUpdateTime2.text = "No data"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    resetStreet2Ui(
                        motionStatus2, lastUpdateText2, lastUpdateTime2, modeType2,
                        brightnessLevel2, brightnessLevelStatus2,
                        overrideTile2, motionTile2, nightTile2,
                        ldrReading2, lightLevelText2, ldrMode2, ldrModePercent2
                    )
                }
            })
    }

    private fun resetStreet2Ui(
        motionStatus2: TextView, lastUpdateText2: TextView, lastUpdateTime2: TextView,
        modeType2: TextView, brightnessLevel2: TextView, brightnessLevelStatus2: TextView,
        overrideTile2: TextView, motionTile2: TextView, nightTile2: TextView,
        ldrReading2: TextView, lightLevelText2: TextView, ldrMode2: TextView, ldrModePercent2: TextView
    ) {
        motionStatus2.text = "CLEAR"
        lastUpdateText2.text = "Last Update:"
        lastUpdateTime2.text = "Unavailable"
        modeType2.text = "AUTO"
        modeType2.setTextColor(Color.parseColor("#A6A3A3"))
        brightnessLevel2.text = "0"
        brightnessLevelStatus2.text = "OFF"
        brightnessLevelStatus2.setTextColor(Color.parseColor("#A6A3A3"))
        overrideTile2.backgroundTintList = null
        overrideTile2.setTextColor(Color.WHITE)
        motionTile2.backgroundTintList = null
        motionTile2.setTextColor(Color.WHITE)
        nightTile2.backgroundTintList = null
        nightTile2.setTextColor(Color.WHITE)
        ldrReading2.text = "0"
        lightLevelText2.text = "UNKNOWN"
        ldrMode2.text = "DayTime Mode"
        ldrModePercent2.text = "0"
    }
}