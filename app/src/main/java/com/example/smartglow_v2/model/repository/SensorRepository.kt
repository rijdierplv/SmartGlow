package com.example.smartglow_v2.model.repository

import com.example.smartglow_v2.model.LogEntry
import com.example.smartglow_v2.model.SensorData
import com.example.smartglow_v2.utils.Constants
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await

class SensorRepository {

    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL).reference
    }

    fun getStreet1Ref(): DatabaseReference = database.child(Constants.PATH_STREET_1)
    fun getStreet2Ref(): DatabaseReference = database.child(Constants.PATH_STREET_2)
    fun getLogs1Ref(): DatabaseReference = database.child(Constants.PATH_LOGS_1)
    fun getLogs2Ref(): DatabaseReference = database.child(Constants.PATH_LOGS_2)

    fun listenToStreet(
        path: String,
        onResult: (Result<SensorData>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = SensorData(
                    status = snapshot.child("status").getValue(String::class.java) ?: "",
                    motion = snapshot.child("motion").getValue(Boolean::class.java) ?: false,
                    lightOn = snapshot.child("lightOn").getValue(Boolean::class.java) ?: false,
                    brightness = snapshot.child("brightness").getValue(Int::class.java) ?: 0,
                    mode = snapshot.child("mode").getValue(String::class.java) ?: "AUTO",
                    ldrValue = snapshot.child("ldrValue").getValue(Int::class.java) ?: 0,
                    lightLevel = snapshot.child("lightLevel").getValue(String::class.java) ?: "UNKNOWN",
                    lastUpdate = snapshot.child("lastUpdate").getValue(String::class.java) ?: ""
                )
                onResult(Result.success(data))
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(Result.failure(error.toException()))
            }
        }
        database.child(path).addValueEventListener(listener)
        return listener
    }

    fun listenToLogs(
        path: String,
        streetLabel: String,
        onResult: (Result<List<LogEntry>>) -> Unit
    ): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries = snapshot.children.mapNotNull { child ->
                    LogEntry(
                        datetime = child.child("datetime").getValue(String::class.java) ?: "",
                        status = child.child("status").getValue(String::class.java) ?: return@mapNotNull null,
                        motion = child.child("motion").getValue(Boolean::class.java) ?: false,
                        brightness = child.child("brightness").getValue(Int::class.java),
                        ldrValue = child.child("ldrValue").getValue(Int::class.java),
                        lightLevel = child.child("lightLevel").getValue(String::class.java),
                        street = streetLabel
                    )
                }
                onResult(Result.success(entries))
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(Result.failure(error.toException()))
            }
        }
        database.child(path).addValueEventListener(listener)
        return listener
    }

    fun removeListener(ref: DatabaseReference, listener: ValueEventListener) {
        ref.removeEventListener(listener)
    }

    suspend fun setStreetMode(streetPath: String, mode: String): Result<Unit> {
        return try {
            database.child(streetPath).child("mode").setValue(mode).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setStreetLight(
        streetPath: String,
        isOn: Boolean,
        brightness: Int
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "lightOn" to isOn,
                "brightness" to brightness
            )
            database.child(streetPath).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveLog(
        logPath: String,
        status: String,
        motion: Boolean = false,
        brightness: Int = 0
    ): Result<Unit> {
        return try {
            val dt = java.text.SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss a",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            val logData = mapOf(
                "status" to status,
                "motion" to motion,
                "brightness" to brightness,
                "datetime" to dt
            )
            database.child(logPath).push().setValue(logData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}