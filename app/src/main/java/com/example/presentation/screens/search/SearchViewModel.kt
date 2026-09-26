package com.example.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.BookEntity
import com.example.data.local.entities.CategoryEntity
import com.example.data.local.entities.SearchHistoryEntity
import com.example.data.repository.BooksRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val searchResults: List<BookEntity> = emptyList(),
    val recentSearches: List<SearchHistoryEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val selectedLanguage: String? = null,
    val selectedCategory: String? = null,
    val isSearching: Boolean = false
)

class SearchViewModel(
    private val repository: BooksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecentSearches()
        loadCategories()
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            repository.getRecentSearches().collect { searches ->
                _uiState.update { it.copy(recentSearches = searches) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
    }

    fun onQueryChanged(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(300) // Debounce
            _uiState.update { it.copy(isSearching = true) }
            repository.searchBooks(newQuery.trim()).collect { results ->
                _uiState.update { it.copy(searchResults = results, isSearching = false) }
            }
        }
    }

    fun executeSearch(query: String) {
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repository.addSearchQuery(query)
            }
        }
    }

    fun setLanguageFilter(lang: String?) {
        _uiState.update { it.copy(selectedLanguage = lang) }
    }

    fun setCategoryFilter(cat: String?) {
        _uiState.update { it.copy(selectedCategory = cat) }
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }
}
