package com.awesomenessstudios.vivian.sense.data.remote.impl

import android.util.Log
import com.awesomenessstudios.vivian.sense.data.models.OverviewStats
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SenseUser
import com.awesomenessstudios.vivian.sense.data.models.UserCommentCount
import com.awesomenessstudios.vivian.sense.data.remote.AnalyticsRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.log


class AnalyticsRemoteDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnalyticsRemoteDataSource {

    private val postsCollection = firestore.collection("posts")
    private val commentsCollection = firestore.collection("comments")
    private val usersCollection = firestore.collection("users")

    override suspend fun getOverviewStats(userId: String): OverviewStats =
        withContext(Dispatchers.IO) {
            try {
                // Get user's posts
                val userPostsSnapshot = postsCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val userPosts = userPostsSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(SensePost::class.java)?.copy(id = doc.id)
                }

                // Get comments on user's posts
                val postIds = userPosts.map { it.id }
                val commentsReceived = if (postIds.isNotEmpty()) {
                    commentsCollection
                        .whereIn("postId", postIds.take(10)) // Firestore 'in' limit
                        .get()
                        .await()
                        .documents
                        .mapNotNull { doc ->
                            Log.d(
                                this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                                "getOverviewStats: $doc"
                            )
                            doc.toObject(SenseComment::class.java)?.copy(id = doc.id)
                        }
                } else emptyList()

                // Get total likes received
                val totalLikesReceived = userPosts.sumOf { it.likeCount }

                // Calculate average sentiment
                val allComments = commentsReceived
                val sentimentScores = allComments.mapNotNull { it.sentiment?.score }
                val averageSentimentScore = if (sentimentScores.isNotEmpty()) {
                    (sentimentScores.average()
                        .toFloat() + 1) * 2.5f // Convert -1 to 1 scale to 0 to 5
                } else 0f

                // Get user join date
                val userDoc = usersCollection.document(userId).get().await()
                val joinDate = userDoc.toObject(SenseUser::class.java)?.createdAt ?: Date()

                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getOverviewStats: $userPosts"
                )
                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getOverviewStats: $totalLikesReceived"
                )
                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getOverviewStats: $commentsReceived"
                )
                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getOverviewStats: $averageSentimentScore"
                )
                OverviewStats(
                    totalPosts = userPosts.size,
                    totalLikesReceived = totalLikesReceived,
                    totalComments = commentsReceived.size,
                    averageSentimentScore = averageSentimentScore,
                    joinDate = joinDate
                )

            } catch (e: Exception) {
                throw Exception("Failed to get overview stats: ${e.message}")
            }
        }

    override suspend fun getUserPosts(userId: String): List<SensePost> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = postsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val posts = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SensePost::class.java)?.copy(id = doc.id)
                }

                // Fetch user data for each post
                posts.map { post ->
                    val userDoc = usersCollection.document(post.userId).get().await()
                    val user = userDoc.toObject(SenseUser::class.java)?.copy(id = userDoc.id)
                    post.copy(user = user)
                }
            } catch (e: Exception) {
                Log.d(this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName, "getUserPosts: $e")
                throw Exception("Failed to get user posts: ${e.message}")
            }
        }

    override suspend fun getCommentsOnUserPosts(userId: String): List<SenseComment> =
        withContext(Dispatchers.IO) {
            try {
                // First get user's posts
                val userPostsSnapshot = postsCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val postIds = userPostsSnapshot.documents.map { it.id }

                if (postIds.isEmpty()) return@withContext emptyList()

                // Get comments on those posts (Firestore 'in' query limited to 10 items)
                val allComments = mutableListOf<SenseComment>()
                postIds.chunked(10).forEach { chunk ->
                    val commentsSnapshot = commentsCollection
                        .whereIn("postId", chunk)
                        .orderBy("createdAt", Query.Direction.DESCENDING)
                        .get()
                        .await()

                    val comments = commentsSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(SenseComment::class.java)?.copy(id = doc.id)
                    }
                    allComments.addAll(comments)
                }

                // Fetch user data for each comment
                allComments.map { comment ->
                    val userDoc = usersCollection.document(comment.userId).get().await()
                    val user = userDoc.toObject(SenseUser::class.java)?.copy(id = userDoc.id)
                    comment.copy(user = user)
                }
            } catch (e: Exception) {
                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getCommentsOnUserPosts: $e"
                )
                throw Exception("Failed to get comments on user posts: ${e.message}")
            }
        }

    override suspend fun getUserComments(userId: String): List<SenseComment> =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = commentsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val comments = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(SenseComment::class.java)?.copy(id = doc.id)
                }

                // Fetch user data for each comment
                comments.map { comment ->
                    val userDoc = usersCollection.document(comment.userId).get().await()
                    val user = userDoc.toObject(SenseUser::class.java)?.copy(id = userDoc.id)
                    comment.copy(user = user)
                }
            } catch (e: Exception) {
                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getUserComments: $e"
                )
                throw Exception("Failed to get user comments: ${e.message}")
            }
        }

    override suspend fun getMostCommentedPost(userId: String): SensePost? =
        withContext(Dispatchers.IO) {
            try {
                val snapshot = postsCollection
                    .whereEqualTo("userId", userId)
                    .orderBy("commentCount", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

                val post = snapshot.documents.firstOrNull()?.let { doc ->
                    doc.toObject(SensePost::class.java)?.copy(id = doc.id)
                }

                post?.let {
                    val userDoc = usersCollection.document(it.userId).get().await()
                    val user = userDoc.toObject(SenseUser::class.java)?.copy(id = userDoc.id)
                    it.copy(user = user)
                }
            } catch (e: Exception) {
                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getMostCommentedPost: $e"
                )
                null
            }
        }

    override suspend fun getTopCommenters(userId: String): List<UserCommentCount> =
        withContext(Dispatchers.IO) {
            try {
                // Get user's posts
                val userPostsSnapshot = postsCollection
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val postIds = userPostsSnapshot.documents.map { it.id }

                if (postIds.isEmpty()) return@withContext emptyList()

                // Get all comments on user's posts
                val allComments = mutableListOf<SenseComment>()
                postIds.chunked(10).forEach { chunk ->
                    val commentsSnapshot = commentsCollection
                        .whereIn("postId", chunk)
                        .get()
                        .await()

                    val comments = commentsSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(SenseComment::class.java)?.copy(id = doc.id)
                    }
                    allComments.addAll(comments)
                }

                // Group by user and count
                val commentCounts = allComments
                    .filter { it.userId != userId } // Exclude self-comments
                    .groupBy { it.userId }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(10)

                // Fetch user data and create UserCommentCount objects
                commentCounts.map { (commentUserId, count) ->
                    val userDoc = usersCollection.document(commentUserId).get().await()
                    val user = userDoc.toObject(SenseUser::class.java)?.copy(id = userDoc.id)
                        ?: SenseUser(id = commentUserId, displayName = "Unknown User")
                    UserCommentCount(user = user, commentCount = count)
                }
            } catch (e: Exception) {
                Log.d(
                    this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                    "getTopCommenters: $e"
                )
                emptyList()
            }
        }

    override suspend fun getResponsesFromUser(userId: String): Int = withContext(Dispatchers.IO) {
        try {
            // Get comments on user's posts
            val userPostsSnapshot = postsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val postIds = userPostsSnapshot.documents.map { it.id }

            if (postIds.isEmpty()) return@withContext 0

            // Count responses from user to comments on their posts
            var responseCount = 0
            postIds.chunked(10).forEach { chunk ->
                val commentsSnapshot = commentsCollection
                    .whereIn("postId", chunk)
                    .whereEqualTo("userId", userId) // Comments made by the user
                    .get()
                    .await()

                // Filter for comments that are responses (have parentCommentId)
                responseCount += commentsSnapshot.documents.count { doc ->
                    val comment = doc.toObject(SenseComment::class.java)
                    !comment?.parentCommentId.isNullOrEmpty()
                }
            }

            responseCount
        } catch (e: Exception) {
            Log.d(
                this@AnalyticsRemoteDataSourceImpl.javaClass.simpleName,
                "getResponsesFromUser: $e"
            )
            0
        }
    }
}