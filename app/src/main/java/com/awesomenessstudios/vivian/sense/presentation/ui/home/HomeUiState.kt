package com.awesomenessstudios.vivian.sense.presentation.ui.home

import com.awesomenessstudios.vivian.sense.data.models.SenseUser

data class HomeUiState(
    val currentUser: SenseUser? = null,
    val error: String? = null
)
