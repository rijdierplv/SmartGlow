package com.example.smartglow_v2.presenter.main

import com.example.smartglow_v2.base.BaseView
import com.example.smartglow_v2.model.LogEntry

interface ActivityLogContract {
    interface View : BaseView {
        fun showLogs(logs: List<LogEntry>)
        fun showEmptyState()
        override fun showError(message: String)
    }

    interface Presenter {
        fun startListening()
        fun stopListening()
        fun onDestroy()
    }
}