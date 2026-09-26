package com.example.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.presentation.localization.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    strings: AppStrings,
    viewModel: SettingsViewModel,
    onStorageManagementClick: () -> Unit,
    onAuthorsClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val currentLang by viewModel.language.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val readerMode by viewModel.readerMode.collectAsState()
    val keepScreenAwake by viewModel.keepScreenAwake.collectAsState()
    val adminPin by viewModel.adminPin.collectAsState()

    var showPinChangeDialog by remember { mutableStateOf(false) }
    var newPinText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = strings.settings, fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.language,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilterChip(
                                selected = currentLang == "en",
                                onClick = { viewModel.setLanguage("en") },
                                label = { Text(strings.englishName) },
                                modifier = Modifier.weight(1f).testTag("settings_lang_en")
                            )
                            FilterChip(
                                selected = currentLang == "ur",
                                onClick = { viewModel.setLanguage("ur") },
                                label = { Text(strings.urduName) },
                                modifier = Modifier.weight(1f).testTag("settings_lang_ur")
                            )
                        }
                    }
                }
            }

            // Theme Mode Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.appearance,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = themeMode == "SYSTEM",
                                onClick = { viewModel.setThemeMode("SYSTEM") },
                                label = { Text(strings.system) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = themeMode == "LIGHT",
                                onClick = { viewModel.setThemeMode("LIGHT") },
                                label = { Text(strings.light) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = themeMode == "DARK",
                                onClick = { viewModel.setThemeMode("DARK") },
                                label = { Text(strings.dark) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Reading Preferences Card
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.readerMode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = readerMode == "LIGHT",
                                onClick = { viewModel.setReaderMode("LIGHT") },
                                label = { Text(strings.light) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = readerMode == "SEPIA",
                                onClick = { viewModel.setReaderMode("SEPIA") },
                                label = { Text(strings.sepia) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = readerMode == "DARK",
                                onClick = { viewModel.setReaderMode("DARK") },
                                label = { Text(strings.dark) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.keepScreenAwake,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = keepScreenAwake,
                                onCheckedChange = { viewModel.setKeepScreenAwake(it) }
                            )
                        }
                    }
                }
            }

            // Data & Admin Links
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        ListItem(
                            headlineContent = { Text(strings.storageManagement) },
                            leadingContent = { Icon(Icons.Default.Storage, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { onStorageManagementClick() }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text(strings.authors) },
                            leadingContent = { Icon(Icons.Default.People, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { onAuthorsClick() }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text(strings.adminDashboard) },
                            leadingContent = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { onAdminClick() }
                        )
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Change Admin PIN (Current: $adminPin)") },
                            leadingContent = { Icon(Icons.Default.Pin, contentDescription = null) },
                            modifier = Modifier.clickable { showPinChangeDialog = true }
                        )
                    }
                }
            }

            // About App Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${strings.appName} v1.0",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Offline-First Digital Library for Books & PDFs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showPinChangeDialog) {
        AlertDialog(
            onDismissRequest = { showPinChangeDialog = false },
            title = { Text("Change Admin PIN") },
            text = {
                OutlinedTextField(
                    value = newPinText,
                    onValueChange = { if (it.length <= 8) newPinText = it },
                    label = { Text("Enter New PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (newPinText.isNotBlank()) {
                        viewModel.updateAdminPin(newPinText)
                        newPinText = ""
                        showPinChangeDialog = false
                    }
                }) {
                    Text(strings.submit)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPinChangeDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
