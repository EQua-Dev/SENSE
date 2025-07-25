package com.awesomenessstudios.vivian.sense.presentation.navigation

sealed class Screen(val route: String) {
    data object SignIn : Screen("sign_in")
    data object SignUp : Screen("sign_up")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object CreatePost : Screen("create_post")
    data object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
}
