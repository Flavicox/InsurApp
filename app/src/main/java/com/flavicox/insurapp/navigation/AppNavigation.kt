package com.flavicox.insurapp.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.flavicox.insurapp.model.ReservationResponse
import com.flavicox.insurapp.screens.*
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import com.flavicox.insurapp.viewmodel.FieldsViewModel
import com.flavicox.insurapp.viewmodel.FieldsViewModelFactory
import com.google.gson.Gson


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val isLogged = viewModel.isLoggedIn()
        startDestination = if (!isLogged) {
            AppScreens.LoginScreen.route
        } else {
            val role = viewModel.getUserRole()
            if (role == "ROLE_ADMIN") AppScreens.AdminScreen.route
            else AppScreens.ListScreen.route
        }
    }

    startDestination?.let { route ->
        NavHost(navController = navController, startDestination = route) {
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
            composable(AppScreens.AdminScreen.route) {
                AdminScreen(navController)
            }
            composable(AppScreens.ScannerScreen.route) {
                ScannerScreen(navController)
            }
            // HorarioScreen con args...
            composable(
                "${AppScreens.HorarioScreen.route}/{fieldId}/{fieldPrice}/{typeField}/{numberField}",
                arguments = listOf(
                    navArgument("fieldId") { type = NavType.IntType },
                    navArgument("fieldPrice") { type = NavType.IntType },
                    navArgument("typeField") { type = NavType.StringType },
                    navArgument("numberField") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val fieldId    = backStackEntry.arguments?.getInt("fieldId")    ?: 0
                val fieldPrice = backStackEntry.arguments?.getInt("fieldPrice") ?: 0
                val typeField  = backStackEntry.arguments?.getString("typeField") ?: ""
                val numberField= backStackEntry.arguments?.getInt("numberField") ?: 0

                // Reconstruimos el fieldTitle aquí
                val fieldTitle = "$typeField - Campo $numberField"

                HorarioScreen(
                    navController   = navController,
                    fieldId         = fieldId,
                    fieldTitle      = fieldTitle,
                    fieldPrice      = fieldPrice,
                    typeField       = typeField,
                    numberField     = numberField
                )
            }
            // Ruta para ResumeScreen que ahora acepta 3 argumentos:
            composable(
                route = "${AppScreens.ResumeScreen.route}/{reservationJson}",
                arguments = listOf(navArgument("reservationJson") { type = NavType.StringType })
            ) { backStackEntry ->
                val json = backStackEntry.arguments?.getString("reservationJson") ?: ""
                val reservation = Gson().fromJson(json, ReservationResponse::class.java)
                ResumeScreen(navController = navController, reservation = reservation)
            }



            composable(AppScreens.ValidateCodeScreen.route) {
                ValidateCodeScreen(navController)
            }
            composable(
                "${AppScreens.PaymentScreen.route}/{fieldLabel}/{date}/{time}/{price}/{isHalfPayment}",
                arguments = listOf(
                    navArgument("fieldLabel") { type = NavType.StringType },
                    navArgument("date") { type = NavType.StringType },
                    navArgument("time") { type = NavType.StringType },
                    navArgument("price") { type = NavType.IntType },
                    navArgument("isHalfPayment") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val fieldLabel = backStackEntry.arguments?.getString("fieldLabel") ?: ""
                val date = backStackEntry.arguments?.getString("date") ?: ""
                val time = backStackEntry.arguments?.getString("time") ?: ""
                val price = backStackEntry.arguments?.getInt("price") ?: 0
                val isHalfPayment = backStackEntry.arguments?.getBoolean("isHalfPayment") ?: false

                PaymentScreen(
                    navController = navController,
                    fieldLabel = fieldLabel,
                    date = date,
                    time = time,
                    price = price,
                    isHalfPayment = isHalfPayment
                )
            }
            composable(AppScreens.ConfirmationScreen.route) {
                ConfirmationScreen(navController)
            }
            composable(AppScreens.SplashScreen.route) {
                SplashScreen(navController)
            }
            // ↘ Ajuste PayScreen: cinco parámetros
            composable(
                "${AppScreens.PayScreen.route}/{fieldId}/{selectedDate}/{selectedTime}/{price}/{isHalfPayment}",
                arguments = listOf(
                    navArgument("fieldId")       { type = NavType.IntType },
                    navArgument("selectedDate")  { type = NavType.StringType },
                    navArgument("selectedTime")  { type = NavType.StringType },
                    navArgument("price")         { type = NavType.IntType },
                    navArgument("isHalfPayment") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val fieldId       = backStackEntry.arguments?.getInt("fieldId") ?: 0
                val selectedDate  = backStackEntry.arguments?.getString("selectedDate") ?: ""
                val selectedTime  = backStackEntry.arguments?.getString("selectedTime") ?: ""
                val price         = backStackEntry.arguments?.getInt("price") ?: 0
                val isHalfPayment = backStackEntry.arguments?.getBoolean("isHalfPayment") ?: false

                PayScreen(
                    navController   = navController,
                    fieldId         = fieldId,
                    selectedDate    = selectedDate,
                    selectedTime    = selectedTime,
                    price           = price,
                    isHalfPayment   = isHalfPayment
                )
            }

            composable(
                "${AppScreens.ValidateReservationScreen.route}/{reservationId}",
                arguments = listOf(
                    navArgument("reservationId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val reservationId = backStackEntry.arguments?.getInt("reservationId") ?: 0
                ValidateReservationScreen(
                    navController   = navController,
                    reservationId   = reservationId
                )
            }
            composable(AppScreens.ProfileScreen.route) {
                ProfileScreen(navController)
            }
            composable(AppScreens.EditProfileScreen.route) {
                EditProfileScreen(navController)
            }
            composable(AppScreens.ChangePasswordScreen.route) {
                ChangePasswordScreen(navController)
            }
        }
    }
}
