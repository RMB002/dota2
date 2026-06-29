package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.HeroDetailScreen
import com.example.ui.screens.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    // Set layout direction to RTL for Persian localization
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    onHeroClick = { heroId ->
                        navController.navigate("hero_detail/$heroId")
                    }
                )
            }
            composable(
                route = "hero_detail/{heroId}",
                arguments = listOf(navArgument("heroId") { type = NavType.StringType })
            ) { backStackEntry ->
                val heroId = backStackEntry.arguments?.getString("heroId")
                HeroDetailScreen(
                    heroId = heroId,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
