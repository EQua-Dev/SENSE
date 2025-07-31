package com.awesomenessstudios.vivian.sense.data.repository


import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.remote.CommentDataSource
import com.awesomenessstudios.vivian.sense.data.remote.PostDataSource
import com.awesomenessstudios.vivian.sense.domain.repository.CommentRepository
import com.awesomenessstudios.vivian.sense.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentDataSource: CommentDataSource
) : CommentRepository {
    override suspend fun writeComment(
        commentText: String,
        postId: String,
        parentCommentId: String?
    ): Result<Unit> {
        return commentDataSource.writeComment(commentText, postId, parentCommentId)
    }

    override fun getCommentsFlow(postId: String): Flow<List<SenseComment>> {
        return commentDataSource.getCommentsFlow(postId)
    }

    override suspend fun updateComment(comment: SenseComment): Result<SenseComment> {
        return commentDataSource.updateComment(comment)
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> {
        return commentDataSource.deleteComment(commentId)
    }

    override suspend fun getCommentsByUser(userId: String): Result<List<SenseComment>> {
        return commentDataSource.getCommentsByUser(userId)
    }

}