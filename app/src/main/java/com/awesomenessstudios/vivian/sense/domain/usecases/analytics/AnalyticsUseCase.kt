package com.awesomenessstudios.vivian.sense.domain.usecases.analytics

import android.util.Log
import com.awesomenessstudios.vivian.sense.data.models.CommentsPostedAnalytics
import com.awesomenessstudios.vivian.sense.data.models.CommentsReceivedAnalytics
import com.awesomenessstudios.vivian.sense.data.models.EngagementAnalytics
import com.awesomenessstudios.vivian.sense.data.models.OverviewStats
import com.awesomenessstudios.vivian.sense.data.models.PostsAnalytics
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SentimentTrends
import com.awesomenessstudios.vivian.sense.domain.repository.AnalyticsRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/*interface AnalyticsUseCase {
    suspend fun getOverviewStats(userId: String): OverviewStats
    suspend fun getPostsAnalytics(userId: String): PostsAnalytics
    suspend fun getCommentsReceivedAnalytics(userId: String): CommentsReceivedAnalytics
    suspend fun getCommentsPostedAnalytics(userId: String): CommentsPostedAnalytics
    suspend fun getEngagementAnalytics(userId: String): EngagementAnalytics
    suspend fun getSentimentTrends(userId: String): SentimentTrends
    suspend fun getDetailedPostsAnalytics(userId: String): List<SensePost>
    suspend fun getDetailedCommentsReceivedAnalytics(userId: String): List<SenseComment>
} */

class AnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) {

    suspend fun getOverviewStats(userId: String): OverviewStats {
        return analyticsRepository.getOverviewStats(userId)
    }

    suspend fun getPostsAnalytics(userId: String): PostsAnalytics {
        val posts = analyticsRepository.getUserPosts(userId)
        Log.d(this.javaClass.simpleName, "getPostsAnalytics: $posts")
        val totalPosts = posts.size
        val averageLikes = if (posts.isNotEmpty()) posts.sumOf { it.likeCount } / posts.size else 0
        val averageComments =
            if (posts.isNotEmpty()) posts.sumOf { it.commentCount } / posts.size else 0
        val averageShares =
            if (posts.isNotEmpty()) posts.sumOf { it.shareCount } / posts.size else 0

        val mostEngagedPost = posts.maxByOrNull { it.likeCount + it.commentCount + it.shareCount }
        val leastEngagedPost = posts.minByOrNull { it.likeCount + it.commentCount + it.shareCount }

        // Group posts by month
        val postsByMonth = posts.groupBy {
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it.createdAt)
        }.mapValues { it.value.size }

        val topPerformingPosts = posts
            .sortedByDescending { it.likeCount + it.commentCount + it.shareCount }
            .take(5)

