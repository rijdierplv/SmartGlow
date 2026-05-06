package com.example.smartglow_v2.view.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R
import com.example.smartglow_v2.model.LogEntry
import com.example.smartglow_v2.presenter.main.ActivityLogContract
import com.example.smartglow_v2.presenter.main.ActivityLogPresenter
import com.example.smartglow_v2.view.adapter.LogAdapter

class ActivityLogActivity : AppCompatActivity(), ActivityLogContract.View {

    private lateinit var presenter: ActivityLogPresenter
    private lateinit var logContainer: LinearLayout
    private lateinit var badgeCount: TextView
    private lateinit var adapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log)

        presenter = ActivityLogPresenter(this)
        adapter = LogAdapter(this)

        logContainer = findViewById(R.id.logContainer)
        badgeCount = findViewById(R.id.badgeCount)

        setupNavigation()
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.dashboardBtn).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        findViewById<ImageButton>(R.id.controlBtn).setOnClickListener {
            startActivity(Intent(this, ControlActivity::class.java))
        }
        findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun showLogs(logs: List<LogEntry>) {
        logContainer.removeAllViews()

        badgeCount.text = logs.size.toString()
        badgeCount.visibility = View.VISIBLE

        for (log in logs) {
            logContainer.addView(adapter.createLogRow(log))
            logContainer.addView(adapter.createDivider())
        }
    }

    override fun showEmptyState() {
        logContainer.removeAllViews()
        badgeCount.visibility = View.GONE
        logContainer.addView(adapter.createEmptyView())
    }

    override fun showError(message: String) {
        logContainer.removeAllViews()
        badgeCount.visibility = View.GONE
        logContainer.addView(TextView(this).apply {
            text = message
            textSize = 13f
            setTextColor(android.graphics.Color.parseColor("#FF4444"))
            setPadding(0, 32, 0, 0)
        })
    }

    override fun showLoading() {}
    override fun hideLoading() {}
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