package com.awesomenessstudios.vivian.sense.presentation.ui.analytics

import com.awesomenessstudios.vivian.sense.data.models.CommentsPostedAnalytics
import com.awesomenessstudios.vivian.sense.data.models.CommentsReceivedAnalytics
import com.awesomenessstudios.vivian.sense.data.models.EngagementAnalytics
import com.awesomenessstudios.vivian.sense.data.models.OverviewStats
import com.awesomenessstudios.vivian.sense.data.models.PostsAnalytics
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SentimentTrends

// UI State
data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val overviewStats: OverviewStats = OverviewStats(),
    val postsAnalytics: PostsAnalytics = PostsAnalytics(),
    val commentsReceivedAnalytics: CommentsReceivedAnalytics = CommentsReceivedAnalytics(),
    val commentsPostedAnalytics: CommentsPostedAnalytics = CommentsPostedAnalytics(),
    val engagementAnalytics: EngagementAnalytics = EngagementAnalytics(),
    val sentimentTrends: SentimentTrends = SentimentTrends()
)


// Detail screens data
data class PostsDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val posts: List<SensePost> = emptyList(),
    val sortedBy: PostsSortType = PostsSortType.MOST_RECENT,
    val analytics: PostsAnalytics = PostsAnalytics()
)

data class CommentsDetailUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val comments: List<SenseComment> = emptyList(),
    val filterBy: CommentFilterType = CommentFilterType.ALL,
    val analytics: CommentsReceivedAnalytics = CommentsReceivedAnalytics()
)

enum class PostsSortType {
    MOST_RECENT,
    MOST_LIKED,
    MOST_COMMENTED,
    BEST_SENTIMENT
}

enum class CommentFilterType {
    ALL,
    POSITIVE,
    NEGATIVE,
    NEUTRAL,
    RECENT
}