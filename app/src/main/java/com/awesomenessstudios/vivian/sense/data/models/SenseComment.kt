package com.awesomenessstudios.vivian.sense.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class SenseComment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val content: String = "",
    val createdAt: Date = Date(),
    val sentiment: SenseSentiment? = null,
    val parentCommentId: String = "",
    val user: SenseUser? = null
) : Parcelable

@Parcelize
data class SenseSentiment(
    val score: Float = 0F,
    val label: String = "neutral",
    val confidence: Float = 0F
): Parcelable
