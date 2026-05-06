package com.example.smartglow_v2.presenter.auth

import com.example.smartglow_v2.base.BaseView

interface SignUpContract {
    interface View : BaseView {
        fun onSignUpSuccess()
        fun showFieldError(field: String, error: String)
    }

    interface Presenter {
        fun validateAndSignUp(email: String, username: String, password: String, confirmPassword: String)
        fun onDestroy()
    }
}