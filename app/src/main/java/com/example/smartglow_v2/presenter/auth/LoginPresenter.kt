package com.example.smartglow_v2.presenter.auth

import android.content.Context
import com.example.smartglow_v2.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private val view: LoginContract.View,
    private val context: Context
) : LoginContract.Presenter {

    private val authRepository = AuthRepository(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun validateAndLogin(email: String, password: String) {
        when {
            email.isBlank() -> view.showFieldError("email", "Email is required")
            password.isBlank() -> view.showFieldError("password", "Password is required")
            else -> {
                view.showLoading()
                scope.launch {
                    val result = withContext(Dispatchers.IO) {
                        authRepository.login(email.trim(), password.trim())
                    }
                    view.hideLoading()
                    result.fold(
                        onSuccess = { view.onLoginSuccess() },
                        onFailure = { view.showError(it.message ?: "Login failed") }
                    )
                }
            }
        }
    }

    override fun onDestroy() {}
}