package com.example.smartglow_v2.presenter.main

import com.example.smartglow_v2.model.SensorData
import com.example.smartglow_v2.model.repository.SensorRepository
import com.example.smartglow_v2.utils.Constants
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ControlPresenter(
    private val view: ControlContract.View
) : ControlContract.Presenter {

    private val repository = SensorRepository()
    private val scope = CoroutineScope(Dispatchers.Main)

    private var street1Listener: ValueEventListener? = null
    private var street2Listener: ValueEventListener? = null
    private var isManualOverride = false
    private var street1Data = SensorData()
    private var street2Data = SensorData()

    override fun startListening() {
        street1Listener = repository.listenToStreet(Constants.PATH_STREET_1) { result ->
            result.fold(
                onSuccess = { data ->
                    street1Data = data
                    isManualOverride = data.isManualMode
                    view.updateStreet1State(data)
                },
                onFailure = { view.onControlError(it.message ?: "Error loading Street 1") }
            )
        }

        street2Listener = repository.listenToStreet(Constants.PATH_STREET_2) { result ->
            result.fold(
                onSuccess = { data ->
                    street2Data = data
                    view.updateStreet2State(data)
                },
                onFailure = { view.onControlError(it.message ?: "Error loading Street 2") }
            )
        }
    }

    override fun stopListening() {
        street1Listener?.let {
            repository.removeListener(repository.getStreet1Ref(), it)
        }
        street2Listener?.let {
            repository.removeListener(repository.getStreet2Ref(), it)
        }
        street1Listener = null
        street2Listener = null
    }

    override fun toggleManualOverride(enabled: Boolean) {
        isManualOverride = enabled
        val mode = if (enabled) "MANUAL" else "AUTO"

        scope.launch {
            val result1 = withContext(Dispatchers.IO) {
                repository.setStreetMode(Constants.PATH_STREET_1, mode)
            }
            val result2 = withContext(Dispatchers.IO) {
                repository.setStreetMode(Constants.PATH_STREET_2, mode)
            }

            val logStatus = if (enabled) "MANUAL MODE" else "AUTO MODE"

            withContext(Dispatchers.IO) {
                repository.saveLog(Constants.PATH_LOGS_1, logStatus, false, street1Data.brightness)
                repository.saveLog(Constants.PATH_LOGS_2, logStatus, false, street2Data.brightness)
            }

            if (result1.isSuccess && result2.isSuccess) {
                view.onControlSuccess(if (enabled) "Manual mode enabled" else "Auto mode enabled")
            } else {
                view.onControlError("Failed to change mode")
            }
        }
    }

    override fun toggleStreetLight(street: Int, isOn: Boolean) {
        if (!isManualOverride) {
            view.showOverrideRequired()
            return
        }

        val brightness = if (isOn) 100 else 0
        val path = if (street == 1) Constants.PATH_STREET_1 else Constants.PATH_STREET_2
        val logPath = if (street == 1) Constants.PATH_LOGS_1 else Constants.PATH_LOGS_2

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.setStreetLight(path, isOn, brightness)
            }
            withContext(Dispatchers.IO) {
                repository.saveLog(logPath, if (isOn) "LIGHT ON" else "LIGHT OFF", false, brightness)
            }
            result.fold(
                onSuccess = { view.onControlSuccess("Street $street ${if (isOn) "ON" else "OFF"}") },
                onFailure = { view.onControlError(it.message ?: "Failed to toggle light") }
            )
        }
    }

    override fun setBrightness(street: Int, brightness: Int) {
        if (!isManualOverride) {
            view.showOverrideRequired()
            return
        }

        val isOn = brightness > 0
        val path = if (street == 1) Constants.PATH_STREET_1 else Constants.PATH_STREET_2
        val logPath = if (street == 1) Constants.PATH_LOGS_1 else Constants.PATH_LOGS_2

        scope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.setStreetLight(path, isOn, brightness)
            }
            val status = when (brightness) {
                100 -> "FULL BRIGHTNESS"
                0 -> "LIGHT OFF"
                else -> "BRIGHTNESS CHANGED"
            }
            withContext(Dispatchers.IO) {
                repository.saveLog(logPath, status, false, brightness)
            }
            result.fold(
                onSuccess = { view.onControlSuccess("Brightness: $brightness%") },
                onFailure = { view.onControlError(it.message ?: "Failed to set brightness") }
            )
        }
    }

    override fun setPresetBrightness(street: Int, preset: ControlContract.BrightnessPreset) {
        val (brightness, status) = when (preset) {
            ControlContract.BrightnessPreset.FULL -> 100 to "FULL BRIGHTNESS"
            ControlContract.BrightnessPreset.DIM -> 40 to "DIM"
            ControlContract.BrightnessPreset.OFF -> 0 to "LIGHT OFF"
        }
        setBrightness(street, brightness)
    }

    override fun onDestroy() {
        stopListening()
    }
}