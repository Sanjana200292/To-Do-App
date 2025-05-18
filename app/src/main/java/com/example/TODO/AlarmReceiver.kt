package com.example.TODO

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.example.TODO.view.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra("notificationId", 0) ?: 0
        val message = intent?.getStringExtra("message") ?: "Task Reminder"

        // Create an intent to launch MainActivity
        val i = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "RoutineCraft",
                "SleepWell Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = context?.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        // Check if the app has the required notification permission
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Build and show the notification
            val builder = NotificationCompat.Builder(context, "RoutineCraft")
                .setSmallIcon(R.drawable.ic_notification) // Ensure you add an appropriate icon
                .setContentTitle("SleepWell Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            try {
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                Toast.makeText(context, "Unable to send notification: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Permission was not granted, handle it accordingly (e.g., show a message or request permission)
            Toast.makeText(context, "Notification permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }
}
