package com.example.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entities.AuthorEntity
import com.example.data.repository.BooksRepository
import com.example.presentation.components.ConfirmationDialog
import com.example.presentation.localization.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAuthorsScreen(
    strings: AppStrings,
    repository: BooksRepository,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val authors by repository.getAllAuthors().collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var editingAuthor by remember { mutableStateOf<AuthorEntity?>(null) }
    var authorToDelete by remember { mutableStateOf<AuthorEntity?>(null) }

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.manageAuthors, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingAuthor = null
                name = ""
                bio = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Author")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(authors) { author ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = author.name, fontWeight = FontWeight.Bold)
                            if (author.biography.isNotEmpty()) {
                                Text(
                                    text = author.biography,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = {
                                editingAuthor = author
                                name = author.name
                                bio = author.biography
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { authorToDelete = author }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(if (editingAuthor != null) "Edit Author" else "Add Author") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Author Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Biography") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            if (editingAuthor != null) {
                                repository.updateAuthor(
                                    editingAuthor!!.copy(
                                        name = name.trim(),
                                        biography = bio.trim()
                                    )
                                )
                            } else {
                                repository.insertAuthor(
                                    AuthorEntity(
                                        name = name.trim(),
                                        biography = bio.trim()
                                    )
                                )
                            }
                            showDialog = false
                        }
                    }
                }) {
                    Text(strings.submit)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    authorToDelete?.let { target ->
        ConfirmationDialog(
            title = "Delete Author?",
            message = "Are you sure you want to delete '${target.name}'?",
            confirmText = strings.delete,
            cancelText = strings.cancel,
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    repository.deleteAuthor(target)
                    authorToDelete = null
                }
            },
            onDismiss = { authorToDelete = null }
        )
    }
}
