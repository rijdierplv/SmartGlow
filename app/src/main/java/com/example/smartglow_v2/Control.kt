package com.example.smartglow_v2

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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Control : AppCompatActivity() {

    private lateinit var activityLogBtn: ImageButton
    private lateinit var dashboardBtn: ImageButton
    private lateinit var tvStreetLightStatus: TextView
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var tvBrightnessValue: TextView
    private lateinit var switchStreetLight: Switch
    private lateinit var switchManualOverride: Switch
    private lateinit var btnFullBrightness: LinearLayout
    private lateinit var btnDim: LinearLayout
    private lateinit var btnAllOff: LinearLayout
    private lateinit var lightLogoStatus: ImageView

    private lateinit var pirRef: DatabaseReference
    private lateinit var logsRef: DatabaseReference

    private var isSyncingUi = false
    private var currentBrightness = 0
    private var currentLightOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()

        pirRef = FirebaseDatabase.getInstance().getReference("pir")
        logsRef = FirebaseDatabase.getInstance().getReference("pirLogs")

        listenToFirebase()
        setupListeners()
        setupNavigation()
    }

    private fun bindViews() {
        activityLogBtn = findViewById(R.id.activityLogBtn)
        dashboardBtn = findViewById(R.id.dashboardBtn)
        tvStreetLightStatus = findViewById(R.id.tvStreetLightStatus)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        tvBrightnessValue = findViewById(R.id.tvBrightnessValue)
        switchStreetLight = findViewById(R.id.switchStreetLight)
        switchManualOverride = findViewById(R.id.switchManualOverride)
        btnFullBrightness = findViewById(R.id.btnFullBrightness)
        btnDim = findViewById(R.id.btnDim)
        btnAllOff = findViewById(R.id.btnAllOff)
        lightLogoStatus = findViewById(R.id.lightLogoStatus)
    }

    private fun listenToFirebase() {
        pirRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false
                val brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0
                val mode = snapshot.child("mode").getValue(String::class.java) ?: "AUTO"

                currentLightOn = isOn
                currentBrightness = brightness

                isSyncingUi = true
                switchStreetLight.isChecked = isOn
                switchManualOverride.isChecked = mode == "MANUAL"
                updateLightUi(isOn, brightness)
                isSyncingUi = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@Control,
                    "Failed to load data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupListeners() {
        switchStreetLight.setOnCheckedChangeListener { _, checked ->
            if (isSyncingUi) return@setOnCheckedChangeListener

            if (checked && !switchManualOverride.isChecked) {
                isSyncingUi = true
                switchStreetLight.isChecked = false
                isSyncingUi = false

                Toast.makeText(
                    this,
                    "Turn on Manual Override first to turn on the light.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnCheckedChangeListener
            }

            val brightness = if (checked) 100 else 0
            currentLightOn = checked
            currentBrightness = brightness

            updateLightUi(checked, brightness)
            pirRef.child("lightOn").setValue(checked)
            pirRef.child("brightness").setValue(brightness)

            saveLog(
                status = if (checked) "LIGHT ON" else "LIGHT OFF",
                motion = false,
                brightness = brightness
            )
        }

        switchManualOverride.setOnCheckedChangeListener { _, checked ->
            if (isSyncingUi) return@setOnCheckedChangeListener

            val mode = if (checked) "MANUAL" else "AUTO"
            pirRef.child("mode").setValue(mode)

            saveLog(
                status = if (checked) "MANUAL MODE" else "AUTO MODE",
                motion = false,
                brightness = currentBrightness
            )
        }

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser || isSyncingUi) return

                if (!switchManualOverride.isChecked) {
                    isSyncingUi = true
                    applyBrightness(seekBarBrightness, tvBrightnessValue, currentBrightness)
                    isSyncingUi = false

                    Toast.makeText(
                        this@Control,
                        "Turn on Manual Override first to adjust brightness.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                val isOn = progress > 0
                currentLightOn = isOn
                currentBrightness = progress

                isSyncingUi = true
                switchStreetLight.isChecked = isOn
                isSyncingUi = false

                updateLightUi(isOn, progress)
                pirRef.child("lightOn").setValue(isOn)
                pirRef.child("brightness").setValue(progress)

                val status = when {
                    progress == 100 -> "FULL BRIGHTNESS"
                    progress == 0 -> "LIGHT OFF"
                    else -> "BRIGHTNESS CHANGED"
                }

                saveLog(
                    status = status,
                    motion = false,
                    brightness = progress
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnFullBrightness.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setManualLightState(
                isOn = true,
                brightness = 100,
                status = "FULL BRIGHTNESS"
            )
        }

        btnDim.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setManualLightState(
                isOn = true,
                brightness = 40,
                status = "DIM"
            )
        }

        btnAllOff.setOnClickListener {
            setManualLightState(
                isOn = false,
                brightness = 0,
                status = "LIGHT OFF"
            )
        }
    }

    private fun setupNavigation() {
        dashboardBtn.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }

        activityLogBtn.setOnClickListener {
            startActivity(Intent(this, ActivityLog::class.java))
        }
    }

    private fun setManualLightState(isOn: Boolean, brightness: Int, status: String) {
        currentLightOn = isOn
        currentBrightness = brightness

        isSyncingUi = true
        switchStreetLight.isChecked = isOn
        isSyncingUi = false

        updateLightUi(isOn, brightness)
        pirRef.child("lightOn").setValue(isOn)
        pirRef.child("brightness").setValue(brightness)

        saveLog(
            status = status,
            motion = false,
            brightness = brightness
        )
    }

    private fun updateLightUi(isOn: Boolean, brightness: Int) {
        tvStreetLightStatus.text = if (isOn) "Currently ON" else "Currently OFF"
        applyLightIcon(lightLogoStatus, isOn)
        applyBrightness(seekBarBrightness, tvBrightnessValue, brightness)
    }

    private fun saveLog(status: String, motion: Boolean = false, brightness: Int = 0) {
        val currentDateTime = SimpleDateFormat(
            "yyyy-MM-dd hh:mm:ss a",
            Locale.getDefault()
        ).format(Date())

        val logData = mapOf(
            "status" to status,
            "motion" to motion,
            "brightness" to brightness,
            "datetime" to currentDateTime
        )

        logsRef.push().setValue(logData)
    }

    private fun applyLightIcon(imageView: ImageView, isOn: Boolean) {
        imageView.setImageResource(
            if (isOn) R.drawable.lightingbulb_on else R.drawable.lightbulb
        )
    }

    private fun applyBrightness(seekBar: SeekBar, label: TextView, progress: Int) {
        seekBar.progress = progress
        label.text = "$progress%"
        val color = if (progress == 0) {
            Color.parseColor("#A6A3A3")
        } else {
            Color.parseColor("#FFA500")
        }

        label.setTextColor(color)

        val drawable = seekBar.progressDrawable?.mutate()
        drawable?.setTint(color)
        seekBar.progressDrawable = drawable
        seekBar.thumbTintList = ColorStateList.valueOf(color)
    }
}