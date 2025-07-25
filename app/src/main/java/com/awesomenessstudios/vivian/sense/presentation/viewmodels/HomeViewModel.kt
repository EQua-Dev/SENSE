package com.awesomenessstudios.vivian.sense.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.GetCurrentUserUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.home.HomeUiState
import com.awesomenessstudios.vivian.sense.presentation.ui.post.PostsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {

            getCurrentUserUseCase.invoke().fold(onSuccess = { currentUser ->
                _homeState.update { it.copy(currentUser = currentUser) }
            }, onFailure = { error -> _homeState.update { it.copy(error = error.message) } })
        }
    }

}