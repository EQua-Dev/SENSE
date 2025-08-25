package com.awesomenessstudios.vivian.sense.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awesomenessstudios.vivian.sense.domain.repository.AuthRepository
import com.awesomenessstudios.vivian.sense.domain.usecases.analytics.AnalyticsUseCase
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.AnalyticsSection
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.AnalyticsUiEvent
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.AnalyticsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsUseCase: AnalyticsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _analyticsState = MutableStateFlow(AnalyticsUiState())
    val analyticsState: StateFlow<AnalyticsUiState> = _analyticsState.asStateFlow()

    fun onAnalyticsEvent(event: AnalyticsUiEvent) {
        when (event) {
            is AnalyticsUiEvent.LoadUserAnalytics -> loadUserAnalytics()
            is AnalyticsUiEvent.RefreshAnalytics -> refreshAnalytics()
            is AnalyticsUiEvent.LoadDetailSection -> loadDetailSection(event.section)
        }
    }

    private fun loadUserAnalytics() {
        viewModelScope.launch {
            _analyticsState.value = _analyticsState.value.copy(isLoading = true, error = null)

            try {
                val currentUser = authRepository.getCurrentUser()
                Log.d(this.javaClass.simpleName, "loadUserAnalytics: $currentUser")
                if (currentUser == null) {
                    _analyticsState.value = _analyticsState.value.copy(
                        isLoading = false,
                        error = "User not authenticated"
                    )
                    return@launch
                }
                currentUser.fold(
                    onSuccess = { user ->
                        Log.d(this.javaClass.simpleName, "loadUserAnalytics: $user")
                        // Load all analytics data
                        val overviewStats = analyticsUseCase.getOverviewStats(user?.id!!)
                        Log.d(this.javaClass.simpleName, "overviewStats: $overviewStats")
                        val postsAnalytics = analyticsUseCase.getPostsAnalytics(user.id)
                        Log.d(this.javaClass.simpleName, "postsAnalytics: $postsAnalytics")
                        val commentsReceivedAnalytics =
                            analyticsUseCase.getCommentsReceivedAnalytics(user.id)
                        Log.d(
                            this.javaClass.simpleName,
                            "commentsReceivedAnalytics: $commentsReceivedAnalytics"
                        )
                        val commentsPostedAnalytics =
                            analyticsUseCase.getCommentsPostedAnalytics(user.id)

                        Log.d(
                            this.javaClass.simpleName,
                            "commentsPostedAnalytics: $commentsPostedAnalytics"
                        )
                        val engagementAnalytics = analyticsUseCase.getEngagementAnalytics(user.id)
                        Log.d(
                            this.javaClass.simpleName,
                            "engagementAnalytics: $engagementAnalytics"
                        )
                        val sentimentTrends = analyticsUseCase.getSentimentTrends(user.id)

                        Log.d(this.javaClass.simpleName, "sentimentTrends: $sentimentTrends")
                        _analyticsState.value = _analyticsState.value.copy(
                            isLoading = false,
                            overviewStats = overviewStats,
                            postsAnalytics = postsAnalytics,
                            commentsReceivedAnalytics = commentsReceivedAnalytics,
                            commentsPostedAnalytics = commentsPostedAnalytics,
                            engagementAnalytics = engagementAnalytics,
                            sentimentTrends = sentimentTrends
                        )
                    },
                    onFailure = {
                        _analyticsState.value = _analyticsState.value.copy(
                            isLoading = false,
                            error = it.message ?: "Unknown error occurred"
                        )
                    }
                )

            } catch (e: Exception) {
                _analyticsState.value = _analyticsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun refreshAnalytics() {
        loadUserAnalytics()
    }

    private fun loadDetailSection(section: AnalyticsSection) {
        // This could trigger navigation to detail screens
        // or load additional data for specific sections
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser() ?: return@launch
                currentUser.fold(
                    onSuccess = { user ->
                        when (section) {
                            AnalyticsSection.POSTS -> {
                                // Load detailed posts data
                                val detailedPosts =
                                    analyticsUseCase.getDetailedPostsAnalytics(user!!.id)
                                // Update state or trigger navigation
                            }

                            AnalyticsSection.COMMENTS_RECEIVED -> {
                                // Load detailed comments data
                                val detailedComments =
                                    analyticsUseCase.getDetailedCommentsReceivedAnalytics(user!!.id)
                            }
                            // Add other cases as needed
                            else -> {
                                // Handle other sections
                            }
                        }
                    },
                    onFailure = {
                        _analyticsState.value = _analyticsState.value.copy(
                            error = it.message ?: "Error loading detail section"
                        )
                    }
                )

            } catch (e: Exception) {
                _analyticsState.value = _analyticsState.value.copy(
                    error = e.message ?: "Error loading detail section"
                )
            }
        }
    }
}