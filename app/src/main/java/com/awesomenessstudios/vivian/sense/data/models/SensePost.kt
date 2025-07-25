package com.awesomenessstudios.vivian.sense.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class SensePost(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val commentCount: Int = 0,
    val user: SenseUser? = null
) : Parcelable
