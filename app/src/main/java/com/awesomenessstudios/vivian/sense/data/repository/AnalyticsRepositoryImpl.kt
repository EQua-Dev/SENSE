package com.awesomenessstudios.vivian.sense.data.repository

import com.awesomenessstudios.vivian.sense.data.models.OverviewStats
import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.UserCommentCount
import com.awesomenessstudios.vivian.sense.data.remote.AnalyticsRemoteDataSource
import com.awesomenessstudios.vivian.sense.domain.repository.AnalyticsRepository
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import javax.inject.Inject


class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsRemoteDataSource: AnalyticsRemoteDataSource,
    private val authRepository: AuthRepository
) : AnalyticsRepository {

    override suspend fun getOverviewStats(userId: String): OverviewStats {
        return analyticsRemoteDataSource.getOverviewStats(userId)
    }

    override suspend fun getUserPosts(userId: String): List<SensePost> {
        return analyticsRemoteDataSource.getUserPosts(userId)
    }

    override suspend fun getCommentsOnUserPosts(userId: String): List<SenseComment> {
        return analyticsRemoteDataSource.getCommentsOnUserPosts(userId)
    }

    override suspend fun getUserComments(userId: String): List<SenseComment> {
        return analyticsRemoteDataSource.getUserComments(userId)
    }

    override suspend fun getMostCommentedPost(userId: String): SensePost? {
        return analyticsRemoteDataSource.getMostCommentedPost(userId)
    }

    override suspend fun getTopCommenters(userId: String): List<UserCommentCount> {
        return analyticsRemoteDataSource.getTopCommenters(userId)
    }

    override suspend fun getResponsesFromUser(userId: String): Int {
        return analyticsRemoteDataSource.getResponsesFromUser(userId)
    }
}
