package com.awesomenessstudios.vivian.sense.data.remote.impl


import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.data.remote.PostDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePostDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : PostDataSource {

    override suspend fun createPost(post: SensePost): Result<SensePost> {
        return try {
            val postId = UUID.randomUUID().toString()
            val newPost = post.copy(
                id = postId,
                createdAt = Date(),
                updatedAt = Date()
            )

            firestore.collection("posts")
                .document(postId)
                .set(newPost)
                .await()

            Result.success(newPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePost(post: SensePost): Result<SensePost> {
        return try {
            val updatedPost = post.copy(updatedAt = Date())

            firestore.collection("posts")
                .document(post.id)
                .set(updatedPost)
                .await()

            Result.success(updatedPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            firestore.collection("posts")
                .document(postId)
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPost(postId: String): Result<SensePost> {
        return try {
            val document = firestore.collection("posts")
                .document(postId)
                .get()
                .await()

            val post = document.toObject(SensePost::class.java)
                ?: throw Exception("Post not found")

            // Fetch user data
            val userDocument = firestore.collection("users")
                .document(post.userId)
                .get()
                .await()

            val user = userDocument.toObject(SenseUser::class.java)
            val postWithUser = post.copy(user = user)

            Result.success(postWithUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPostsFlow(): Flow<List<SensePost>> = callbackFlow {
        val listener = firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SensePost::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(posts)
            }

        awaitClose { listener.remove() }
    }

    override fun getUserPostsFlow(userId: String): Flow<List<SensePost>> = callbackFlow {
        val listener = firestore.collection("posts")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SensePost::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(posts)
            }

        awaitClose { listener.remove() }
    }

    override suspend fun uploadImage(imageUri: String, postId: String): Result<String> {
        return try {
            val imageRef = storage.reference.child("post_images/$postId.jpg")
            val uploadTask = imageRef.putFile(android.net.Uri.parse(imageUri)).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
