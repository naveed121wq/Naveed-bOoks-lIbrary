package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookNumber: String = "",
    val title: String,
    val author: String,
    val category: String,
    val subcategory: String = "",
    val language: String = "English", // English, Urdu, Arabic
    val description: String = "",
    val publisher: String = "",
    val publicationYear: String = "",
    val isbn: String = "",
    val tags: String = "",
    val customNotes: String = "",
    val pdfFilePath: String,
    val coverImagePath: String = "",
    val fileSize: Long = 0L,
    val pageCount: Int = 0,
    val lastReadPage: Int = 1,
    val isFavorite: Boolean = false,
    val isFeatured: Boolean = false,
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val readCount: Int = 0,
    val averageRating: Float = 0f,
    val ratingsCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val lastReadTime: Long = 0L
)
