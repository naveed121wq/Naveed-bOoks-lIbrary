package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY nameEn ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Long): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories WHERE nameEn = :nameEn OR nameUr = :nameUr LIMIT 1")
    suspend fun getCategoryByName(nameEn: String, nameUr: String): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    fun getTotalCategoriesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("UPDATE categories SET booksCount = :count WHERE nameEn = :nameEn")
    suspend fun updateBookCount(nameEn: String, count: Int)

    @Query("SELECT * FROM categories")
    suspend fun getAllCategoriesList(): List<CategoryEntity>
}
