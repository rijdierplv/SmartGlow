package com.example.smartglow_v2.view.main

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R
import com.example.smartglow_v2.model.SensorData
import com.example.smartglow_v2.presenter.main.DashboardContract
import com.example.smartglow_v2.presenter.main.DashboardPresenter
import com.example.smartglow_v2.utils.Constants

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    private lateinit var presenter: DashboardPresenter

    // Street 1 views
    private lateinit var s1LightStatus: TextView
    private lateinit var s1LightStatusLogo: ImageView
    private lateinit var s1LastUpdateTime: TextView
    private lateinit var s1MotionStatus: TextView
    private lateinit var s1ModeType: TextView
    private lateinit var s1BrightnessLevel: TextView
    private lateinit var s1BrightnessStatus: TextView
    private lateinit var s1OverrideTile: TextView
    private lateinit var s1MotionTile: TextView
    private lateinit var s1NightTile: TextView
    private lateinit var s1LdrReading: TextView
    private lateinit var s1LightLevelText: TextView
    private lateinit var s1LdrMode: TextView
    private lateinit var s1LdrPercent: TextView

    // Street 2 views
    private lateinit var s2LightStatus: TextView
    private lateinit var s2LightStatusLogo: ImageView
    private lateinit var s2LastUpdateTime: TextView
    private lateinit var s2MotionStatus: TextView
    private lateinit var s2ModeType: TextView
    private lateinit var s2BrightnessLevel: TextView
    private lateinit var s2BrightnessStatus: TextView
    private lateinit var s2OverrideTile: TextView
    private lateinit var s2MotionTile: TextView
    private lateinit var s2NightTile: TextView
    private lateinit var s2LdrReading: TextView
    private lateinit var s2LightLevelText: TextView
    private lateinit var s2LdrMode: TextView
    private lateinit var s2LdrPercent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        presenter = DashboardPresenter(this)
        bindViews()
        setupNavigation()
    }

    private fun bindViews() {
        // Street 1
        s1LightStatus = findViewById(R.id.lightStatus)
        s1LightStatusLogo = findViewById(R.id.lightStatusLogo)
        s1LastUpdateTime = findViewById(R.id.lastUpdateTime)
        s1MotionStatus = findViewById(R.id.motionStatus)
        s1ModeType = findViewById(R.id.modeType)
        s1BrightnessLevel = findViewById(R.id.brightnessLevel)
        s1BrightnessStatus = findViewById(R.id.brightnessLevelStatus)
        s1OverrideTile = findViewById(R.id.overrideTile)
        s1MotionTile = findViewById(R.id.motionTile)
        s1NightTile = findViewById(R.id.nightTile)
        s1LdrReading = findViewById(R.id.ldrReading)
        s1LightLevelText = findViewById(R.id.lightLevelText)
        s1LdrMode = findViewById(R.id.ldrMode)
        s1LdrPercent = findViewById(R.id.ldrModePercent)

        // Street 2
        s2LightStatus = findViewById(R.id.lightStatus2)
        s2LightStatusLogo = findViewById(R.id.lightStatusLogo2)
        s2LastUpdateTime = findViewById(R.id.lastUpdateTime2)
        s2MotionStatus = findViewById(R.id.motionStatus2)
        s2ModeType = findViewById(R.id.modeType2)
        s2BrightnessLevel = findViewById(R.id.brightnessLevel2)
        s2BrightnessStatus = findViewById(R.id.brightnessLevelStatus2)
        s2OverrideTile = findViewById(R.id.overrideTile2)
        s2MotionTile = findViewById(R.id.motionTile2)
        s2NightTile = findViewById(R.id.nightTile2)
        s2LdrReading = findViewById(R.id.ldrReading2)
        s2LightLevelText = findViewById(R.id.lightLevelText2)
        s2LdrMode = findViewById(R.id.ldrMode2)
        s2LdrPercent = findViewById(R.id.ldrModePercent2)
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.controlBtn).setOnClickListener {
            startActivity(Intent(this, ControlActivity::class.java))
        }
        findViewById<ImageButton>(R.id.activityLogBtn).setOnClickListener {
            startActivity(Intent(this, ActivityLogActivity::class.java))
        }
        // Profile button (4th nav item)
        findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun updateStreet1Data(data: SensorData) {
        updateStreetUI(
            data,
            s1LightStatus, s1LightStatusLogo, s1LastUpdateTime,
            s1MotionStatus, s1ModeType, s1BrightnessLevel, s1BrightnessStatus,
            s1OverrideTile, s1MotionTile, s1NightTile,
            s1LdrReading, s1LightLevelText, s1LdrMode, s1LdrPercent
        )
    }

    override fun updateStreet2Data(data: SensorData) {
        updateStreetUI(
            data,
            s2LightStatus, s2LightStatusLogo, s2LastUpdateTime,
            s2MotionStatus, s2ModeType, s2BrightnessLevel, s2BrightnessStatus,
            s2OverrideTile, s2MotionTile, s2NightTile,
            s2LdrReading, s2LightLevelText, s2LdrMode, s2LdrPercent
        )
    }

    private fun updateStreetUI(
        data: SensorData,
        lightStatus: TextView, lightStatusLogo: ImageView, lastUpdateTime: TextView,
        motionStatus: TextView, modeType: TextView, brightnessLevel: TextView,
        brightnessStatus: TextView, overrideTile: TextView, motionTile: TextView,
        nightTile: TextView, ldrReading: TextView, lightLevelText: TextView,
        ldrMode: TextView, ldrPercent: TextView
    ) {
        // LDR
        ldrReading.text = data.ldrPercent.toString()
        ldrPercent.text = data.ldrPercent.toString()
        lightLevelText.text = data.lightLevel

        if (data.isDark) {
            ldrMode.text = "NightTime Mode"
            lightLevelText.setTextColor(Color.parseColor(Constants.COLOR_SUCCESS))
            nightTile.backgroundTintList = ColorStateList.valueOf(Color.parseColor(Constants.COLOR_SUCCESS))
            nightTile.setTextColor(Color.BLACK)
        } else {
            ldrMode.text = "DayTime Mode"
            lightLevelText.setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
            nightTile.backgroundTintList = null
            nightTile.setTextColor(Color.WHITE)
        }

        // Motion
        motionStatus.text = data.status.ifEmpty { "CLEAR" }
        motionStatus.setTextColor(
            if (data.isMotionDetected) Color.parseColor(Constants.COLOR_SUCCESS)
            else Color.parseColor(Constants.COLOR_TEXT_SECONDARY)
        )

        // Light status
        if (data.lightOn) {
            lightStatus.text = "Lights ON"
            lightStatusLogo.setImageResource(R.drawable.lightingbulb_on)
            brightnessLevel.text = data.brightness.toString()
            brightnessStatus.text = "ON"
            brightnessStatus.setTextColor(Color.parseColor(Constants.COLOR_ACCENT))
        } else {
            lightStatus.text = "Lights OFF"
            lightStatusLogo.setImageResource(R.drawable.lightbulb)
            brightnessLevel.text = "0"
            brightnessStatus.text = "OFF"
            brightnessStatus.setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
        }

        // Mode
        modeType.text = data.mode
        modeType.setTextColor(
            if (data.isManualMode) Color.parseColor(Constants.COLOR_ACCENT)
            else Color.parseColor(Constants.COLOR_TEXT_SECONDARY)
        )

        // Override tile
        if (data.isManualMode) {
            overrideTile.backgroundTintList = ColorStateList.valueOf(Color.parseColor(Constants.COLOR_SUCCESS))
            overrideTile.setTextColor(Color.BLACK)
        } else {
            overrideTile.backgroundTintList = null
            overrideTile.setTextColor(Color.WHITE)
        }

        // Motion tile
        if (data.isMotionDetected) {
            motionTile.backgroundTintList = ColorStateList.valueOf(Color.parseColor(Constants.COLOR_SUCCESS))
            motionTile.setTextColor(Color.BLACK)
        } else {
            motionTile.backgroundTintList = null
            motionTile.setTextColor(Color.WHITE)
        }

        // Last update
        lastUpdateTime.text = formatDateTime(data.lastUpdate)
    }

    private fun formatDateTime(datetime: String): String {
        return if (datetime.length >= 10) {
            val datePart = datetime.substring(0, 10)
            val timePart = if (datetime.length > 11) datetime.substring(11) else ""
            "$datePart  $timePart"
        } else {
            "No data"
        }
    }

    override fun onStreet1Error(message: String) {
        resetStreetUI(
            s1MotionStatus, s1LastUpdateTime, s1ModeType,
            s1BrightnessLevel, s1BrightnessStatus,
            s1OverrideTile, s1MotionTile, s1NightTile,
            s1LdrReading, s1LightLevelText, s1LdrMode, s1LdrPercent
        )
    }

    override fun onStreet2Error(message: String) {
        resetStreetUI(
            s2MotionStatus, s2LastUpdateTime, s2ModeType,
            s2BrightnessLevel, s2BrightnessStatus,
            s2OverrideTile, s2MotionTile, s2NightTile,
            s2LdrReading, s2LightLevelText, s2LdrMode, s2LdrPercent
        )
    }

    private fun resetStreetUI(
        motionStatus: TextView, lastUpdateTime: TextView, modeType: TextView,
        brightnessLevel: TextView, brightnessStatus: TextView,
        overrideTile: TextView, motionTile: TextView, nightTile: TextView,
        ldrReading: TextView, lightLevelText: TextView, ldrMode: TextView, ldrPercent: TextView
    ) {
        motionStatus.text = "CLEAR"
        lastUpdateTime.text = "Unavailable"
        modeType.text = "AUTO"
        modeType.setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
        brightnessLevel.text = "0"
        brightnessStatus.text = "OFF"
        brightnessStatus.setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
        overrideTile.backgroundTintList = null
        overrideTile.setTextColor(Color.WHITE)
        motionTile.backgroundTintList = null
        motionTile.setTextColor(Color.WHITE)
        nightTile.backgroundTintList = null
        nightTile.setTextColor(Color.WHITE)
        ldrReading.text = "0"
        lightLevelText.text = "UNKNOWN"
        ldrMode.text = "DayTime Mode"
        ldrPercent.text = "0"
    }

    override fun showLoading() {}
    override fun hideLoading() {}
    override fun showError(message: String) {}
    override fun showSuccess(message: String) {}

    override fun onResume() {
        super.onResume()
        presenter.startListening()
    }

    override fun onPause() {
        super.onPause()
        presenter.stopListening()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}