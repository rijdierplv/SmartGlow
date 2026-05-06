package com.example.smartglow_v2.view.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R

class ChoicesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choices)

        findViewById<Button>(R.id.loginChoiceButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.signUpChoiceButton).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}