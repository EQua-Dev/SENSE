package com.awesomenessstudios.vivian.sense.presentation.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.awesomenessstudios.vivian.sense.presentation.ui.analytics.AnalyticsScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.AuthUiState
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.SignInScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.auth.SignUpScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.home.HomeScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.notifications.NotificationsScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.post.CreatePostScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.post.PostDetailScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.profile.ProfileScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.search.SearchScreen
import com.awesomenessstudios.vivian.sense.presentation.ui.settings.SettingsScreen
import com.awesomenessstudios.vivian.sense.presentation.viewmodels.MainViewModel

@Composable
fun SenseNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        modifier = Modifier.padding(vertical = 12.dp),
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
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
//                currentUser = ,
                onSignOut = {
                    mainViewModel.signOut()
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToAnalytics = { navController.navigate(Screen.Analytics.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToPostDetail = {
                    navController.navigate(
                        Screen.PostDetail.route.replace(
                            "{postId}",
                            it
                        )
                    )
                }
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },

                )
        }
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->

            val postId = backStackEntry.arguments?.getString("postId") ?: ""

            PostDetailScreen(postId = postId)
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },

                )
        }
        composable(Screen.Notifications.route) {
            NotificationsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },

                )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },

                )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },

                )
        }
        composable(Screen.Analytics.route) {
            AnalyticsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },

                )
        }
    }
}