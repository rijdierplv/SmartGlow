package com.example.smartglow_v2

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val backButton: ImageButton = findViewById(R.id.backbutton)
        val usernameInput: EditText = findViewById(R.id.userinput)
        val emailInput: EditText = findViewById(R.id.emailinput)
        val passwordInput: EditText = findViewById(R.id.passwordinput)
        val confirmInput: EditText = findViewById(R.id.confirminput)
        val signUpButton: Button = findViewById(R.id.signupbutton)
        val loginText: TextView = findViewById(R.id.textrani)

        backButton.setOnClickListener {
            finish()
        }

        loginText.setOnClickListener {
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
        }

        signUpButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmInput.text.toString().trim()

            if (username.isEmpty()) {
                usernameInput.error = "Username is required"
                usernameInput.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                emailInput.error = "Email is required"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Enter a valid email"
                emailInput.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordInput.error = "Password is required"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                passwordInput.error = "Password must be at least 6 characters"
                passwordInput.requestFocus()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                confirmInput.error = "Please confirm your password"
                confirmInput.requestFocus()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                confirmInput.error = "Passwords do not match"
                confirmInput.requestFocus()
                return@setOnClickListener
            }

            Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LogIn::class.java)
            startActivity(intent)
            finish()
        }
    }
}