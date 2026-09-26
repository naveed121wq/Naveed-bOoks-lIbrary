package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.ReadingHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history WHERE bookId = :bookId LIMIT 1")
    fun getHistoryForBook(bookId: Long): Flow<ReadingHistoryEntity?>

    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<ReadingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: ReadingHistoryEntity): Long

    @Query("DELETE FROM reading_history WHERE bookId = :bookId")
    suspend fun deleteForBook(bookId: Long)
}
