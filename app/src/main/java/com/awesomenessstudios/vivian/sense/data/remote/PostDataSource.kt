package com.awesomenessstudios.vivian.sense.data.remote

import com.awesomenessstudios.vivian.sense.data.models.SensePost
import kotlinx.coroutines.flow.Flow

interface PostDataSource {
    suspend fun createPost(post: SensePost): Result<SensePost>
    suspend fun updatePost(post: SensePost): Result<SensePost>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun getPost(postId: String): Result<SensePost>
    fun getPostsFlow(): Flow<List<SensePost>>
    fun getUserPostsFlow(userId: String): Flow<List<SensePost>>
    suspend fun uploadImage(imageUri: String, postId: String): Result<String>
}