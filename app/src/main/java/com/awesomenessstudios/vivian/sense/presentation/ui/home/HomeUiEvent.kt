package com.awesomenessstudios.vivian.sense.presentation.ui.home

sealed class HomeUiEvent {
    data class OnLikeClicked(val postId: String): HomeUiEvent()
}