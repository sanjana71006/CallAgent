package com.callmate.ai.presentation.settings.help

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FaqItem(
    val question: String,
    val answer: String,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val faqs = remember {
        listOf(
            // Getting Started
            FaqItem(
                category = "GETTING STARTED",
                question = "How does CallMate AI work?",
                answer = "CallMate AI screens incoming phone calls using on-device speech recognition, an intelligent local AI screening engine, and speech synthesis. It speaks with callers politely to find out who they are and why they are calling, transcribing the call in real time."
            ),
            FaqItem(
                category = "GETTING STARTED",
                question = "How do I enable or pause the assistant?",
                answer = "In the 'You' tab, use the master 'AI Assistant is On/Off' toggle switch. When turned Off, CallMate will not answer or screen calls automatically."
            ),
            FaqItem(
                category = "GETTING STARTED",
                question = "How do I configure Silent Mode?",
                answer = "Navigate to 'You' -> 'Silent Mode'. Enable the master toggle and select which categories (Telemarketing, Spam, Unknown Callers, etc.) should be muted."
            ),

            // Calls & Screening
            FaqItem(
                category = "CALLS & SCREENING",
                question = "How does real-time call screening work?",
                answer = "When an incoming call arrives, CallMate answers and plays your greeting. As the caller speaks, their speech is transcribed live on your screen. You can choose to Listen Live, Take Over the Call, or Decline."
            ),
            FaqItem(
                category = "CALLS & SCREENING",
                question = "Why can't some calls be screened via telecom?",
                answer = "On Android, third-party apps require default dialer privileges to directly intercept carrier calls. The current CallMate architecture includes a full Simulator engine for local and development testing."
            ),
            FaqItem(
                category = "CALLS & SCREENING",
                question = "How do I take over a call during screening?",
                answer = "While on the Live Call screen, tap the green 'Take Over' phone button. The AI assistant immediately steps aside and hands over the audio line to you."
            ),

            // AI Assistant
            FaqItem(
                category = "AI ASSISTANT",
                question = "How do I customize my screening instructions?",
                answer = "Go to 'You' -> 'Your Instructions'. Type custom prompt instructions (up to 1,000 characters), such as 'Never share my personal number' or 'Ask delivery agents to leave parcels at the gate'."
            ),
            FaqItem(
                category = "AI ASSISTANT",
                question = "How do I change assistant voice and language?",
                answer = "Go to 'You' -> 'Voice & Language'. You can select between English, Hindi, Telugu, Tamil, and Kannada, adjust voice pitch and speed, and test sample audio."
            ),
            FaqItem(
                category = "AI ASSISTANT",
                question = "Does CallMate require an internet connection?",
                answer = "No! CallMate AI is built with an offline-first architecture. If local Ollama or cloud backend is unavailable, it automatically falls back to its built-in rule-based heuristic screening engine."
            ),

            // Privacy & Data
            FaqItem(
                category = "PRIVACY & DATA",
                question = "What data is stored and where?",
                answer = "All your call logs, transcripts, saved addresses, and assistant preferences are stored strictly on your local device in encrypted Room SQLite database and DataStore."
            ),
            FaqItem(
                category = "PRIVACY & DATA",
                question = "How do I export or delete my data?",
                answer = "Go to 'You' -> 'Account & Data'. You can export your data as JSON or delete your call history, transcripts, addresses, or all local data with a single tap."
            ),

            // Troubleshooting
            FaqItem(
                category = "TROUBLESHOOTING",
                question = "The assistant is not hearing caller speech",
                answer = "Verify that Microphone permission is granted in 'You' -> 'Assistant Health Check'. If denied, tap 'Grant Permission' to allow audio recording."
            ),
            FaqItem(
                category = "TROUBLESHOOTING",
                question = "Voice audio is not playing back",
                answer = "Check your device media volume and ensure the Android Text-to-Speech voice engine is installed in your device's System Settings -> Accessibility -> Text-to-Speech."
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help Center", fontWeight = FontWeight.Bold) },
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
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, "CallMate AI Support Inquiry")
                            putExtra(Intent.EXTRA_TEXT, "Hello CallMate Team,\n\nI need assistance with: ")
                        }
                        try {
                            context.startActivity(Intent.createChooser(emailIntent, "Contact Support via Email"))
                        } catch (e: Exception) {
                            // Handled if no email app installed
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Outlined.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact Support", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "How can we help?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Browse answers to frequently asked questions about CallMate AI screening and privacy.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Group FAQs by category
            val groupedFaqs = faqs.groupBy { it.category }
            groupedFaqs.forEach { (category, items) ->
                item {
                    Text(
                        text = category,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(items) { faq ->
                    ExpandableFaqCard(faq = faq)
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ExpandableFaqCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = faq.question,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = faq.answer,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}
