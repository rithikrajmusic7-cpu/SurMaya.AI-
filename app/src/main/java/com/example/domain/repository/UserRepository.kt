package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getActiveUserFlow(): Flow<User?>
    suspend fun getActiveUser(): User?
    suspend fun login(email: String, password: String): Result<User>
    suspend fun loginWithGoogle(idToken: String): Result<User>
    suspend fun register(displayName: String, email: String, password: String): Result<User>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun guestLogin(): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun updateUserCredits(credits: Int): Result<Unit>
    suspend fun upgradeSubscription(planName: String): Result<Unit>
}
