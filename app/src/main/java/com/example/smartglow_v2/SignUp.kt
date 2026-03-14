package com.example.smartglow_v2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        val emailInput: EditText = findViewById(R.id.emailinput)
        val usernameInput: EditText = findViewById(R.id.userinput)
        val passwordInput: EditText = findViewById(R.id.passwordinput)
        val confirmPasswordInput: EditText = findViewById(R.id.confirminput)
        val signupButton: Button = findViewById(R.id.signupbutton)
        val backButton: ImageButton = findViewById(R.id.backbutton)
        val loginText: TextView = findViewById(R.id.textrani)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signupButton.setOnClickListener {

            val email = emailInput.text.toString().trim()
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val confirmPassword = confirmPasswordInput.text.toString().trim()

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidUsername(username)) {
                Toast.makeText(
                    this,
                    "Username must be 3-20 characters and contain only letters, numbers, or underscore (_)",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                Toast.makeText(
                    this,
                    "Password must be at least 8 characters and include a letter, number, and special character",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            val database = FirebaseDatabase.getInstance(FIREBASE_DB_URL)
            val usersRef = database.getReference("users")

            val emailKey = encodeEmail(email)

            usersRef.child(emailKey).get()
                .addOnSuccessListener { snapshot ->

                    if (snapshot.exists()) {
                        Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show()
                    } else {

                        val userData = mapOf(
                            "email" to email,
                            "username" to username,
                            "password" to password
                        )

                        usersRef.child(emailKey).setValue(userData)
                            .addOnSuccessListener {

                                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()

                                startActivity(Intent(this, LogIn::class.java))
                                finish()
                            }
                            .addOnFailureListener { e ->
                                val msg = e.message ?: "Unknown error"
                                Toast.makeText(this, "Failed to create account: $msg", Toast.LENGTH_LONG).show()
                            }
                    }
                }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
            finish()
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
            finish()
        }
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasLetter && hasDigit && hasSpecial
    }

    private fun isValidUsername(username: String): Boolean {
        return username.matches(Regex("^[A-Za-z0-9_]{3,20}$"))
    }
}