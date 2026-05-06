package com.example.smartglow_v2.view.auth

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R
import com.example.smartglow_v2.presenter.auth.SignUpContract
import com.example.smartglow_v2.presenter.auth.SignUpPresenter
import com.example.smartglow_v2.utils.showToast

class SignUpActivity : AppCompatActivity(), SignUpContract.View {

    private lateinit var presenter: SignUpPresenter
    private lateinit var emailInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var confirmInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        presenter = SignUpPresenter(this, this)

        emailInput = findViewById(R.id.emailinput)
        usernameInput = findViewById(R.id.userinput)
        passwordInput = findViewById(R.id.passwordinput)
        confirmInput = findViewById(R.id.confirminput)

        findViewById<android.widget.Button>(R.id.signupbutton).setOnClickListener {
            presenter.validateAndSignUp(
                emailInput.text.toString(),
                usernameInput.text.toString(),
                passwordInput.text.toString(),
                confirmInput.text.toString()
            )
        }

        findViewById<TextView>(R.id.textrani).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.backbutton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun showLoading() {
        findViewById<android.widget.Button>(R.id.signupbutton).isEnabled = false
        findViewById<android.widget.Button>(R.id.signupbutton).text = "Creating account..."
    }

    override fun hideLoading() {
        findViewById<android.widget.Button>(R.id.signupbutton).isEnabled = true
        findViewById<android.widget.Button>(R.id.signupbutton).text = "Sign Up"
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun showSuccess(message: String) {
        showToast(message)
    }

    override fun showFieldError(field: String, error: String) {
        when (field) {
            "email" -> emailInput.error = error
            "username" -> usernameInput.error = error
            "password" -> passwordInput.error = error
            "confirmPassword" -> confirmInput.error = error
        }
    }

    override fun onSignUpSuccess() {
        showToast("Account Created!")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}