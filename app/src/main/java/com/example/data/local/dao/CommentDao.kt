package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.CommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getCommentsForBook(bookId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity): Long

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE bookId = :bookId")
    suspend fun deleteForBook(bookId: Long)

    @Query("SELECT * FROM comments")
    suspend fun getAllCommentsList(): List<CommentEntity>
}
