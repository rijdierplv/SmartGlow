package com.example.smartglow_v2.model.repository

import android.content.Context
import com.example.smartglow_v2.model.User
import com.example.smartglow_v2.utils.Constants
import com.example.smartglow_v2.utils.clearSession
import com.example.smartglow_v2.utils.encodeEmail
import com.example.smartglow_v2.utils.getCurrentEmail
import com.example.smartglow_v2.utils.getSharedPrefs
import com.example.smartglow_v2.utils.saveSession
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class AuthRepository(private val context: Context) {

    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance(Constants.FIREBASE_DB_URL).reference
    }

    private val usersRef: DatabaseReference by lazy {
        database.child(Constants.PATH_USERS)
    }

    fun isLoggedIn(): Boolean = context.getCurrentEmail() != null

    fun getCurrentUserEmail(): String? = context.getCurrentEmail()

    fun getUsername(): String =
        context.getSharedPrefs().getString(Constants.KEY_USERNAME, "User") ?: "User"

    fun logout() {
        context.clearSession()
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val emailKey = email.encodeEmail()
            val snapshot = usersRef.child(emailKey).get().await()

            if (!snapshot.exists()) {
                Result.failure(Exception("Invalid Email or Password"))
            } else {
                val savedPassword = snapshot.child("password").getValue(String::class.java)
                val username = snapshot.child("username").getValue(String::class.java) ?: "User"

                if (password == savedPassword) {
                    context.saveSession(email, username)
                    Result.success(User(username, email, password))
                } else {
                    Result.failure(Exception("Invalid Email or Password"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, username: String, password: String): Result<User> {
        return try {
            val emailKey = email.encodeEmail()
            val snapshot = usersRef.child(emailKey).get().await()

            if (snapshot.exists()) {
                Result.failure(Exception("Email already registered"))
            } else {
                val userData = User(username, email, password)
                usersRef.child(emailKey).setValue(userData).await()
                context.saveSession(email, username)
                Result.success(userData)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val email = context.getCurrentEmail()
            if (email != null) {
                val emailKey = email.encodeEmail()
                usersRef.child(emailKey).removeValue().await()
                logout()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUsername(newUsername: String): Result<Unit> {
        return try {
            val email = context.getCurrentEmail()?: return Result.failure(Exception("No user logged in"))
            val emailKey = email.encodeEmail()
            usersRef.child(emailKey).child("username").setValue(newUsername).await()
            context.saveSession(email, newUsername)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
