package com.example.smartglow_v2.view.main

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R
import com.example.smartglow_v2.model.SensorData
import com.example.smartglow_v2.presenter.main.ControlContract
import com.example.smartglow_v2.presenter.main.ControlPresenter
import com.example.smartglow_v2.utils.Constants
import com.example.smartglow_v2.utils.showToast

class ControlActivity : AppCompatActivity(), ControlContract.View {

    private lateinit var presenter: ControlPresenter

    // Shared
    private lateinit var switchManualOverride: Switch

    // Street 1
    private lateinit var s1SwitchLight: Switch
    private lateinit var s1TvStatus: TextView
    private lateinit var s1SeekBar: SeekBar
    private lateinit var s1TvBrightness: TextView
    private lateinit var s1BtnFull: LinearLayout
    private lateinit var s1BtnDim: LinearLayout
    private lateinit var s1BtnOff: LinearLayout
    private lateinit var s1Logo: ImageView

    // Street 2
    private lateinit var s2SwitchLight: Switch
    private lateinit var s2TvStatus: TextView
    private lateinit var s2SeekBar: SeekBar
    private lateinit var s2TvBrightness: TextView
    private lateinit var s2BtnFull: LinearLayout
    private lateinit var s2BtnDim: LinearLayout
    private lateinit var s2BtnOff: LinearLayout
    private lateinit var s2Logo: ImageView

