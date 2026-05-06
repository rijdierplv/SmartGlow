package com.example.smartglow_v2.model

data class User(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis()
)