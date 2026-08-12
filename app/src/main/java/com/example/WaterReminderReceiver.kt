package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class WaterReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        showWaterNotification(context)

        // Reschedule next notification if interval is stored
        val prefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("water_reminder_enabled", false)
        val intervalHours = prefs.getInt("water_reminder_interval", 2)

        if (isEnabled) {
            WaterReminderScheduler.scheduleWaterReminder(context, intervalHours)
        }
    }

    companion object {
        const val CHANNEL_ID = "water_hydration_reminders_channel"
        const val NOTIFICATION_ID = 1001

        fun showWaterNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Hydration & Water Intake Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminds you to log water and stay hydrated throughout the day."
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💧 Time to Drink Water!")
                .setContentText("Stay hydrated and meet your daily goal! Tap to log a glass now.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build()

            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }
}
