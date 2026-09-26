package com.example.presentation.screens.storage

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.storage.BackupManager
import com.example.data.storage.FileStorageManager
import com.example.presentation.localization.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageManagementScreen(
    strings: AppStrings,
    fileStorageManager: FileStorageManager,
    backupManager: BackupManager,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<FileStorageManager.StorageStats?>(null) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    fun refreshStats() {
        scope.launch {
            stats = fileStorageManager.getStorageStats()
        }
    }

    LaunchedEffect(Unit) {
        refreshStats()
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }

    // SAF Create Document for JSON export
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val success = backupManager.exportBackupToJson(uri)
                snackbarMessage = if (success) "Backup exported successfully!" else "Failed to export backup."
            }
        }
    }

    // SAF Open Document for JSON restore
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val success = backupManager.restoreBackupFromJson(uri)
                snackbarMessage = if (success) "Library restored from backup!" else "Failed to restore backup."
                refreshStats()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.storageManagement, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Total Storage Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.totalStorageUsed,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val totalMb = (stats?.totalSize ?: 0L) / (1024f * 1024f)
                        Text(
                            text = "%.2f MB".format(totalMb),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Storage Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Storage Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val pdfMb = (stats?.pdfsSize ?: 0L) / (1024f * 1024f)
                        val coversMb = (stats?.coversSize ?: 0L) / (1024f * 1024f)
                        val dbKb = (stats?.databaseSize ?: 0L) / 1024f
                        val cacheMb = (stats?.cacheSize ?: 0L) / (1024f * 1024f)

                        StorageRow(name = strings.pdfStorage, sizeText = "%.2f MB".format(pdfMb), icon = Icons.Default.PictureAsPdf)
                        StorageRow(name = strings.coversStorage, sizeText = "%.2f MB".format(coversMb), icon = Icons.Default.Image)
                        StorageRow(name = strings.databaseStorage, sizeText = "%.1f KB".format(dbKb), icon = Icons.Default.Storage)
                        StorageRow(name = strings.cacheStorage, sizeText = "%.2f MB".format(cacheMb), icon = Icons.Default.Cached)
                    }
                }
            }

            // Clean Cache Button
            item {
                Button(
                    onClick = {
                        scope.launch {
                            val freed = fileStorageManager.clearRenderCache()
                            val freedKb = freed / 1024f
                            snackbarMessage = "Cache cleared! Freed %.1f KB".format(freedKb)
                            refreshStats()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.clearCache, fontWeight = FontWeight.Bold)
                }
            }

            // Backup & Restore
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.backupRestore,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedButton(
                            onClick = { exportLauncher.launch("books_library_backup.json") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.exportBackup)
                        }

                        OutlinedButton(
                            onClick = { restoreLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(strings.restoreBackup)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorageRow(name: String, sizeText: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = sizeText, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}
