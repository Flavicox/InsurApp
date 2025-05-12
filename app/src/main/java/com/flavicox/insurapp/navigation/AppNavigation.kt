package com.flavicox.insurapp.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.flavicox.insurapp.screens.*
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        // Esto se ejecuta una sola vez
        val isLogged = viewModel.isLoggedIn()
        startDestination = if (isLogged) {
            AppScreens.ListScreen.route
        } else {
            AppScreens.LoginScreen.route
        }
    }

    // Solo renderiza NavHost cuando ya se tiene el destino inicial
    startDestination?.let { route ->
        NavHost(navController = navController, startDestination = AppScreens.SplashScreen.route) {
            composable(AppScreens.SplashScreen.route) {
                SplashScreen(navController)
            }
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
            composable(AppScreens.ValidateCodeScreen.route) {
                ValidateCodeScreen(navController)
            }
            composable(AppScreens.HorarioScreen.route) {
                HorarioScreen(navController)
            }
        }
    }
}
