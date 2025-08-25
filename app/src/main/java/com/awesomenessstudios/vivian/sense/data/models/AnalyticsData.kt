package com.awesomenessstudios.vivian.sense.data.models

import java.util.Date

// Analytics Data Models
data class OverviewStats(
    val totalPosts: Int = 0,
    val totalLikesReceived: Int = 0,
    val totalComments: Int = 0,
    val averageSentimentScore: Float = 0f,
    val joinDate: Date = Date(),
    val mostActiveDay: String = ""
)

data class PostsAnalytics(
    val totalPosts: Int = 0,
    val averageLikes: Int = 0,
    val averageComments: Int = 0,
    val averageShares: Int = 0,
    val mostEngagedPost: SensePost? = null,
    val leastEngagedPost: SensePost? = null,
    val postsByMonth: Map<String, Int> = emptyMap(),
    val averagePostSentiment: Float = 0f,
    val topPerformingPosts: List<SensePost> = emptyList()
)

data class CommentsReceivedAnalytics(
    val totalComments: Int = 0,
    val positiveSentiment: Int = 0,
    val neutralSentiment: Int = 0,
    val negativeSentiment: Int = 0,
    val averageSentiment: Float = 0f,
    val mostCommentedPost: SensePost? = null,
    val topCommenters: List<UserCommentCount> = emptyList(),
    val recentComments: List<SenseComment> = emptyList()
)

data class CommentsPostedAnalytics(
    val totalComments: Int = 0,
    val onOwnPosts: Int = 0,
    val onOthersPosts: Int = 0,
    val averageSentiment: Float = 0f,
    val mostActiveOn: String = "", // Which user's posts they comment on most
    val commentsByMonth: Map<String, Int> = emptyMap(),
    val sentimentBreakdown: Map<String, Int> = emptyMap(),
    val recentComments: List<SenseComment> = emptyList()
)

data class EngagementAnalytics(
    val weeklyEngagement: List<Int> = emptyList(), // Last 7 days
    val monthlyEngagement: List<Int> = emptyList(), // Last 12 months
    val peakActivity: String = "", // Time of day or day of week
    val engagementRate: Float = 0f,
    val responseRate: Float = 0f, // How often they respond to comments
    val averageResponseTime: String = ""
)

data class SentimentTrends(
    val monthlyTrend: List<Pair<String, Float>> = emptyList(),
    val overallTrend: String = "", // "Improving", "Declining", "Stable"
    val bestMonth: String = "",
    val challengingMonth: String = "",
    val sentimentDistribution: Map<String, Int> = emptyMap()
)

data class UserCommentCount(
    val user: SenseUser,
    val commentCount: Int
)
