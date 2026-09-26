package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getBookmarksForBook(bookId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND pageNumber = :page LIMIT 1")
    suspend fun getBookmarkForPage(bookId: Long, page: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId AND pageNumber = :page")
    suspend fun deleteBookmarkByPage(bookId: Long, page: Int)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteAllForBook(bookId: Long)

    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarksList(): List<BookmarkEntity>
}
