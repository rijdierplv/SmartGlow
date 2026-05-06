package com.example.smartglow_v2.presenter.main

import com.example.smartglow_v2.model.repository.SensorRepository
import com.example.smartglow_v2.utils.Constants
import com.google.firebase.database.ValueEventListener

class DashboardPresenter(
    private val view: DashboardContract.View
) : DashboardContract.Presenter {

    private val repository = SensorRepository()
    private var street1Listener: ValueEventListener? = null
    private var street2Listener: ValueEventListener? = null

    override fun startListening() {
        street1Listener = repository.listenToStreet(Constants.PATH_STREET_1) { result ->
            result.fold(
                onSuccess = { view.updateStreet1Data(it) },
                onFailure = { view.onStreet1Error(it.message ?: "Error loading Street 1") }
            )
        }

        street2Listener = repository.listenToStreet(Constants.PATH_STREET_2) { result ->
            result.fold(
                onSuccess = { view.updateStreet2Data(it) },
                onFailure = { view.onStreet2Error(it.message ?: "Error loading Street 2") }
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

    override fun onDestroy() {
        stopListening()
    }
}