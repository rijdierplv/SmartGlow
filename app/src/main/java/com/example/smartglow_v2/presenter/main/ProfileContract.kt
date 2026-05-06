package com.example.smartglow_v2.presenter.main

import com.example.smartglow_v2.base.BaseView

interface ProfileContract {
    interface View : BaseView {
        fun showUserInfo(username: String, email: String)
        fun onUsernameUpdated(newUsername: String)
        fun onAccountDeleted()
        fun showLogoutConfirmation()
    }

    interface Presenter {
        fun loadUserInfo()
        fun updateUsername(newUsername: String)
        fun logout()
        fun deleteAccount()
        fun onDestroy()
    }
}