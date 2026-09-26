package com.example.presentation.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BookEntity
import com.example.data.repository.BooksRepository
import com.example.presentation.components.ConfirmationDialog
import com.example.presentation.localization.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    strings: AppStrings,
    repository: BooksRepository,
    onAddBookClick: () -> Unit,
    onEditBookClick: (Long) -> Unit,
    onManageCategoriesClick: () -> Unit,
    onManageAuthorsClick: () -> Unit,
    onStorageManagementClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val allBooks by repository.getAllBooks().collectAsState(initial = emptyList())
    val totalCategories by repository.getTotalCategoriesCount().collectAsState(initial = 0)
    val totalAuthors by repository.getTotalAuthorsCount().collectAsState(initial = 0)

    var bookToDelete by remember { mutableStateOf<BookEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.adminDashboard, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddBookClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.addNewBook, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("admin_add_book_fab")
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
            // Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(title = strings.totalBooks, value = "${allBooks.size}", modifier = Modifier.weight(1f))
                    StatCard(title = strings.totalCategories, value = "$totalCategories", modifier = Modifier.weight(1f))
                    StatCard(title = strings.totalAuthors, value = "$totalAuthors", modifier = Modifier.weight(1f))
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Quick Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onManageCategoriesClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.categories, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onManageAuthorsClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(strings.authors, fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onStorageManagementClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Storage", fontSize = 12.sp)
                    }
                }
            }

            // Books Management List
            item {
                Text(
                    text = "${strings.totalBooks} (${allBooks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (allBooks.isEmpty()) {
                item {
                    Text(
                        text = "No books added yet. Tap Add New Book to upload your first PDF book.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(allBooks) { book ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = book.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${book.author} • ${book.category} • ${book.language}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (book.pageCount > 0) {
                                    Text(
                                        text = "${book.pageCount} ${strings.pages}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row {
                                IconButton(onClick = { onEditBookClick(book.id) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { bookToDelete = book }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    bookToDelete?.let { target ->
        ConfirmationDialog(
            title = strings.deleteConfirmTitle,
            message = "${strings.deleteConfirmMsg}\n\nTitle: ${target.title}",
            confirmText = strings.delete,
            cancelText = strings.cancel,
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    repository.deleteBook(target)
                    bookToDelete = null
                }
            },
            onDismiss = { bookToDelete = null }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
