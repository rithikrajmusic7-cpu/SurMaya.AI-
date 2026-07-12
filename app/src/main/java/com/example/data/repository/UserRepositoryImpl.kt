package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.example.data.mapper.toDomain
import com.example.data.mapper.toEntity
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val context: Context
) : UserRepository {

    private val prefs = context.getSharedPreferences("surmaya_dev_prefs", Context.MODE_PRIVATE)

    private fun isDeveloperModeActiveAndValid(): Boolean {
        val isEnabled = prefs.getBoolean("dev_mode_enabled", false)
        if (!isEnabled) return false
        
        val activationTime = prefs.getLong("dev_mode_activation_time", 0L)
        val currentTime = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L // 30 days
        
        val isValid = currentTime < (activationTime + thirtyDaysInMillis)
        if (!isValid && isEnabled) {
            // Auto-disable if expired
            prefs.edit().putBoolean("dev_mode_enabled", false).apply()
            return false
        }
        return isValid
    }

    override fun getActiveUserFlow(): Flow<User?> {
        return userDao.getActiveUserFlow().map { entity ->
            val user = entity?.toDomain()
            if (user != null && isDeveloperModeActiveAndValid()) {
                user.copy(
                    subscriptionPlan = "premier",
                    creditsRemaining = 1000000
                )
            } else {
                user
            }
        }
    }

    override suspend fun getActiveUser(): User? {
        val entity = userDao.getActiveUser()
        val user = entity?.toDomain()
        return if (user != null && isDeveloperModeActiveAndValid()) {
            user.copy(
                subscriptionPlan = "premier",
                creditsRemaining = 1000000
            )
        } else {
            user
        }
    }

    override suspend fun login(email: String, password: String): Result<User> {
        if (email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
        }
        val user = User(
            id = UUID.randomUUID().toString(),
            email = email,
            displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() },
            token = "jwt_token_surmaya_${UUID.randomUUID()}",
            avatarUrl = null,
            subscriptionPlan = "free",
            creditsRemaining = 50
        )
        userDao.insertOrUpdateUser(user.toEntity(isLoggedIn = true))
        return Result.success(user)
    }

    override suspend fun loginWithGoogle(idToken: String): Result<User> {
        val user = User(
            id = "google_" + UUID.randomUUID().toString().take(8),
            email = "google_user@gmail.com",
            displayName = "Google Creator",
            token = "google_oauth_token_${UUID.randomUUID()}",
            avatarUrl = "https://lh3.googleusercontent.com/a/default-user=s120",
            subscriptionPlan = "free",
            creditsRemaining = 50
        )
        userDao.insertOrUpdateUser(user.toEntity(isLoggedIn = true))
        return Result.success(user)
    }

    override suspend fun register(displayName: String, email: String, password: String): Result<User> {
        if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("All fields are required"))
        }
        val user = User(
            id = UUID.randomUUID().toString(),
            email = email,
            displayName = displayName,
            token = "jwt_token_surmaya_${UUID.randomUUID()}",
            avatarUrl = null,
            subscriptionPlan = "free",
            creditsRemaining = 50
        )
        userDao.insertOrUpdateUser(user.toEntity(isLoggedIn = true))
        return Result.success(user)
    }

    override suspend fun forgotPassword(email: String): Result<Unit> {
        if (email.isBlank()) {
            return Result.failure(IllegalArgumentException("Email is required"))
        }
        return Result.success(Unit)
    }

    override suspend fun guestLogin(): Result<User> {
        val user = User(
            id = "guest_" + UUID.randomUUID().toString().take(6),
            email = "guest_surmaya@aistudio.com",
            displayName = "Guest Creator",
            token = null,
            avatarUrl = null,
            subscriptionPlan = "free",
            creditsRemaining = 10 // Guest gets 10 credits
        )
        userDao.insertOrUpdateUser(user.toEntity(isLoggedIn = true))
        return Result.success(user)
    }

    override suspend fun logout(): Result<Unit> {
        val active = userDao.getActiveUser()
        if (active != null) {
            userDao.logoutUser(active.id)
            userDao.clearAllUsers()
        }
        return Result.success(Unit)
    }

    override suspend fun updateUserCredits(credits: Int): Result<Unit> {
        val active = userDao.getActiveUser() ?: return Result.failure(IllegalStateException("No active user"))
        val updated = active.copy(creditsRemaining = credits)
        userDao.insertOrUpdateUser(updated)
        return Result.success(Unit)
    }

    override suspend fun upgradeSubscription(planName: String): Result<Unit> {
        val active = userDao.getActiveUser() ?: return Result.failure(IllegalStateException("No active user"))
        val credits = when (planName.lowercase()) {
            "pro" -> 2500
            "premier" -> 10000
            else -> 50
        }
        val updated = active.copy(subscriptionPlan = planName, creditsRemaining = credits)
        userDao.insertOrUpdateUser(updated)
        return Result.success(Unit)
    }
}
