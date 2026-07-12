package com.example.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val token: String?,
    val avatarUrl: String?,
    val subscriptionPlan: String, // "free", "pro", "premier"
    val creditsRemaining: Int
)
