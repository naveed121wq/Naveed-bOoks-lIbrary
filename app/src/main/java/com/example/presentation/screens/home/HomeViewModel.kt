package com.example.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.BookEntity
import com.example.data.local.entities.CategoryEntity
import com.example.data.local.entities.AuthorEntity
import com.example.data.repository.BooksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ViewMode {
    GRID, LIST
}

enum class SortOption {
    LATEST, TITLE, RATING, MOST_READ
}

data class HomeUiState(
    val featuredBooks: List<BookEntity> = emptyList(),
    val continueReadingBooks: List<BookEntity> = emptyList(),
    val recentlyAddedBooks: List<BookEntity> = emptyList(),
    val popularBooks: List<BookEntity> = emptyList(),
    val mostLikedBooks: List<BookEntity> = emptyList(),
    val allBooks: List<BookEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val authors: List<AuthorEntity> = emptyList(),
    val selectedCategory: String? = null,
    val viewMode: ViewMode = ViewMode.GRID,
    val sortOption: SortOption = SortOption.LATEST,
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: BooksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllBooks().collect { all ->
                _uiState.update { it.copy(allBooks = all, isLoading = false) }
            }
        }
        viewModelScope.launch {
            repository.getFeaturedBooks().collect { featured ->
                _uiState.update { it.copy(featuredBooks = featured) }
            }
        }
        viewModelScope.launch {
            repository.getContinueReadingBooks().collect { cr ->
                _uiState.update { it.copy(continueReadingBooks = cr) }
            }
        }
        viewModelScope.launch {
            repository.getRecentlyAddedBooks().collect { recent ->
                _uiState.update { it.copy(recentlyAddedBooks = recent) }
            }
        }
        viewModelScope.launch {
            repository.getPopularBooks().collect { pop ->
                _uiState.update { it.copy(popularBooks = pop) }
            }
        }
        viewModelScope.launch {
            repository.getMostLikedBooks().collect { liked ->
                _uiState.update { it.copy(mostLikedBooks = liked) }
            }
        }
        viewModelScope.launch {
            repository.getAllCategories().collect { cats ->
                _uiState.update { it.copy(categories = cats) }
            }
        }
        viewModelScope.launch {
            repository.getAllAuthors().collect { auths ->
                _uiState.update { it.copy(authors = auths) }
            }
        }
    }

    fun setCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(viewMode = if (it.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID)
        }
    }

    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(sortOption = option) }
    }

    fun toggleFavorite(book: BookEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(book)
        }
    }
}
