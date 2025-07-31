package com.awesomenessstudios.vivian.sense.presentation.ui.post

import com.awesomenessstudios.vivian.sense.data.models.SenseComment

sealed class PostDetailUiEvent {
    data class LoadPostDetail(val postId: String) : PostDetailUiEvent()
    data class OnCommentTextChanged(val commentText: String) : PostDetailUiEvent()
    data class LoadPostComments(val postId: String) : PostDetailUiEvent()
    data class LikePost(val postId: String) : PostDetailUiEvent()
    data object GetCurrentUser : PostDetailUiEvent()
    data class DeletePost(val postId: String) : PostDetailUiEvent()
    data class WriteComment(
        val commentText: String,
        val postId: String,
        val parentCommentId: String?
    ) : PostDetailUiEvent()
}