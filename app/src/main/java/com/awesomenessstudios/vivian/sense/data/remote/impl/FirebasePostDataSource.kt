package com.awesomenessstudios.vivian.sense.data.remote.impl


import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.data.remote.PostDataSource
import com.awesomenessstudios.vivian.sense.ml.SentimentAnalyzer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePostDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val sentimentAnalyzer: SentimentAnalyzer
) : PostDataSource {

    override suspend fun createPost(post: SensePost): Result<SensePost> {
        return try {
            val postId = UUID.randomUUID().toString()
            val finalImageUrl =
                if (post.imageUrl.isNotEmpty() && !post.imageUrl.startsWith("http")) {
                    val uploadResult = uploadImage(post.imageUrl, postId)
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()
                    } else {
                        throw uploadResult.exceptionOrNull() ?: Exception("Image upload failed")
                    }
                } else {
                    post.imageUrl
                }

            val newPost = post.copy(
                id = postId,
                imageUrl = finalImageUrl ?: "",
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
            // Handle image upload if imageUrl contains a local URI
            val finalImageUrl =
                if (post.imageUrl.isNotEmpty() && !post.imageUrl.startsWith("http")) {
                    // Upload new image and get the download URL
                    val uploadResult = uploadImage(post.imageUrl, post.id)
                    if (uploadResult.isSuccess) {
                        uploadResult.getOrNull()
                    } else {
                        throw uploadResult.exceptionOrNull() ?: Exception("Image upload failed")
                    }
                } else {
                    post.imageUrl // Keep existing URL or empty string
                }

            val updatedPost = post.copy(
                imageUrl = finalImageUrl ?: "",
                updatedAt = Date()
            )

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
            // Optional: Delete associated image from storage
            try {
                storage.reference.child("post_images/$postId.jpg").delete().await()
            } catch (e: Exception) {
                // Log but don't fail the entire operation if image deletion fails
            }

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
        val currentUserId = auth.currentUser?.uid
        val listener = firestore.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val documents = snapshot?.documents ?: emptyList()

                // Launch a coroutine to fetch like status for each post
                CoroutineScope(Dispatchers.IO).launch {
                    val posts = documents.mapNotNull { doc ->
                        val post = doc.toObject(SensePost::class.java)?.copy(id = doc.id)
                            ?: return@mapNotNull null

                        if (currentUserId != null) {
                            val likeSnapshot = firestore.collection("posts")
                                .document(post.id)
                                .collection("likes")
                                .document(currentUserId)
                                .get()
                                .await()

                            post.copy(isLikedByCurrentUser = likeSnapshot.exists())
                        } else {
                            post
                        }
                    }

                    trySend(posts).isSuccess
                }
            }
        awaitClose { listener.remove() }
    }

    override fun getPostDetailFlow(postId: String): Flow<SensePost> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val listener = firestore.collection("posts")
            .document(postId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val post = snapshot?.toObject(SensePost::class.java)?.copy(id = snapshot.id)
                if (post == null) {
                    close(IllegalStateException("Post not found"))
                    return@addSnapshotListener
                }

                CoroutineScope(Dispatchers.IO).launch {
                    val enrichedPost = if (currentUserId != null) {
                        val likeSnapshot = firestore.collection("posts")
                            .document(postId)
                            .collection("likes")
                            .document(currentUserId)
                            .get()
                            .await()

                        post.copy(isLikedByCurrentUser = likeSnapshot.exists())
                    } else {
                        post
                    }

                    trySend(enrichedPost).isSuccess
                }
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

    override suspend fun likePost(postId: String): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val postRef = firestore.collection("posts").document(postId)
            val likeRef = postRef.collection("likes").document(currentUserId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val likeSnapshot = transaction.get(likeRef)
                val currentLikeCount = snapshot.getLong("likeCount") ?: 0

                if (likeSnapshot.exists()) {
                    // User has already liked the post, so unlike it
                    transaction.delete(likeRef)
                    transaction.update(postRef, "likeCount", currentLikeCount - 1)
                } else {
                    // User has not liked the post, so like it
                    val likeData = mapOf(
                        "userId" to currentUserId,
                        "likedAt" to FieldValue.serverTimestamp()
                    )
                    transaction.set(likeRef, likeData)
                    transaction.update(postRef, "likeCount", currentLikeCount + 1)
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    suspend fun getLikes(postId: String): List<SenseUser> {
        val likeDocs = firestore.collection("posts")
            .document(postId)
            .collection("likes")
            .get()
            .await()

        val userIds = likeDocs.documents.mapNotNull { it.getString("userId") }

        return userIds.mapNotNull { userId ->
            firestore.collection("users").document(userId).get().await()
                .toObject(SenseUser::class.java)
        }
    }


}
