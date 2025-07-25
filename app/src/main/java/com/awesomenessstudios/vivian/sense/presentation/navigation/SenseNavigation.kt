package com.awesomenessstudios.vivian.sense.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.AuthUiState
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.SignInScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.SignUpScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.home.HomeScreen
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.MainViewModel

@Composable
fun SenseNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthUiState.Authenticated -> Screen.Home.route
            is AuthUiState.Unauthenticated -> Screen.SignIn.route
            else -> Screen.SignIn.route
        }
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToSignIn = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSignOut = {
                    mainViewModel.signOut()
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}