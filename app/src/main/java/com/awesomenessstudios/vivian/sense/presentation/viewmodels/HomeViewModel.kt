package com.awesomenessstudios.vivian.sense.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.GetCurrentUserUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.posts.GetPostsUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.posts.LikePostUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.home.HomeUiEvent
import com.awesomenessstudios.vivian.sense.presentation.ui.home.HomeUiState
import com.awesomenessstudios.vivian.sense.presentation.ui.post.PostsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getPostsUseCase: GetPostsUseCase,
    private val likePostUseCase: LikePostUseCase
) : ViewModel() {

    private val _homeState = MutableStateFlow(HomeUiState())
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()


    private val _postsState = MutableStateFlow<PostsUiState>(PostsUiState.Loading)
    val postsState: StateFlow<PostsUiState> = _postsState.asStateFlow()

    init {
        getCurrentUser()
        loadPosts()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.OnLikeClicked -> {
                likePost(event.postId)

            }
        }
    }


    private fun loadPosts() {
        viewModelScope.launch {
            _postsState.value = PostsUiState.Loading
            getPostsUseCase()
                .catch { error ->
                    _postsState.value = PostsUiState.Error(error.message ?: "Failed to load posts")
                }
                .collect { posts ->
                    _postsState.value = PostsUiState.Success(posts)
                }
        }
    }

    private fun likePost(postId: String) {
        viewModelScope.launch {
            val result = likePostUseCase(postId)
            if (result.isSuccess) {
                refreshPosts()
            } else {
                // Optionally show error or log it
                val error = result.exceptionOrNull()
                // Handle error (e.g., log or show UI message)
            }
        }
    }

    private fun refreshPosts() {
        loadPosts()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {

            getCurrentUserUseCase.invoke().fold(onSuccess = { currentUser ->
                _homeState.update { it.copy(currentUser = currentUser) }
            }, onFailure = { error -> _homeState.update { it.copy(error = error.message) } })
        }
    }

}