    // Sync guards
    private var syncingS1 = false
    private var syncingS2 = false
    private var syncingOverride = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)

        presenter = ControlPresenter(this)
        bindViews()
        setupListeners()
        setupNavigation()

        // Default disabled until data arrives
        setControlsEnabled(false)
    }

    private fun bindViews() {
        switchManualOverride = findViewById(R.id.switchManualOverride)

        s1SwitchLight = findViewById(R.id.switchStreetLight)
        s1TvStatus = findViewById(R.id.tvStreetLightStatus)
        s1SeekBar = findViewById(R.id.seekBarBrightness)
        s1TvBrightness = findViewById(R.id.tvBrightnessValue)
        s1BtnFull = findViewById(R.id.btnFullBrightness)
        s1BtnDim = findViewById(R.id.btnDim)
        s1BtnOff = findViewById(R.id.btnAllOff)
        s1Logo = findViewById(R.id.lightLogoStatus)

        s2SwitchLight = findViewById(R.id.switchStreetLight2)
        s2TvStatus = findViewById(R.id.tvStreetLightStatus2)
        s2SeekBar = findViewById(R.id.seekBarBrightness2)
        s2TvBrightness = findViewById(R.id.tvBrightnessValue2)
        s2BtnFull = findViewById(R.id.btnFullBrightness2)
        s2BtnDim = findViewById(R.id.btnDim2)
        s2BtnOff = findViewById(R.id.btnAllOff2)
        s2Logo = findViewById(R.id.lightLogoStatus2)
    }

    private fun setupListeners() {

        // Manual Override
        switchManualOverride.setOnCheckedChangeListener { _, checked ->
            if (syncingOverride) return@setOnCheckedChangeListener
            presenter.toggleManualOverride(checked)

            // 🔥 Enable/disable controls
            setControlsEnabled(checked)
        }

        // Street 1 Switch
        s1SwitchLight.setOnCheckedChangeListener { _, checked ->
            if (syncingS1) return@setOnCheckedChangeListener
            if (!switchManualOverride.isChecked) {
                showOverrideRequired()
                return@setOnCheckedChangeListener
            }
            presenter.toggleStreetLight(1, checked)
        }

        // Street 1 SeekBar
        s1SeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                if (!switchManualOverride.isChecked) return

                s1TvBrightness.text = "$progress%"
                updateSeekBarColor(s1SeekBar, s1TvBrightness, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (syncingS1 || !switchManualOverride.isChecked) return
                presenter.setBrightness(1, s1SeekBar.progress)
            }
        })

        // Street 1 Presets
        s1BtnFull.setOnClickListener {
            if (!switchManualOverride.isChecked) return@setOnClickListener showOverrideRequired()
            presenter.setPresetBrightness(1, ControlContract.BrightnessPreset.FULL)
        }
        s1BtnDim.setOnClickListener {
            if (!switchManualOverride.isChecked) return@setOnClickListener showOverrideRequired()
            presenter.setPresetBrightness(1, ControlContract.BrightnessPreset.DIM)
        }
        s1BtnOff.setOnClickListener {
            if (!switchManualOverride.isChecked) return@setOnClickListener showOverrideRequired()
            presenter.setPresetBrightness(1, ControlContract.BrightnessPreset.OFF)
        }

        // Street 2 Switch
        s2SwitchLight.setOnCheckedChangeListener { _, checked ->
            if (syncingS2) return@setOnCheckedChangeListener
            if (!switchManualOverride.isChecked) {
                showOverrideRequired()
                return@setOnCheckedChangeListener
            }
            presenter.toggleStreetLight(2, checked)
        }

        // Street 2 SeekBar
        s2SeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser) return
                if (!switchManualOverride.isChecked) return

                s2TvBrightness.text = "$progress%"
                updateSeekBarColor(s2SeekBar, s2TvBrightness, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (syncingS2 || !switchManualOverride.isChecked) return
                presenter.setBrightness(2, s2SeekBar.progress)
            }
        })

        // Street 2 Presets
        s2BtnFull.setOnClickListener {
            if (!switchManualOverride.isChecked) return@setOnClickListener showOverrideRequired()
            presenter.setPresetBrightness(2, ControlContract.BrightnessPreset.FULL)
        }
        s2BtnDim.setOnClickListener {
            if (!switchManualOverride.isChecked) return@setOnClickListener showOverrideRequired()
            presenter.setPresetBrightness(2, ControlContract.BrightnessPreset.DIM)
        }
        s2BtnOff.setOnClickListener {
            if (!switchManualOverride.isChecked) return@setOnClickListener showOverrideRequired()
            presenter.setPresetBrightness(2, ControlContract.BrightnessPreset.OFF)
        }
    }

    private fun setControlsEnabled(enabled: Boolean) {
        val alpha = if (enabled) 1f else 0.5f

        listOf(
            s1SwitchLight, s1SeekBar, s1BtnFull, s1BtnDim, s1BtnOff,
            s2SwitchLight, s2SeekBar, s2BtnFull, s2BtnDim, s2BtnOff
        ).forEach {
            it.isEnabled = enabled
            it.alpha = alpha
        }
    }

    private fun updateSeekBarColor(seekBar: SeekBar, label: TextView, progress: Int) {
        val color = if (progress == 0)
            Color.parseColor(Constants.COLOR_TEXT_SECONDARY)
        else
            Color.parseColor(Constants.COLOR_ACCENT)

        label.setTextColor(color)
        seekBar.progressDrawable?.mutate()?.setTint(color)
        seekBar.thumbTintList = ColorStateList.valueOf(color)
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.dashboardBtn).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        findViewById<ImageButton>(R.id.activityLogBtn).setOnClickListener {
            startActivity(Intent(this, ActivityLogActivity::class.java))
        }
        findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun updateStreet1State(data: SensorData) {
        syncingS1 = true
        s1SwitchLight.isChecked = data.lightOn
        syncingS1 = false

        syncingOverride = true
        switchManualOverride.isChecked = data.isManualMode
        syncingOverride = false

        // 🔥 Apply control state from backend
        setControlsEnabled(data.isManualMode)

        s1TvStatus.text = if (data.lightOn) "Currently ON" else "Currently OFF"
        s1Logo.setImageResource(if (data.lightOn) R.drawable.lightingbulb_on else R.drawable.lightbulb)

        syncingS1 = true
        s1SeekBar.progress = data.brightness
        s1TvBrightness.text = "${data.brightness}%"
        updateSeekBarColor(s1SeekBar, s1TvBrightness, data.brightness)
        syncingS1 = false
    }

    override fun updateStreet2State(data: SensorData) {
        syncingS2 = true
        s2SwitchLight.isChecked = data.lightOn
        syncingS2 = false

        s2TvStatus.text = if (data.lightOn) "Currently ON" else "Currently OFF"
        s2Logo.setImageResource(if (data.lightOn) R.drawable.lightingbulb_on else R.drawable.lightbulb)

        syncingS2 = true
        s2SeekBar.progress = data.brightness
        s2TvBrightness.text = "${data.brightness}%"
        updateSeekBarColor(s2SeekBar, s2TvBrightness, data.brightness)
        syncingS2 = false
    }

    override fun showOverrideRequired() {
        showToast("Turn on Manual Override first")
    }

    override fun onControlSuccess(message: String) {
        showToast(message)
    }

    override fun onControlError(message: String) {
        showToast(message)
    }

    override fun showLoading() {}
    override fun hideLoading() {}
    override fun showError(message: String) { showToast(message) }
    override fun showSuccess(message: String) { showToast(message) }

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