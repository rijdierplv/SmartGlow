package com.example.smartglow_v2.presenter.main

import com.example.smartglow_v2.base.BaseView
import com.example.smartglow_v2.model.SensorData

interface DashboardContract {
    interface View : BaseView {
        fun updateStreet1Data(data: SensorData)
        fun updateStreet2Data(data: SensorData)
        fun onStreet1Error(message: String)
        fun onStreet2Error(message: String)
    }

    interface Presenter {
        fun startListening()
        fun stopListening()
        fun onDestroy()
    }
}