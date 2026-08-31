package com.callmate.ai.presentation.settings.voice

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callmate.ai.CallMateApp
import com.callmate.ai.core.theme.AccentGreenLight
import com.callmate.ai.presentation.settings.SettingsViewModel
import java.util.Locale

data class SupportedLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val locale: Locale,
    val sampleText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceLanguageScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    var speechRate by remember(settings.speechRate) { mutableStateOf(settings.speechRate) }
    var speechPitch by remember(settings.speechPitch) { mutableStateOf(settings.speechPitch) }
    var isTestingAudio by remember { mutableStateOf(false) }

    val languages = remember {
        listOf(
            SupportedLanguage("en-US", "English", "English", Locale.US, "Hello! I am CallMate AI, your personal call screening assistant."),
            SupportedLanguage("hi-IN", "Hindi", "हिन्दी", Locale("hi", "IN"), "नमस्ते! मैं कॉलमेट एआई हूँ, आपका व्यक्तिगत कॉल सहायक।"),
            SupportedLanguage("te-IN", "Telugu", "తెలుగు", Locale("te", "IN"), "నమస్కారం! నేను కాల్‌మేట్ AI, మీ కాల్ అసిస్టెంట్."),
            SupportedLanguage("ta-IN", "Tamil", "தமிழ்", Locale("ta", "IN"), "வணக்கம்! நான் கால்மேட் ஏஐ, உங்கள் அழைப்பு உதவியாளர்."),
            SupportedLanguage("kn-IN", "Kannada", "ಕನ್ನಡ", Locale("kn", "IN"), "ನಮಸ್ಕಾರ! ನಾನು ಕಾಲ್‌ಮೇಟ್ ಎಐ, ನಿಮ್ಮ ಕರೆ ಸಹಾಯಕ.")
        )
    }

    val selectedLangObj = languages.find { it.code.equals(settings.language, ignoreCase = true) } ?: languages.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice & Language", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        isTestingAudio = true
                        val ttsManager = CallMateApp.instance.textToSpeechManager
                        ttsManager.speak(
                            text = selectedLangObj.sampleText,
                            pitch = speechPitch,
                            rate = speechRate
                        )
                        isTestingAudio = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Voice (${selectedLangObj.displayName})", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header Info Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RecordVoiceOver,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "VOICE ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Custom Vocal Personality",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "CallMate speaks naturally using on-device neural synthesis",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Language Selector Card
            Text(
                text = "PRIMARY ASSISTANT LANGUAGE",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    languages.forEachIndexed { index, lang ->
                        val isSelected = lang.code.equals(settings.language, ignoreCase = true)
                        Surface(
                            onClick = { viewModel.updateLanguage(lang.code) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = lang.displayName,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = lang.nativeName,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        if (index < languages.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }

            // Speech Rate & Pitch Slider Card
            Text(
                text = "SPEED & PITCH ADJUSTMENTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Speech Rate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Speech Rate", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("${String.format(Locale.US, "%.1f", speechRate)}x", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speechRate,
                        onValueChange = { speechRate = it },
                        onValueChangeFinished = { viewModel.updateSpeechRate(speechRate) },
                        valueRange = 0.5f..2.0f,
                        steps = 5
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speech Pitch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Voice Pitch", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("${String.format(Locale.US, "%.1f", speechPitch)}x", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = speechPitch,
                        onValueChange = { speechPitch = it },
                        onValueChangeFinished = { viewModel.updateSpeechPitch(speechPitch) },
                        valueRange = 0.5f..2.0f,
                        steps = 5
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
