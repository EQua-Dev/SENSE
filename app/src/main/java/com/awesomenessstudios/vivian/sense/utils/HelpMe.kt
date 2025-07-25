package com.awesomenessstudios.vivian.sense.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HelpMe {
    fun formatDate(date: Date): String {
        val now = Date()
        val diff = now.time - date.time

        return when {
            diff < 60000 -> "just now"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    }
}