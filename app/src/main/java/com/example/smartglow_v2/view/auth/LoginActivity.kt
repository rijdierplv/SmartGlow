package com.example.smartglow_v2.view.auth

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R
import com.example.smartglow_v2.presenter.auth.LoginContract
import com.example.smartglow_v2.presenter.auth.LoginPresenter
import com.example.smartglow_v2.utils.goToDashboard
import com.example.smartglow_v2.utils.showToast

class LoginActivity : AppCompatActivity(), LoginContract.View {

    private lateinit var presenter: LoginPresenter
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        presenter = LoginPresenter(this, this)

        emailInput = findViewById(R.id.emailinput)
        passwordInput = findViewById(R.id.passwordinput)

        findViewById<android.widget.Button>(R.id.loginbutton).setOnClickListener {
            presenter.validateAndLogin(
                emailInput.text.toString(),
                passwordInput.text.toString()
            )
        }

        findViewById<TextView>(R.id.textrani).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.backbutton).setOnClickListener {
            finish()
        }
    }

    override fun showLoading() {
        findViewById<android.widget.Button>(R.id.loginbutton).isEnabled = false
        findViewById<android.widget.Button>(R.id.loginbutton).text = "Logging in..."
    }

    override fun hideLoading() {
        findViewById<android.widget.Button>(R.id.loginbutton).isEnabled = true
        findViewById<android.widget.Button>(R.id.loginbutton).text = "Log In"
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
            "password" -> passwordInput.error = error
        }
    }

    override fun onLoginSuccess() {
        showToast("Login Successful!")
        goToDashboard()
        finish()
    }
}