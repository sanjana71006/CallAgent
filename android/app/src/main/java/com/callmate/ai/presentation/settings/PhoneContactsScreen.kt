package com.callmate.ai.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callmate.ai.core.contacts.PhoneContact
import com.callmate.ai.core.contacts.ContactsManager
import com.callmate.ai.core.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneContactsScreen(
    contactsManager: ContactsManager,
    onNavigateBack: () -> Unit,
    onCallContactWithAi: (name: String, number: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var showDirectDialDialog by remember { mutableStateOf(false) }
    var dialerName by remember { mutableStateOf("") }
    var dialerNumber by remember { mutableStateOf("") }

    val defaultSampleContacts = remember {
        listOf(
            PhoneContact(id = "101", name = "Mom (Home)", phoneNumber = "+91 98765 43210"),
            PhoneContact(id = "102", name = "Dr. Mehta Clinic", phoneNumber = "+91 91234 56789"),
            PhoneContact(id = "103", name = "Rahul Sharma (Work)", phoneNumber = "+91 98111 22334"),
            PhoneContact(id = "104", name = "Priya Patel", phoneNumber = "+91 99887 76655"),
            PhoneContact(id = "105", name = "Amazon Delivery Desk", phoneNumber = "+91 80000 11223"),
            PhoneContact(id = "106", name = "David Miller (Manager)", phoneNumber = "+1 (555) 234-5678"),
            PhoneContact(id = "107", name = "Swiggy Support", phoneNumber = "+91 88888 99999")
        )
    }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val deviceContacts = contactsManager.getAllContacts()
            contacts = if (deviceContacts.isNotEmpty()) {
                deviceContacts
            } else {
                defaultSampleContacts
            }
        }
    }

    val filteredContacts = contacts.filter {
        query.isBlank() || it.name.contains(query, true) || it.phoneNumber.contains(query, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Call Contacts with AI", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Call anyone using CallMate AI Voice Agent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDirectDialDialog = true }) {
                        Icon(Icons.Default.Dialpad, contentDescription = "Direct Dial", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDirectDialDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Dialpad, contentDescription = null) },
                text = { Text("Direct AI Dial", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                placeholder = { Text("Search by contact name or number...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Stats / Info Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tap 'Call with AI' on any contact to start an outbound voice session.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "${filteredContacts.size} Contacts Available",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts, key = { "${it.id}:${it.phoneNumber}" }) { contact ->
                    ContactCardItem(
                        contact = contact,
                        onCallWithAi = {
                            onCallContactWithAi(contact.name, contact.phoneNumber)
                        }
                    )
                }
            }
        }
    }

    // Direct AI Dial Dialog
    if (showDirectDialDialog) {
        AlertDialog(
            onDismissRequest = { showDirectDialDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dial Any Number with AI", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter the contact or business details. CallMate AI will initiate the voice call on your behalf.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = dialerName,
                        onValueChange = { dialerName = it },
                        label = { Text("Recipient Name (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dialerNumber,
                        onValueChange = { dialerNumber = it },
                        label = { Text("Phone Number *") },
                        placeholder = { Text("+91 98765 43210") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nameToUse = dialerName.ifBlank { "Custom Recipient" }
                        val numToUse = dialerNumber.ifBlank { "+91 98765 43210" }
                        showDirectDialDialog = false
                        onCallContactWithAi(nameToUse, numToUse)
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Start AI Call", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectDialDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContactCardItem(
    contact: PhoneContact,
    onCallWithAi: () -> Unit
) {
    val initials = remember(contact.name) {
        contact.name.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first().uppercaseChar() }
            .joinToString("")
            .ifEmpty { "C" }
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Contact Avatar with initials
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlueLight, PrimaryBlueDark)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name.ifBlank { "Saved Contact" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = contact.phoneNumber,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // "Call with AI" Action Button
            Button(
                onClick = onCallWithAi,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI Call",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
