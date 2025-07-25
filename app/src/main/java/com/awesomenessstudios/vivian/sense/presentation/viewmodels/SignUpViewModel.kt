package com.awesomenessstudios.vivian.sense.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.SignUpUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.SignUpState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignUpState())
    val state: StateFlow<SignUpState> = _state.asStateFlow()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun updateDisplayName(displayName: String) {
        _state.value = _state.value.copy(displayName = displayName, error = null)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun signUp() {
        val currentState = _state.value

        when {
            currentState.email.isBlank() -> {
                _state.value = currentState.copy(error = "Email is required")
                return
            }
            currentState.password.isBlank() -> {
                _state.value = currentState.copy(error = "Password is required")
                return
            }
            currentState.displayName.isBlank() -> {
                _state.value = currentState.copy(error = "Display name is required")
                return
            }
            currentState.password != currentState.confirmPassword -> {
                _state.value = currentState.copy(error = "Passwords do not match")
                return
            }
            currentState.password.length < 6 -> {
                _state.value = currentState.copy(error = "Password must be at least 6 characters")
                return
            }
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            signUpUseCase(
                currentState.email,
                currentState.password,
                currentState.displayName
            ).fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = null
                    )
                    // You may also want to add a `success` or `isRegistered` flag here if needed
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error.message ?: "Sign up failed"
                    )
                }
            )
        }
    }
}
