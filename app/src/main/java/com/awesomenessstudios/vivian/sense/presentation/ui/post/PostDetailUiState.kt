package com.awesomenessstudios.vivian.sense.presentation.ui.post

import com.awesomenessstudios.vivian.sense.data.models.SenseComment
import com.awesomenessstudios.vivian.sense.data.models.SensePost
import com.awesomenessstudios.vivian.sense.data.models.SenseUser

data class PostDetailUiState(
    val commentText: String = "",
    val commentsLoading: Boolean = false,
    val comments: List<SenseComment> = emptyList(),
    val commentsError: String? = null,
    val postDetailLoading: Boolean = false,
    val postDetail: SensePost? = null,
    val postDetailError: String? = null,
    val currentUser: SenseUser? = null
)
