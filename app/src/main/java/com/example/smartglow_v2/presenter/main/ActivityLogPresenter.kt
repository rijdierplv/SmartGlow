package com.example.smartglow_v2.presenter.main

import com.example.smartglow_v2.model.LogEntry
import com.example.smartglow_v2.model.repository.SensorRepository
import com.example.smartglow_v2.utils.Constants
import com.google.firebase.database.ValueEventListener

class ActivityLogPresenter(
    private val view: ActivityLogContract.View
) : ActivityLogContract.Presenter {

    private val repository = SensorRepository()
    private var logs1Listener: ValueEventListener? = null
    private var logs2Listener: ValueEventListener? = null
    private var street1Logs = emptyList<LogEntry>()
    private var street2Logs = emptyList<LogEntry>()

    override fun startListening() {
        logs1Listener = repository.listenToLogs(
            Constants.PATH_LOGS_1,
            "Street 1"
        ) { result ->
            result.fold(
                onSuccess = { logs ->
                    street1Logs = logs
                    mergeAndShowLogs()
                },
                onFailure = { view.showError("Street 1: ${it.message}") }
            )
        }

        logs2Listener = repository.listenToLogs(
            Constants.PATH_LOGS_2,
            "Street 2"
        ) { result ->
            result.fold(
                onSuccess = { logs ->
                    street2Logs = logs
                    mergeAndShowLogs()
                },
                onFailure = { view.showError("Street 2: ${it.message}") }
            )
        }
    }

    private fun mergeAndShowLogs() {
        val merged = (street1Logs + street2Logs)
            .sortedByDescending { it.datetime }

        if (merged.isEmpty()) {
            view.showEmptyState()
        } else {
            view.showLogs(merged)
        }
    }

    override fun stopListening() {
        logs1Listener?.let {
            repository.removeListener(repository.getLogs1Ref(), it)
        }
        logs2Listener?.let {
            repository.removeListener(repository.getLogs2Ref(), it)
        }
        logs1Listener = null
        logs2Listener = null
    }

    override fun onDestroy() {
        stopListening()
    }
}