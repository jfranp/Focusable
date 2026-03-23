package com.example.focusable.data.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.focusable.data.R
import com.example.focusable.domain.model.DistractionType

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "focusable_distraction_channel"
        private const val NOTIFICATION_ID_NOISE = 1001
        private const val NOTIFICATION_ID_MOTION = 1002
    }

    init {
        createChannel()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showDistractionNotification(type: DistractionType) {
        val (titleRes, messageRes, notificationId) = when (type) {
            DistractionType.NOISE -> Triple(
                R.string.notification_noise_title,
                R.string.notification_noise_message,
                NOTIFICATION_ID_NOISE
            )
            DistractionType.MOTION -> Triple(
                R.string.notification_motion_title,
                R.string.notification_motion_message,
                NOTIFICATION_ID_MOTION
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(messageRes))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
