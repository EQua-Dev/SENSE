package com.awesomenessstudios.vivian.sense.data.repository


import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.remote.PostDataSource
import com.awesomenessstudios.vivian.sense.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDataSource: PostDataSource
) : PostRepository {

    override suspend fun createPost(post: SensePost): Result<SensePost> {
        return postDataSource.createPost(post)
    }

    override suspend fun updatePost(post: SensePost): Result<SensePost> {
        return postDataSource.updatePost(post)
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return postDataSource.deletePost(postId)
    }

    override suspend fun getPost(postId: String): Result<SensePost> {
        return postDataSource.getPost(postId)
    }

    override fun getPostsFlow(): Flow<List<SensePost>> {
        return postDataSource.getPostsFlow()
    }

    override fun getUserPostsFlow(userId: String): Flow<List<SensePost>> {
        return postDataSource.getUserPostsFlow(userId)
    }

    override suspend fun likePost(postId: String): Result<Unit> {
        return postDataSource.likePost(postId)
    }

    suspend fun uploadImage(imageUri: String, postId: String): Result<String> {
        return postDataSource.uploadImage(imageUri, postId)
    }
}