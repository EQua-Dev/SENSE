package com.awesomenessstudios.vivian.sense

import android.app.Application
import android.util.Log
import com.awesomenessstudios.vivian.sense.ml.SentimentAnalyzer
import com.awesomenessstudios.vivian.sense.ml.models.TextType
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class SenseApplication: Application() {
    @Inject
    lateinit var sentimentAnalyzer: SentimentAnalyzer

    // Create an application-level coroutine scope
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        applicationScope.launch {
            sentimentAnalyzer.initialize()
            // Test the analyzer
            val result = sentimentAnalyzer.analyzeSentiment(
                textId = "test1",
                text = "I love this app!",
                textType = TextType.COMMENT
            )
            Log.d("TEST", "Sentiment: ${result?.sentimentResult?.label}")
        }
    }
}