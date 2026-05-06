package com.example.smartglow_v2.presenter.main

import android.content.Context
import com.example.smartglow_v2.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfilePresenter(
    private val view: ProfileContract.View,
    private val context: Context
) : ProfileContract.Presenter {

    private val authRepository = AuthRepository(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun loadUserInfo() {
        val email = authRepository.getCurrentUserEmail() ?: return
        val username = authRepository.getUsername()
        view.showUserInfo(username, email)
    }

    override fun updateUsername(newUsername: String) {
        when {
            newUsername.isBlank() -> view.showError("Username cannot be empty")
            !newUsername.matches(Regex("^[A-Za-z0-9_]{3,20}$")) ->
                view.showError("Invalid username format")
            else -> {
                view.showLoading()
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        authRepository.updateUsername(newUsername.trim())
                    }
                    view.hideLoading()
                    result.fold(
                        onSuccess = { view.onUsernameUpdated(newUsername.trim()) },
                        onFailure = { view.showError(it.message ?: "Failed to update username") }
                    )
                }
            }
        }
    }

    override fun logout() {
        view.showLogoutConfirmation()
    }

    fun confirmLogout() {
        authRepository.logout()
    }

    override fun deleteAccount() {
        view.showLoading()
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                authRepository.deleteAccount()
            }
            view.hideLoading()
            result.fold(
                onSuccess = { view.onAccountDeleted() },
                onFailure = { view.showError(it.message ?: "Failed to delete account") }
            )
        }
    }

    override fun onDestroy() {}
}