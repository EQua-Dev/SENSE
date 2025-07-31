package com.awesomenessstudios.vivian.sense.ml.preprocessing


import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextPreprocessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val vocab: Map<String, Int> by lazy { loadVocab() }

    companion object {
        private const val MAX_SEQUENCE_LENGTH = 100 // Matches your Python script's `maxlen`
        private const val VOCAB_FILE_NAME = "vocab.txt"
    }

    /**
     * Preprocesses text for sentiment analysis model input.
     */
    fun preprocessText(text: String): ProcessedText {
        val cleanedText = text.lowercase()
        val tokens = cleanedText.split("\\s+".toRegex())
        val inputIds = convertToInputIds(tokens)

        return ProcessedText(
            cleanedText = cleanedText,
            tokens = tokens,
            inputIds = inputIds
        )
    }

    /**
     * Loads the vocabulary from the `vocab.txt` asset file.
     */
    private fun loadVocab(): Map<String, Int> {
        val vocabMap = mutableMapOf<String, Int>()
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open(VOCAB_FILE_NAME)))
            reader.useLines { lines ->
                lines.forEachIndexed { index, word ->
                    vocabMap[word] = index
                }
            }
        } catch (e: Exception) {
            // Log the error and return a minimal vocab as a fallback
            // Log.e("TextPreprocessor", "Failed to load vocabulary", e)
            return mapOf("[PAD]" to 0, "[UNK]" to 1, "[CLS]" to 2, "[SEP]" to 3)
        }
        return vocabMap
    }

    /**
     * Converts a list of tokens into an array of integer IDs.
     */
    private fun convertToInputIds(tokens: List<String>): IntArray {
        val inputIds = tokens.map { token ->
            vocab[token] ?: vocab["[UNK]"] ?: 1
        }.toIntArray()

        return padSequence(inputIds)
    }

    /**
     * Pads the input IDs to the maximum sequence length.
     */
    private fun padSequence(ids: IntArray): IntArray {
        if (ids.size >= MAX_SEQUENCE_LENGTH) {
            return ids.sliceArray(0 until MAX_SEQUENCE_LENGTH)
        }
        return ids + IntArray(MAX_SEQUENCE_LENGTH - ids.size) { vocab["[PAD]"] ?: 0 }
    }
}

data class ProcessedText(
    val cleanedText: String,
    val tokens: List<String>,
    val inputIds: IntArray
)
/*
@Singleton
class TextPreprocessor @Inject constructor(
    @ApplicationContext private val context: Context // Add Context dependency
)
{

    companion object {
        private const val MAX_SEQUENCE_LENGTH = 128
        private const val VOCAB_SIZE = 10000
        private const val VOCAB_FILE_NAME = "vocab.txt"

    }


    // A lazy property to load the vocabulary only once
    private val vocab: Map<String, Int> by lazy {
        loadVocab()
    }

    */
/**
     * Preprocesses text for sentiment analysis model input
     *//*

    fun preprocessText(text: String): ProcessedText {
        val cleanedText = cleanText(text)
        val tokens = tokenize(cleanedText)
        val paddedTokens = padSequence(tokens)

        return ProcessedText(
            originalText = text,
            cleanedText = cleanedText,
            tokens = tokens,
            paddedTokens = paddedTokens,
            inputIds = convertToInputIds(paddedTokens)
        )
    }

    // This function replaces the old createSimpleVocab()
    private fun loadVocab(): Map<String, Int> {
        val vocabMap = mutableMapOf<String, Int>()
        try {
            context.assets.open(VOCAB_FILE_NAME).bufferedReader().useLines { lines ->
                lines.forEachIndexed { index, word ->
                    vocabMap[word] = index
                }
            }
        } catch (e: Exception) {
            // Handle file not found or other errors
            // Log.e(TAG, "Failed to load vocabulary file", e)
            // Fallback to a minimal vocab or throw an exception
            return createSimpleVocabFallback()
        }
        return vocabMap
    }

    */
/**
     * Clean and normalize input text
     *//*

    private fun cleanText(text: String): String {
        return text
            .lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "") // Remove special characters
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .trim()
    }

    */
/**
     * Simple tokenization (in production, use proper tokenizer like SentencePiece)
     *//*

    private fun tokenize(text: String): List<String> {
        return text.split(" ").filter { it.isNotBlank() }
    }

    */
/**
     * Pad or truncate sequence to fixed length
     *//*

    private fun padSequence(tokens: List<String>): List<String> {
        return when {
            tokens.size > MAX_SEQUENCE_LENGTH -> tokens.take(MAX_SEQUENCE_LENGTH)
            tokens.size < MAX_SEQUENCE_LENGTH -> tokens + List(MAX_SEQUENCE_LENGTH - tokens.size) { "[PAD]" }
            else -> tokens
        }
    }

    */
/**
     * Convert tokens to input IDs (simplified vocabulary mapping)
     *//*

    private fun convertToInputIds(tokens: List<String>): IntArray {
        // This is a simplified approach. In production, use proper vocabulary mapping
//        val vocab = createSimpleVocab()
        val vocab = loadVocab()
        return tokens.map { token ->
            vocab[token] ?: vocab["[UNK]"] ?: 0
        }.toIntArray()
    }

    */
/**
     * A fallback for when the actual vocab file is not found
     *//*

    private fun createSimpleVocabFallback(): Map<String, Int> {
        return mapOf(
            "[PAD]" to 0,
            "[UNK]" to 1,
            // ... any other special tokens your model expects
        )
    }

    */
/**
     * Create a simple vocabulary mapping (replace with proper vocab in production)
     *//*

    private fun createSimpleVocab(): Map<String, Int> {
        // This is a placeholder. In production, load from actual model vocabulary
        return mapOf(
            "[PAD]" to 0,
            "[UNK]" to 1,
            "[CLS]" to 2,
            "[SEP]" to 3,
            // Add more vocabulary mappings based on your model
        )
    }
}

data class ProcessedText(
    val originalText: String,
    val cleanedText: String,
    val tokens: List<String>,
    val paddedTokens: List<String>,
    val inputIds: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessedText

        if (originalText != other.originalText) return false
        if (cleanedText != other.cleanedText) return false
        if (tokens != other.tokens) return false
        if (paddedTokens != other.paddedTokens) return false
        if (!inputIds.contentEquals(other.inputIds)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalText.hashCode()
        result = 31 * result + cleanedText.hashCode()
        result = 31 * result + tokens.hashCode()
        result = 31 * result + paddedTokens.hashCode()
        result = 31 * result + inputIds.contentHashCode()
        return result
    }
}*/
