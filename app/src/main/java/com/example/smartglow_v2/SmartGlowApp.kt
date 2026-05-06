package com.example.smartglow_v2

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class SmartGlowApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Enable Firebase offline persistence
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}