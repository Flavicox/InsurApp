package com.flavicox.insurapp.navigation

sealed class AppScreens(val route: String){
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")
    object RegisterSecondScreen: AppScreens("registar2_screen")
}