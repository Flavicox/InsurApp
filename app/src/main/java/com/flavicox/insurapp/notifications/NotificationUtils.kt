package com.flavicox.insurapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun scheduleNotification(
    context: Context,
    title: String,
    dateTime: String,
    typeField: String,
    numberField: Int
) {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val futureDate = try {
        formatter.parse(dateTime)
    } catch (e: Exception) {
        e.printStackTrace()
        return
    }

    /*val triggerTime = Calendar.getInstance().apply {
        time = futureDate!!
        add(Calendar.HOUR_OF_DAY, -3)
    }.timeInMillis

     */

    //15 segundos
    val triggerTime = System.currentTimeMillis() + 15 * 1000

    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("hour", dateTime.substring(11, 16))
        putExtra("typeField", typeField.uppercase())
        putExtra("numberField", numberField.toString())
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        triggerTime.toInt(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // PEGA AQUÍ LA VERIFICACIÓN
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val canSchedule = alarmManager.canScheduleExactAlarms()
        if (!canSchedule) {
            Toast.makeText(
                context,
                "Para recibir notificaciones exactas, activa 'Permitir alarmas exactas' en ajustes.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // 🔸 Este bloque puede quedar o ir dentro del if anterior si prefieres condicionar su ejecución
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        triggerTime,
        pendingIntent
    )
}