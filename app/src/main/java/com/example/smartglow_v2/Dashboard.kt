package com.example.smartglow_v2

import android.os.Bundle
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

        val lightStatus = findViewById<TextView>(R.id.lightStatus)
        val lightStatusLogo = findViewById<ImageView>(R.id.lightStatusLogo)
        val lastUpdateText = findViewById<TextView>(R.id.lastUpdateText)
        val lastUpdateTime = findViewById<TextView>(R.id.lastUpdateTime)
        val motionStatus = findViewById<TextView>(R.id.motionStatus)
        val modeType = findViewById<TextView>(R.id.modeType)

        val pirRef = FirebaseDatabase.getInstance().getReference("pir")

        pirRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java)
                val lastUpdate = snapshot.child("lastUpdate").getValue(String::class.java)

                motionStatus.text = status ?: "CLEAR"
                if (status == "DETECTED") {
                    motionStatus.setTextColor(android.graphics.Color.parseColor("#90EE90"))
                } else {
                    motionStatus.setTextColor(android.graphics.Color.parseColor("#A6A3A3"))
                }

                if (status == "DETECTED") {
                    lightStatus.text = "Lights ON"
                    lightStatusLogo.setImageResource(R.drawable.lightingbulb_on)
                } else {
                    lightStatus.text = "Lights OFF"
                    lightStatusLogo.setImageResource(R.drawable.lightbulb)
                }

                modeType.text = "AUTO"

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
            }
        })
    }
}