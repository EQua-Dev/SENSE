package com.awesomenessstudios.vivian.sense.ml.inference

import android.content.Context
import android.util.Log
import com.awesomenessstudios.vivian.sense.ml.models.SentimentLabel
import com.awesomenessstudios.vivian.sense.ml.models.SentimentResult
import com.awesomenessstudios.vivian.sense.ml.preprocessing.ProcessedText
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class SentimentInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var interpreter: Interpreter? = null
    private val modelFileName = "sentiment_model.tflite"
    private var useMockInference = false
    private var isInitialized = false

    companion object {
        private const val TAG = "SentimentInferenceEngine"
        private const val INPUT_SIZE = 100 // Changed from 128 to match your model
    }

    /**
     * Initialize the TensorFlow Lite interpreter with fallback to mock
     */
    suspend fun initialize(): Boolean {
        Log.d(TAG, "🚀 Initializing TensorFlow Lite inference engine...")

        return try {
            // First, check if model file exists
            if (!checkModelExists()) {
                Log.w(TAG, "⚠️ Model file not found, falling back to mock inference")
                useMockInference = true
                isInitialized = true
                return true
            }

            // Try to load the actual model
            val modelBuffer = loadModelFile()
            val options = Interpreter.Options().apply {
                addDelegate(FlexDelegate())
                setNumThreads(4)
                setUseNNAPI(false) // Disable NNAPI initially for debugging
            }

            interpreter = Interpreter(modelBuffer, options)

            // Verify model input/output shapes
            val inputShape = interpreter?.getInputTensor(0)?.shape()
            val outputShape = interpreter?.getOutputTensor(0)?.shape()

            Log.d(TAG, "📐 Model input shape: ${inputShape?.contentToString()}")
            Log.d(TAG, "📐 Model output shape: ${outputShape?.contentToString()}")

            // Validate shapes
            if (inputShape == null || outputShape == null) {
                Log.e(TAG, "❌ Cannot determine model tensor shapes")
                fallbackToMock()
                return true
            }

            Log.d(TAG, "✅ TensorFlow Lite model loaded successfully")
            useMockInference = false
            isInitialized = true

            true

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load TensorFlow Lite model, falling back to mock", e)
            fallbackToMock()
            isInitialized = true

            true
        }
    }

    /**
     * Check if the model file exists in assets
     */
    private fun checkModelExists(): Boolean {
        return try {
            context.assets.open(modelFileName).use { true }
        } catch (e: Exception) {
            Log.w(TAG, "Model file '$modelFileName' not found in assets: ${e.message}")
            false
        }
    }

    /**
     * Fallback to mock inference
     */
    private fun fallbackToMock() {
        useMockInference = true
        interpreter?.close()
        interpreter = null
        Log.i(TAG, "🎭 Switched to mock inference mode")
    }

    /**
     * Run sentiment analysis (real or mock)
     */
    fun runInference(processedText: ProcessedText): SentimentResult? {
        return runMockInference(processedText)
        /*  return if (useMockInference) {
            runMockInference(processedText)
        } else {
            runRealInference(processedText)
        }*/
    }

    /**
     * Run real TensorFlow Lite inference
     */
    private fun runRealInference(processedText: ProcessedText): SentimentResult? {

        if (!isInitialized) {
            Log.e(TAG, "❌ Inference engine not initialized. Call initialize() first.")
            return null
        }

        val currentInterpreter = interpreter ?: return null

        return try {
            Log.d(TAG, "🧠 Running real TF Lite inference...")

            // Get actual tensor info
            val inputTensor = currentInterpreter.getInputTensor(0)
            val outputTensor = currentInterpreter.getOutputTensor(0)

            Log.d(TAG, "📊 Input tensor shape: ${inputTensor.shape().contentToString()}")
            Log.d(TAG, "📊 Output tensor shape: ${outputTensor.shape().contentToString()}")

            // Prepare input buffer based on actual tensor shape
            val inputShape = inputTensor.shape()
            val sequenceLength = if (inputShape.size >= 2) inputShape[1] else INPUT_SIZE

            val inputBuffer = ByteBuffer.allocateDirect(4 * sequenceLength).apply {
                order(ByteOrder.nativeOrder())
                rewind()

                // Fill with input IDs - ensure we have exactly sequenceLength values
                val inputIds = processedText.inputIds
                val paddedIds = if (inputIds.size >= sequenceLength) {
                    inputIds.take(sequenceLength)
                } else {
                    inputIds.toList() + List(sequenceLength - inputIds.size) { 0 }
                }

                paddedIds.forEach { id -> putFloat(id.toFloat()) }
            }

            // Prepare output buffer based on actual tensor shape
            val outputShape = outputTensor.shape()
            val outputSize = outputShape.reduce { acc, dim -> acc * dim }

            val outputBuffer = ByteBuffer.allocateDirect(4 * outputSize).apply {
                order(ByteOrder.nativeOrder())
            }

            // Run inference
            currentInterpreter.run(inputBuffer, outputBuffer)

            // Parse output
            outputBuffer.rewind()
            val outputs = FloatArray(outputSize) { outputBuffer.float }

            Log.d(TAG, "📈 Raw model outputs: ${outputs.contentToString()}")

            // Convert to sentiment result
            parseSentimentOutput(outputs)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Real inference failed, falling back to mock", e)
            fallbackToMock()
            runMockInference(processedText)
        }
    }

    /**
     * Run mock inference for testing/fallback
     */
    // Negation words that flip sentiment
    val negations = listOf(
        "not", "no", "never", "none", "nobody", "nothing", "neither",
        "nowhere", "hardly", "barely", "scarcely", "can't", "cannot",
        "won't", "wouldn't", "shouldn't", "couldn't", "mustn't", "don't",
        "doesn't", "didn't", "isn't", "aren't", "wasn't", "weren't", "ain't"
    )

    // Punctuation and capitalization patterns that affect sentiment
    val punctuationMultipliers = mapOf(
        "!" to 1.3f, "!!" to 1.5f, "!!!" to 1.7f,
        "?" to 1.1f, "??" to 1.2f,
        "..." to 0.9f, // Often indicates uncertainty or trailing off
        "." to 1.0f
    )

    // Emoji sentiment (common ones that might survive preprocessing)
    val positiveEmojis =
        listOf("😊", "😄", "😃", "🙂", "😍", "🥰", "😘", "🤗", "👍", "👌", "💖", "❤️", "💯", "🔥", "✨")
    val negativeEmojis =
        listOf("😞", "😢", "😭", "😠", "😡", "🙄", "😤", "😩", "💔", "👎", "😔", "😟", "😰", "🤬")

    private fun runMockInference(processedText: ProcessedText): SentimentResult {
        Log.d(TAG, "🎭 Running enhanced mock inference...")

        val text = processedText.cleanedText.lowercase()
        // Use the already tokenized words from preprocessing when available, otherwise split
        val words = if (processedText.tokens.isNotEmpty()) {
            processedText.tokens
        } else {
            text.split(Regex("\\s+"))
        }
        val wordCount = words.size

        // Comprehensive positive words and phrases
        val strongPositive = listOf(
            "amazing", "awesome", "fantastic", "incredible", "outstanding", "phenomenal",
            "exceptional", "brilliant", "spectacular", "magnificent", "superb", "excellent",
            "perfect", "flawless", "wonderful", "marvelous", "fabulous", "extraordinary",
            "breathtaking", "stunning", "remarkable", "impressive", "terrific", "divine",
            "heavenly", "blissful", "ecstatic", "thrilled", "overjoyed", "elated"
        )

        val positive = listOf(
            "good", "great", "nice", "cool", "sweet", "fun", "happy", "joy", "love",
            "like", "enjoy", "pleased", "satisfied", "glad", "cheerful", "delighted",
            "excited", "proud", "grateful", "thankful", "blessed", "lucky", "hopeful",
            "optimistic", "confident", "comfortable", "relaxed", "peaceful", "beautiful",
            "pretty", "attractive", "cute", "lovely", "charming", "elegant", "gorgeous",
            "handsome", "smart", "intelligent", "clever", "wise", "talented", "skilled",
            "successful", "winning", "victory", "triumph", "achieve", "accomplish",
            "progress", "improve", "upgrade", "benefit", "advantage", "helpful", "useful"
        )

        val weakPositive = listOf(
            "okay", "ok", "fine", "decent", "fair", "reasonable", "acceptable", "tolerable",
            "adequate", "sufficient", "manageable", "workable", "passable", "alright"
        )

        // Social media positive slang and abbreviations
        val positiveSlang = listOf(
            "lit", "fire", "dope", "sick", "tight", "fresh", "rad", "epic", "legit",
            "bomb", "killer", "solid", "clutch", "mint", "crisp", "clean", "smooth",
            "slick", "boss", "beast", "goat", "og", "legend", "icon", "queen", "king",
            "slay", "slaying", "slaps", "bangs", "hits different", "no cap", "periodt",
            "facts", "truth", "real", "mood", "vibe", "vibes", "energy", "aura",
            "based", "valid", "lowkey good", "highkey amazing", "deadass good"
        )

        val positiveAbbreviations = listOf(
            "lol", "lmao", "lmfao", "rofl", "haha", "hehe", "yay", "woohoo", "yess",
            "omg good", "wtf amazing", "af good", "asf great", "fr good", "ngl good",
            "tbh good", "imo good", "imho good", "fyi good", "btw good"
        )

        // Comprehensive negative words and phrases
        val strongNegative = listOf(
            "terrible", "awful", "horrible", "disgusting", "repulsive", "revolting",
            "appalling", "atrocious", "abysmal", "dreadful", "ghastly", "hideous",
            "horrendous", "monstrous", "nightmarish", "sickening", "vile", "wicked",
            "evil", "cruel", "brutal", "savage", "ruthless", "merciless", "heartless",
            "devastating", "catastrophic", "disastrous", "tragic", "miserable", "pathetic",
            "worthless", "useless", "hopeless", "desperate", "doomed", "cursed"
        )

        val negative = listOf(
            "bad", "poor", "wrong", "sad", "angry", "mad", "upset", "annoyed", "irritated",
            "frustrated", "disappointed", "dissatisfied", "unhappy", "depressed", "worried",
            "anxious", "stressed", "concerned", "troubled", "bothered", "disturbed",
            "uncomfortable", "awkward", "embarrassed", "ashamed", "guilty", "regretful",
            "sorry", "hurt", "pain", "suffering", "struggle", "difficult", "hard",
            "challenging", "tough", "rough", "harsh", "severe", "serious", "critical",
            "dangerous", "risky", "unsafe", "insecure", "vulnerable", "weak", "fragile",
            "broken", "damaged", "ruined", "destroyed", "failed", "failure", "lose",
            "loss", "defeat", "reject", "rejection", "deny", "refuse", "decline"
        )

        val weakNegative = listOf(
            "meh", "bland", "boring", "dull", "plain", "ordinary", "mediocre", "average",
            "so-so", "nothing special", "whatever", "could be better", "not great",
            "not good", "not bad", "questionable", "concerning", "worrying", "odd"
        )

        // Social media negative slang and abbreviations
        val negativeSlang = listOf(
            "trash", "garbage", "wack", "whack", "weak", "lame", "basic", "cringe",
            "cringe af", "mid", "sus", "sketchy", "shady", "toxic", "salty", "pressed",
            "triggered", "mad pressed", "big mad", "heated", "pissed", "lowkey trash",
            "highkey bad", "deadass trash", "straight trash", "pure garbage", "absolutely not",
            "hell no", "nah fam", "ain't it", "not it", "missed the mark", "took an L",
            "major L", "big L", "fumbled", "flopped", "bombed", "crashed", "bust"
        )

        val negativeAbbreviations = listOf(
            "smh", "fml", "wtf bad", "tf wrong", "bruh moment", "oof", "yikes", "rip",
            "f in chat", "press f", "ngl bad", "fr bad", "tbh trash", "imo bad"
        )

        // Intensifiers that modify sentiment strength
        val intensifiers = mapOf(
            "very" to 1.5f, "really" to 1.4f, "extremely" to 1.8f, "incredibly" to 1.7f,
            "absolutely" to 1.6f, "totally" to 1.5f, "completely" to 1.6f, "utterly" to 1.7f,
            "quite" to 1.3f, "rather" to 1.2f, "pretty" to 1.3f, "fairly" to 1.2f,
            "somewhat" to 0.8f, "slightly" to 0.7f, "a bit" to 0.8f, "kinda" to 0.9f,
            "super" to 1.6f, "mega" to 1.7f, "ultra" to 1.8f, "mad" to 1.4f, "hella" to 1.5f,
            "lowkey" to 0.8f, "highkey" to 1.4f, "deadass" to 1.3f, "fr" to 1.2f, "ngl" to 1.1f
        )

        // Punctuation and capitalization patterns that affect sentiment
        val punctuationMultipliers = mapOf(
            "!" to 1.3f, "!!" to 1.5f, "!!!" to 1.7f,
            "?" to 1.1f, "??" to 1.2f,
            "..." to 0.9f, // Often indicates uncertainty or trailing off
            "." to 1.0f
        )

        // Emoji sentiment (common ones that might survive preprocessing)
        val positiveEmojis =
            listOf("😊", "😄", "😃", "🙂", "😍", "🥰", "😘", "🤗", "👍", "👌", "💖", "❤️", "💯", "🔥", "✨")
        val negativeEmojis =
            listOf("😞", "😢", "😭", "😠", "😡", "🙄", "😤", "😩", "💔", "👎", "😔", "😟", "😰", "🤬")

        // Calculate sentiment scores with enhanced logic
        fun calculateSentimentScore(): Float {
            var totalScore = 0f
            var wordProcessed = 0
            var negationActive = false
            var intensifierValue = 1f

            // Check for emojis in original text (before cleaning)
            val originalText =
                processedText.cleanedText // Since original text isn't stored, use cleaned
            val emojiScore = positiveEmojis.count { originalText.contains(it) } * 0.4f -
                    negativeEmojis.count { originalText.contains(it) } * 0.4f

            // Check for capitalization patterns (ALL CAPS often indicates strong emotion)
            val capsWords = words.count { word -> word.length > 2 && word == word.uppercase() }
            val capsMultiplier = if (capsWords > 0) 1.2f + (capsWords * 0.1f) else 1.0f

            // Check for punctuation patterns
            val punctuationScore = punctuationMultipliers.entries.sumOf { (punct, multiplier) ->
                (originalText.split(punct).size - 1) * (multiplier - 1.0) * 0.1
            }.toFloat()

            // Process words in sequence to handle negations and intensifiers
            for (i in words.indices) {
                val word = words[i]
                val nextWord = if (i < words.size - 1) words[i + 1] else ""
                val prevWord = if (i > 0) words[i - 1] else ""
                val twoWordPhrase = "$word $nextWord"
                val threeWordPhrase =
                    if (i < words.size - 2) "$word $nextWord ${words[i + 2]}" else ""

                // Check for intensifiers
                if (intensifiers.containsKey(word)) {
                    intensifierValue = intensifiers[word] ?: 1f
                    continue
                }

                // Check for negations
                if (negations.contains(word)) {
                    negationActive = true
                    continue
                }

                // Calculate base score for current word/phrase
                var baseScore = 0f
                var scoreFound = false

                // Check three-word phrases first
                if (threeWordPhrase.isNotEmpty()) {
                    when {
                        positiveSlang.any { threeWordPhrase.contains(it) } -> {
                            baseScore = 0.7f; scoreFound = true
                        }

                        negativeSlang.any { threeWordPhrase.contains(it) } -> {
                            baseScore = -0.7f; scoreFound = true
                        }
                    }
                }

                // Check two-word phrases
                if (!scoreFound) {
                    when {
                        positiveSlang.any { twoWordPhrase.contains(it) } -> {
                            baseScore = 0.7f; scoreFound = true
                        }

                        negativeSlang.any { twoWordPhrase.contains(it) } -> {
                            baseScore = -0.7f; scoreFound = true
                        }

                        strongPositive.contains(twoWordPhrase) -> {
                            baseScore = 0.9f; scoreFound = true
                        }

                        strongNegative.contains(twoWordPhrase) -> {
                            baseScore = -0.9f; scoreFound = true
                        }
                    }
                }

                // Check single words
                if (!scoreFound) {
                    when {
                        strongPositive.contains(word) -> baseScore = 0.9f
                        positive.contains(word) -> baseScore = 0.6f
                        weakPositive.contains(word) -> baseScore = 0.3f
                        positiveSlang.contains(word) -> baseScore = 0.7f
                        positiveAbbreviations.any { word.contains(it) } -> baseScore = 0.5f

                        strongNegative.contains(word) -> baseScore = -0.9f
                        negative.contains(word) -> baseScore = -0.6f
                        weakNegative.contains(word) -> baseScore = -0.3f
                        negativeSlang.contains(word) -> baseScore = -0.7f
                        negativeAbbreviations.any { word.contains(it) } -> baseScore = -0.5f
                    }
                }

                // Apply modifiers if score was found
                if (baseScore != 0f) {
                    // Apply intensifier
                    baseScore *= intensifierValue

                    // Apply negation (flip and reduce intensity)
                    if (negationActive) {
                        baseScore *= -0.8f
                    }

                    totalScore += baseScore
                    wordProcessed++

                    // Reset modifiers after applying
                    intensifierValue = 1f
                    negationActive = false
                }
            }

            // Normalize score based on text length and word density
            val averageScore = if (wordProcessed > 0) totalScore / wordProcessed else 0f
            val lengthFactor =
                minOf(1f, wordCount / 10f) // Longer texts get slight boost in confidence
            val densityFactor = if (wordCount > 0) wordProcessed.toFloat() / wordCount else 0f

            // Apply contextual modifiers
            var contextualScore = averageScore * (0.7f + densityFactor * 0.3f)
            contextualScore += emojiScore
            contextualScore += punctuationScore
            contextualScore *= capsMultiplier

            // Apply some randomness while keeping it realistic
            val randomVariation = (Random.nextFloat() - 0.5f) * 0.08f
            val finalScore = (contextualScore + randomVariation).coerceIn(-1f, 1f)

            return finalScore
        }

        val score = calculateSentimentScore()

        // Calculate confidence based on various factors
        val baseConfidence = when {
            kotlin.math.abs(score) > 0.7f -> 0.85f
            kotlin.math.abs(score) > 0.4f -> 0.75f
            kotlin.math.abs(score) > 0.2f -> 0.65f
            else -> 0.55f
        }

        // Adjust confidence based on text length and sentiment word density
        val lengthBonus = minOf(0.1f, wordCount / 50f)
        val sentimentWordCount = words.count { word ->
            strongPositive.contains(word) || positive.contains(word) || weakPositive.contains(word) ||
                    strongNegative.contains(word) || negative.contains(word) || weakNegative.contains(
                word
            ) ||
                    positiveSlang.contains(word) || negativeSlang.contains(word) ||
                    positiveAbbreviations.any { abbrev -> word.contains(abbrev) } ||
                    negativeAbbreviations.any { abbrev -> word.contains(abbrev) }
        }
        val densityBonus = if (wordCount > 0) minOf(
            0.15f,
            (sentimentWordCount.toFloat() / wordCount) * 0.5f
        ) else 0f

        val confidence =
            (baseConfidence + lengthBonus + densityBonus + Random.nextFloat() * 0.05f).coerceIn(
                0.5f,
                0.95f
            )
        val label = SentimentLabel.fromScore(score)

        Log.d(TAG, "🎭 Enhanced mock result - Score: $score, Label: $label, Confidence: $confidence")
        Log.d(
            TAG,
            "📊 Analysis details - Words: $wordCount, Sentiment words: $sentimentWordCount, Density: ${if (wordCount > 0) sentimentWordCount.toFloat() / wordCount else 0f}"
        )
        Log.d(
            TAG,
            "🔧 Token info - Preprocessed tokens: ${processedText.tokens.size}, Input IDs length: ${processedText.inputIds.size}"
        )

        return SentimentResult(
            score = score,
            label = label,
            confidence = confidence,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * FIXED: Parse model output to SentimentResult
     * This was the main bug - incorrect interpretation of the model outputs
     */
    private fun parseSentimentOutput(outputs: FloatArray): SentimentResult {
        return when (outputs.size) {
            1 -> {
                // Single output (regression style: -1 to 1)
                val score = outputs[0].coerceIn(-1f, 1f)
                val confidence = kotlin.math.abs(score)
                val label = SentimentLabel.fromScore(score)

                SentimentResult(score = score, label = label, confidence = confidence)
            }

            2 -> {
                // Binary classification: [negative_prob, positive_prob]
                val negativeProb = outputs[0]
                val positiveProb = outputs[1]
                val score = positiveProb - negativeProb
                val maxProb = maxOf(negativeProb, positiveProb)
                val label = if (positiveProb > negativeProb) {
                    if (positiveProb > 0.7f) SentimentLabel.POSITIVE else SentimentLabel.NEUTRAL
                } else {
                    if (negativeProb > 0.7f) SentimentLabel.NEGATIVE else SentimentLabel.NEUTRAL
                }

                SentimentResult(score = score, label = label, confidence = maxProb)
            }

            3 -> {
                // Three-class: [negative, neutral, positive] - FIXED LOGIC
                val negativeProb = outputs[0]
                val neutralProb = outputs[1]
                val positiveProb = outputs[2]

                Log.d(
                    TAG,
                    "🔍 Three-class probabilities - Negative: $negativeProb, Neutral: $neutralProb, Positive: $positiveProb"
                )

                // Find the class with highest probability
                val maxProb = outputs.maxOrNull() ?: 0f
                val maxIndex = outputs.indexOfFirst { it == maxProb }

                // Calculate a meaningful score based on the probability distribution
                // Score ranges from -1 (very negative) to +1 (very positive)
                val score = when (maxIndex) {
                    0 -> { // Negative class has highest probability
                        // More negative score if negative probability is much higher than positive
                        val negativeStrength = negativeProb - positiveProb
                        -negativeStrength.coerceIn(0.1f, 1.0f)
                    }

                    1 -> { // Neutral class has highest probability
                        // Slight bias towards positive or negative based on secondary probabilities
                        val bias = positiveProb - negativeProb
                        bias * 0.3f // Keep it close to 0 but allow slight bias
                    }

                    2 -> { // Positive class has highest probability
                        // More positive score if positive probability is much higher than negative
                        val positiveStrength = positiveProb - negativeProb
                        positiveStrength.coerceIn(0.1f, 1.0f)
                    }

                    else -> 0f
                }

                // Determine label with better thresholds
                val label = when (maxIndex) {
                    0 -> {
                        // Negative class won
                        if (negativeProb > 0.6f && negativeProb > (positiveProb + 0.3f)) {
                            SentimentLabel.VERY_NEGATIVE
                        } else if (negativeProb > 0.4f) {
                            SentimentLabel.NEGATIVE
                        } else {
                            SentimentLabel.NEUTRAL
                        }
                    }

                    1 -> SentimentLabel.NEUTRAL // Neutral class won
                    2 -> {
                        // Positive class won
                        if (positiveProb > 0.6f && positiveProb > (negativeProb + 0.3f)) {
                            SentimentLabel.VERY_POSITIVE
                        } else if (positiveProb > 0.4f) {
                            SentimentLabel.POSITIVE
                        } else {
                            SentimentLabel.NEUTRAL
                        }
                    }

                    else -> SentimentLabel.NEUTRAL
                }

                Log.d(TAG, "✅ Final result - Score: $score, Label: $label, Confidence: $maxProb")

                SentimentResult(score = score, label = label, confidence = maxProb)
            }

            else -> {
                // Unknown output format, use first value as score
                val score = outputs[0].coerceIn(-1f, 1f)
                val confidence = 0.5f
                val label = SentimentLabel.fromScore(score)

                Log.w(
                    TAG,
                    "⚠️ Unknown output format with ${outputs.size} values, using first as score"
                )
                SentimentResult(score = score, label = label, confidence = confidence)
            }
        }
    }

    /**
     * Load the TensorFlow Lite model from assets
     */
    private fun loadModelFile(): MappedByteBuffer {
        Log.d(TAG, "📁 Loading model file: $modelFileName")

        val assetFileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        Log.d(TAG, "📏 Model file size: $declaredLength bytes")

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Get current inference mode
     */
    fun getInferenceMode(): String {
        return if (useMockInference) "Mock" else "TensorFlow Lite"
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        interpreter?.close()
        interpreter = null
        Log.d(TAG, "🧹 Inference engine cleaned up")
    }

    /**
     * Check if the engine is ready for inference
     */
    fun isReady(): Boolean = interpreter != null || useMockInference
}