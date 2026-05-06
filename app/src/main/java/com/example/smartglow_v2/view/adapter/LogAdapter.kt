package com.example.smartglow_v2.view.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.smartglow_v2.R
import com.example.smartglow_v2.model.LogEntry
import com.example.smartglow_v2.utils.Constants

class LogAdapter(private val context: Context) {

    fun createLogRow(log: LogEntry): View {
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(16, 16, 16, 16)
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Icon bubble
        val iconBg = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(48, 48)
            setBackgroundResource(R.drawable.tile_bg)
            gravity = Gravity.CENTER
        }
        iconBg.addView(ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(24, 24)
            setImageResource(getIconRes(log))
            imageTintList = ColorStateList.valueOf(getIconColor(log))
        })

        // Text block
        val textBlock = createTextBlock(log)

        // Time label
        val timeView = TextView(context).apply {
            text = log.formattedTime
            textSize = 12f
            setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
        }

        row.addView(iconBg)
        row.addView(textBlock)
        row.addView(timeView)

        return row
    }

    private fun createTextBlock(log: LogEntry): LinearLayout {
        val textBlock = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 12
            }
        }

        // Title row
        val titleRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        titleRow.addView(TextView(context).apply {
            text = getTitleText(log)
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        })

        titleRow.addView(TextView(context).apply {
            text = "  ${log.street}"
            textSize = 10f
            setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
            setBackgroundColor(
                if (log.street == "Street 1") Color.parseColor("#1A5276")
                else Color.parseColor("#1A3A50")
            )
            setPadding(10, 2, 10, 2)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginStart = 6 }
        })

        // Subtitle
        val subtitle = TextView(context).apply {
            text = log.subtitleText
            textSize = 12f
            setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
        }

        // Chips row
        val chipsRow = createChipsRow(log)

        textBlock.addView(titleRow)
        textBlock.addView(subtitle)
        textBlock.addView(chipsRow)

        return textBlock
    }

    private fun createChipsRow(log: LogEntry): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
            gravity = Gravity.CENTER_VERTICAL

            if (log.isMotionLog) {
                addView(TextView(context).apply {
                    text = if (log.motion) "● ON" else "● OFF"
                    textSize = 11f
                    setTextColor(
                        if (log.motion) Color.parseColor(Constants.COLOR_SUCCESS)
                        else Color.parseColor(Constants.COLOR_TEXT_SECONDARY)
                    )
                })
            }

            if (log.brightness != null) {
                addView(TextView(context).apply {
                    text = if (childCount > 0) "  ✱ ${log.brightness}%" else "✱ ${log.brightness}%"
                    textSize = 11f
                    setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
                })
            }

            addView(TextView(context).apply {
                text = if (childCount > 0) "  ${log.formattedDate}" else log.formattedDate
                textSize = 11f
                setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
            })
        }
    }

    private fun getIconRes(log: LogEntry): Int {
        return when {
            log.isMotionLog -> R.drawable.motion
            log.status.contains("ON", ignoreCase = true) ||
                    log.status == "FULL BRIGHTNESS" ||
                    log.status == "DIM" ||
                    log.status == "BRIGHTNESS CHANGED" -> R.drawable.lightingbulb_on
            else -> R.drawable.lightbulb
        }
    }

    private fun getIconColor(log: LogEntry): Int {
        return when {
            log.isMotionLog -> Color.parseColor(Constants.COLOR_SUCCESS)
            log.status.contains("OFF", ignoreCase = true) ||
                    log.status == "AUTO MODE" -> Color.parseColor(Constants.COLOR_TEXT_SECONDARY)
            else -> Color.parseColor(Constants.COLOR_ACCENT_LIGHT)
        }
    }

    private fun getTitleText(log: LogEntry): String {
        return if (log.isMotionLog) "Motion" else log.status
    }

    fun createDivider(): View {
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            )
            setBackgroundColor(Color.parseColor(Constants.COLOR_DIVIDER))
        }
    }

    fun createEmptyView(): TextView {
        return TextView(context).apply {
            text = "No activity logs found."
            textSize = 14f
            setTextColor(Color.parseColor(Constants.COLOR_TEXT_SECONDARY))
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 48 }
        }
    }
}