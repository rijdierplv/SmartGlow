package com.example.smartglow_v2.presenter.auth

import android.content.Context
import com.example.smartglow_v2.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpPresenter(
    private val view: SignUpContract.View,
    private val context: Context
) : SignUpContract.Presenter {

    private val authRepository = AuthRepository(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun validateAndSignUp(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ) {
        when {
            email.isBlank() -> view.showFieldError("email", "Email is required")
            username.isBlank() -> view.showFieldError("username", "Username is required")
            password.isBlank() -> view.showFieldError("password", "Password is required")
            confirmPassword.isBlank() -> view.showFieldError("confirmPassword", "Please confirm password")
            !isValidUsername(username) -> view.showFieldError(
                "username",
                "Username must be 3-20 characters (letters, numbers, underscore only)"
            )
            password != confirmPassword -> view.showFieldError(
                "confirmPassword",
                "Passwords do not match"
            )
            !isValidPassword(password) -> view.showFieldError(
                "password",
                "Min 8 chars with letter, number, and special character"
            )
            else -> {
                view.showLoading()
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        authRepository.signUp(
                            email.trim(),
                            username.trim(),
                            password
                        )
                    }
                    view.hideLoading()
                    result.fold(
                        onSuccess = { view.onSignUpSuccess() },
                        onFailure = { view.showError(it.message ?: "Sign up failed") }
                    )
                }
            }
        }
    }

    private fun isValidUsername(username: String): Boolean {
        return username.matches(Regex("^[A-Za-z0-9_]{3,20}$"))
    }

    private fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecial = password.any { !it.isLetterOrDigit() }
        return hasLetter && hasDigit && hasSpecial
    }

    override fun onDestroy() {}
}