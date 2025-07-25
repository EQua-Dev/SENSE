package com.awesomenessstudios.vivian.sense.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.SignInUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.SignInState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignInState())
    val state: StateFlow<SignInState> = _state.asStateFlow()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun signIn() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.value = currentState.copy(error = "Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            signInUseCase(currentState.email, currentState.password).fold(
                onSuccess = {
                    _state.value = currentState.copy(isLoading = false)
                },
                onFailure = { error ->
                    _state.value = currentState.copy(
                        isLoading = false,
                        error = error.message ?: "Sign up failed"
                    )
                }
            )
        }
    }
}