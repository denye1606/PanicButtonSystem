package com.example.panicbutton

import platform.AVFoundation.AVAudioPlayer
import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

class IosPlatformActions : PlatformActions {

    private var audioPlayer: AVAudioPlayer? = null

    init {
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        val center = UNUserNotificationCenter.currentNotificationCenter()

        center.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or
                    UNAuthorizationOptionSound or
                    UNAuthorizationOptionBadge
        ) { granted, error ->

            if (granted) {
                println("iOS notification permission granted")
            } else {
                println("iOS notification permission denied")
            }

            if (error != null) {
                println("Notification error: $error")
            }
        }
    }

    override fun notify(room: RoomStatus) {

        val content = UNMutableNotificationContent().apply {
            setTitle("EMERGENCY ALERT")

            setBody(
                "${room.roomName.value}: " +
                        "${room.patientName.value} is calling!"
            )

            setSound(UNNotificationSound.defaultSound())
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "Emergency_${room.id}_${NSDate().timeIntervalSince1970}",
            content = content,
            trigger = null
        )

        UNUserNotificationCenter
            .currentNotificationCenter()
            .addNotificationRequest(request) { error ->

                if (error != null) {
                    println("Notification error: $error")
                } else {
                    println("Emergency notification sent")
                }
            }
    }

    override fun stopSound() {
        audioPlayer?.stop()
        audioPlayer = null
    }

    override fun playTone(duration: Int) {
        // Vibrate for physical feedback
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
        println("Emergency tone requested for $duration ms (vibrating)")
    }

    override fun openMap(lat: Double, lng: Double) {

        val urlString =
            "http://maps.apple.com/?ll=$lat,$lng&q=Patient%20Location"

        val url = NSURL(string = urlString)

        if (url != null) {
            UIApplication.sharedApplication.openURL(
                url,
                options = emptyMap<Any?, Any?>(),
                completionHandler = null
            )
        }
    }
}
