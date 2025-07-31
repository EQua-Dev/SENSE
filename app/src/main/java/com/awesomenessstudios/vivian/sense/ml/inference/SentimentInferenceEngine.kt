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
        private const val INPUT_SIZE = 128
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
            isInitialized = false

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
//        return runRealInference(processedText)

        return if (useMockInference) {
            runMockInference(processedText)
        } else {
            runRealInference(processedText)
        }
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

                // Fill with input IDs
                val inputIds = processedText.inputIds.take(sequenceLength)
                inputIds.forEach { id -> putFloat(id.toFloat()) }

                // Pad if necessary
                repeat(sequenceLength - inputIds.size) {
                    putFloat(0f)
                }
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
    private fun runMockInference(processedText: ProcessedText): SentimentResult {
        Log.d(TAG, "🎭 Running mock inference...")

        // Simple rule-based mock sentiment analysis
        val text = processedText.cleanedText.lowercase()

        val positiveWords = listOf("good", "great", "awesome", "love", "excellent", "amazing", "wonderful", "fantastic", "perfect", "happy", "joy", "beautiful")
        val negativeWords = listOf("bad", "terrible", "awful", "hate", "horrible", "disgusting", "worst", "annoying", "stupid", "angry", "sad", "ugly")

        val positiveCount = positiveWords.count { text.contains(it) }
        val negativeCount = negativeWords.count { text.contains(it) }

        val score = when {
            positiveCount > negativeCount -> (0.3f + Random.nextFloat() * 0.7f) // 0.3 to 1.0
            negativeCount > positiveCount -> (-0.3f - Random.nextFloat() * 0.7f) // -1.0 to -0.3
            else -> (Random.nextFloat() - 0.5f) * 0.4f // -0.2 to 0.2
        }

        val confidence = 0.6f + Random.nextFloat() * 0.3f // 0.6 to 0.9
        val label = SentimentLabel.fromScore(score)

        Log.d(TAG, "🎭 Mock result - Score: $score, Label: $label, Confidence: $confidence")

        return SentimentResult(
            score = score,
            label = label,
            confidence = confidence
        )
    }

    /**
     * Parse model output to SentimentResult
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
                // Three-class: [negative, neutral, positive]
                val negativeProb = outputs[0]
                val neutralProb = outputs[1]
                val positiveProb = outputs[2]

                val score = positiveProb - negativeProb
                val maxProb = outputs.maxOrNull() ?: 0f
                val maxIndex = outputs.indexOfFirst { it == maxProb }

                val label = when (maxIndex) {
                    0 -> if (negativeProb > 0.7f) SentimentLabel.VERY_NEGATIVE else SentimentLabel.NEGATIVE
                    1 -> SentimentLabel.NEUTRAL
                    2 -> if (positiveProb > 0.7f) SentimentLabel.VERY_POSITIVE else SentimentLabel.POSITIVE
                    else -> SentimentLabel.NEUTRAL
                }

                SentimentResult(score = score, label = label, confidence = maxProb)
            }

            else -> {
                // Unknown output format, use first value as score
                val score = outputs[0].coerceIn(-1f, 1f)
                val confidence = 0.5f
                val label = SentimentLabel.fromScore(score)

                Log.w(TAG, "⚠️ Unknown output format with ${outputs.size} values, using first as score")
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