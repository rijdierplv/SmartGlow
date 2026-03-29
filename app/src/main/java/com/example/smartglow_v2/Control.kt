package com.example.smartglow_v2

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.Image
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Control : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_control)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val activityLogBtn = findViewById<ImageButton>(R.id.activityLogBtn)
        val tvStreetLightStatus  = findViewById<TextView>(R.id.tvStreetLightStatus)
        val dashboardBtn         = findViewById<ImageButton>(R.id.dashboardBtn)
        val seekBarBrightness    = findViewById<SeekBar>(R.id.seekBarBrightness)
        val tvBrightnessValue    = findViewById<TextView>(R.id.tvBrightnessValue)
        val switchStreetLight    = findViewById<Switch>(R.id.switchStreetLight)
        val switchManualOverride = findViewById<Switch>(R.id.switchManualOverride)
        val btnFullBrightness    = findViewById<LinearLayout>(R.id.btnFullBrightness)
        val btnDim               = findViewById<LinearLayout>(R.id.btnDim)
        val btnAllOff            = findViewById<LinearLayout>(R.id.btnAllOff)
        val lightLogoStatus      = findViewById<ImageView>(R.id.lightLogoStatus)  // ← ADD THIS

        val pirRef = FirebaseDatabase.getInstance().getReference("pir")

        // ── Listen to Firebase: sync switch + seekbar on load ──
        pirRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isOn       = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false
                val brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0
                val mode       = snapshot.child("mode").getValue(String::class.java) ?: "AUTO"

                // Update switchStreetLight without re-triggering its listener
                switchStreetLight.setOnCheckedChangeListener(null)
                switchStreetLight.isChecked = isOn
                switchStreetLight.setOnCheckedChangeListener { _, checked ->
                    if (checked && !switchManualOverride.isChecked) {
                        switchStreetLight.setOnCheckedChangeListener(null)
                        switchStreetLight.isChecked = false
                        switchStreetLight.setOnCheckedChangeListener(null)
                        Toast.makeText(
                            this@Control,
                            "Turn on Manual Override first to turn on the light.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnCheckedChangeListener
                    }
                    pirRef.child("lightOn").setValue(checked)
                    tvStreetLightStatus.text = if (checked) "Currently ON" else "Currently OFF"
                    applyLightIcon(lightLogoStatus, checked)  // ← ADD THIS
                    if (checked) {
                        pirRef.child("brightness").setValue(100)
                        applyBrightness(seekBarBrightness, tvBrightnessValue, 100)
                    } else {
                        pirRef.child("brightness").setValue(0)
                        applyBrightness(seekBarBrightness, tvBrightnessValue, 0)
                    }
                }

                // Sync switchManualOverride without re-triggering listener
                switchManualOverride.setOnCheckedChangeListener(null)
                switchManualOverride.isChecked = mode == "MANUAL"
                switchManualOverride.setOnCheckedChangeListener { _, checked ->
                    pirRef.child("mode").setValue(if (checked) "MANUAL" else "AUTO")
                }

                applyBrightness(seekBarBrightness, tvBrightnessValue, brightness)
                tvStreetLightStatus.text = if (isOn) "Currently ON" else "Currently OFF"
                applyLightIcon(lightLogoStatus, isOn)  // ← ADD THIS
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        // ── Initial switchStreetLight listener ──
        switchStreetLight.setOnCheckedChangeListener { _, checked ->
            if (checked && !switchManualOverride.isChecked) {
                switchStreetLight.setOnCheckedChangeListener(null)
                switchStreetLight.isChecked = false
                switchStreetLight.setOnCheckedChangeListener(null)
                Toast.makeText(
                    this,
                    "Turn on Manual Override first to turn on the light.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnCheckedChangeListener
            }
            pirRef.child("lightOn").setValue(checked)
            tvStreetLightStatus.text = if (checked) "Currently ON" else "Currently OFF"
            applyLightIcon(lightLogoStatus, checked)  // ← ADD THIS
            if (checked) {
                pirRef.child("brightness").setValue(100)
                applyBrightness(seekBarBrightness, tvBrightnessValue, 100)
            } else {
                pirRef.child("brightness").setValue(0)
                applyBrightness(seekBarBrightness, tvBrightnessValue, 0)
            }
        }

        // ── Initial switchManualOverride listener ──
        switchManualOverride.setOnCheckedChangeListener { _, checked ->
            pirRef.child("mode").setValue(if (checked) "MANUAL" else "AUTO")
        }

        // ── SeekBar listener ──
        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    if (!switchManualOverride.isChecked) {
                        seekBar?.progress = 0
                        tvBrightnessValue.text = "0%"
                        tvBrightnessValue.setTextColor(
                            ContextCompat.getColor(this@Control, android.R.color.darker_gray)
                        )
                        Toast.makeText(
                            this@Control,
                            "Turn on Manual Override first to adjust brightness.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    applyBrightness(seekBarBrightness, tvBrightnessValue, progress)
                    val isOn = progress > 0
                    switchStreetLight.isChecked = isOn
                    applyLightIcon(lightLogoStatus, isOn)  // ← ADD THIS
                    pirRef.child("lightOn").setValue(isOn)
                    pirRef.child("brightness").setValue(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // ── FULL BRIGHTNESS ──
        btnFullBrightness.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            applyBrightness(seekBarBrightness, tvBrightnessValue, 100)
            switchStreetLight.isChecked = true
            applyLightIcon(lightLogoStatus, true)  // ← ADD THIS
            pirRef.child("lightOn").setValue(true)
            pirRef.child("brightness").setValue(100)
        }

        // ── DIM 40% ──
        btnDim.setOnClickListener {
            if (!switchManualOverride.isChecked) {
                Toast.makeText(this, "Turn on Manual Override first.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            applyBrightness(seekBarBrightness, tvBrightnessValue, 40)
            switchStreetLight.isChecked = true
            applyLightIcon(lightLogoStatus, true)  // ← ADD THIS
            pirRef.child("lightOn").setValue(true)
            pirRef.child("brightness").setValue(40)
        }

        // ── ALL OFF ──
        btnAllOff.setOnClickListener {
            applyBrightness(seekBarBrightness, tvBrightnessValue, 0)
            switchStreetLight.isChecked = false
            applyLightIcon(lightLogoStatus, false)  // ← ADD THIS
            pirRef.child("lightOn").setValue(false)
            pirRef.child("brightness").setValue(0)
        }

        dashboardBtn.setOnClickListener {
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
        }
        activityLogBtn.setOnClickListener {
            val intent = Intent(this, ActivityLog::class.java)
            startActivity(intent)
        }
    }

    // ── Light icon helper ──
    private fun applyLightIcon(imageView: ImageView, isOn: Boolean) {
        imageView.setImageResource(
            if (isOn) R.drawable.lightingbulb_on else R.drawable.lightbulb
        )
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
        seekBar.setThumbTintList(ColorStateList.valueOf(color))
    }
}