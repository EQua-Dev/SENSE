package com.awesomenessstudios.vivian.sense.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import com.awesomenessstudios.vivian.sense.domain.usecases.posts.CreatePostUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.post.CreatePostState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    private var currentUser: SenseUser? = null

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    currentUser = user
                },
                onFailure = {
                    _state.value = _state.value.copy(error = "User not found")
                }
            )
        }
    }

    fun updateContent(content: String) {
        _state.value = _state.value.copy(content = content, error = null)
    }

    fun updateSelectedImage(imageUri: String?) {
        _state.value = _state.value.copy(selectedImageUri = imageUri, error = null)
    }

    fun createPost() {
        val currentState = _state.value
        val user = currentUser

        if (user == null) {
            _state.value = currentState.copy(error = "User not authenticated")
            return
        }

        if (currentState.content.isBlank()) {
            _state.value = currentState.copy(error = "Post content cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = currentState.copy(isLoading = true, error = null)

            val post = SensePost(
                userId = user.id,
                content = currentState.content,
                imageUrl = currentState.selectedImageUri ?: "",
                user = user
            )

            createPostUseCase(post).fold(
                onSuccess = {
                    _state.value = CreatePostState(isSuccess = true)
                },
                onFailure = { error ->
                    _state.value = currentState.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create post"
                    )
                }
            )
        }
    }

    fun resetState() {
        _state.value = CreatePostState()
    }
}