        return PostsAnalytics(
            totalPosts = totalPosts,
            averageLikes = averageLikes,
            averageComments = averageComments,
            averageShares = averageShares,
            mostEngagedPost = mostEngagedPost,
            leastEngagedPost = leastEngagedPost,
            postsByMonth = postsByMonth,
            topPerformingPosts = topPerformingPosts
        )
    }

    suspend fun getCommentsReceivedAnalytics(userId: String): CommentsReceivedAnalytics {
        val commentsReceived = analyticsRepository.getCommentsOnUserPosts(userId)
        val totalComments = commentsReceived.size

        // Count sentiment breakdown
        var positive = 0
        var neutral = 0
        var negative = 0
        var totalSentimentScore = 0f
        var sentimentCount = 0

        commentsReceived.forEach { comment ->
            comment.sentiment?.let { sentiment ->
                sentimentCount++
                totalSentimentScore += sentiment.score

                when {
                    sentiment.score > 0.1f -> positive++
                    sentiment.score < -0.1f -> negative++
                    else -> neutral++
                }
            }
        }

        val averageSentiment = if (sentimentCount > 0) totalSentimentScore / sentimentCount else 0f
        val mostCommentedPost = analyticsRepository.getMostCommentedPost(userId)
        val topCommenters = analyticsRepository.getTopCommenters(userId)
        val recentComments = commentsReceived.sortedByDescending { it.createdAt }.take(10)

        return CommentsReceivedAnalytics(
            totalComments = totalComments,
            positiveSentiment = positive,
            neutralSentiment = neutral,
            negativeSentiment = negative,
            averageSentiment = averageSentiment,
            mostCommentedPost = mostCommentedPost,
            topCommenters = topCommenters,
            recentComments = recentComments
        )
    }

    suspend fun getCommentsPostedAnalytics(userId: String): CommentsPostedAnalytics {
        val userComments = analyticsRepository.getUserComments(userId)
        val userPosts = analyticsRepository.getUserPosts(userId)
        val userPostIds = userPosts.map { it.id }.toSet()

        val onOwnPosts = userComments.count { it.postId in userPostIds }
        val onOthersPosts = userComments.size - onOwnPosts

        // Calculate average sentiment
        var totalSentimentScore = 0f
        var sentimentCount = 0
        val sentimentBreakdown = mutableMapOf<String, Int>()

        userComments.forEach { comment ->
            comment.sentiment?.let { sentiment ->
                sentimentCount++
                totalSentimentScore += sentiment.score

                val label = when {
                    sentiment.score > 0.5f -> "Very Positive"
                    sentiment.score > 0.1f -> "Positive"
                    sentiment.score < -0.5f -> "Very Negative"
                    sentiment.score < -0.1f -> "Negative"
                    else -> "Neutral"
                }
                sentimentBreakdown[label] = sentimentBreakdown.getOrDefault(label, 0) + 1
            }
        }

        val averageSentiment = if (sentimentCount > 0) totalSentimentScore / sentimentCount else 0f

        // Group by month
        val commentsByMonth = userComments.groupBy {
            SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it.createdAt)
        }.mapValues { it.value.size }

        val recentComments = userComments.sortedByDescending { it.createdAt }.take(10)

        return CommentsPostedAnalytics(
            totalComments = userComments.size,
            onOwnPosts = onOwnPosts,
            onOthersPosts = onOthersPosts,
            averageSentiment = averageSentiment,
            commentsByMonth = commentsByMonth,
            sentimentBreakdown = sentimentBreakdown,
            recentComments = recentComments
        )
    }

    suspend fun getEngagementAnalytics(userId: String): EngagementAnalytics {
        val posts = analyticsRepository.getUserPosts(userId)
        val comments = analyticsRepository.getUserComments(userId)
        val commentsReceived = analyticsRepository.getCommentsOnUserPosts(userId)

        // Calculate weekly engagement (last 7 days)
        val calendar = Calendar.getInstance()
        val weeklyEngagement = mutableListOf<Int>()

        repeat(7) { dayOffset ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -dayOffset)
            val dayStart = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dayEnd = calendar.time

            val dayPosts = posts.count { it.createdAt >= dayStart && it.createdAt < dayEnd }
            val dayComments = comments.count { it.createdAt >= dayStart && it.createdAt < dayEnd }

            weeklyEngagement.add(0, dayPosts + dayComments)
        }

        // Calculate engagement rate
        val totalPosts = posts.size
        val totalEngagement = posts.sumOf { it.likeCount + it.commentCount + it.shareCount }
        val engagementRate = if (totalPosts > 0) totalEngagement.toFloat() / totalPosts else 0f

        // Calculate response rate (how often user responds to comments on their posts)
        val totalCommentsReceived = commentsReceived.size
        val responsesToComments = analyticsRepository.getResponsesFromUser(userId)
        val responseRate = if (totalCommentsReceived > 0) {
            responsesToComments.toFloat() / totalCommentsReceived
        } else 0f

        return EngagementAnalytics(
            weeklyEngagement = weeklyEngagement,
            engagementRate = engagementRate,
            responseRate = responseRate,
            peakActivity = determinePeakActivity(posts + comments.map { mapCommentToPost(it) })
        )
    }

    suspend fun getSentimentTrends(userId: String): SentimentTrends {
        val posts = analyticsRepository.getUserPosts(userId)
        val comments = analyticsRepository.getUserComments(userId)
        val commentsReceived = analyticsRepository.getCommentsOnUserPosts(userId)

        // Combine all content with sentiment
        val allContentWithSentiment = mutableListOf<Pair<Date, Float>>()

        // Add comments posted by user
        comments.forEach { comment ->
            comment.sentiment?.let { sentiment ->
                allContentWithSentiment.add(comment.createdAt to sentiment.score)
            }
        }

        // Add comments received (represents how others perceive user's content)
        commentsReceived.forEach { comment ->
            comment.sentiment?.let { sentiment ->
                allContentWithSentiment.add(comment.createdAt to sentiment.score)
            }
        }

        // Group by month and calculate average sentiment
        val monthlyTrend = allContentWithSentiment
            .groupBy { SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(it.first) }
            .mapValues { entry ->
                val scores = entry.value.map { it.second }
                if (scores.isNotEmpty()) scores.average().toFloat() else 0f
            }
            .toList()
            .sortedBy {
                SimpleDateFormat("MMM yyyy", Locale.getDefault()).parse(it.first)
            }

        // Determine trend direction
        val overallTrend = if (monthlyTrend.size >= 2) {
            val recent = monthlyTrend.takeLast(3).map { it.second }.average()
            val earlier = monthlyTrend.dropLast(3).takeLast(3).map { it.second }.average()

            when {
                recent > earlier + 0.1 -> "Improving"
                recent < earlier - 0.1 -> "Declining"
                else -> "Stable"
            }
        } else "Insufficient Data"

        val bestMonth = monthlyTrend.maxByOrNull { it.second }?.first ?: ""
        val challengingMonth = monthlyTrend.minByOrNull { it.second }?.first ?: ""

        return SentimentTrends(
            monthlyTrend = monthlyTrend,
            overallTrend = overallTrend,
            bestMonth = bestMonth,
            challengingMonth = challengingMonth
        )
    }

    suspend fun getDetailedPostsAnalytics(userId: String): List<SensePost> {
        return analyticsRepository.getUserPosts(userId)
            .sortedByDescending { it.likeCount + it.commentCount + it.shareCount }
    }

    suspend fun getDetailedCommentsReceivedAnalytics(userId: String): List<SenseComment> {
        return analyticsRepository.getCommentsOnUserPosts(userId)
            .sortedByDescending { it.createdAt }
    }

    private fun determinePeakActivity(content: List<Any>): String {
        // Analyze content creation times to determine peak activity
        val hourCounts = mutableMapOf<Int, Int>()

        content.forEach { item ->
            val date = when (item) {
                is SensePost -> item.createdAt
                is SenseComment -> item.createdAt
                else -> null
            }

            date?.let {
                val calendar = Calendar.getInstance()
                calendar.time = it
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                hourCounts[hour] = hourCounts.getOrDefault(hour, 0) + 1
            }
        }

        val peakHour = hourCounts.maxByOrNull { it.value }?.key
        return when (peakHour) {
            in 6..11 -> "Morning (${peakHour}:00)"
            in 12..17 -> "Afternoon (${peakHour}:00)"
            in 18..21 -> "Evening (${peakHour}:00)"
            else -> "Night (${peakHour ?: 0}:00)"
        }
    }

    private fun mapCommentToPost(comment: SenseComment): SensePost {
        // Helper function to treat comments as content for activity analysis
        return SensePost(
            id = comment.id,
            userId = comment.userId,
            content = comment.content,
            createdAt = comment.createdAt
        )
    }
}