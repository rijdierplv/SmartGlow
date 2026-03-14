package com.example.smartglow_v2

// Replace '.' so the email is safe to use as a Firebase key
fun encodeEmail(email: String): String {
    return email.replace(".", ",")
}

// Realtime Database URL for this Firebase project.
// If you created a different database instance, copy its URL from
// Firebase console → Realtime Database → Data and update this value.
const val FIREBASE_DB_URL = "https://eldroidproject-94df3-default-rtdb.firebaseio.com"


