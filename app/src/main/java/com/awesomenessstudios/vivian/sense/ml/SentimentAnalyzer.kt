package com.awesomenessstudios.vivian.sense.ml


import android.util.Log
import com.awesomenessstudios.vivian.sense.ml.inference.SentimentInferenceEngine
import com.awesomenessstudios.vivian.sense.ml.models.BulkSentimentResult
import com.awesomenessstudios.vivian.sense.ml.models.SentimentAnalysis
import com.awesomenessstudios.vivian.sense.ml.models.SentimentLabel
import com.awesomenessstudios.vivian.sense.ml.models.SentimentResult
import com.awesomenessstudios.vivian.sense.ml.models.SentimentSummary
import com.awesomenessstudios.vivian.sense.ml.models.TextType
import com.awesomenessstudios.vivian.sense.ml.preprocessing.TextPreprocessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SentimentAnalyzer @Inject constructor(
    private val textPreprocessor: TextPreprocessor,
    private val inferenceEngine: SentimentInferenceEngine
) {

    companion object {
        private const val TAG = "SentimentAnalyzer"
        private const val MIN_TEXT_LENGTH = 3
        private const val MAX_BATCH_SIZE = 50
    }

    private var isInitialized = false
    private var initializationError: String? = null

    /**
     * Initialize the sentiment analyzer with detailed logging
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        if (isInitialized) {
            Log.d(TAG, "Already initialized")
            return@withContext true
        }

        Log.d(TAG, "Starting sentiment analyzer initialization...")

        try {
            val success = inferenceEngine.initialize()
            isInitialized = success

            if (success) {
                Log.d(TAG, "✅ Sentiment analyzer initialized successfully")
                initializationError = null

                // Run a test inference to verify everything works
                testInference()
            } else {
                val error = "❌ Failed to initialize inference engine"
                Log.e(TAG, error)
                initializationError = error
            }

            success
        } catch (e: Exception) {
            val error = "❌ Exception during initialization: ${e.message}"
            Log.e(TAG, error, e)
            initializationError = error
            isInitialized = false
            false
        }
    }

    /**
     * Test inference with a simple sentence
     */
    private suspend fun testInference() {
        Log.d(TAG, "Running test inference...")
        try {
            val testResult = analyzeSentiment(
                textId = "test_init",
                text = "This is a test sentence",
                textType = TextType.COMMENT
            )

            if (testResult != null) {
                Log.d(TAG, "✅ Test inference successful: ${testResult.sentimentResult.label}")
            } else {
                Log.w(TAG, "⚠️ Test inference returned null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Test inference failed", e)
        }
    }

    /**
     * Analyze sentiment with detailed debugging
     */
    suspend fun analyzeSentiment(
        textId: String,
        text: String,
        textType: TextType
    ): SentimentAnalysis? = withContext(Dispatchers.Default) {

        Log.d(TAG, "🔍 Analyzing sentiment for text: '$text' (ID: $textId)")

        // Check initialization
        if (!isInitialized) {
            Log.e(TAG, "❌ Analyzer not initialized. Error: $initializationError")
            return@withContext null
        }

        if (!inferenceEngine.isReady()) {
            Log.e(TAG, "❌ Inference engine not ready")
            return@withContext null
        }

        // Check text length
        if (isTextTooShort(text)) {
            Log.d(TAG, "⚠️ Text too short (${text.length} chars), returning neutral")
            return@withContext createNeutralAnalysis(textId, text, textType)
        }

        try {
            Log.d(TAG, "📝 Preprocessing text...")
            val processedText = textPreprocessor.preprocessText(text)
            Log.d(TAG, "✅ Text preprocessed: ${processedText.cleanedText}")
            Log.d(TAG, "🔢 Token count: ${processedText.tokens.size}")
            Log.d(TAG, "🔢 Input IDs length: ${processedText.inputIds.size}")

            Log.d(TAG, "🧠 Running inference...")
            val sentimentResult = inferenceEngine.runInference(processedText)

            if (sentimentResult == null) {
                Log.e(TAG, "❌ Inference returned null")
                return@withContext createNeutralAnalysis(textId, text, textType)
            }

            Log.d(TAG, "✅ Inference successful!")
            Log.d(TAG, "📊 Score: ${sentimentResult.score}")
            Log.d(TAG, "🏷️ Label: ${sentimentResult.label}")
            Log.d(TAG, "📈 Confidence: ${sentimentResult.confidence}")

            SentimentAnalysis(
                textId = textId,
                textType = textType,
                originalText = text,
                sentimentResult = sentimentResult
            )

        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception during sentiment analysis for text: $textId", e)
            createNeutralAnalysis(textId, text, textType)
        }
    }

    /**
     * Get detailed status information
     */
    fun getDebugInfo(): String {
        return buildString {
            appendLine("=== Sentiment Analyzer Debug Info ===")
            appendLine("Initialized: $isInitialized")
            appendLine("Engine Ready: ${inferenceEngine.isReady()}")
            appendLine("Initialization Error: ${initializationError ?: "None"}")
            appendLine("=====================================")
        }
    }

    /**
     * Analyze sentiment of multiple texts in batch
     */
    suspend fun analyzeBulkSentiment(
        targetId: String,
        texts: List<Pair<String, String>>,
        textType: TextType
    ): BulkSentimentResult = withContext(Dispatchers.Default) {

        Log.d(TAG, "📦 Starting bulk sentiment analysis for ${texts.size} texts")

        val analyses = mutableListOf<SentimentAnalysis>()

        texts.chunked(MAX_BATCH_SIZE).forEachIndexed { batchIndex, batch ->
            Log.d(
                TAG,
                "📦 Processing batch ${batchIndex + 1}/${(texts.size + MAX_BATCH_SIZE - 1) / MAX_BATCH_SIZE}"
            )

            batch.forEach { (textId, text) ->
                val analysis = analyzeSentiment(textId, text, textType)
                analysis?.let { analyses.add(it) }
            }
        }

        val summary = calculateSentimentSummary(analyses)

        Log.d(
            TAG,
            "✅ Bulk analysis completed: ${analyses.size}/${texts.size} texts processed successfully"
        )

        BulkSentimentResult(
            targetId = targetId,
            analyses = analyses,
            summary = summary
        )
    }

    /**
     * Calculate summary statistics from sentiment analyses
     */
    private fun calculateSentimentSummary(analyses: List<SentimentAnalysis>): SentimentSummary {
        if (analyses.isEmpty()) {
            return SentimentSummary(
                totalCount = 0,
                averageScore = 0f,
                dominantLabel = SentimentLabel.NEUTRAL,
                distribution = emptyMap()
            )
        }

        val scores = analyses.map { it.sentimentResult.score }
        val labels = analyses.map { it.sentimentResult.label }

        val averageScore = scores.average().toFloat()
        val distribution = labels.groupingBy { it }.eachCount()
        val dominantLabel = distribution.maxByOrNull { it.value }?.key ?: SentimentLabel.NEUTRAL

        return SentimentSummary(
            totalCount = analyses.size,
            averageScore = averageScore,
            dominantLabel = dominantLabel,
            distribution = distribution
        )
    }

    /**
     * Smart text validation that doesn't reject meaningful short content
     */
    private fun isTextTooShort(text: String): Boolean {
        val trimmedText = text.trim()

        // Empty or whitespace-only text is too short
        if (trimmedText.isEmpty()) return true

        // Check for emojis - they're meaningful even if short
        val emojiPattern =
            Regex("[\\p{So}\\p{Cn}\\u200D\\uFE0F\\u20E3\\u2030-\\u2BFF\\u1F000-\\u1F6FF\\u1F700-\\u1F77F\\u1F780-\\u1F7FF\\u1F800-\\u1F8FF\\u1F900-\\u1F9FF\\uD83C\\uD83D\\uD83E]")
        if (emojiPattern.containsMatchIn(trimmedText)) {
            Log.d(TAG, "✅ Emoji detected in short text, allowing analysis")
            return false
        }

        // Common meaningful short expressions
        val meaningfulShortExpressions = setOf(
            // Basic responses
            "ok", "okay", "yes", "no", "nah", "yep", "yup", "nope",
            // Reactions
            "wow", "omg", "wtf", "lol", "lmao", "rofl", "haha", "hehe",
            "yay", "ugh", "meh", "hmm", "ooh", "ahh", "eww", "oof",
            // Social media
            "rip", "smh", "tbh", "ngl", "fr", "bet", "cap", "sus",
            // Emotions
            "sad", "mad", "bad", "good", "nice", "cool", "hot", "cute",
            // Slang
            "lit", "fire", "sick", "dope", "mid", "trash", "vibe", "mood"
        )

        if (meaningfulShortExpressions.contains(trimmedText.lowercase())) {
            Log.d(TAG, "✅ Meaningful short expression detected: '$trimmedText'")
            return false
        }

        // Check for repeated characters (like "!!!" or "???") which can be meaningful
        if (trimmedText.matches(Regex("^[!?.,]{2,}$"))) {
            Log.d(TAG, "✅ Punctuation pattern detected: '$trimmedText'")
            return false
        }

        // Only reject if less than 2 characters and not meaningful
        val isTooShort = trimmedText.length < 2
        if (isTooShort) {
            Log.d(TAG, "⚠️ Text truly too short: '$trimmedText' (${trimmedText.length} chars)")
        }

        return isTooShort
    }


    /**
     * Create a neutral sentiment analysis for edge cases
     */
    private fun createNeutralAnalysis(
        textId: String,
        text: String,
        textType: TextType
    ): SentimentAnalysis {
        Log.d(TAG, "🔄 Creating neutral analysis for: $textId")
        return SentimentAnalysis(
            textId = textId,
            textType = textType,
            originalText = text,
            sentimentResult = SentimentResult(
                score = 0f,
                label = SentimentLabel.NEUTRAL,
                confidence = 0.5f
            )
        )
    }

    /**
     * Get sentiment color for UI display
     */
    fun getSentimentColor(label: SentimentLabel): Long {
        return when (label) {
            SentimentLabel.VERY_NEGATIVE -> 0xFFD32F2F
            SentimentLabel.NEGATIVE -> 0xFFEF5350
            SentimentLabel.NEUTRAL -> 0xFF9E9E9E
            SentimentLabel.POSITIVE -> 0xFF66BB6A
            SentimentLabel.VERY_POSITIVE -> 0xFF388E3C
        }
    }

    /**
     * Get sentiment emoji for UI display
     */
    fun getSentimentEmoji(label: SentimentLabel): String {
        return when (label) {
            SentimentLabel.VERY_NEGATIVE -> "😞"
            SentimentLabel.NEGATIVE -> "😕"
            SentimentLabel.NEUTRAL -> "😐"
            SentimentLabel.POSITIVE -> "🙂"
            SentimentLabel.VERY_POSITIVE -> "😄"
        }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        inferenceEngine.cleanup()
        isInitialized = false
        initializationError = null
        Log.d(TAG, "🧹 Sentiment analyzer cleaned up")
    }

    /**
     * Check if analyzer is ready
     */
    fun isReady(): Boolean = isInitialized && inferenceEngine.isReady()
}