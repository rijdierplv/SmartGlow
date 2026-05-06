package com.example.smartglow_v2.view.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R
import com.example.smartglow_v2.utils.goToDashboard
import com.example.smartglow_v2.utils.isLoggedIn

class WelcomeScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        // Check if user is already logged in
        if (isLoggedIn(this)) {
            goToDashboard()
            finish()
            return
        }

        findViewById<Button>(R.id.getstartedButton).setOnClickListener {
            startActivity(Intent(this, ChoicesActivity::class.java))
            finish()
        }
    }
}