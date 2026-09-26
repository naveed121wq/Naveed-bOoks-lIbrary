package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.RatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings WHERE bookId = :bookId LIMIT 1")
    fun getUserRating(bookId: Long): Flow<RatingEntity?>

    @Query("SELECT AVG(rating) FROM ratings WHERE bookId = :bookId")
    suspend fun getAverageRating(bookId: Long): Float?

    @Query("SELECT COUNT(*) FROM ratings WHERE bookId = :bookId")
    suspend fun getRatingsCount(bookId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: RatingEntity): Long

    @Query("DELETE FROM ratings WHERE bookId = :bookId")
    suspend fun deleteForBook(bookId: Long)
}
