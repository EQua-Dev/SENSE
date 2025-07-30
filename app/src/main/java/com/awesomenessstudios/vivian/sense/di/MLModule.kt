package com.awesomenessstudios.vivian.sense.di


import android.content.Context
import com.awesomenessstudios.vivian.sense.ml.SentimentAnalyzer
import com.awesomenessstudios.vivian.sense.ml.inference.SentimentInferenceEngine
import com.awesomenessstudios.vivian.sense.ml.preprocessing.TextPreprocessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MLModule {

    @Provides
    @Singleton
    fun provideTextPreprocessor(): TextPreprocessor {
        return TextPreprocessor()
    }

    @Provides
    @Singleton
    fun provideSentimentInferenceEngine(
        @ApplicationContext context: Context
    ): SentimentInferenceEngine {
        return SentimentInferenceEngine(context)
    }

    @Provides
    @Singleton
    fun provideSentimentAnalyzer(
        textPreprocessor: TextPreprocessor,
        inferenceEngine: SentimentInferenceEngine
    ): SentimentAnalyzer {
        return SentimentAnalyzer(textPreprocessor, inferenceEngine)
    }
}