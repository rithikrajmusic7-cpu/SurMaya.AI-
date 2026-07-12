package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val token: String?,
    val avatarUrl: String?,
    val subscriptionPlan: String, // "free", "pro", "premier"
    val creditsRemaining: Int,
    val isLoggedIn: Boolean = false
)
