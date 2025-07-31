package com.awesomenessstudios.vivian.sense.domain.usecases.comments


import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.domain.repository.CommentRepository
import com.awesomenessstudios.vivian.sense.domain.repository.PostRepository
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(
        commentText: String,
        postId: String,
        parentCommentId: String?
    ): Result<Unit> {
        return commentRepository.writeComment(commentText, postId, parentCommentId)
    }
}

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    operator fun invoke(postId: String) = commentRepository.getCommentsFlow(postId)
}

class GetUserCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(userId: String) = commentRepository.getCommentsByUser(userId)
}

class LikePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return postRepository.likePost(postId)
    }
}

class UpdateCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(comment: SenseComment): Result<SenseComment> {
        return commentRepository.updateComment(comment)
    }
}


class DeleteCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(commentId: String): Result<Unit> {
        return commentRepository.deleteComment(commentId)
    }
}
