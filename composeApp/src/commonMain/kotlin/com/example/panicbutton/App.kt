package com.example.panicbutton

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App(panicManager: PanicManager) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        var currentScreen by remember { mutableStateOf("login") }

        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
            if (currentScreen == "login") {
                LoginScreen(onLoginSuccess = { currentScreen = "dashboard" })
            } else {
                MainDashboard(
                    rooms = panicManager.roomsData,
                    history = panicManager.historyLog,
                    currentSound = panicManager.selectedSound.value,
                    onSoundChange = { panicManager.selectedSound.value = it },
                    onTrigger = { room -> panicManager.triggerPanic(room) },
                    onStop = { room -> panicManager.stopAlarm(room) },
                    onViewLocation = { room -> panicManager.openMapForRoom(room) },
                    onExportPdf = { /* Export PDF logic (platform specific) */ }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.HealthAndSafety, null, Modifier.size(90.dp), Color(0xFF8AB4F8))
        Text("Caregiver Login", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = user, onValueChange = { user = it },
            label = { Text("ID") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF8AB4F8))
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = pass, onValueChange = { pass = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF8AB4F8))
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { if (user == "admin" && pass == "1234") onLoginSuccess() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8AB4F8))
        ) {
            Text("LOGIN", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SoundSettingsDialog(
    currentSound: SoundType,
    onSoundChange: (SoundType) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Alarm Sound") },
        text = {
            Column {
                SoundType.entries.forEach { sound ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (sound == currentSound),
                                onClick = { onSoundChange(sound) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (sound == currentSound),
                            onClick = { onSoundChange(sound) }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(sound.displayName, fontSize = 18.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE")
            }
        }
    )
}

@Composable
fun FrequencySummary(rooms: List<RoomStatus>) {
    val mostActive = rooms.maxByOrNull { it.pushCount.value }
    if (mostActive != null && mostActive.pushCount.value > 0) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            border = BorderStroke(1.dp, Color(0xFF8AB4F8))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color(0xFF8AB4F8))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("High Frequency Alert", color = Color(0xFF8AB4F8), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        "${mostActive.roomName.value} (${mostActive.patientName.value}) has called ${mostActive.pushCount.value} times.",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    rooms: List<RoomStatus>,
    history: List<HistoryEntry>,
    currentSound: SoundType,
    onSoundChange: (SoundType) -> Unit,
    onTrigger: (RoomStatus) -> Unit,
    onStop: (RoomStatus) -> Unit,
    onViewLocation: (RoomStatus) -> Unit,
    onExportPdf: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var historyFilter by remember { mutableStateOf("All Alerts") }
    var filterExpanded by remember { mutableStateOf(false) }

    if (showSettings) {
        SoundSettingsDialog(
            currentSound = currentSound,
            onSoundChange = onSoundChange,
            onDismiss = { showSettings = false }
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF1E1E1E))) {
                CenterAlignedTopAppBar(
                    title = { Text("PANIC SYSTEM", fontWeight = FontWeight.Black, color = Color(0xFF8AB4F8)) },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1E1E1E))
                )
                TabRow(selectedTabIndex = selectedTab, containerColor = Color(0xFF1E1E1E), contentColor = Color.White) {
                    Tab(selectedTab == 0, { selectedTab = 0 }, text = { Text("BEDS", fontWeight = FontWeight.Bold) })
                    Tab(selectedTab == 1, { selectedTab = 1 }, text = { Text("HISTORY", fontWeight = FontWeight.Bold) })
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(Color(0xFF121212))) {
            if (selectedTab == 0) {
                LazyColumn(Modifier.fillMaxSize().padding(16.dp)) {
                    item { FrequencySummary(rooms) }
                    items(rooms) { room ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = if (room.isPanicking.value) Color(0xFFB00020) else Color(0xFF2D2D2D))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(room.roomName.value, color = Color.Gray, fontSize = 12.sp)
                                        if (room.pushCount.value > 0) {
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                color = Color(0xFF8AB4F8),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "Calls: ${room.pushCount.value}",
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    fontSize = 10.sp,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    BasicTextField(
                                        value = room.patientName.value,
                                        onValueChange = { room.patientName.value = it },
                                        textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                                if (room.isPanicking.value) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        if (room.latitude.value != null && room.longitude.value != null) {
                                            TextButton(
                                                onClick = { onViewLocation(room) },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                                            ) {
                                                Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text("LOCATION", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Button({ onStop(room) }, colors = ButtonDefaults.buttonColors(containerColor = Color.White)) {
                                            Text("STOP", color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    IconButton({ onTrigger(room) }) {
                                        Icon(Icons.Default.NotificationsActive, null, tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val filteredHistory = if (historyFilter == "Daily Reports (24h)") {
                    history.filter { it.patientName == "Daily Summary" }
                } else {
                    history
                }

                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Box(Modifier.fillMaxWidth()) {
                        Row(Modifier.align(Alignment.CenterEnd)) {
                            IconButton(onClick = onExportPdf) {
                                Icon(Icons.Default.PictureAsPdf, "Export PDF", tint = Color(0xFF8AB4F8))
                            }
                            
                            OutlinedButton(
                                onClick = { filterExpanded = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8AB4F8)),
                                border = BorderStroke(1.dp, Color(0xFF8AB4F8))
                            ) {
                                Icon(Icons.Default.FilterList, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(historyFilter)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                        }

                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false },
                            modifier = Modifier.background(Color(0xFF2D2D2D))
                        ) {
                            listOf("All Alerts", "Daily Reports (24h)").forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter, color = Color.White) },
                                    onClick = {
                                        historyFilter = filter
                                        filterExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                                Text("Patient", Modifier.weight(1f), color = Color.Gray)
                                Text("Time/Date", Modifier.weight(1f), color = Color.Gray)
                                Text("Resp/Total", color = Color.Gray)
                            }
                        }
                        items(filteredHistory) { entry ->
                            val isSummary = entry.patientName == "Daily Summary"
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSummary) Color(0xFF1E1E1E) else Color.Transparent
                                ),
                                border = if (isSummary) BorderStroke(1.dp, Color(0xFF8AB4F8)) else null
                            ) {
                                Row(Modifier.fillMaxWidth().padding(12.dp)) {
                                    Text(
                                        entry.patientName,
                                        Modifier.weight(1f),
                                        color = if (isSummary) Color(0xFF8AB4F8) else Color.White,
                                        fontWeight = if (isSummary) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(entry.timestamp, Modifier.weight(1f), color = Color.Gray)
                                    Text(
                                        entry.responseTime,
                                        color = if (isSummary) Color.White else Color(0xFF8AB4F8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
