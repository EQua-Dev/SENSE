package com.awesomenessstudios.vivian.sense.data.remote.impl

import android.util.Log
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SenseSentiment
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.data.remote.CommentDataSource
import com.awesomenessstudios.vivian.sense.data.remote.PostDataSource
import com.awesomenessstudios.vivian.sense.ml.SentimentAnalyzer
import com.awesomenessstudios.vivian.sense.ml.models.TextType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.w3c.dom.Comment
import java.util.Date
import java.util.UUID
import javax.inject.Inject

class FirebaseCommentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val sentimentAnalyzer: SentimentAnalyzer
) : CommentDataSource {

    override fun getCommentsFlow(postId: String): Flow<List<SenseComment>> = callbackFlow {
        val listenerRegistration = firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SenseComment::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(comments).isSuccess
            }

        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun updateComment(comment: SenseComment): Result<SenseComment> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteComment(commentId: String): Result<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getCommentsByUser(userId: String): Result<List<SenseComment>> {
        TODO("Not yet implemented")
    }

    override suspend fun writeComment(
        commentText: String,
        postId: String,
        parentCommentId: String?
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            // Fetch full user profile from Firestore
            val userSnapshot = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            val user = userSnapshot.toObject(SenseUser::class.java)
                ?: return Result.failure(Exception("User profile not found"))

            val commentId = UUID.randomUUID().toString()
            val postRef = firestore.collection("posts").document(postId)
            val commentRef = postRef.collection("comments").document(commentId)

            // Analyze sentiment
            val sentiment = sentimentAnalyzer.analyzeSentiment(
                textId = commentId,
                text = commentText,
                textType = TextType.COMMENT
            )?.sentimentResult?.let {
                SenseSentiment(
                    score = it.score,
                    label = it.label.name.lowercase(),
                    confidence = it.confidence
                )
            }

            val newComment = SenseComment(
                id = commentId,
                postId = postId,
                userId = currentUser.uid,
                content = commentText,
                createdAt = Date(),
                sentiment = sentiment,
                parentCommentId = parentCommentId ?: "",
                user = user
            )

            Log.d(this.javaClass.simpleName, "writeComment: $newComment")
            // Write comment and increment comment count in a transaction
            firestore.runTransaction { transaction ->

                // Safely increment comment count
                val snapshot = transaction.get(postRef)
                val currentCount = snapshot.getLong("commentCount") ?: 0

                transaction.set(commentRef, newComment)
                transaction.update(postRef, "commentCount", currentCount + 1)
            }.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Log.d(this.javaClass.simpleName, "writeComment: $e")
            Result.failure(e)
        }
    }
}