package com.awesomenessstudios.vivian.sense

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.awesomenessstudios.vivian.sense.ml.SentimentAnalyzer
import com.awesomenessstudios.vivian.sense.ml.models.TextType
import com.awesomenessstudios.vivian.sense.ml.utils.ModelFileChecker
import com.awesomenessstudios.vivian.sense.ml.utils.SentimentTestHelper
import com.awesomenessstudios.vivian.sense.presentation.navigation.Screen
import com.awesomenessstudios.vivian.sense.presentation.navigation.SenseNavigation
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.AuthUiState
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.MainViewModel
import com.awesomenessstudios.vivian.sense.ui.theme.SENSETheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    @Inject
    lateinit var sentimentAnalyzer: SentimentAnalyzer
    @Inject lateinit var modelFileChecker: ModelFileChecker
    @Inject lateinit var sentimentTestHelper: SentimentTestHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Run comprehensive model diagnostics
        lifecycleScope.launch {
            runModelDiagnostics()
        }
        enableEdgeToEdge()
        setContent {
            SENSETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    SenseApp()

                }
            }
        }
    }


    private suspend fun runModelDiagnostics() {
        Log.d("MODEL_TEST", "🚀 Starting comprehensive model diagnostics...")

        // Step 1: Check model file
        val diagnosis = modelFileChecker.diagnoseModelFile()
        modelFileChecker.printDiagnosisReport(diagnosis)

        // Step 2: Test sentiment analyzer initialization
        Log.d("MODEL_TEST", "\n🔧 Testing sentiment analyzer...")
        val initSuccess = sentimentAnalyzer.initialize()
        Log.d("MODEL_TEST", "Initialization result: ${if (initSuccess) "✅ Success" else "❌ Failed"}")

        // Step 3: Print analyzer debug info
        Log.d("MODEL_TEST", sentimentAnalyzer.getDebugInfo())

        // Step 4: Run test cases
        if (sentimentAnalyzer.isReady()) {
            Log.d("MODEL_TEST", "\n🧪 Running sentiment tests...")

            // Quick test first
            val quickResult = sentimentAnalyzer.analyzeSentiment(
                textId = "quick_test",
                text = "I love this app!",
                textType = TextType.COMMENT
            )

            if (quickResult != null) {
                Log.d("MODEL_TEST", "✅ Quick test SUCCESS!")
                Log.d("MODEL_TEST", "   Text: 'I love this app!'")
                Log.d("MODEL_TEST", "   Result: ${quickResult.sentimentResult.label}")
                Log.d("MODEL_TEST", "   Score: ${quickResult.sentimentResult.score}")
                Log.d("MODEL_TEST", "   Confidence: ${quickResult.sentimentResult.confidence}")

                // Run full test suite
                sentimentTestHelper.runTests(lifecycleScope)
            } else {
                Log.e("MODEL_TEST", "❌ Quick test FAILED - result is null")
                troubleshootNullResult()
            }
        } else {
            Log.e("MODEL_TEST", "❌ Sentiment analyzer is not ready")
        }
    }

    private fun troubleshootNullResult() {
        Log.d("MODEL_TEST", "\n🔍 TROUBLESHOOTING NULL RESULT:")
        Log.d("MODEL_TEST", "1. Check if model file exists in app/src/main/assets/sentiment_model.tflite")
        Log.d("MODEL_TEST", "2. Verify the model file is a valid .tflite file")
        Log.d("MODEL_TEST", "3. Check that build.gradle.kts has: aaptOptions { noCompress \"tflite\" }")
        Log.d("MODEL_TEST", "4. The enhanced code should fallback to mock mode if model is missing")
        Log.d("MODEL_TEST", "5. If mock mode is not working, there may be an exception in the code")
    }
}


@Composable
fun SenseApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    // Handle navigation based on auth state
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Authenticated -> {

                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }

            is AuthUiState.Unauthenticated -> {
                navController.navigate(Screen.SignIn.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }

            else -> { /* Loading state, do nothing */
            }
        }
    }

    SenseNavigation(navController = navController, mainViewModel = mainViewModel)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SENSETheme {
//        Greeting("Android")
    }
}