package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY id DESC")
    fun getAllBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookById(id: Long): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookByIdDirect(id: Long): BookEntity?

    @Query("SELECT * FROM books WHERE isFeatured = 1 ORDER BY id DESC")
    fun getFeaturedBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY dateAdded DESC LIMIT 10")
    fun getRecentlyAddedBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY readCount DESC LIMIT 10")
    fun getPopularBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY likesCount DESC LIMIT 10")
    fun getMostLikedBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY lastModified DESC")
    fun getFavoriteBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE lastReadTime > 0 ORDER BY lastReadTime DESC LIMIT 10")
    fun getContinueReadingBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE category = :category ORDER BY id DESC")
    fun getBooksByCategory(category: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE author = :author ORDER BY id DESC")
    fun getBooksByAuthor(author: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR isbn LIKE '%' || :query || '%'")
    fun searchBooks(query: String): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE title = :title AND author = :author LIMIT 1")
    suspend fun findDuplicate(title: String, author: String): BookEntity?

    @Query("SELECT COUNT(*) FROM books")
    fun getTotalBooksCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM books WHERE category = :category")
    suspend fun getCountByCategory(category: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookEntity): Long

    @Update
    suspend fun updateBook(book: BookEntity)

    @Delete
    suspend fun deleteBook(book: BookEntity)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: Long)

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE books SET isLiked = :isLiked, likesCount = likesCount + :delta WHERE id = :id")
    suspend fun updateLike(id: Long, isLiked: Boolean, delta: Int)

    @Query("UPDATE books SET lastReadPage = :page, lastReadTime = :time, readCount = readCount + 1 WHERE id = :id")
    suspend fun updateReadingProgress(id: Long, page: Int, time: Long)

    @Query("UPDATE books SET averageRating = :avgRating, ratingsCount = :count WHERE id = :id")
    suspend fun updateRatingSummary(id: Long, avgRating: Float, count: Int)

    @Query("SELECT * FROM books")
    suspend fun getAllBooksList(): List<BookEntity>
}
