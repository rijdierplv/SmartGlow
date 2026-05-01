package com.example.smartglow_v2

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
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

    // ── Holds raw log entries from both streets ───────────────────
    private data class LogEntry(
        val datetime: String,
        val status: String,
        val motion: Boolean,
        val brightness: Int?,
        val street: String        // "Street 1" or "Street 2"
    )

    private var street1Logs = listOf<LogEntry>()
    private var street2Logs = listOf<LogEntry>()

    private lateinit var logContainer: LinearLayout
    private lateinit var badgeCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        logContainer = findViewById(R.id.logContainer)
        badgeCount   = findViewById(R.id.badgeCount)

        listenStreet(
            path       = "Logs_street1",
            streetLabel = "Street 1",
            onUpdate   = { logs ->
                street1Logs = logs
                renderLogs()
            }
        )

        listenStreet(
            path       = "Logs_street2",
            streetLabel = "Street 2",
            onUpdate   = { logs ->
                street2Logs = logs
                renderLogs()
            }
        )

        // ── Navigation ────────────────────────────────────────────
        findViewById<ImageButton>(R.id.dashboardBtn).setOnClickListener {
            startActivity(Intent(this, Dashboard::class.java))
        }
        findViewById<ImageButton>(R.id.controlBtn).setOnClickListener {
            startActivity(Intent(this, Control::class.java))
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  FIREBASE – listen to one street path
    // ═══════════════════════════════════════════════════════════════
    private fun listenStreet(
        path: String,
        streetLabel: String,
        onUpdate: (List<LogEntry>) -> Unit
    ) {
        FirebaseDatabase.getInstance()
            .getReference(path)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val entries = snapshot.children.mapNotNull { child ->
                        val status     = child.child("status").getValue(String::class.java) ?: return@mapNotNull null
                        val datetime   = child.child("datetime").getValue(String::class.java) ?: ""
                        val motion     = child.child("motion").getValue(Boolean::class.java) ?: false
                        val brightness = child.child("brightness").getValue(Int::class.java)
                        LogEntry(datetime, status, motion, brightness, streetLabel)
                    }
                    onUpdate(entries)
                }

                override fun onCancelled(error: DatabaseError) {
                    logContainer.removeAllViews()
                    logContainer.addView(makeErrorView("Error loading $streetLabel: ${error.message}"))
                }
            })
    }

    // ═══════════════════════════════════════════════════════════════
    //  RENDER – merge, sort newest-first, build rows
    // ═══════════════════════════════════════════════════════════════
    private fun renderLogs() {
        val merged = (street1Logs + street2Logs)
            .sortedByDescending { it.datetime }

        logContainer.removeAllViews()

        if (merged.isEmpty()) {
            badgeCount.visibility = View.GONE
            logContainer.addView(makeEmptyView())
            return
        }

        badgeCount.text       = merged.size.toString()
        badgeCount.visibility = View.VISIBLE

        for (log in merged) {
            logContainer.addView(buildRow(log))
            logContainer.addView(makeDivider())
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ROW BUILDER
    // ═══════════════════════════════════════════════════════════════
    private fun buildRow(log: LogEntry): View {

        // ── Parse datetime ────────────────────────────────────────
        val parts    = log.datetime.split(" ")
        val timePart = if (parts.size >= 3) "${parts[1].take(5)} ${parts[2]}" else log.datetime
        val datePart = if (parts.isNotEmpty() && parts[0].length >= 10) {
            val month  = parts[0].substring(5, 7).toIntOrNull() ?: 0
            val day    = parts[0].substring(8, 10)
            val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            "${months.getOrElse(month) { "" }} $day"
        } else ""

        // ── Classify entry ────────────────────────────────────────
        val isMotionLog = log.status == "DETECTED" || log.status == "CLEAR"

        val iconRes = when {
            isMotionLog -> R.drawable.motion
            log.status.contains("ON",  ignoreCase = true) ||
                    log.status == "FULL BRIGHTNESS" ||
                    log.status == "DIM" ||
                    log.status == "BRIGHTNESS CHANGED" -> R.drawable.lightingbulb_on
            else -> R.drawable.lightbulb
        }

        val iconTint = when {
            isMotionLog -> Color.parseColor("#90EE90")
            log.status.contains("OFF", ignoreCase = true) ||
                    log.status == "AUTO MODE" -> Color.parseColor("#A6A3A3")
            else -> Color.parseColor("#FFA500")
        }

        val titleText = when (log.status) {
            "DETECTED", "CLEAR" -> "Motion"
            else -> log.status
        }

        val subtitleText = when (log.status) {
            "DETECTED"           -> "Motion sensor triggered"
            "CLEAR"              -> "No motion detected"
            "LIGHT ON"           -> "Light manually turned on"
            "LIGHT OFF"          -> "Light manually turned off"
            "FULL BRIGHTNESS"    -> "Brightness set to maximum"
            "DIM"                -> "Brightness set to dim mode"
            "BRIGHTNESS CHANGED" -> "Brightness manually adjusted"
            "MANUAL MODE"        -> "Manual override enabled"
            "AUTO MODE"          -> "Automatic mode enabled"
            "DARK"               -> "Dark environment detected"
            "DAYTIME"            -> "Daytime – lights off"
            else                 -> "System activity recorded"
        }

        // ── Street chip color ─────────────────────────────────────
        val streetChipColor = if (log.street == "Street 1")
            Color.parseColor("#1A5276")
        else
            Color.parseColor("#1A3A50")

        // ── Assemble row ──────────────────────────────────────────
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 16)
            gravity     = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Icon bubble
        val iconBg = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(48, 48)
            setBackgroundResource(R.drawable.tile_bg)
            gravity = Gravity.CENTER
        }
        iconBg.addView(ImageView(this).apply {
            layoutParams  = LinearLayout.LayoutParams(24, 24)
            setImageResource(iconRes)
            imageTintList = ColorStateList.valueOf(iconTint)
        })

        // Text block
        val textBlock = LinearLayout(this).apply {
            orientation  = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            ).also { it.marginStart = 12 }
        }

        // Title row: title + street chip
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        titleRow.addView(TextView(this).apply {
            text      = titleText
            textSize  = 15f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        })
        titleRow.addView(TextView(this).apply {
            text = "  ${log.street}"
            textSize = 10f
            setTextColor(Color.parseColor("#A6A3A3"))
            setBackgroundColor(streetChipColor)
            setPadding(10, 2, 10, 2)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.marginStart = 6 }
        })

        // Subtitle
        val subtitle = TextView(this).apply {
            text     = subtitleText
            textSize = 12f
            setTextColor(Color.parseColor("#A6A3A3"))
        }

        // Chips row (motion state + brightness + date)
        val chipsRow = LinearLayout(this).apply {
            orientation  = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = 4 }
            gravity = Gravity.CENTER_VERTICAL
        }

        if (isMotionLog) {
            chipsRow.addView(TextView(this).apply {
                text     = if (log.motion) "● ON" else "● OFF"
                textSize = 11f
                setTextColor(
                    if (log.motion) Color.parseColor("#90EE90")
                    else Color.parseColor("#A6A3A3")
                )
            })
        }

        if (log.brightness != null) {
            chipsRow.addView(TextView(this).apply {
                text     = if (isMotionLog) "  ✱ ${log.brightness}%" else "✱ ${log.brightness}%"
                textSize = 11f
                setTextColor(Color.parseColor("#A6A3A3"))
            })
        }

        chipsRow.addView(TextView(this).apply {
            text     = if (chipsRow.childCount > 0) "  $datePart" else datePart
            textSize = 11f
            setTextColor(Color.parseColor("#A6A3A3"))
        })

        textBlock.addView(titleRow)
        textBlock.addView(subtitle)
        textBlock.addView(chipsRow)

        // Time label
        val timeView = TextView(this).apply {
            text     = timePart
            textSize = 12f
            setTextColor(Color.parseColor("#A6A3A3"))
        }

        row.addView(iconBg)
        row.addView(textBlock)
        row.addView(timeView)
        return row
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPER VIEWS
    // ═══════════════════════════════════════════════════════════════
    private fun makeDivider(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1
        )
        setBackgroundColor(Color.parseColor("#1A3A50"))
    }

    private fun makeEmptyView(): TextView = TextView(this).apply {
        text     = "No activity logs found."
        textSize = 14f
        setTextColor(Color.parseColor("#A6A3A3"))
        gravity  = Gravity.CENTER
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).also { it.topMargin = 48 }
    }

    private fun makeErrorView(message: String): TextView = TextView(this).apply {
        text     = message
        textSize = 13f
        setTextColor(Color.parseColor("#FF4444"))
        setPadding(0, 32, 0, 0)
    }
}