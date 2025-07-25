package com.awesomenessstudios.vivian.sense.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.GetCurrentUserUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.SignOutUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    init {
        checkAuthState()
        observeAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            getCurrentUserUseCase().fold(
                onSuccess = { user ->
                    _authState.value = if (user != null) {
                        AuthUiState.Authenticated(user)
                    } else {
                        AuthUiState.Unauthenticated
                    }
                },
                onFailure = { error ->
                    _authState.value = AuthUiState.Error(error.message ?: "Unknown error")
                }
            )
        }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUserFlow().collect { user ->
                _authState.value = if (user != null) {
                    AuthUiState.Authenticated(user)
                } else {
                    AuthUiState.Unauthenticated
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
        }
    }
}
