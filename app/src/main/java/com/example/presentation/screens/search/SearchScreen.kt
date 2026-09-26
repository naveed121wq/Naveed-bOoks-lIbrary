package com.example.presentation.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.presentation.components.BookListItem
import com.example.presentation.localization.AppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    strings: AppStrings,
    viewModel: SearchViewModel,
    onBookClick: (Long) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val filteredResults = uiState.searchResults.filter { book ->
        val matchesLang = uiState.selectedLanguage == null || book.language.equals(uiState.selectedLanguage, ignoreCase = true)
        val matchesCat = uiState.selectedCategory == null || book.category == uiState.selectedCategory
        matchesLang && matchesCat
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.query,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        placeholder = { Text(strings.searchPlaceholder, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear text")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp)
                            .testTag("search_text_field")
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filters Row: Languages & Categories
            item {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedLanguage == null,
                        onClick = { viewModel.setLanguageFilter(null) },
                        label = { Text(strings.all) }
                    )
                    listOf("English", "Urdu", "Arabic").forEach { lang ->
                        FilterChip(
                            selected = uiState.selectedLanguage == lang,
                            onClick = {
                                viewModel.setLanguageFilter(if (uiState.selectedLanguage == lang) null else lang)
                            },
                            label = { Text(lang) }
                        )
                    }
                }
            }

            // Recent Searches (if query is empty)
            if (uiState.query.isBlank()) {
                if (uiState.recentSearches.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.recentSearches,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { viewModel.clearSearchHistory() }) {
                                Text(strings.clear)
                            }
                        }
                    }

                    items(uiState.recentSearches) { recent ->
                        ListItem(
                            headlineContent = { Text(recent.query) },
                            leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                            trailingContent = {
                                IconButton(onClick = { viewModel.deleteRecentSearch(recent.query) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove")
                                }
                            },
                            modifier = Modifier.clickable {
                                viewModel.onQueryChanged(recent.query)
                            }
                        )
                    }
                }
            } else {
                // Search Results
                if (uiState.isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (filteredResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = strings.noResultsFound,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = strings.noResultsDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "${filteredResults.size} ${strings.pages}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    items(filteredResults) { book ->
                        BookListItem(
                            book = book,
                            onClick = {
                                viewModel.executeSearch(uiState.query)
                                onBookClick(book.id)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
