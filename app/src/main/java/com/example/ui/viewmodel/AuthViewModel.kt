package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import com.example.core.security.DeveloperSessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.Context

class AuthViewModel(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("surmaya_dev_prefs", Context.MODE_PRIVATE)

    private val _isDevMode = MutableStateFlow(false)
    val isDevMode = _isDevMode.asStateFlow()

    private val _devDaysRemaining = MutableStateFlow(0L)
    val devDaysRemaining = _devDaysRemaining.asStateFlow()

    init {
        refreshDevModeState()
    }

    fun refreshDevModeState() {
        val sessionManager = DeveloperSessionManager.getInstance(getApplication())
        val isEnabled = sessionManager.isDeveloperAuthenticated()
        _isDevMode.value = isEnabled
        if (isEnabled) {
            var activationTime = prefs.getLong("dev_mode_activation_time", 0L)
            if (activationTime == 0L) {
                activationTime = System.currentTimeMillis()
                prefs.edit().putLong("dev_mode_activation_time", activationTime).apply()
            }
            val currentTime = System.currentTimeMillis()
            val thirtyDaysInMillis = 30L * 24L * 60L * 60L * 1000L
            val remainingMillis = (activationTime + thirtyDaysInMillis) - currentTime
            if (remainingMillis <= 0L) {
                // Auto-disable if expired
                sessionManager.clearSession()
                _isDevMode.value = false
                _devDaysRemaining.value = 0L
            } else {
                // Round up days remaining to ensure accurate display
                val days = (remainingMillis + (24L * 60L * 60L * 1000L - 1)) / (24L * 60L * 60L * 1000L)
                _devDaysRemaining.value = days
            }
        } else {
            _devDaysRemaining.value = 0L
        }
    }

    fun setDevMode(enabled: Boolean) {
        val sessionManager = DeveloperSessionManager.getInstance(getApplication())
        if (enabled) {
            sessionManager.createSession()
            prefs.edit().putLong("dev_mode_activation_time", System.currentTimeMillis()).apply()
        } else {
            sessionManager.clearSession()
        }
        refreshDevModeState()
    }

    fun extendDevMode(password: String): Boolean {
        val sessionManager = DeveloperSessionManager.getInstance(getApplication())
        if (sessionManager.validateCredentials("Developer SurMaya AI 2026", password)) {
            prefs.edit()
                .putLong("dev_mode_activation_time", System.currentTimeMillis())
                .apply()
            sessionManager.createSession()
            refreshDevModeState()
            return true
        }
        return false
    }

    fun simulateExpiration() {
        val pastTime = System.currentTimeMillis() - (31L * 24L * 60L * 60L * 1000L)
        prefs.edit()
            .putLong("dev_mode_activation_time", pastTime)
            .apply()
        refreshDevModeState()
    }

    val currentUser: StateFlow<User?> = userRepository.getActiveUserFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun login(email: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.login(email, "password123")
            onResult(result)
        }
    }

    fun loginWithGoogle(onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.loginWithGoogle("google_oauth_token")
            onResult(result)
        }
    }

    fun register(displayName: String, email: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.register(displayName, email, "password123")
            onResult(result)
        }
    }

    fun forgotPassword(email: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.forgotPassword(email)
            onResult(result)
        }
    }

    fun guestLogin(onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = userRepository.guestLogin()
            onResult(result)
        }
    }

    fun logout(onResult: () -> Unit = {}) {
        viewModelScope.launch {
            userRepository.logout()
            onResult()
        }
    }

    fun upgradeSubscription(plan: String) {
        viewModelScope.launch {
            userRepository.upgradeSubscription(plan)
        }
    }

    fun updateUserCredits(credits: Int) {
        viewModelScope.launch {
            userRepository.updateUserCredits(credits)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                val repo = ServiceLocator.getUserRepository(application)
                return AuthViewModel(application, repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
