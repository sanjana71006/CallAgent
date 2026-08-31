package com.callmate.ai.presentation.settings.account

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callmate.ai.core.theme.AccentGreenLight
import com.callmate.ai.core.theme.AccentRedLight
import com.callmate.ai.presentation.auth.AuthViewModel
import com.callmate.ai.presentation.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDataScreen(
    viewModel: SettingsViewModel,
    authViewModel: AuthViewModel? = null,
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val authUiState = authViewModel?.uiState?.collectAsState()?.value
    val profile = uiState.userProfile

    var actionDialogType by remember { mutableStateOf<String?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Dialog for Destructive & Account Actions
    if (actionDialogType != null) {
        val (title, body, confirmBtnText, isDestructive, action) = when (actionDialogType) {
            "calls" -> Quint(
                "Delete Call History?",
                "This will permanently delete all call log entries stored on this device. Cloud account data will not be affected.",
                "Delete",
                true,
                { viewModel.deleteCallHistory { snackbarMessage = "Call history deleted" } }
            )
            "transcripts" -> Quint(
                "Delete All Transcripts?",
                "This will permanently delete all live caller speech transcripts from your device database.",
                "Delete",
                true,
                { viewModel.deleteTranscripts { snackbarMessage = "All transcripts deleted" } }
            )
            "addresses" -> Quint(
                "Delete Saved Addresses?",
                "This will remove all saved Home, Work, and College addresses from your local storage.",
                "Delete",
                true,
                { viewModel.deleteSavedAddresses { snackbarMessage = "Saved addresses deleted" } }
            )
            "reset_settings" -> Quint(
                "Reset Assistant Settings?",
                "This will reset all assistant parameters (voice pitch, rate, silent mode, greeting) to their original defaults.",
                "Reset",
                false,
                { viewModel.resetAllSettings { snackbarMessage = "Settings restored to defaults" } }
            )
            "delete_all" -> Quint(
                "Clear All Local Data?",
                "This will erase ALL local call logs, transcripts, saved addresses, and preferences. Your MongoDB cloud account will NOT be deleted.",
                "Clear Local Data",
                true,
                { viewModel.deleteAllLocalData { snackbarMessage = "All local data wiped completely" } }
            )
            "logout" -> Quint(
                "Sign Out",
                "Are you sure you want to sign out?",
                "Sign Out",
                false,
                {
                    authViewModel?.logout {
                        onNavigateToLogin()
                    }
                }
            )
            "delete_account" -> Quint(
                "Delete Account",
                "This permanently deletes your CallMate account and cloud account information.",
                "Delete Account",
                true,
                {
                    authViewModel?.deleteAccount(
                        onSuccess = {
                            viewModel.deleteAllLocalData {
                                onNavigateToLogin()
                            }
                        },
                        onError = { err ->
                            snackbarMessage = "Error: $err"
                        }
                    )
                }
            )
            else -> Quint("", "", "", false, {})
        }

        AlertDialog(
            onDismissRequest = { actionDialogType = null },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = { Text(body) },
            confirmButton = {
                TextButton(
                    onClick = {
                        action()
                        actionDialogType = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(confirmBtnText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { actionDialogType = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Data", fontWeight = FontWeight.Bold) },
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Feedback notification
            if (snackbarMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentGreenLight.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreenLight)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = snackbarMessage ?: "", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                }
            }

            // Account Identity Card (Name, Email, Phone, Registration Date, Account Status)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ACCOUNT IDENTITY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = AccentGreenLight.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = authUiState?.currentUser?.accountStatus ?: if (authUiState?.isLoggedIn == true) "ACTIVE" else "LOCAL ONLY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentGreenLight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = authUiState?.currentUser?.name ?: profile.name,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val emailText = authUiState?.currentUser?.email?.ifBlank { null } ?: "No cloud email"
                            Text(
                                text = emailText,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Detail rows
                    AccountDetailItem(
                        label = "Phone Number",
                        value = authUiState?.currentUser?.phoneNumber?.ifBlank { null } ?: profile.phoneNumber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AccountDetailItem(
                        label = "Registration Date",
                        value = authUiState?.currentUser?.createdAt?.take(10) ?: "Aug 2026"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AccountDetailItem(
                        label = "Account Storage",
                        value = if (authUiState?.isLoggedIn == true) "MongoDB Atlas Cloud" else "Local Device SQLite"
                    )
                }
            }

            // Export Data Action
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                DataActionRow(
                    icon = Icons.Outlined.FileDownload,
                    title = "Export My Data",
                    subtitle = "Generate a JSON summary of your profile and settings to share",
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        val exportText = viewModel.exportDataSummary()
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_TEXT, exportText)
                            putExtra(Intent.EXTRA_TITLE, "CallMate_AI_Data_Export.json")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export CallMate Data"))
                    }
                )
            }

            // Session & Account Controls Header
            Text(
                text = "ACCOUNT ACTIONS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Session Controls Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    DataActionRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        title = "Sign Out",
                        subtitle = "End active session on this device (keeps local data)",
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = { actionDialogType = "logout" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))
                    DataActionRow(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently remove your account and cloud information",
                        iconColor = AccentRedLight,
                        isDestructive = true,
                        onClick = { actionDialogType = "delete_account" }
                    )
                }
            }

            // Local Data Deletion Header
            Text(
                text = "LOCAL DATA MANAGEMENT (OFFLINE)",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Destructive Actions Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    DataActionRow(
                        icon = Icons.Outlined.DeleteSweep,
                        title = "Clear Local Data",
                        subtitle = "Delete all records on this device (keeps MongoDB account)",
                        iconColor = AccentRedLight,
                        isDestructive = true,
                        onClick = { actionDialogType = "delete_all" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    DataActionRow(
                        icon = Icons.Outlined.DeleteOutline,
                        title = "Delete Call History",
                        subtitle = "Remove screened calls list from local Room database",
                        iconColor = AccentRedLight,
                        onClick = { actionDialogType = "calls" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    DataActionRow(
                        icon = Icons.Outlined.Subtitles,
                        title = "Delete All Transcripts",
                        subtitle = "Erase caller conversation text transcripts",
                        iconColor = AccentRedLight,
                        onClick = { actionDialogType = "transcripts" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    DataActionRow(
                        icon = Icons.Outlined.LocationOff,
                        title = "Delete Saved Addresses",
                        subtitle = "Clear all Home, Work, and College locations",
                        iconColor = AccentRedLight,
                        onClick = { actionDialogType = "addresses" }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), modifier = Modifier.padding(horizontal = 16.dp))

                    DataActionRow(
                        icon = Icons.Outlined.SettingsBackupRestore,
                        title = "Reset Assistant Settings",
                        subtitle = "Restore all parameters to factory defaults",
                        iconColor = Color(0xFFE65100),
                        onClick = { actionDialogType = "reset_settings" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AccountDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DataActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconColor.copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDestructive) AccentRedLight else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
