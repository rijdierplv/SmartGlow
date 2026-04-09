package com.example.smartglow_v2

import android.content.Intent
import android.content.res.ColorStateList
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

        val activityLogBtn = findViewById<ImageButton>(R.id.activityLogBtn)
        val controlBtn = findViewById<ImageButton>(R.id.controlBtn)
        val lightStatus = findViewById<TextView>(R.id.lightStatus)
        val lightStatusLogo = findViewById<ImageView>(R.id.lightStatusLogo)
        val lastUpdateText = findViewById<TextView>(R.id.lastUpdateText)
        val lastUpdateTime = findViewById<TextView>(R.id.lastUpdateTime)
        val motionStatus = findViewById<TextView>(R.id.motionStatus)
        val modeType = findViewById<TextView>(R.id.modeType)
        val brightnessLevel = findViewById<TextView>(R.id.brightnessLevel)
        val brightnessLevelStatus = findViewById<TextView>(R.id.brightnessLevelStatus)
        val overrideTile = findViewById<TextView>(R.id.overrideTile)
        val motionTile = findViewById<TextView>(R.id.motionTile)
        val nightTile = findViewById<TextView>(R.id.nightTile)

        val ldrReading = findViewById<TextView>(R.id.ldrReading)
        val lightLevelText = findViewById<TextView>(R.id.lightLevelText)
        val ldrMode = findViewById<TextView>(R.id.ldrMode)
        val ldrModePercent = findViewById<TextView>(R.id.ldrModePercent)

        val pirRef = FirebaseDatabase.getInstance().getReference("pir")

        pirRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                val lastUpdate = snapshot.child("lastUpdate").getValue(String::class.java)
                val isOn = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false
                val brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0
                val mode = snapshot.child("mode").getValue(String::class.java) ?: "AUTO"

                val rawLdrValue = snapshot.child("ldrValue").getValue(Int::class.java) ?: 0
                val lightLevel = snapshot.child("lightLevel").getValue(String::class.java) ?: "UNKNOWN"

                val ldrPercent = ((rawLdrValue / 4095.0) * 100).toInt().coerceIn(0, 100)

                ldrReading.text = ldrPercent.toString()
                lightLevelText.text = lightLevel
                ldrModePercent.text = ldrPercent.toString()

                if (lightLevel == "DARK") {
                    ldrMode.text = "NightTime Mode"
                    lightLevelText.setTextColor(android.graphics.Color.parseColor("#90EE90"))

                    nightTile.backgroundTintList = ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#90EE90")
                    )
                    nightTile.setTextColor(android.graphics.Color.parseColor("#000000"))
                } else {
                    ldrMode.text = "DayTime Mode"
                    lightLevelText.setTextColor(android.graphics.Color.parseColor("#A6A3A3"))

                    nightTile.backgroundTintList = null
                    nightTile.setTextColor(android.graphics.Color.WHITE)
                }

                motionStatus.text = status ?: "CLEAR"
                motionStatus.setTextColor(
                    if (status == "DETECTED") android.graphics.Color.parseColor("#90EE90")
                    else android.graphics.Color.parseColor("#A6A3A3")
                )

                if (isOn) {
                    lightStatus.text = "Lights ON"
                    lightStatusLogo.setImageResource(R.drawable.lightingbulb_on)
                    brightnessLevel.text = brightness.toString()
                    brightnessLevelStatus.text = "ON"
                    brightnessLevelStatus.setTextColor(
                        android.graphics.Color.parseColor("#FFA500")
                    )
                } else {
                    lightStatus.text = "Lights OFF"
                    lightStatusLogo.setImageResource(R.drawable.lightbulb)
                    brightnessLevel.text = "0"
                    brightnessLevelStatus.text = "OFF"
                    brightnessLevelStatus.setTextColor(
                        android.graphics.Color.parseColor("#A6A3A3")
                    )
                }

                modeType.text = mode
                modeType.setTextColor(
                    if (mode == "MANUAL") android.graphics.Color.parseColor("#FFA500")
                    else android.graphics.Color.parseColor("#A6A3A3")
                )

                if (mode == "MANUAL") {
                    overrideTile.backgroundTintList = ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#90EE90")
                    )
                    overrideTile.setTextColor(android.graphics.Color.parseColor("#000000"))
                } else {
                    overrideTile.backgroundTintList = null
                    overrideTile.setTextColor(android.graphics.Color.WHITE)
                }

                if (status == "DETECTED") {
                    motionTile.backgroundTintList = ColorStateList.valueOf(
                        android.graphics.Color.parseColor("#90EE90")
                    )
                    motionTile.setTextColor(android.graphics.Color.parseColor("#000000"))
                } else {
                    motionTile.backgroundTintList = null
                    motionTile.setTextColor(android.graphics.Color.WHITE)
                }

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
                motionStatus.text = "CLEAR"
                lastUpdateText.text = "Last Update:"
                lastUpdateTime.text = "Unavailable"
                modeType.text = "AUTO"
                modeType.setTextColor(android.graphics.Color.parseColor("#A6A3A3"))
                brightnessLevel.text = "0"
                brightnessLevelStatus.text = "OFF"
                brightnessLevelStatus.setTextColor(android.graphics.Color.parseColor("#A6A3A3"))

                overrideTile.backgroundTintList = null
                overrideTile.setTextColor(android.graphics.Color.WHITE)

                motionTile.backgroundTintList = null
                motionTile.setTextColor(android.graphics.Color.WHITE)

                nightTile.backgroundTintList = null
                nightTile.setTextColor(android.graphics.Color.WHITE)

                ldrReading.text = "0"
                lightLevelText.text = "UNKNOWN"
                ldrMode.text = "DayTime Mode"
                ldrModePercent.text = "0"
            }
        })

        controlBtn.setOnClickListener {
            startActivity(Intent(this, Control::class.java))
        }

        activityLogBtn.setOnClickListener {
            startActivity(Intent(this, ActivityLog::class.java))
        }
    }
}