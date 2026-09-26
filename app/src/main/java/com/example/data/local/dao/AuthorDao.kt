package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.AuthorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {
    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun getAllAuthors(): Flow<List<AuthorEntity>>

    @Query("SELECT * FROM authors WHERE id = :id")
    fun getAuthorById(id: Long): Flow<AuthorEntity?>

    @Query("SELECT * FROM authors WHERE name = :name LIMIT 1")
    suspend fun getAuthorByName(name: String): AuthorEntity?

    @Query("SELECT COUNT(*) FROM authors")
    fun getTotalAuthorsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuthor(author: AuthorEntity): Long

    @Update
    suspend fun updateAuthor(author: AuthorEntity)

    @Delete
    suspend fun deleteAuthor(author: AuthorEntity)

    @Query("SELECT * FROM authors")
    suspend fun getAllAuthorsList(): List<AuthorEntity>
}
