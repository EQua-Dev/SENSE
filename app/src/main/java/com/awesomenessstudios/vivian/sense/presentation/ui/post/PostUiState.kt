package com.awesomenessstudios.vivian.sense.presentation.ui.post


import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost

data class CreatePostState(
    val content: String = "",
    val selectedImageUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class PostsUiState {
    data object Loading : PostsUiState()
    data class Success(val posts: List<SensePost>) : PostsUiState()
    data class Error(val message: String) : PostsUiState()
}