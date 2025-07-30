package com.awesomenessstudios.vivian.sense.ml.models


import androidx.annotation.Keep

@Keep
data class SentimentResult(
    val score: Float, // Range: -1.0 (very negative) to 1.0 (very positive)
    val label: SentimentLabel,
    val confidence: Float, // Range: 0.0 to 1.0
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
        fun fromScore(score: Float): SentimentLabel {
            return when {
                score <= -0.6f -> VERY_NEGATIVE
                score <= -0.2f -> NEGATIVE
                score >= 0.6f -> VERY_POSITIVE
                score >= 0.2f -> POSITIVE
                else -> NEUTRAL
            }
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