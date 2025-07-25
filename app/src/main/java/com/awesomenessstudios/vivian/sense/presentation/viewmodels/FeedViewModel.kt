package com.awesomenessstudios.vivian.sense.presentation.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.GetCurrentUserUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.posts.GetPostsUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.post.PostsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
) : ViewModel() {

    private val _postsState = MutableStateFlow<PostsUiState>(PostsUiState.Loading)
    val postsState: StateFlow<PostsUiState> = _postsState.asStateFlow()

    init {
        loadPosts()
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

    fun refreshPosts() {
        loadPosts()
    }
}
