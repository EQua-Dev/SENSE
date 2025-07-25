package com.awesomenessstudios.vivian.sense.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class SenseUser(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val profilePicture: String = "",
    val bio: String = "",
    val createdAt: Date = Date()
) : Parcelable
