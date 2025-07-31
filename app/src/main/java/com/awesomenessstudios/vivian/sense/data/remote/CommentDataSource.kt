package com.awesomenessstudios.vivian.sense.data.remote

import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import kotlinx.coroutines.flow.Flow

interface CommentDataSource {

    suspend fun writeComment(  commentText: String,
                               postId: String,
                               parentCommentId: String? = null): Result<Unit>
    fun getCommentsFlow(postId: String): Flow<List<SenseComment>>

    //    suspend fun createComment(comment: SenseComment): Result<SenseComment>
    suspend fun updateComment(comment: SenseComment): Result<SenseComment>
    suspend fun deleteComment(commentId: String): Result<Unit>
    //    fun getCommentsFlow(postId: String): Flow<List<SenseComment>>
    suspend fun getCommentsByUser(userId: String): Result<List<SenseComment>>
}