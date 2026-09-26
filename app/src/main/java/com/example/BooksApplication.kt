package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AuthorEntity
import com.example.data.local.entities.CategoryEntity
import com.example.data.local.preferences.UserPreferencesRepository
import com.example.data.repository.BooksRepository
import com.example.data.storage.BackupManager
import com.example.data.storage.FileStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BooksApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: AppDatabase
        private set

    lateinit var fileStorageManager: FileStorageManager
        private set

    lateinit var backupManager: BackupManager
        private set

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var booksRepository: BooksRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        fileStorageManager = FileStorageManager(this)
        backupManager = BackupManager(this, database)
        userPreferencesRepository = UserPreferencesRepository(this)
        booksRepository = BooksRepository(database, fileStorageManager)

        // Seed initial categories & authors if database is fresh
        applicationScope.launch {
            seedDefaultData()
        }
    }

    private suspend fun seedDefaultData() {
        val catDao = database.categoryDao()
        val authDao = database.authorDao()

        val existingCats = catDao.getAllCategoriesList()
        if (existingCats.isEmpty()) {
            val defaults = listOf(
                CategoryEntity(nameEn = "Islamic Studies", nameUr = "اسلامیات", description = "Books on Islam, Quran, Hadith, and Seerah"),
                CategoryEntity(nameEn = "Urdu Literature", nameUr = "اردو ادب", description = "Classic and contemporary Urdu literature, prose, and criticism"),
                CategoryEntity(nameEn = "English Literature", nameUr = "انگریزی ادب", description = "English fiction, classic novels, and literary works"),
                CategoryEntity(nameEn = "History", nameUr = "تاریخ", description = "World history, regional histories, and historical accounts"),
                CategoryEntity(nameEn = "Poetry", nameUr = "شاعری", description = "Ghazals, Nazms, and poetic anthologies"),
                CategoryEntity(nameEn = "Novels & Fiction", nameUr = "ناول و فکشن", description = "Compelling novels and narrative fiction"),
                CategoryEntity(nameEn = "Education & Knowledge", nameUr = "تعلیم و معلومات", description = "Educational resources and general knowledge"),
                CategoryEntity(nameEn = "Science & Tech", nameUr = "سائنس و ٹیکنالوجی", description = "Scientific exploration and technology guides")
            )
            catDao.insertAll(defaults)
        }

        val existingAuthors = authDao.getAllAuthorsList()
        if (existingAuthors.isEmpty()) {
            val defaultAuthors = listOf(
                AuthorEntity(name = "Allama Muhammad Iqbal", biography = "Philosopher, poet, and politician in British India who is widely regarded as having inspired the Pakistan Movement."),
                AuthorEntity(name = "Mirza Asadullah Khan Ghalib", biography = "Prominent Urdu and Persian poet during the last years of the Mughal Empire."),
                AuthorEntity(name = "Saadat Hasan Manto", biography = "Celebrated writer, playwright and author born in Ludhiana, famous for his poignant short stories."),
                AuthorEntity(name = "Ashfaq Ahmed", biography = "Renowned Pakistani writer, playwright and broadcaster, author of numerous classic Urdu dramas and philosophical books."),
                AuthorEntity(name = "Bano Qudsia", biography = "Eminent Pakistani novelist, playwright and spiritualist, celebrated for her masterpiece novel Raja Gidh.")
            )
            for (author in defaultAuthors) {
                authDao.insertAuthor(author)
            }
        }
    }
}
