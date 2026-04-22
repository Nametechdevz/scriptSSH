package com.iptvapp.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.iptvapp.player.domain.model.StreamType
import com.iptvapp.player.ui.screens.admin.AdminScreen
import com.iptvapp.player.ui.screens.home.HomeScreen
import com.iptvapp.player.ui.screens.login.LoginScreen
import com.iptvapp.player.ui.screens.player.PlayerScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Player : Screen("player/{streamId}/{streamType}/{title}/{extension}") {
        fun createRoute(streamId: Int, streamType: StreamType, title: String, extension: String = "m3u8") =
            "player/$streamId/${streamType.name}/${title.encodeUrlSafe()}/$extension"
    }
    object Admin : Screen("admin")
}

private fun String.encodeUrlSafe() = java.net.URLEncoder.encode(this, "UTF-8")
private fun String.decodeUrlSafe() = java.net.URLDecoder.decode(this, "UTF-8")

@Composable
fun IPTVNavGraph(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onChannelClick = { channel ->
                    navController.navigate(
                        Screen.Player.createRoute(channel.streamId, StreamType.LIVE, channel.name)
                    )
                },
                onMovieClick = { movie ->
                    navController.navigate(
                        Screen.Player.createRoute(
                            movie.streamId,
                            StreamType.VOD,
                            movie.name,
                            movie.containerExtension
                        )
                    )
                },
                onSeriesClick = { series ->
                    navController.navigate(
                        Screen.Player.createRoute(series.seriesId, StreamType.SERIES, series.name)
                    )
                },
                onAdminClick = { navController.navigate(Screen.Admin.route) }
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("streamId") { type = NavType.IntType },
                navArgument("streamType") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType },
                navArgument("extension") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val streamId = backStackEntry.arguments?.getInt("streamId") ?: 0
            val streamTypeStr = backStackEntry.arguments?.getString("streamType") ?: "LIVE"
            val title = backStackEntry.arguments?.getString("title")?.decodeUrlSafe() ?: ""
            val extension = backStackEntry.arguments?.getString("extension") ?: "m3u8"
            val streamType = StreamType.valueOf(streamTypeStr)

            PlayerScreen(
                streamId = streamId,
                streamType = streamType,
                title = title,
                extension = extension,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Admin.route) {
            AdminScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
