package com.awesomenessstudios.vivian.sense.domain.repository

import com.awesomenessstudios.vivian.sense.data.models.OverviewStats
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.UserCommentCount

interface AnalyticsRepository {
    suspend fun getOverviewStats(userId: String): OverviewStats
    suspend fun getUserPosts(userId: String): List<SensePost>
    suspend fun getCommentsOnUserPosts(userId: String): List<SenseComment>
    suspend fun getUserComments(userId: String): List<SenseComment>
    suspend fun getMostCommentedPost(userId: String): SensePost?
    suspend fun getTopCommenters(userId: String): List<UserCommentCount>
    suspend fun getResponsesFromUser(userId: String): Int
}