package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.storage.FileStorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BooksRepository(
    private val database: AppDatabase,
    private val fileStorageManager: FileStorageManager
) {
    private val bookDao = database.bookDao()
    private val authorDao = database.authorDao()
    private val categoryDao = database.categoryDao()
    private val bookmarkDao = database.bookmarkDao()
    private val readingHistoryDao = database.readingHistoryDao()
    private val ratingDao = database.ratingDao()
    private val commentDao = database.commentDao()
    private val searchHistoryDao = database.searchHistoryDao()

    // Books
    fun getAllBooks(): Flow<List<BookEntity>> = bookDao.getAllBooks()
    fun getBookById(id: Long): Flow<BookEntity?> = bookDao.getBookById(id)
    suspend fun getBookByIdDirect(id: Long): BookEntity? = bookDao.getBookByIdDirect(id)
    fun getFeaturedBooks(): Flow<List<BookEntity>> = bookDao.getFeaturedBooks()
    fun getRecentlyAddedBooks(): Flow<List<BookEntity>> = bookDao.getRecentlyAddedBooks()
    fun getPopularBooks(): Flow<List<BookEntity>> = bookDao.getPopularBooks()
    fun getMostLikedBooks(): Flow<List<BookEntity>> = bookDao.getMostLikedBooks()
    fun getFavoriteBooks(): Flow<List<BookEntity>> = bookDao.getFavoriteBooks()
    fun getContinueReadingBooks(): Flow<List<BookEntity>> = bookDao.getContinueReadingBooks()
    fun getBooksByCategory(category: String): Flow<List<BookEntity>> = bookDao.getBooksByCategory(category)
    fun getBooksByAuthor(author: String): Flow<List<BookEntity>> = bookDao.getBooksByAuthor(author)
    fun searchBooks(query: String): Flow<List<BookEntity>> = bookDao.searchBooks(query)
    fun getTotalBooksCount(): Flow<Int> = bookDao.getTotalBooksCount()

    suspend fun findDuplicate(title: String, author: String): BookEntity? = withContext(Dispatchers.IO) {
        bookDao.findDuplicate(title, author)
    }

    suspend fun insertBook(book: BookEntity): Long = withContext(Dispatchers.IO) {
        val id = bookDao.insertBook(book)
        updateCategoryCount(book.category)
        id
    }

    suspend fun updateBook(book: BookEntity) = withContext(Dispatchers.IO) {
        bookDao.updateBook(book)
        updateCategoryCount(book.category)
    }

    suspend fun deleteBook(book: BookEntity) = withContext(Dispatchers.IO) {
        fileStorageManager.deleteBookFiles(book.pdfFilePath, book.coverImagePath)
        bookDao.deleteBook(book)
        bookmarkDao.deleteAllForBook(book.id)
        readingHistoryDao.deleteForBook(book.id)
        ratingDao.deleteForBook(book.id)
        commentDao.deleteForBook(book.id)
        updateCategoryCount(book.category)
    }

    suspend fun toggleFavorite(book: BookEntity) = withContext(Dispatchers.IO) {
        val newFav = !book.isFavorite
        bookDao.updateFavorite(book.id, newFav)
    }

    suspend fun toggleLike(book: BookEntity) = withContext(Dispatchers.IO) {
        val newLiked = !book.isLiked
        val delta = if (newLiked) 1 else -1
        bookDao.updateLike(book.id, newLiked, delta)
    }

    suspend fun updateReadingProgress(bookId: Long, page: Int, totalPages: Int) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        bookDao.updateReadingProgress(bookId, page, now)
        readingHistoryDao.insertOrUpdate(
            ReadingHistoryEntity(
                bookId = bookId,
                lastPageRead = page,
                totalPages = totalPages,
                timestamp = now
            )
        )
    }

    // Categories
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    fun getTotalCategoriesCount(): Flow<Int> = categoryDao.getTotalCategoriesCount()
    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }
    suspend fun updateCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(category)
    }
    suspend fun deleteCategory(category: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }
    private suspend fun updateCategoryCount(categoryName: String) = withContext(Dispatchers.IO) {
        val count = bookDao.getCountByCategory(categoryName)
        categoryDao.updateBookCount(categoryName, count)
    }

    // Authors
    fun getAllAuthors(): Flow<List<AuthorEntity>> = authorDao.getAllAuthors()
    fun getAuthorById(id: Long): Flow<AuthorEntity?> = authorDao.getAuthorById(id)
    fun getTotalAuthorsCount(): Flow<Int> = authorDao.getTotalAuthorsCount()
    suspend fun insertAuthor(author: AuthorEntity): Long = withContext(Dispatchers.IO) {
        authorDao.insertAuthor(author)
    }
    suspend fun updateAuthor(author: AuthorEntity) = withContext(Dispatchers.IO) {
        authorDao.updateAuthor(author)
    }
    suspend fun deleteAuthor(author: AuthorEntity) = withContext(Dispatchers.IO) {
        authorDao.deleteAuthor(author)
    }

    // Bookmarks
    fun getBookmarksForBook(bookId: Long): Flow<List<BookmarkEntity>> = bookmarkDao.getBookmarksForBook(bookId)
    suspend fun isPageBookmarked(bookId: Long, page: Int): Boolean = withContext(Dispatchers.IO) {
        bookmarkDao.getBookmarkForPage(bookId, page) != null
    }
    suspend fun toggleBookmark(bookId: Long, page: Int, title: String) = withContext(Dispatchers.IO) {
        val existing = bookmarkDao.getBookmarkForPage(bookId, page)
        if (existing != null) {
            bookmarkDao.deleteBookmark(existing)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(bookId = bookId, pageNumber = page, title = title)
            )
        }
    }
    suspend fun deleteBookmark(bookmark: BookmarkEntity) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(bookmark)
    }

    // Ratings
    fun getUserRating(bookId: Long): Flow<RatingEntity?> = ratingDao.getUserRating(bookId)
    suspend fun rateBook(bookId: Long, rating: Int) = withContext(Dispatchers.IO) {
        ratingDao.insertRating(RatingEntity(bookId = bookId, rating = rating))
        val avg = ratingDao.getAverageRating(bookId) ?: rating.toFloat()
        val count = ratingDao.getRatingsCount(bookId)
        bookDao.updateRatingSummary(bookId, avg, count)
    }

    // Comments
    fun getCommentsForBook(bookId: Long): Flow<List<CommentEntity>> = commentDao.getCommentsForBook(bookId)
    suspend fun addComment(bookId: Long, userName: String, text: String): Long = withContext(Dispatchers.IO) {
        commentDao.insertComment(CommentEntity(bookId = bookId, userName = userName, commentText = text))
    }

    // Search History
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> = searchHistoryDao.getRecentSearches()
    suspend fun addSearchQuery(query: String) = withContext(Dispatchers.IO) {
        if (query.isNotBlank()) {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query.trim()))
        }
    }
    suspend fun deleteSearchQuery(query: String) = withContext(Dispatchers.IO) {
        searchHistoryDao.deleteQuery(query)
    }
    suspend fun clearSearchHistory() = withContext(Dispatchers.IO) {
        searchHistoryDao.clearHistory()
    }
}
