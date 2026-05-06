package com.example.smartglow_v2.presenter.auth

import com.example.smartglow_v2.base.BaseView

interface LoginContract {
    interface View : BaseView {
        fun onLoginSuccess()
        fun showFieldError(field: String, error: String)
    }

    interface Presenter {
        fun validateAndLogin(email: String, password: String)
        fun onDestroy()
    }
}