package com.example.smartglow_v2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ActivityLog : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val dashboardBtn  = findViewById<ImageButton>(R.id.dashboardBtn)
        val controlBtn    = findViewById<ImageButton>(R.id.controlBtn)
        val logContainer  = findViewById<LinearLayout>(R.id.logContainer)
        val badgeCount    = findViewById<TextView>(R.id.badgeCount)

        val logsRef = FirebaseDatabase.getInstance().getReference("pirLogs")

        logsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                logContainer.removeAllViews()

                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    badgeCount.visibility = View.GONE
                    logContainer.addView(TextView(this@ActivityLog).apply {
                        text = "No activity logs found."
                        textSize = 14f
                        setTextColor(Color.parseColor("#A6A3A3"))
                        gravity = Gravity.CENTER
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).also { it.topMargin = 48 }
                    })
                    return
                }

                val logs = snapshot.children.toList().reversed()
                badgeCount.text = logs.size.toString()
                badgeCount.visibility = View.VISIBLE

                for (log in logs) {
                    val status   = log.child("status").getValue(String::class.java) ?: ""
                    val datetime = log.child("datetime").getValue(String::class.java) ?: ""
                    val motion   = log.child("motion").getValue(Boolean::class.java) ?: false

                    val parts    = datetime.split(" ")
                    val timePart = if (parts.size >= 3) "${parts[1].substring(0, 5)} ${parts[2]}"
                    else datetime
                    val datePart = if (parts.isNotEmpty() && parts[0].length >= 10) {
                        val month  = parts[0].substring(5, 7).toIntOrNull() ?: 0
                        val day    = parts[0].substring(8, 10)
                        val months = listOf("","Jan","Feb","Mar","Apr","May","Jun",
                            "Jul","Aug","Sep","Oct","Nov","Dec")
                        "${months.getOrElse(month) { "" }} $day"
                    } else ""

                    val isMotion     = status == "DETECTED" || status == "CLEAR"
                    val iconRes      = when {
                        isMotion                                    -> R.drawable.motion
                        status.contains("ON", ignoreCase = true)   -> R.drawable.lightingbulb_on
                        else                                        -> R.drawable.lightbulb
                    }
                    val titleText    = when (status) {
                        "DETECTED", "CLEAR" -> "Motion"
                        else                -> status
                    }
                    val subtitleText = when {
                        isMotion -> "Motion sensor triggered"
                        else     -> "Light manually turned off"
                    }

                    // ── Row ──
                    val row = LinearLayout(this@ActivityLog).apply {
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(0, 16, 0, 16)
                        gravity = Gravity.CENTER_VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                    }

                    // Icon
                    val iconBg = LinearLayout(this@ActivityLog).apply {
                        layoutParams = LinearLayout.LayoutParams(48, 48)
                        setBackgroundResource(R.drawable.tile_bg)
                        gravity = Gravity.CENTER
                    }
                    val icon = ImageView(this@ActivityLog).apply {
                        layoutParams = LinearLayout.LayoutParams(24, 24)
                        setImageResource(iconRes)
                        imageTintList = android.content.res.ColorStateList.valueOf(
                            if (isMotion) Color.parseColor("#90EE90")
                            else Color.parseColor("#FFA500")
                        )
                    }
                    iconBg.addView(icon)

                    // Text block
                    val textBlock = LinearLayout(this@ActivityLog).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                        ).also { it.marginStart = 12 }
                    }
                    val title = TextView(this@ActivityLog).apply {
                        text = titleText
                        textSize = 15f
                        setTextColor(Color.WHITE)
                        setTypeface(null, android.graphics.Typeface.BOLD)
                    }
                    val subtitle = TextView(this@ActivityLog).apply {
                        text = subtitleText
                        textSize = 12f
                        setTextColor(Color.parseColor("#A6A3A3"))
                    }

                    // Chips row
                    val chipsRow = LinearLayout(this@ActivityLog).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).also { it.topMargin = 4 }
                        gravity = Gravity.CENTER_VERTICAL
                    }
                    chipsRow.addView(TextView(this@ActivityLog).apply {
                        text = if (motion) "● ON" else "● OFF"
                        textSize = 11f
                        setTextColor(
                            if (motion) Color.parseColor("#90EE90")
                            else Color.parseColor("#A6A3A3")
                        )
                    })

                    val brightness = log.child("brightness").getValue(Int::class.java)
                    if (brightness != null && brightness > 0) {
                        chipsRow.addView(TextView(this@ActivityLog).apply {
                            text = "  ✱ ${brightness}%"
                            textSize = 11f
                            setTextColor(Color.parseColor("#A6A3A3"))
                        })
                    }
                    chipsRow.addView(TextView(this@ActivityLog).apply {
                        text = "  $datePart"
                        textSize = 11f
                        setTextColor(Color.parseColor("#A6A3A3"))
                    })

                    textBlock.addView(title)
                    textBlock.addView(subtitle)
                    textBlock.addView(chipsRow)

                    // Time
                    val timeView = TextView(this@ActivityLog).apply {
                        text = timePart
                        textSize = 12f
                        setTextColor(Color.parseColor("#A6A3A3"))
                    }

                    // Divider
                    val divider = View(this@ActivityLog).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1
                        )
                        setBackgroundColor(Color.parseColor("#1A3A50"))
                    }

                    row.addView(iconBg)
                    row.addView(textBlock)
                    row.addView(timeView)
                    logContainer.addView(row)
                    logContainer.addView(divider)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                logContainer.removeAllViews()
                logContainer.addView(TextView(this@ActivityLog).apply {
                    text = "Error: ${error.message}"
                    textSize = 13f
                    setTextColor(Color.parseColor("#FF4444"))
                    setPadding(0, 32, 0, 0)
                })
            }
        })

        dashboardBtn.setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }
        controlBtn.setOnClickListener {
            startActivity(Intent(this, Control::class.java))
        }
    }
}