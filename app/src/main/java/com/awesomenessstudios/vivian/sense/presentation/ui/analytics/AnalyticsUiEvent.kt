package com.awesomenessstudios.vivian.sense.presentation.ui.analytics


// UI Events
sealed class AnalyticsUiEvent {
    object LoadUserAnalytics : AnalyticsUiEvent()
    object RefreshAnalytics : AnalyticsUiEvent()
    data class LoadDetailSection(val section: AnalyticsSection) : AnalyticsUiEvent()
}