package com.awesomenessstudios.vivian.sense.presentation.viewmodels


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.domain.usecases.auth.GetCurrentUserUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.comments.CreateCommentUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.comments.GetCommentsUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.posts.GetPostDetailUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.posts.GetPostsUseCase
import com.awesomenessstudios.vivian.sense.domain.usecases.posts.LikePostUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.post.PostDetailUiEvent
import com.awesomenessstudios.vivian.sense.presentation.ui.post.PostDetailUiState
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
class PostDetailViewModel @Inject constructor(
    private val getPostDetailUseCase: GetPostDetailUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val likePostUseCase: LikePostUseCase
) : ViewModel() {

    private val _postDetailState = MutableStateFlow(PostDetailUiState())
    val postDetailState: StateFlow<PostDetailUiState> = _postDetailState.asStateFlow()

    fun onPostDetailEvent(event: PostDetailUiEvent) {
        when (event) {
            is PostDetailUiEvent.WriteComment -> {
                writeComment(event.commentText, event.postId, event.parentCommentId)
            }

            is PostDetailUiEvent.LikePost -> {
                likePost(event.postId)
            }

            is PostDetailUiEvent.LoadPostComments -> {
                getComments(event.postId)
            }

            is PostDetailUiEvent.LoadPostDetail -> {
                loadPostDetail(event.postId)
            }

            is PostDetailUiEvent.OnCommentTextChanged -> {
                _postDetailState.update { it.copy(commentText = event.commentText) }
            }

            is PostDetailUiEvent.DeletePost -> {
                TODO()
            }

            PostDetailUiEvent.GetCurrentUser -> {
                getCurrentUser()
            }
        }
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            val result = getCurrentUserUseCase()
            when {
                result.isSuccess -> {
                    val user = result.getOrNull()
                    _postDetailState.update { it.copy(currentUser = user) }
                }

                result.isFailure -> {
                    // Optional: log error or show UI feedback
                    val exception = result.exceptionOrNull()
                    Log.e("GetCurrentUser", "Failed to fetch user", exception)
                }
            }

        }
    }

    private fun loadPostDetail(postId: String) {
        viewModelScope.launch {
            _postDetailState.update { it.copy(postDetailLoading = true) }
            getPostDetailUseCase(postId)
                .catch { error ->
                    _postDetailState.update {
                        it.copy(
                            postDetailLoading = false,
                            postDetailError =
                                error.message ?: "Failed to load posts"

                        )
                    }
                }
                .collect { post ->
                    _postDetailState.update {
                        it.copy(
                            postDetailLoading = false,
                            postDetail = post
                        )
                    }
                }
        }
    }

    private fun getComments(postId: String) {
        viewModelScope.launch {
            _postDetailState.update { it.copy(commentsLoading = true) }
            getCommentsUseCase(postId)
                .catch { error ->
                    _postDetailState.update {
                        it.copy(
                            commentsLoading = false,
                            commentsError =
                                error.message ?: "Failed to load comments"

                        )
                    }
                }
                .collect { postComments ->
                    _postDetailState.update {
                        it.copy(
                            commentsLoading = false,
                            comments = postComments
                        )
                    }
                }
        }
    }

    private fun refreshPostDetail(postId: String) {
        loadPostDetail(postId)
        getComments(postId)
    }

    private fun writeComment(
        commentText: String,
        postId: String,
        parentCommentId: String?
    ) {
        viewModelScope.launch {
            val result = createCommentUseCase.invoke(
                commentText, postId, parentCommentId
            )

            when {
                result.isSuccess -> {
                    refreshPostDetail(postId)

                }

                result.isFailure -> {

                }
            }
        }
    }

    private fun likePost(postId: String) {
        viewModelScope.launch {
            val result = likePostUseCase(postId)
            if (result.isSuccess) {
                refreshPostDetail(postId)
            } else {
                // Optionally show error or log it
                val error = result.exceptionOrNull()
                // Handle error (e.g., log or show UI message)
            }
        }
    }

}
