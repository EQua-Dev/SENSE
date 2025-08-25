package com.awesomenessstudios.vivian.sense.ml.models

import androidx.annotation.Keep

@Keep
data class SentimentResult(
    val score: Float, // Range: -1.0 (very negative) to 1.0 (very positive)
    val label: SentimentLabel, val confidence: Float, // Range: 0.0 to 1.0
    val timestamp: Long = System.currentTimeMillis()
)

@Keep
enum class SentimentLabel {
    VERY_NEGATIVE,
    NEGATIVE,
    NEUTRAL,
    POSITIVE,
    VERY_POSITIVE;

    companion object {
        /**
         * IMPROVED: Better thresholds for sentiment classification
         */
        fun fromScore(score: Float): SentimentLabel {
            return when {
                score <= -0.5f -> VERY_NEGATIVE
                score <= -0.1f -> NEGATIVE
                score >= 0.5f -> VERY_POSITIVE
                score >= 0.1f -> POSITIVE
                else -> NEUTRAL
            }
        }
    }

    /**
     * Get color for UI display
     */
    fun getColor(): Long {
        return when (this) {
            VERY_NEGATIVE -> 0xFFD32F2F
            NEGATIVE -> 0xFFEF5350
            NEUTRAL -> 0xFF9E9E9E
            POSITIVE -> 0xFF66BB6A
            VERY_POSITIVE -> 0xFF388E3C
        }
    }

    /**
     * Get emoji for UI display
     */
    fun getEmoji(): String {
        return when (this) {
            VERY_NEGATIVE -> "😞"
            NEGATIVE -> "😕"
            NEUTRAL -> "😐"
            POSITIVE -> "🙂"
            VERY_POSITIVE -> "😄"
        }
    }

    /**
     * Get human-readable description
     */
    fun getDescription(): String {
        return when (this) {
            VERY_NEGATIVE -> "Very Negative"
            NEGATIVE -> "Negative"
            NEUTRAL -> "Neutral"
            POSITIVE -> "Positive"
            VERY_POSITIVE -> "Very Positive"
        }
    }
}


@Keep
data class SentimentAnalysis(
    val textId: String, // Comment ID or Post ID
    val textType: TextType,
    val originalText: String,
    val sentimentResult: SentimentResult,
    val processedAt: Long = System.currentTimeMillis()
)

@Keep
enum class TextType {
    COMMENT,
    POST
}

// For bulk analysis results
@Keep
data class BulkSentimentResult(
    val targetId: String, // Post ID for comment analysis
    val analyses: List<SentimentAnalysis>,
    val summary: SentimentSummary,
    val processedAt: Long = System.currentTimeMillis()
)

@Keep
data class SentimentSummary(
    val totalCount: Int,
    val averageScore: Float,
    val dominantLabel: SentimentLabel,
    val distribution: Map<SentimentLabel, Int>
)