package com.awesomenessstudios.vivian.sense.ml.utils

// utils/ModelFileChecker.kt

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelFileChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "ModelFileChecker"
        private const val MODEL_FILE_NAME = "sentiment_model.tflite"
    }

    /**
     * Comprehensive model file diagnostic
     */
    fun diagnoseModelFile(): ModelDiagnosis {
        Log.d(TAG, "🔍 Starting model file diagnosis...")

        val diagnosis = ModelDiagnosis()

        // Step 1: Check if file exists
        diagnosis.fileExists = checkFileExists()
        if (!diagnosis.fileExists) {
            Log.e(TAG, "❌ Model file '$MODEL_FILE_NAME' not found in assets/")
            return diagnosis
        }

        // Step 2: Check file size
        diagnosis.fileSize = getFileSize()
        Log.d(TAG, "📏 Model file size: ${diagnosis.fileSize} bytes")

        if (diagnosis.fileSize == 0L) {
            Log.e(TAG, "❌ Model file is empty")
            return diagnosis
        }

        // Step 3: Try to load as TensorFlow Lite model
        try {
            val modelBuffer = loadModelBuffer()
            diagnosis.canLoadBuffer = true

            // Step 4: Try to create interpreter
            val interpreter = Interpreter(modelBuffer)
            diagnosis.canCreateInterpreter = true

            // Step 5: Inspect model structure
            inspectModelStructure(interpreter, diagnosis)

            interpreter.close()
            Log.d(TAG, "✅ Model file diagnosis completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load or inspect model", e)
            diagnosis.error = e.message
        }

        return diagnosis
    }

    /**
     * Check if model file exists in assets
     */
    private fun checkFileExists(): Boolean {
        return try {
            context.assets.open(MODEL_FILE_NAME).use { true }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get model file size
     */
    private fun getFileSize(): Long {
        return try {
            context.assets.open(MODEL_FILE_NAME).use { it.available().toLong() }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Load model as byte buffer
     */
    private fun loadModelBuffer(): MappedByteBuffer {
        val assetFileDescriptor = context.assets.openFd(MODEL_FILE_NAME)
        val inputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Inspect model structure and tensors
     */
    private fun inspectModelStructure(interpreter: Interpreter, diagnosis: ModelDiagnosis) {
        // Input tensor info
        val inputTensor = interpreter.getInputTensor(0)
        diagnosis.inputShape = inputTensor.shape()
        diagnosis.inputDataType = inputTensor.dataType().toString()

        Log.d(TAG, "📊 Input tensor:")
        Log.d(TAG, "   Shape: ${diagnosis.inputShape?.contentToString()}")
        Log.d(TAG, "   Data type: ${diagnosis.inputDataType}")

        // Output tensor info
        val outputTensor = interpreter.getOutputTensor(0)
        diagnosis.outputShape = outputTensor.shape()
        diagnosis.outputDataType = outputTensor.dataType().toString()

        Log.d(TAG, "📊 Output tensor:")
        Log.d(TAG, "   Shape: ${diagnosis.outputShape?.contentToString()}")
        Log.d(TAG, "   Data type: ${diagnosis.outputDataType}")

        // Model metadata
        diagnosis.inputTensorCount = interpreter.inputTensorCount
        diagnosis.outputTensorCount = interpreter.outputTensorCount

        Log.d(TAG, "📊 Model info:")
        Log.d(TAG, "   Input tensors: ${diagnosis.inputTensorCount}")
        Log.d(TAG, "   Output tensors: ${diagnosis.outputTensorCount}")
    }

    /**
     * Print detailed diagnosis report
     */
    fun printDiagnosisReport(diagnosis: ModelDiagnosis) {
        Log.d(TAG, "\n" + "=".repeat(50))
        Log.d(TAG, "📋 MODEL FILE DIAGNOSIS REPORT")
        Log.d(TAG, "=".repeat(50))
        Log.d(TAG, "File exists: ${if (diagnosis.fileExists) "✅ Yes" else "❌ No"}")
        Log.d(TAG, "File size: ${diagnosis.fileSize} bytes")
        Log.d(TAG, "Can load buffer: ${if (diagnosis.canLoadBuffer) "✅ Yes" else "❌ No"}")
        Log.d(TAG, "Can create interpreter: ${if (diagnosis.canCreateInterpreter) "✅ Yes" else "❌ No"}")

        if (diagnosis.inputShape != null) {
            Log.d(TAG, "Input shape: ${diagnosis.inputShape?.contentToString()}")
            Log.d(TAG, "Input data type: ${diagnosis.inputDataType}")
        }

        if (diagnosis.outputShape != null) {
            Log.d(TAG, "Output shape: ${diagnosis.outputShape?.contentToString()}")
            Log.d(TAG, "Output data type: ${diagnosis.outputDataType}")
        }

        Log.d(TAG, "Input tensor count: ${diagnosis.inputTensorCount}")
        Log.d(TAG, "Output tensor count: ${diagnosis.outputTensorCount}")

        if (diagnosis.error != null) {
            Log.e(TAG, "Error: ${diagnosis.error}")
        }

        Log.d(TAG, "=".repeat(50))

        // Recommendations
        if (!diagnosis.fileExists) {
            Log.w(TAG, "💡 RECOMMENDATION: Download a model file and place it in app/src/main/assets/")
        } else if (!diagnosis.canCreateInterpreter) {
            Log.w(TAG, "💡 RECOMMENDATION: The model file may be corrupted or incompatible")
        } else {
            Log.i(TAG, "💡 RECOMMENDATION: Model file looks good!")
        }
    }
}

/**
 * Data class to hold diagnosis results
 */
data class ModelDiagnosis(
    var fileExists: Boolean = false,
    var fileSize: Long = 0L,
    var canLoadBuffer: Boolean = false,
    var canCreateInterpreter: Boolean = false,
    var inputShape: IntArray? = null,
    var inputDataType: String? = null,
    var outputShape: IntArray? = null,
    var outputDataType: String? = null,
    var inputTensorCount: Int = 0,
    var outputTensorCount: Int = 0,
    var error: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelDiagnosis

        if (fileExists != other.fileExists) return false
        if (fileSize != other.fileSize) return false
        if (canLoadBuffer != other.canLoadBuffer) return false
        if (canCreateInterpreter != other.canCreateInterpreter) return false
        if (inputShape != null) {
            if (other.inputShape == null) return false
            if (!inputShape.contentEquals(other.inputShape)) return false
        } else if (other.inputShape != null) return false
        if (inputDataType != other.inputDataType) return false
        if (outputShape != null) {
            if (other.outputShape == null) return false
            if (!outputShape.contentEquals(other.outputShape)) return false
        } else if (other.outputShape != null) return false
        if (outputDataType != other.outputDataType) return false
        if (inputTensorCount != other.inputTensorCount) return false
        if (outputTensorCount != other.outputTensorCount) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileExists.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + canLoadBuffer.hashCode()
        result = 31 * result + canCreateInterpreter.hashCode()
        result = 31 * result + (inputShape?.contentHashCode() ?: 0)
        result = 31 * result + (inputDataType?.hashCode() ?: 0)
        result = 31 * result + (outputShape?.contentHashCode() ?: 0)
        result = 31 * result + (outputDataType?.hashCode() ?: 0)
        result = 31 * result + inputTensorCount
        result = 31 * result + outputTensorCount
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}