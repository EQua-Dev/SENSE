package com.awesomenessstudios.vivian.sense.ml.preprocessing


import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextPreprocessor @Inject constructor() {

    companion object {
        private const val MAX_SEQUENCE_LENGTH = 128
        private const val VOCAB_SIZE = 10000
    }

    /**
     * Preprocesses text for sentiment analysis model input
     */
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

    /**
     * Clean and normalize input text
     */
    private fun cleanText(text: String): String {
        return text
            .lowercase()
            .replace(Regex("[^a-zA-Z0-9\\s]"), "") // Remove special characters
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .trim()
    }

    /**
     * Simple tokenization (in production, use proper tokenizer like SentencePiece)
     */
    private fun tokenize(text: String): List<String> {
        return text.split(" ").filter { it.isNotBlank() }
    }

    /**
     * Pad or truncate sequence to fixed length
     */
    private fun padSequence(tokens: List<String>): List<String> {
        return when {
            tokens.size > MAX_SEQUENCE_LENGTH -> tokens.take(MAX_SEQUENCE_LENGTH)
            tokens.size < MAX_SEQUENCE_LENGTH -> tokens + List(MAX_SEQUENCE_LENGTH - tokens.size) { "[PAD]" }
            else -> tokens
        }
    }

    /**
     * Convert tokens to input IDs (simplified vocabulary mapping)
     */
    private fun convertToInputIds(tokens: List<String>): IntArray {
        // This is a simplified approach. In production, use proper vocabulary mapping
        val vocab = createSimpleVocab()
        return tokens.map { token ->
            vocab[token] ?: vocab["[UNK]"] ?: 0
        }.toIntArray()
    }

    /**
     * Create a simple vocabulary mapping (replace with proper vocab in production)
     */
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
}