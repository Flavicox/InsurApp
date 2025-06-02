package com.flavicox.insurapp.navigation

sealed class AppScreens(val route: String){
    object LoginScreen: AppScreens("login_screen")
    object RegisterScreen: AppScreens("register_screen")
    object RegisterSecondScreen: AppScreens("registar2_screen")
    object ListScreen: AppScreens("list_screen")
    object ValidateCodeScreen: AppScreens("validate_code_screen")
    object HorarioScreen: AppScreens("horario_screen")
    object SplashScreen : AppScreens("splash_screen")
    object ResumeScreen : AppScreens("resume_screen")
    object PaymentScreen : AppScreens("payment_screen")
    object ConfirmationScreen : AppScreens("confirmation_screen")
    object AdminScreen : AppScreens("admin_screen")
    object ScannerScreen : AppScreens("scanner_screen")
    object PayScreen : AppScreens("pay_screen")
    object ValidateReservationScreen:  AppScreens("validate_reservation")

}