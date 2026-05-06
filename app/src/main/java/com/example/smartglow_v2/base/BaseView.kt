package com.example.smartglow_v2.base

interface BaseView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showSuccess(message: String)
}