package com.awesomenessstudios.vivian.sense

import android.os.Bundle
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
import androidx.navigation.compose.rememberNavController
import com.awesomenessstudios.vivian.sense.presentation.navigation.Screen
import com.awesomenessstudios.vivian.sense.presentation.navigation.SenseNavigation
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.AuthUiState
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.MainViewModel
import com.awesomenessstudios.vivian.sense.ui.theme.SENSETheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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