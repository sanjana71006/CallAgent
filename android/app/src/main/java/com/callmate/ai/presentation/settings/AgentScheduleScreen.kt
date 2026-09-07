package com.callmate.ai.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScheduleScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.uiState.collectAsState()
    var startText by remember(settings.settings.scheduledAgentStartMinutes) {
        mutableStateOf(formatMinutes(settings.settings.scheduledAgentStartMinutes))
    }
    var endText by remember(settings.settings.scheduledAgentEndMinutes) {
        mutableStateOf(formatMinutes(settings.settings.scheduledAgentEndMinutes))
    }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agent Schedule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "When enabled, CallMate answers both saved contacts and unknown numbers during this time range.",
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Answer calls automatically", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = settings.settings.scheduledAgentEnabled,
                    onCheckedChange = { enabled ->
                        parseRange(startText, endText)?.let { (start, end) ->
                            viewModel.updateScheduledAgent(enabled, start, end)
                        }
                    }
                )
            }
            OutlinedTextField(
                value = startText,
                onValueChange = { startText = it },
                label = { Text("Start time (HH:mm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = endText,
                onValueChange = { endText = it },
                label = { Text("End time (HH:mm)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val range = parseRange(startText, endText)
                    if (range == null) {
                        error = "Use 24-hour times such as 22:00 or 07:30."
                    } else {
                        error = null
                        viewModel.updateScheduledAgent(
                            settings.settings.scheduledAgentEnabled,
                            range.first,
                            range.second
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save schedule")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
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
    return parse(start)?.let { startMinutes -> parse(end)?.let { endMinutes -> startMinutes to endMinutes } }
}
