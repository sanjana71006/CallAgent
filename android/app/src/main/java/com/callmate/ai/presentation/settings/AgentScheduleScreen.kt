package com.callmate.ai.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScheduleScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settingsState by viewModel.uiState.collectAsState()
    val schedule = settingsState.settings

    var isEnabled by remember(schedule.scheduledAgentEnabled) {
        mutableStateOf(schedule.scheduledAgentEnabled)
    }
    var startText by remember(schedule.scheduledAgentStartMinutes) {
        mutableStateOf(formatMinutes(schedule.scheduledAgentStartMinutes))
    }
    var endText by remember(schedule.scheduledAgentEndMinutes) {
        mutableStateOf(formatMinutes(schedule.scheduledAgentEndMinutes))
    }
    var error by remember { mutableStateOf<String?>(null) }
    var showSavedMessage by remember { mutableStateOf(false) }

    val currentMinutes = remember {
        val cal = Calendar.getInstance()
        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }

    val isCurrentlyActive = remember(isEnabled, schedule.scheduledAgentStartMinutes, schedule.scheduledAgentEndMinutes) {
        if (!isEnabled) false
        else {
            val start = schedule.scheduledAgentStartMinutes
            val end = schedule.scheduledAgentEndMinutes
            if (start <= end) currentMinutes in start..end
            else currentMinutes >= start || currentMinutes <= end
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Call Schedule", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val range = parseRange(startText, endText)
                        if (range == null) {
                            error = "Please enter valid 24-hour times (e.g. 22:00 or 07:30)."
                        } else {
                            error = null
                            viewModel.updateScheduledAgent(isEnabled, range.first, range.second)
                            showSavedMessage = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Schedule Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Live Status Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (isCurrentlyActive) {
                    MaterialTheme.colorScheme.primaryContainer
                } else if (isEnabled) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isCurrentlyActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                color = if (isCurrentlyActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = if (isCurrentlyActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCurrentlyActive) "🟢 ACTIVE RIGHT NOW" else if (isEnabled) "⏰ SCHEDULED (${startText} - ${endText})" else "⚪ SCHEDULE INACTIVE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isCurrentlyActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isCurrentlyActive) {
                                "AI is currently answering ALL incoming calls automatically (known contacts & unknown numbers)."
                            } else if (isEnabled) {
                                "AI will automatically intercept and handle all calls between $startText and $endText."
                            } else {
                                "Enable this feature to have AI handle calls during your sleep, work, or focus hours."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Main Switch Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Handle All Calls in Schedule",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Screens both saved contacts and unknown callers",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
            }

            // Quick Preset Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "QUICK PRESETS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(
                        title = "🌙 Night (10PM-7AM)",
                        isSelected = startText == "22:00" && endText == "07:00",
                        onClick = {
                            startText = "22:00"
                            endText = "07:00"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        title = "💼 Work (9AM-5PM)",
                        isSelected = startText == "09:00" && endText == "17:00",
                        onClick = {
                            startText = "09:00"
                            endText = "17:00"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetChip(
                        title = "🍽️ Lunch (1PM-2PM)",
                        isSelected = startText == "13:00" && endText == "14:00",
                        onClick = {
                            startText = "13:00"
                            endText = "14:00"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetChip(
                        title = "⚡ 24/7 Always On",
                        isSelected = startText == "00:00" && endText == "23:59",
                        onClick = {
                            startText = "00:00"
                            endText = "23:59"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Custom Time Inputs
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "CUSTOM TIME RANGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )

                    OutlinedTextField(
                        value = startText,
                        onValueChange = { startText = it },
                        label = { Text("Start Time (24h HH:mm)") },
                        placeholder = { Text("e.g. 22:00") },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = endText,
                        onValueChange = { endText = it },
                        label = { Text("End Time (24h HH:mm)") },
                        placeholder = { Text("e.g. 07:00") },
                        leadingIcon = { Icon(Icons.Default.Stop, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Confirmation / Error feedback
            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (showSavedMessage) {
                Text(
                    text = "✓ Schedule updated successfully!",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PresetChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatMinutes(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)

private fun parseRange(start: String, end: String): Pair<Int, Int>? {
    fun parse(value: String): Int? {
        val parts = value.trim().split(":")
        if (parts.size != 2) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        if (hour !in 0..23 || minute !in 0..59) return null
        return hour * 60 + minute
    }
    val startMin = parse(start) ?: return null
    val endMin = parse(end) ?: return null
    return startMin to endMin
}
