package com.awesomenessstudios.vivian.sense.domain.repository

import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {


    suspend fun writeComment(commentText: String,
                             postId: String,
                             parentCommentId: String?): Result<Unit>
    fun getCommentsFlow(postId: String): Flow<List<SenseComment>>


//    suspend fun createComment(comment: SenseComment): Result<SenseComment>
    suspend fun updateComment(comment: SenseComment): Result<SenseComment>
    suspend fun deleteComment(commentId: String): Result<Unit>
//    fun getCommentsFlow(postId: String): Flow<List<SenseComment>>
    suspend fun getCommentsByUser(userId: String): Result<List<SenseComment>>
}