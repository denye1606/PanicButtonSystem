package com.example.panicbutton

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.app.NotificationCompat

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun notify(room: RoomStatus) {
        val notification = NotificationCompat.Builder(context, PanicService.ALERT_CHANNEL_ID)
            .setContentTitle("EMERGENCY ALERT")
            .setContentText("${room.roomName.value}: ${room.patientName.value} is calling!")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, notification)
    }

    override fun stopSound() {
        toneGenerator?.stopTone()
    }

    override fun playTone(duration: Int) {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_S, duration)
        } catch (e: Exception) {}
    }

    override fun openMap(lat: Double, lng: Double) {
        val uri = "geo:$lat,$lng?q=$lat,$lng(Patient Location)"
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
