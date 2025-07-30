package com.awesomenessstudios.vivian.sense.ml.utils

// utils/SentimentTestHelper.kt


import android.util.Log
import com.awesomenessstudios.vivian.sense.ml.SentimentAnalyzer
import com.awesomenessstudios.vivian.sense.ml.models.TextType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SentimentTestHelper @Inject constructor(
    private val sentimentAnalyzer: SentimentAnalyzer
) {

    companion object {
        private const val TAG = "SentimentTestHelper"
    }

    /**
     * Run comprehensive sentiment analysis tests
     */
    fun runTests(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            Log.d(TAG, "🧪 Starting sentiment analysis tests...")

            // Print debug info
            Log.d(TAG, sentimentAnalyzer.getDebugInfo())

            // Initialize if not already done
            if (!sentimentAnalyzer.isReady()) {
                Log.d(TAG, "🔄 Initializing sentiment analyzer...")
                val initSuccess = sentimentAnalyzer.initialize()
                Log.d(TAG, if (initSuccess) "✅ Initialization successful" else "❌ Initialization failed")

                if (!initSuccess) {
                    Log.e(TAG, "❌ Cannot proceed with tests - initialization failed")
                    return@launch
                }
            }

            // Test cases
            val testCases = listOf(
                "I love this app!" to "Should be positive",
                "This is terrible and I hate it" to "Should be negative",
                "This is okay I guess" to "Should be neutral",
                "Amazing, fantastic, wonderful experience!" to "Should be very positive",
                "Worst app ever, absolutely horrible" to "Should be very negative",
                "Hello" to "Short text",
                "" to "Empty text",
                "The weather is nice today and I'm feeling good about everything" to "Long positive text"
            )

            Log.d(TAG, "🎯 Running ${testCases.size} test cases...")

            testCases.forEachIndexed { index, (text, expected) ->
                Log.d(TAG, "\n--- Test ${index + 1}/${testCases.size} ---")
                Log.d(TAG, "📝 Text: '$text'")
                Log.d(TAG, "🎯 Expected: $expected")

                try {
                    val result = sentimentAnalyzer.analyzeSentiment(
                        textId = "test_$index",
                        text = text,
                        textType = TextType.COMMENT
                    )

                    if (result != null) {
                        val sentiment = result.sentimentResult
                        Log.d(TAG, "✅ Result:")
                        Log.d(TAG, "   📊 Score: ${sentiment.score}")
                        Log.d(TAG, "   🏷️ Label: ${sentiment.label}")
                        Log.d(TAG, "   📈 Confidence: ${sentiment.confidence}")
                        Log.d(TAG, "   ${sentimentAnalyzer.getSentimentEmoji(sentiment.label)} ${sentiment.label}")
                    } else {
                        Log.e(TAG, "❌ Result: null")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Exception during test", e)
                }

                // Small delay between tests
                kotlinx.coroutines.delay(100)
            }

            Log.d(TAG, "\n🏁 All tests completed!")

            // Test bulk analysis
            testBulkAnalysis()
        }
    }

    /**
     * Test bulk sentiment analysis
     */
    private suspend fun testBulkAnalysis() {
        Log.d(TAG, "\n📦 Testing bulk sentiment analysis...")

        val bulkTexts = listOf(
            "comment1" to "This is great!",
            "comment2" to "I don't like this",
            "comment3" to "It's okay",
            "comment4" to "Absolutely amazing!",
            "comment5" to "Terrible experience"
        )

        try {
            val bulkResult = sentimentAnalyzer.analyzeBulkSentiment(
                targetId = "test_post",
                texts = bulkTexts,
                textType = TextType.COMMENT
            )

            Log.d(TAG, "📊 Bulk Analysis Results:")
            Log.d(TAG, "   Total: ${bulkResult.summary.totalCount}")
            Log.d(TAG, "   Average Score: ${bulkResult.summary.averageScore}")
            Log.d(TAG, "   Dominant Label: ${bulkResult.summary.dominantLabel}")
            Log.d(TAG, "   Distribution: ${bulkResult.summary.distribution}")

            bulkResult.analyses.forEach { analysis ->
                val sentiment = analysis.sentimentResult
                Log.d(TAG, "   - ${analysis.textId}: ${sentiment.label} (${sentiment.score})")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Bulk analysis failed", e)
        }
    }

    /**
     * Quick single test
     */
    fun quickTest(text: String, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            Log.d(TAG, "⚡ Quick test for: '$text'")

            if (!sentimentAnalyzer.isReady()) {
                sentimentAnalyzer.initialize()
            }

            val result = sentimentAnalyzer.analyzeSentiment(
                textId = "quick_test",
                text = text,
                textType = TextType.COMMENT
            )

            if (result != null) {
                val sentiment = result.sentimentResult
                Log.d(TAG, "⚡ Quick result: ${sentiment.label} (score: ${sentiment.score}, confidence: ${sentiment.confidence})")
            } else {
                Log.e(TAG, "⚡ Quick test failed - null result")
            }
        }
    }
}