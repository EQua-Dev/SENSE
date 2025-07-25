package com.awesomenessstudios.vivian.sense.presentation.ui.auth

import com.awesomenessstudios.vivian.sense.data.models.SenseUser

sealed class AuthUiState {
    data object Loading : AuthUiState()
    data object Unauthenticated : AuthUiState()
    data class Authenticated(val user: SenseUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)