package com.flavicox.insurapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.flavicox.insurapp.screens.HorarioScreen
import com.flavicox.insurapp.screens.ListScreen
import com.flavicox.insurapp.screens.LoginScreen
import com.flavicox.insurapp.screens.RegisterScreen
import com.flavicox.insurapp.screens.RegisterSecondScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppScreens.LoginScreen.route) {
        composable(AppScreens.LoginScreen.route) {
            LoginScreen(navController)
        }
        composable(AppScreens.RegisterScreen.route) {
            RegisterScreen(navController)
        }
        composable(AppScreens.RegisterSecondScreen.route) {
            RegisterSecondScreen(navController)
        }
        composable(AppScreens.ListScreen.route) {
            ListScreen(navController)
        }
        composable(AppScreens.HorarioScreen.route) {
            HorarioScreen(navController)
        }
    }
}
