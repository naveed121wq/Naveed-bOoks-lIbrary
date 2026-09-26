package com.example.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.entities.CategoryEntity
import com.example.data.repository.BooksRepository
import com.example.presentation.components.ConfirmationDialog
import com.example.presentation.localization.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    strings: AppStrings,
    repository: BooksRepository,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val categories by repository.getAllCategories().collectAsState(initial = emptyList())

    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    var nameEn by remember { mutableStateOf("") }
    var nameUr by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.manageCategories, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingCategory = null
                nameEn = ""
                nameUr = ""
                desc = ""
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
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
            items(categories) { category ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = category.nameEn, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${category.nameUr} • ${category.booksCount} books",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            IconButton(onClick = {
                                editingCategory = category
                                nameEn = category.nameEn
                                nameUr = category.nameUr
                                desc = category.description
                                showDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { categoryToDelete = category }) {
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
            title = { Text(if (editingCategory != null) "Edit Category" else "Add Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("English Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nameUr,
                        onValueChange = { nameUr = it },
                        label = { Text("Urdu Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (nameEn.isNotBlank()) {
                        scope.launch {
                            if (editingCategory != null) {
                                repository.updateCategory(
                                    editingCategory!!.copy(
                                        nameEn = nameEn.trim(),
                                        nameUr = nameUr.trim(),
                                        description = desc.trim()
                                    )
                                )
                            } else {
                                repository.insertCategory(
                                    CategoryEntity(
                                        nameEn = nameEn.trim(),
                                        nameUr = nameUr.trim(),
                                        description = desc.trim()
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

    categoryToDelete?.let { target ->
        ConfirmationDialog(
            title = "Delete Category?",
            message = "Are you sure you want to delete '${target.nameEn}'?",
            confirmText = strings.delete,
            cancelText = strings.cancel,
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    repository.deleteCategory(target)
                    categoryToDelete = null
                }
            },
            onDismiss = { categoryToDelete = null }
        )
    }
}
