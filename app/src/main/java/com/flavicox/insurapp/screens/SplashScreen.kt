package com.flavicox.insurapp.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.flavicox.insurapp.R
import com.flavicox.insurapp.navigation.AppScreens
import com.flavicox.insurapp.viewmodel.AuthViewModel
import com.flavicox.insurapp.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(context))

    LaunchedEffect(Unit) {
        delay(1500) // Tiempo visible de splash
        val isLogged = viewModel.isLoggedIn()
        navController.navigate(
            if (isLogged) AppScreens.ListScreen.route else AppScreens.LoginScreen.route
        ) {
            popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Cargando...", fontSize = 18.sp)
    }
}
