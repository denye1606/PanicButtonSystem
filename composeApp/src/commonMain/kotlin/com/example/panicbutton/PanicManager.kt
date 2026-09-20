package com.example.panicbutton

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.database.database
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface PlatformActions {
    fun notify(room: RoomStatus)
    fun stopSound()
    fun playTone(duration: Int)
    fun openMap(lat: Double, lng: Double)
}

class PanicManager(
    private val scope: CoroutineScope,
    private val platformActions: PlatformActions,
    private val persistenceManager: PersistenceManager = PersistenceManager()
) {
    val roomsData = mutableStateListOf(
        RoomStatus(1, mutableStateOf("Room 101"), mutableStateOf("Patient A")),
        RoomStatus(2, mutableStateOf("Room 102"), mutableStateOf("Patient B")),
        RoomStatus(3, mutableStateOf("Room 103"), mutableStateOf("Patient C")),
        RoomStatus(4, mutableStateOf("Room 104"), mutableStateOf("Patient D"))
    )
    
    val historyLog = mutableStateListOf<HistoryEntry>()
    var selectedSound = mutableStateOf(SoundType.MORSE_SOS)
    
    private var alarmJob: Job? = null

    init {
        loadPersistentData()
        checkResetTimer()
        listenToESP32()
    }

    private fun loadPersistentData() {
        roomsData.forEach { room ->
            room.pushCount.value = persistenceManager.getPushCount(room.id)
        }
        historyLog.clear()
        historyLog.addAll(persistenceManager.getHistory())
    }

    fun checkResetTimer() {
        val lastReset = persistenceManager.getLastResetTime()
        val now = Clock.System.now().toEpochMilliseconds()
        val twentyFourHours = 24 * 60 * 60 * 1000L

        if (lastReset == 0L) {
            persistenceManager.saveLastResetTime(now)
        } else if (now - lastReset >= twentyFourHours) {
            performDailyReset()
        }
    }

    private fun performDailyReset() {
        val summary = roomsData.joinToString(", ") { "${it.roomName.value}: ${it.pushCount.value}" }
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val timeStr = "${now.year}-${now.monthNumber}-${now.dayOfMonth}"
        
        historyLog.add(0, HistoryEntry("Daily Summary", timeStr, "Total Calls: $summary"))
        
        roomsData.forEach { it.pushCount.value = 0 }
        persistenceManager.clearCounts(roomsData.size)
        persistenceManager.saveLastResetTime(Clock.System.now().toEpochMilliseconds())
        persistenceManager.saveHistory(historyLog)
    }

    private fun listenToESP32() {
        try {
            val database = Firebase.database
            roomsData.forEachIndexed { index, room ->
                val roomNumber = index + 1
                val roomPath = "emergency/room$roomNumber/isTriggered"
                val locationPath = "emergency/room$roomNumber/location"
                
                val roomRef = database.reference(roomPath)
                val locationRef = database.reference(locationPath)
                
                scope.launch {
                    try {
                        roomRef.valueEvents.collect { snapshot ->
                            val isTriggered = snapshot.value<Boolean>()
                            if (isTriggered) {
                                triggerPanic(room)
                                roomRef.setValue(false)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Listen for location updates (specifically useful for Room 4)
                scope.launch {
                    try {
                        locationRef.valueEvents.collect { snapshot ->
                            if (snapshot.exists) {
                                val lat = snapshot.child("lat").value<Double?>()
                                val lng = snapshot.child("lng").value<Double?>()
                                room.latitude.value = lat
                                room.longitude.value = lng
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerPanic(room: RoomStatus) {
        if (!room.isPanicking.value) {
            room.isPanicking.value = true
            room.startTime.value = Clock.System.now().toEpochMilliseconds()
            room.pushCount.value++
            persistenceManager.savePushCount(room.id, room.pushCount.value)
            
            // Notification handling would be via an interface
            platformActions.notify(room)
            
            if (alarmJob == null || alarmJob?.isActive == false) {
                alarmJob = scope.launch {
                    playAlarmLoop()
                }
            }
        }
    }

    fun stopAlarm(room: RoomStatus) {
        if (room.isPanicking.value) {
            val duration = (Clock.System.now().toEpochMilliseconds() - room.startTime.value) / 1000
            room.isPanicking.value = false

            val anyOtherPanicking = roomsData.any { it.isPanicking.value }
            if (!anyOtherPanicking) {
                alarmJob?.cancel()
                alarmJob = null
                platformActions.stopSound()
            }

            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val timeStr = "${now.hour}:${now.minute}:${now.second}"
            historyLog.add(0, HistoryEntry(room.patientName.value, timeStr, "$duration sec"))
            persistenceManager.saveHistory(historyLog)
        }
    }

    private suspend fun playAlarmLoop() {
        while (true) {
            yield()
            when (selectedSound.value) {
                SoundType.MORSE_SOS -> playMorseSOS()
                SoundType.CONTINUOUS_SIREN -> playContinuousSiren()
                SoundType.FAST_BEEPS -> playFastBeeps()
                SoundType.PULSE -> playPulse()
            }
        }
    }

    private suspend fun playMorseSOS() {
        repeat(3) { platformActions.playTone(200); delay(200) }
        delay(400)
        repeat(3) { platformActions.playTone(600); delay(200) }
        delay(400)
        repeat(3) { platformActions.playTone(200); delay(200) }
        delay(2000)
    }

    private suspend fun playContinuousSiren() { platformActions.playTone(1000); delay(1100) }
    private suspend fun playFastBeeps() { platformActions.playTone(100); delay(200) }
    private suspend fun playPulse() { platformActions.playTone(400); delay(800) }

    fun openMapForRoom(room: RoomStatus) {
        val lat = room.latitude.value
        val lng = room.longitude.value
        if (lat != null && lng != null) {
            platformActions.openMap(lat, lng)
        }
    }
}
