package com.example.presentation.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.BookEntity
import com.example.data.local.entities.CommentEntity
import com.example.data.local.entities.RatingEntity
import com.example.data.repository.BooksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BookDetailsUiState(
    val book: BookEntity? = null,
    val comments: List<CommentEntity> = emptyList(),
    val userRating: RatingEntity? = null,
    val relatedBooks: List<BookEntity> = emptyList(),
    val isFavorite: Boolean = false,
    val isLiked: Boolean = false,
    val isLoading: Boolean = true
)

class BookDetailsViewModel(
    private val bookId: Long,
    private val repository: BooksRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailsUiState())
    val uiState: StateFlow<BookDetailsUiState> = _uiState.asStateFlow()

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            repository.getBookById(bookId).collect { book ->
                if (book != null) {
                    _uiState.update {
                        it.copy(
                            book = book,
                            isFavorite = book.isFavorite,
                            isLiked = book.isLiked,
                            isLoading = false
                        )
                    }
                    loadAdditionalData(book)
                }
            }
        }
    }

    private fun loadAdditionalData(book: BookEntity) {
        viewModelScope.launch {
            repository.getCommentsForBook(bookId).collect { comments ->
                _uiState.update { it.copy(comments = comments) }
            }
        }
        viewModelScope.launch {
            repository.getUserRating(bookId).collect { rating ->
                _uiState.update { it.copy(userRating = rating) }
            }
        }
        viewModelScope.launch {
            repository.getBooksByCategory(book.category).collect { categoryBooks ->
                val related = categoryBooks.filter { it.id != bookId }
                _uiState.update { it.copy(relatedBooks = related) }
            }
        }
    }

    fun toggleFavorite() {
        val currentBook = _uiState.value.book ?: return
        viewModelScope.launch {
            repository.toggleFavorite(currentBook)
            _uiState.update { it.copy(isFavorite = !it.isFavorite) }
        }
    }

    fun toggleLike() {
        val currentBook = _uiState.value.book ?: return
        viewModelScope.launch {
            repository.toggleLike(currentBook)
            _uiState.update { it.copy(isLiked = !it.isLiked) }
        }
    }

    fun rateBook(stars: Int) {
        viewModelScope.launch {
            repository.rateBook(bookId, stars)
        }
    }

    fun addComment(userName: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(bookId, userName, text)
        }
    }
}
