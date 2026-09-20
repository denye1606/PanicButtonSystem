package com.example.panicbutton

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class PanicService : Service() {

    companion object {
        const val CHANNEL_ID = "PanicServiceChannel"
        const val ALERT_CHANNEL_ID = "EmergencyAlertChannel"
        const val NOTIFICATION_ID = 1
    }

    private val panicManager: PanicManager
        get() = PanicApp.panicManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        panicManager.checkResetTimer()
        when (intent?.action) {
            "STOP_ALARM" -> {
                val roomId = intent.getIntExtra("ROOM_ID", -1)
                panicManager.roomsData.find { it.id == roomId }?.let { panicManager.stopAlarm(it) }
            }
            "TRIGGER_PANIC" -> {
                val roomId = intent.getIntExtra("ROOM_ID", -1)
                panicManager.roomsData.find { it.id == roomId }?.let { panicManager.triggerPanic(it) }
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Emergency Monitoring Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Emergency Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(true)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Panic System Active")
            .setContentText("Monitoring for emergency button presses...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
