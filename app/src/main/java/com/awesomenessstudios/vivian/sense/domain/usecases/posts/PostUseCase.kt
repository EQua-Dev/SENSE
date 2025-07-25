package com.awesomenessstudios.vivian.sense.domain.usecases.posts


import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.domain.repository.PostRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(post: SensePost): Result<SensePost> {
        return postRepository.createPost(post)
    }
}

class GetPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke() = postRepository.getPostsFlow()
}

class GetUserPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    operator fun invoke(userId: String) = postRepository.getUserPostsFlow(userId)
}

class DeletePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return postRepository.deletePost(postId)
    }
}
