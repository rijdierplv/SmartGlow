package com.example.smartglow_v2.view.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartglow_v2.R
import com.example.smartglow_v2.presenter.main.ProfileContract
import com.example.smartglow_v2.presenter.main.ProfilePresenter
import com.example.smartglow_v2.utils.goToLogin
import com.example.smartglow_v2.utils.showToast

class ProfileActivity : AppCompatActivity(), ProfileContract.View {

    private lateinit var presenter: ProfilePresenter

    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var editUsernameLayout: LinearLayout
    private lateinit var editUsernameInput: EditText
    private lateinit var btnEditUsername: LinearLayout
    private lateinit var btnSaveUsername: LinearLayout
    private lateinit var btnCancelEdit: LinearLayout
    private lateinit var btnLogout: LinearLayout
    private lateinit var btnDeleteAccount: LinearLayout
    private lateinit var tvAppVersion: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        presenter = ProfilePresenter(this, this)

        bindViews()
        setupListeners()
        setupNavigation()
    }

    private fun bindViews() {
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        editUsernameLayout = findViewById(R.id.editUsernameLayout)
        editUsernameInput = findViewById(R.id.editUsernameInput)
        btnEditUsername = findViewById(R.id.btnEditUsername)
        btnSaveUsername = findViewById(R.id.btnSaveUsername)
        btnCancelEdit = findViewById(R.id.btnCancelEdit)
        btnLogout = findViewById(R.id.btnLogout)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
    }

    private fun setupListeners() {
        btnEditUsername.setOnClickListener {
            editUsernameLayout.visibility = View.VISIBLE
            editUsernameInput.setText(tvUsername.text.toString())
            editUsernameInput.requestFocus()
        }

        btnSaveUsername.setOnClickListener {
            val newUsername = editUsernameInput.text.toString().trim()
            presenter.updateUsername(newUsername)
        }

        btnCancelEdit.setOnClickListener {
            editUsernameLayout.visibility = View.GONE
        }

        btnLogout.setOnClickListener {
            presenter.logout()
        }

        btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    presenter.deleteAccount()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupNavigation() {
        findViewById<ImageButton>(R.id.dashboardBtn).setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
        }
        findViewById<ImageButton>(R.id.controlBtn).setOnClickListener {
            startActivity(Intent(this, ControlActivity::class.java))
        }
        findViewById<ImageButton>(R.id.activityLogBtn).setOnClickListener {
            startActivity(Intent(this, ActivityLogActivity::class.java))
        }
    }

    override fun showUserInfo(username: String, email: String) {
        tvUsername.text = username
        tvEmail.text = email
    }

    override fun onUsernameUpdated(newUsername: String) {
        tvUsername.text = newUsername
        editUsernameLayout.visibility = View.GONE
        showToast("Username updated successfully")
    }

    override fun onAccountDeleted() {
        showToast("Account deleted successfully")
        goToLogin()
        finish()
    }

    override fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                (presenter as? ProfilePresenter)?.confirmLogout()
                showToast("Logged out successfully")
                goToLogin()
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun showLoading() {
        findViewById<View>(R.id.loadingOverlay)?.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        findViewById<View>(R.id.loadingOverlay)?.visibility = View.GONE
    }

    override fun showError(message: String) {
        showToast(message)
    }

    override fun showSuccess(message: String) {
        showToast(message)
    }

    override fun onResume() {
        super.onResume()
        presenter.loadUserInfo()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}