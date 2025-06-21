package com.flavicox.insurapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.flavicox.insurapp.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this) // ⬅️ Aquí
        setContent {
            AppNavigation()
        }
    }
}

@Preview
@Composable
fun MainPreview(){
    AppNavigation()
}

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "reserva_channel",
            "Recordatorios de reservas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notificaciones para recordarte tus partidos"
        }
        val manager: NotificationManager =
            context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}