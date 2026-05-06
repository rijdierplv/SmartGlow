package com.example.smartglow_v2.presenter.main

import com.example.smartglow_v2.base.BaseView
import com.example.smartglow_v2.model.SensorData

interface ControlContract {
    interface View : BaseView {
        fun updateStreet1State(data: SensorData)
        fun updateStreet2State(data: SensorData)
        fun showOverrideRequired()
        fun onControlSuccess(message: String)
        fun onControlError(message: String)
    }

    interface Presenter {
        fun startListening()
        fun stopListening()

        fun toggleManualOverride(enabled: Boolean)
        fun toggleStreetLight(street: Int, isOn: Boolean)
        fun setBrightness(street: Int, brightness: Int)
        fun setPresetBrightness(street: Int, preset: BrightnessPreset)

        fun onDestroy()
    }

    enum class BrightnessPreset {
        FULL, DIM, OFF
    }
}