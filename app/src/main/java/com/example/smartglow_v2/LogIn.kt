package com.example.smartglow_v2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class LogIn : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        val emailInput: EditText = findViewById(R.id.emailinput)
        val passwordInput: EditText = findViewById(R.id.passwordinput)
        val loginButton: Button = findViewById(R.id.loginbutton)
        val signUpText: TextView = findViewById(R.id.textrani)
        val backButton: ImageButton = findViewById(R.id.backbutton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backButton.setOnClickListener {
            finish()
        }

        signUpText.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val emailKey = encodeEmail(email)

            val database = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
            val userRef = database.getReference("users").child(emailKey)

            userRef.get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val savedPassword = snapshot.child("password").getValue(String::class.java)

                    if (password == savedPassword) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                        val sharedPref = getSharedPreferences("SmartGlowPrefs", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("current_user", email)
                        editor.apply()

                        startActivity(Intent(this, Dashboard::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    val msg = e.message ?: "Unknown error"
                    Toast.makeText(this, "Login failed: $msg", Toast.LENGTH_LONG).show()
                }
        }
    }
}