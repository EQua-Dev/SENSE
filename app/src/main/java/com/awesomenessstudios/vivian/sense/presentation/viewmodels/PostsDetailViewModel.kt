package com.awesomenessstudios.vivian.sense.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import com.awesomenessstudios.vivian.sense.domain.usecases.analytics.AnalyticsUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.AnalyticsSection
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.AnalyticsUiEvent
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.AnalyticsUiState
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.PostsDetailUiEvent
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.PostsDetailUiState
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.PostsSortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostsDetailViewModel @Inject constructor(
    private val analyticsUseCase: AnalyticsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _postsDetailState = MutableStateFlow(PostsDetailUiState())
    val postsDetailState: StateFlow<PostsDetailUiState> = _postsDetailState.asStateFlow()

    fun onEvent(event: PostsDetailUiEvent) {
        when (event) {
            is PostsDetailUiEvent.LoadPostsDetail -> loadPostsDetail()
            is PostsDetailUiEvent.ChangeSorting -> changeSorting(event.sortType)
        }
    }

    private fun loadPostsDetail() {
        viewModelScope.launch {
            _postsDetailState.value = _postsDetailState.value.copy(isLoading = true, error = null)

            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _postsDetailState.value = _postsDetailState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }

                currentUser.fold(onSuccess = { user ->
                    val posts = analyticsUseCase.getDetailedPostsAnalytics(user!!.id)
                    val analytics = analyticsUseCase.getPostsAnalytics(user.id)

                    val sortedPosts = sortPosts(posts, _postsDetailState.value.sortedBy)

                    _postsDetailState.value = _postsDetailState.value.copy(
                        isLoading = false,
                        posts = sortedPosts,
                        analytics = analytics
                    )
                }, onFailure = { e ->
                    _postsDetailState.value = _postsDetailState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                })

            } catch (e: Exception) {
                _postsDetailState.value = _postsDetailState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun changeSorting(sortType: PostsSortType) {
        val currentState = _postsDetailState.value
        val sortedPosts = sortPosts(currentState.posts, sortType)

        _postsDetailState.value = currentState.copy(
            sortedBy = sortType,
            posts = sortedPosts
        )
    }

    private fun sortPosts(posts: List<SensePost>, sortType: PostsSortType): List<SensePost> {
        return when (sortType) {
            PostsSortType.MOST_RECENT -> posts.sortedByDescending { it.createdAt }
            PostsSortType.MOST_LIKED -> posts.sortedByDescending { it.likeCount }
            PostsSortType.MOST_COMMENTED -> posts.sortedByDescending { it.commentCount }
            PostsSortType.BEST_SENTIMENT -> posts.sortedByDescending {
                it.likeCount + it.commentCount + it.shareCount
            }
        }
    }
}
