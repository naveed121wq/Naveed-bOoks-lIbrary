package com.example.data.storage

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {

    suspend fun exportBackupToJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("timestamp", System.currentTimeMillis())

            // Export Categories
            val categories = database.categoryDao().getAllCategoriesList()
            val catArray = JSONArray()
            for (c in categories) {
                val obj = JSONObject()
                obj.put("nameEn", c.nameEn)
                obj.put("nameUr", c.nameUr)
                obj.put("description", c.description)
                catArray.put(obj)
            }
            root.put("categories", catArray)

            // Export Authors
            val authors = database.authorDao().getAllAuthorsList()
            val authArray = JSONArray()
            for (a in authors) {
                val obj = JSONObject()
                obj.put("name", a.name)
                obj.put("biography", a.biography)
                authArray.put(obj)
            }
            root.put("authors", authArray)

            // Export Books
            val books = database.bookDao().getAllBooksList()
            val bookArray = JSONArray()
            for (b in books) {
                val obj = JSONObject()
                obj.put("title", b.title)
                obj.put("author", b.author)
                obj.put("category", b.category)
                obj.put("subcategory", b.subcategory)
                obj.put("language", b.language)
                obj.put("description", b.description)
                obj.put("publisher", b.publisher)
                obj.put("publicationYear", b.publicationYear)
                obj.put("isbn", b.isbn)
                obj.put("tags", b.tags)
                obj.put("customNotes", b.customNotes)
                obj.put("isFavorite", b.isFavorite)
                obj.put("isFeatured", b.isFeatured)
                bookArray.put(obj)
            }
            root.put("books", bookArray)

            // Write to Uri
            context.contentResolver.openOutputStream(uri)?.use { out ->
                OutputStreamWriter(out).use { writer ->
                    writer.write(root.toString(2))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreBackupFromJson(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val stringBuilder = java.lang.StringBuilder()
            context.contentResolver.openInputStream(uri)?.use { inStream ->
                BufferedReader(InputStreamReader(inStream)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line)
                        line = reader.readLine()
                    }
                }
            }
            val root = JSONObject(stringBuilder.toString())

            // Restore Categories
            if (root.has("categories")) {
                val catArray = root.getJSONArray("categories")
                for (i in 0 until catArray.length()) {
                    val obj = catArray.getJSONObject(i)
                    val nameEn = obj.getString("nameEn")
                    val nameUr = obj.getString("nameUr")
                    val desc = obj.optString("description", "")
                    val existing = database.categoryDao().getCategoryByName(nameEn, nameUr)
                    if (existing == null) {
                        database.categoryDao().insertCategory(
                            CategoryEntity(nameEn = nameEn, nameUr = nameUr, description = desc)
                        )
                    }
                }
            }

            // Restore Authors
            if (root.has("authors")) {
                val authArray = root.getJSONArray("authors")
                for (i in 0 until authArray.length()) {
                    val obj = authArray.getJSONObject(i)
                    val name = obj.getString("name")
                    val bio = obj.optString("biography", "")
                    val existing = database.authorDao().getAuthorByName(name)
                    if (existing == null) {
                        database.authorDao().insertAuthor(
                            AuthorEntity(name = name, biography = bio)
                        )
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
