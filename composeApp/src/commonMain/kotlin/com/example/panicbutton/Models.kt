package com.example.panicbutton

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.serialization.Serializable

enum class SoundType(val displayName: String) {
    MORSE_SOS("Morse SOS"),
    CONTINUOUS_SIREN("Continuous Siren"),
    FAST_BEEPS("Fast Beeps"),
    PULSE("Rhythmic Pulse")
}

data class RoomStatus(
    val id: Int,
    var roomName: MutableState<String>,
    var patientName: MutableState<String>,
    var isPanicking: MutableState<Boolean> = mutableStateOf(false),
    var startTime: MutableState<Long> = mutableStateOf(0L),
    var pushCount: MutableState<Int> = mutableIntStateOf(0),
    var latitude: MutableState<Double?> = mutableStateOf(null),
    var longitude: MutableState<Double?> = mutableStateOf(null)
)

@Serializable
data class HistoryEntry(
    val patientName: String,
    val timestamp: String,
    val responseTime: String
)
