package com.flavicox.insurapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.flavicox.insurapp.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "¡Recordatorio de partido!"
        val hour = intent.getStringExtra("hour") ?: "tu reserva"
        val typeField = intent.getStringExtra("typeField") ?: "campo"
        val numberField = intent.getStringExtra("numberField") ?: "?"

        val message = "Tienes una reserva a las $hour en $typeField - Campo $numberField"

        val notification = NotificationCompat.Builder(context, "reserva_channel")
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Permiso para Android 13+
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        }
    }
}
