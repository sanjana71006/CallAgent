package com.callmate.ai.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.callmate.ai.core.contacts.PhoneContact
import com.callmate.ai.core.contacts.ContactsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneContactsScreen(
    contactsManager: ContactsManager,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var contacts by remember { mutableStateOf<List<PhoneContact>>(emptyList()) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            contacts = contactsManager.getAllContacts()
        }
    }

    val filteredContacts = contacts.filter {
        query.isBlank() || it.name.contains(query, true) || it.phoneNumber.contains(query, true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Phone Contacts") },
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
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                label = { Text("Search by name or number") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))
            Text("${filteredContacts.size} contacts", style = MaterialTheme.typography.labelMedium)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredContacts, key = { "${it.id}:${it.phoneNumber}" }) { contact ->
                    ListItem(
                        headlineContent = { Text(contact.name.ifBlank { "Unknown contact" }) },
                        supportingContent = { Text(contact.phoneNumber) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
