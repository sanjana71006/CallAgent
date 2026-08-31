package com.callmate.ai.presentation.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callmate.ai.core.theme.*
import com.callmate.ai.domain.model.TranscriptMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveCallScreen(
    viewModel: CallViewModel,
    onCallEnded: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto scroll when new transcript messages arrive
    LaunchedEffect(uiState.transcripts.size) {
        if (uiState.transcripts.isNotEmpty()) {
            listState.animateScrollToItem(uiState.transcripts.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.currentCall?.callerName ?: "Unknown Caller",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val durationMins = uiState.callDurationSeconds / 60
                        val durationSecs = uiState.callDurationSeconds % 60
                        Text(
                            text = String.format("Live Screening • %02d:%02d", durationMins, durationSecs),
                            fontSize = 12.sp,
                            color = AccentGreen
                        )
                    }
                },
                actions = {
                    // Status Badge
                    Surface(
                        color = when (uiState.callState) {
                            CallState.USER_TAKEOVER -> AccentAmber.copy(alpha = 0.15f)
                            else -> ElectricBlue.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.callState == CallState.USER_TAKEOVER) AccentAmber else AccentGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.callState == CallState.USER_TAKEOVER) "In Call" else "AI Screening",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (uiState.callState == CallState.USER_TAKEOVER) AccentAmber else ElectricBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Live Status Banner
            LiveStatusIndicator(aiVoiceState = uiState.aiVoiceState)

            // Transcript Stream
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(uiState.transcripts) { msg ->
                    TranscriptBubble(message = msg)
                }
            }

            // Quick Speech Simulation Prompts (Useful during demo/development)
            QuickPromptBar(
                onPromptSelected = { promptText ->
                    viewModel.handleCallerSpeech(promptText)
                }
            )

            // Bottom Call Controls
            LiveCallControls(
                isMuted = uiState.isMuted,
                isTakenOver = uiState.callState == CallState.USER_TAKEOVER,
                onToggleMute = { viewModel.toggleMute() },
                onTakeOver = { viewModel.takeOverCall() },
                onEndCall = {
                    viewModel.endCall()
                    onCallEnded()
                }
            )
        }
    }
}

@Composable
fun LiveStatusIndicator(aiVoiceState: AiVoiceState) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaAnim"
    )

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            when (aiVoiceState) {
                AiVoiceState.SPEAKING -> {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Assistant Speaking...",
                        fontSize = 12.sp,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
                AiVoiceState.LISTENING -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Listening to caller speech...",
                        fontSize = 12.sp,
                        color = AccentGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
                AiVoiceState.THINKING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = AccentPurple
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Generating Response...",
                        fontSize = 12.sp,
                        color = AccentPurple,
                        fontWeight = FontWeight.Medium
                    )
                }
                AiVoiceState.IDLE -> {
                    Text(
                        text = "Connection Active",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TranscriptBubble(message: TranscriptMessage) {
    val isAi = message.speaker.equals("ai", ignoreCase = true)
    val isCaller = message.speaker.equals("caller", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isAi) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isAi) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = if (isAi) "AI ASSISTANT" else "CALLER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAi) ElectricBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isAi) 16.dp else 4.dp,
                    bottomEnd = if (isAi) 4.dp else 16.dp
                ),
                color = if (isAi) NavyPrimary else MaterialTheme.colorScheme.surface,
                border = if (isAi) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = if (isAi) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun QuickPromptBar(onPromptSelected: (String) -> Unit) {
    val samplePrompts = listOf(
        "I'm calling about the senior software engineer interview.",
        "Your Amazon courier package is at the main entrance gate.",
        "Special pre-approved credit card loan offer just for you!",
        "This is an emergency callback request."
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "Simulator Quick Speech Prompts:",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            samplePrompts.take(3).forEach { prompt ->
                AssistChip(
                    onClick = { onPromptSelected(prompt) },
                    label = {
                        Text(
                            text = prompt.take(22) + "...",
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun LiveCallControls(
    isMuted: Boolean,
    isTakenOver: Boolean,
    onToggleMute: () -> Unit,
    onTakeOver: () -> Unit,
    onEndCall: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = if (isMuted) AccentRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = if (isMuted) AccentRed else MaterialTheme.colorScheme.onSurface
                )
            }

            // Take Over Button
            if (!isTakenOver) {
                Button(
                    onClick = onTakeOver,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Over", fontWeight = FontWeight.Bold)
                }
            }

            // End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(52.dp)
                    .background(AccentRed, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White
                )
            }
        }
    }
}
