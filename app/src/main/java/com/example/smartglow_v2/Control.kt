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

    // ── Shared ────────────────────────────────────────────────────
    private lateinit var switchManualOverride: Switch

    // ── Street 1 views ────────────────────────────────────────────
    private lateinit var switchStreetLight: Switch
    private lateinit var tvStreetLightStatus: TextView
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var tvBrightnessValue: TextView
    private lateinit var btnFullBrightness: LinearLayout
    private lateinit var btnDim: LinearLayout
    private lateinit var btnAllOff: LinearLayout
    private lateinit var lightLogoStatus: ImageView

    // ── Street 2 views ────────────────────────────────────────────
    private lateinit var switchStreetLight2: Switch
    private lateinit var tvStreetLightStatus2: TextView
    private lateinit var seekBarBrightness2: SeekBar
    private lateinit var tvBrightnessValue2: TextView
    private lateinit var btnFullBrightness2: LinearLayout
    private lateinit var btnDim2: LinearLayout
    private lateinit var btnAllOff2: LinearLayout
    private lateinit var lightLogoStatus2: ImageView

    // ── Firebase refs ─────────────────────────────────────────────
    private lateinit var street1Ref: DatabaseReference
    private lateinit var street2Ref: DatabaseReference
    private lateinit var logs1Ref: DatabaseReference
    private lateinit var logs2Ref: DatabaseReference

    // ── UI sync guards ────────────────────────────────────────────
    private var isSyncingS1       = false
    private var isSyncingS2       = false
    private var isSyncingOverride = false

    // ── Cached state ──────────────────────────────────────────────
    private var s1Brightness = 0
    private var s1LightOn    = false
    private var s2Brightness = 0
    private var s2LightOn    = false

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

        street1Ref = FirebaseDatabase.getInstance().getReference("sensors_street1")
        street2Ref = FirebaseDatabase.getInstance().getReference("sensors_street2")
        logs1Ref   = FirebaseDatabase.getInstance().getReference("Logs_street1")
        logs2Ref   = FirebaseDatabase.getInstance().getReference("Logs_street2")

        listenStreet1Firebase()
        listenStreet2Firebase()
        setupManualOverrideListener()
        setupStreet1Listeners()
        setupStreet2Listeners()
        setupNavigation()
    }

    // ═══════════════════════════════════════════════════════════════
    //  BIND VIEWS
    // ═══════════════════════════════════════════════════════════════
    private fun bindViews() {
        switchManualOverride  = findViewById(R.id.switchManualOverride)

        // Street 1
        switchStreetLight     = findViewById(R.id.switchStreetLight)
        tvStreetLightStatus   = findViewById(R.id.tvStreetLightStatus)
        seekBarBrightness     = findViewById(R.id.seekBarBrightness)
        tvBrightnessValue     = findViewById(R.id.tvBrightnessValue)
        btnFullBrightness     = findViewById(R.id.btnFullBrightness)
        btnDim                = findViewById(R.id.btnDim)
        btnAllOff             = findViewById(R.id.btnAllOff)
        lightLogoStatus       = findViewById(R.id.lightLogoStatus)

        // Street 2
        switchStreetLight2    = findViewById(R.id.switchStreetLight2)
        tvStreetLightStatus2  = findViewById(R.id.tvStreetLightStatus2)
        seekBarBrightness2    = findViewById(R.id.seekBarBrightness2)
        tvBrightnessValue2    = findViewById(R.id.tvBrightnessValue2)
        btnFullBrightness2    = findViewById(R.id.btnFullBrightness2)
        btnDim2               = findViewById(R.id.btnDim2)
        btnAllOff2            = findViewById(R.id.btnAllOff2)
        lightLogoStatus2      = findViewById(R.id.lightLogoStatus2)
    }

    // ═══════════════════════════════════════════════════════════════
    //  FIREBASE LISTENERS
    // ═══════════════════════════════════════════════════════════════
    private fun listenStreet1Firebase() {
        street1Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn       = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false
                val brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0
                val mode       = snapshot.child("mode").getValue(String::class.java) ?: "AUTO"

                s1LightOn    = isOn
                s1Brightness = brightness

                isSyncingS1 = true
                switchStreetLight.isChecked = isOn
                // Street 1 is the source of truth for the shared override switch
                if (!isSyncingOverride) {
                    isSyncingOverride = true
                    switchManualOverride.isChecked = (mode == "MANUAL")
                    isSyncingOverride = false
                }
                updateStreet1Ui(isOn, brightness)
                isSyncingS1 = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Control, "Street 1: Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun listenStreet2Firebase() {
        street2Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn       = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false
                val brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0
                // mode for Street 2 is driven by the shared switch; no need to read it here

                s2LightOn    = isOn
                s2Brightness = brightness

                isSyncingS2 = true
                switchStreetLight2.isChecked = isOn
                updateStreet2Ui(isOn, brightness)
                isSyncingS2 = false
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@Control, "Street 2: Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ═══════════════════════════════════════════════════════════════
    //  SINGLE MANUAL OVERRIDE — writes mode to BOTH streets
    // ═══════════════════════════════════════════════════════════════
    private fun setupManualOverrideListener() {
        switchManualOverride.setOnCheckedChangeListener { _, checked ->
            if (isSyncingOverride) return@setOnCheckedChangeListener

            val mode = if (checked) "MANUAL" else "AUTO"

            street1Ref.child("mode").setValue(mode)
            street2Ref.child("mode").setValue(mode)

            val logStatus = if (checked) "MANUAL MODE" else "AUTO MODE"
            saveLog(logs1Ref, logStatus, false, s1Brightness)
            saveLog(logs2Ref, logStatus, false, s2Brightness)
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  STREET 1 – INTERACTION LISTENERS
    // ═══════════════════════════════════════════════════════════════
    private fun setupStreet1Listeners() {

        switchStreetLight.setOnCheckedChangeListener { _, checked ->
            if (isSyncingS1) return@setOnCheckedChangeListener

            if (checked && !switchManualOverride.isChecked) {
                isSyncingS1 = true
                switchStreetLight.isChecked = false
                isSyncingS1 = false
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            val brightness = if (checked) 100 else 0
            s1LightOn    = checked
            s1Brightness = brightness

            updateStreet1Ui(checked, brightness)
            street1Ref.child("lightOn").setValue(checked)
            street1Ref.child("brightness").setValue(brightness)
            saveLog(logs1Ref, if (checked) "LIGHT ON" else "LIGHT OFF", false, brightness)
        }

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser || isSyncingS1) return

                if (!switchManualOverride.isChecked) {
                    isSyncingS1 = true
                    applyBrightness(seekBarBrightness, tvBrightnessValue, s1Brightness)
                    isSyncingS1 = false
                    Toast.makeText(this@Control, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                    return
                }

                val isOn = progress > 0
                s1LightOn    = isOn
                s1Brightness = progress

                isSyncingS1 = true
                switchStreetLight.isChecked = isOn
                isSyncingS1 = false

                updateStreet1Ui(isOn, progress)
                street1Ref.child("lightOn").setValue(isOn)
                street1Ref.child("brightness").setValue(progress)

                val status = when (progress) {
                    100  -> "FULL BRIGHTNESS"
                    0    -> "LIGHT OFF"
                    else -> "BRIGHTNESS CHANGED"
                }
                saveLog(logs1Ref, status, false, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnFullBrightness.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setStreet1State(true, 100, "FULL BRIGHTNESS")
        }

        btnDim.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setStreet1State(true, 40, "DIM")
        }

        btnAllOff.setOnClickListener {
            setStreet1State(false, 0, "LIGHT OFF")
        }
    }

    private fun setStreet1State(isOn: Boolean, brightness: Int, status: String) {
        s1LightOn    = isOn
        s1Brightness = brightness

        isSyncingS1 = true
        switchStreetLight.isChecked = isOn
        isSyncingS1 = false

        updateStreet1Ui(isOn, brightness)
        street1Ref.child("lightOn").setValue(isOn)
        street1Ref.child("brightness").setValue(brightness)
        saveLog(logs1Ref, status, false, brightness)
    }

    private fun updateStreet1Ui(isOn: Boolean, brightness: Int) {
        tvStreetLightStatus.text = if (isOn) "Currently ON" else "Currently OFF"
        lightLogoStatus.setImageResource(
            if (isOn) R.drawable.lightingbulb_on else R.drawable.lightbulb
        )
        applyBrightness(seekBarBrightness, tvBrightnessValue, brightness)
    }

    // ═══════════════════════════════════════════════════════════════
    //  STREET 2 – INTERACTION LISTENERS
    // ═══════════════════════════════════════════════════════════════
    private fun setupStreet2Listeners() {

        switchStreetLight2.setOnCheckedChangeListener { _, checked ->
            if (isSyncingS2) return@setOnCheckedChangeListener

            if (checked && !switchManualOverride.isChecked) {
                isSyncingS2 = true
                switchStreetLight2.isChecked = false
                isSyncingS2 = false
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnCheckedChangeListener
            }

            val brightness = if (checked) 100 else 0
            s2LightOn    = checked
            s2Brightness = brightness

            updateStreet2Ui(checked, brightness)
            street2Ref.child("lightOn").setValue(checked)
            street2Ref.child("brightness").setValue(brightness)
            saveLog(logs2Ref, if (checked) "LIGHT ON" else "LIGHT OFF", false, brightness)
        }

        seekBarBrightness2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (!fromUser || isSyncingS2) return

                if (!switchManualOverride.isChecked) {
                    isSyncingS2 = true
                    applyBrightness(seekBarBrightness2, tvBrightnessValue2, s2Brightness)
                    isSyncingS2 = false
                    Toast.makeText(this@Control, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                    return
                }

                val isOn = progress > 0
                s2LightOn    = isOn
                s2Brightness = progress

                isSyncingS2 = true
                switchStreetLight2.isChecked = isOn
                isSyncingS2 = false

                updateStreet2Ui(isOn, progress)
                street2Ref.child("lightOn").setValue(isOn)
                street2Ref.child("brightness").setValue(progress)

                val status = when (progress) {
                    100  -> "FULL BRIGHTNESS"
                    0    -> "LIGHT OFF"
                    else -> "BRIGHTNESS CHANGED"
                }
                saveLog(logs2Ref, status, false, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnFullBrightness2.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setStreet2State(true, 100, "FULL BRIGHTNESS")
        }

        btnDim2.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            setStreet2State(true, 40, "DIM")
        }

        btnAllOff2.setOnClickListener {
            setStreet2State(false, 0, "LIGHT OFF")
        }
    }

    private fun setStreet2State(isOn: Boolean, brightness: Int, status: String) {
        s2LightOn    = isOn
        s2Brightness = brightness

        isSyncingS2 = true
        switchStreetLight2.isChecked = isOn
        isSyncingS2 = false

        updateStreet2Ui(isOn, brightness)
        street2Ref.child("lightOn").setValue(isOn)
        street2Ref.child("brightness").setValue(brightness)
        saveLog(logs2Ref, status, false, brightness)
    }

    private fun updateStreet2Ui(isOn: Boolean, brightness: Int) {
        tvStreetLightStatus2.text = if (isOn) "Currently ON" else "Currently OFF"
        lightLogoStatus2.setImageResource(
            if (isOn) R.drawable.lightingbulb_on else R.drawable.lightbulb
        )
        applyBrightness(seekBarBrightness2, tvBrightnessValue2, brightness)
    }

    // ═══════════════════════════════════════════════════════════════
    //  NAVIGATION
    // ═══════════════════════════════════════════════════════════════
    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.dashboardBtn).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }
        findViewById<ImageButton>(R.id.activityLogBtn).setOnClickListener {
            startActivity(Intent(this, ActivityLog::class.java))
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SHARED HELPERS
    // ═══════════════════════════════════════════════════════════════
    private fun saveLog(
        ref: DatabaseReference,
        status: String,
        motion: Boolean = false,
        brightness: Int = 0
    ) {
        val dt = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault()).format(Date())
        val logData = mapOf(
            "status"     to status,
            "motion"     to motion,
            "brightness" to brightness,
            "datetime"   to dt
        )
        ref.push().setValue(logData)
    }

    private fun applyBrightness(seekBar: SeekBar, label: TextView, progress: Int) {
        seekBar.progress = progress
        label.text = "$progress%"

        val color = if (progress == 0) Color.parseColor("#A6A3A3")
        else Color.parseColor("#FFA500")

        label.setTextColor(color)

        val drawable = seekBar.progressDrawable?.mutate()
        drawable?.setTint(color)
        seekBar.progressDrawable = drawable
        seekBar.thumbTintList = ColorStateList.valueOf(color)
    }
}