package com.example.data.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FileStorageManager(private val context: Context) {

    private val booksDir: File
        get() = File(context.filesDir, "books").apply { if (!exists()) mkdirs() }

    private val coversDir: File
        get() = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }

    private val renderCacheDir: File
        get() = File(context.cacheDir, "pdf_renders").apply { if (!exists()) mkdirs() }

    suspend fun savePdfFromUri(uri: Uri, fileName: String): Pair<String, Long> = withContext(Dispatchers.IO) {
        val sanitized = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val destFile = File(booksDir, "${System.currentTimeMillis()}_$sanitized")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        Pair(destFile.absolutePath, destFile.length())
    }

    suspend fun saveCoverFromUri(uri: Uri): String = withContext(Dispatchers.IO) {
        val destFile = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        destFile.absolutePath
    }

    suspend fun generateCoverFromPdf(pdfPath: String): String = withContext(Dispatchers.IO) {
        val pdfFile = File(pdfPath)
        if (!pdfFile.exists()) return@withContext ""
        try {
            val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                val width = 400
                val height = (width * (page.height.toFloat() / page.width.toFloat())).toInt()
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                pfd.close()

                val coverFile = File(coversDir, "pdf_cover_${System.currentTimeMillis()}.jpg")
                FileOutputStream(coverFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                bitmap.recycle()
                return@withContext coverFile.absolutePath
            }
            renderer.close()
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        ""
    }

    suspend fun getPdfPageCount(pdfPath: String): Int = withContext(Dispatchers.IO) {
        val file = File(pdfPath)
        if (!file.exists()) return@withContext 0
        try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            pfd.close()
            count
        } catch (e: Exception) {
            0
        }
    }

    suspend fun deleteBookFiles(pdfPath: String, coverPath: String) = withContext(Dispatchers.IO) {
        try {
            if (pdfPath.isNotEmpty()) {
                val pdf = File(pdfPath)
                if (pdf.exists()) pdf.delete()
            }
            if (coverPath.isNotEmpty() && !coverPath.startsWith("http")) {
                val cover = File(coverPath)
                if (cover.exists()) cover.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun clearRenderCache(): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        if (renderCacheDir.exists()) {
            renderCacheDir.listFiles()?.forEach { file ->
                freed += file.length()
                file.delete()
            }
        }
        freed
    }

    data class StorageStats(
        val pdfsSize: Long,
        val coversSize: Long,
        val databaseSize: Long,
        val cacheSize: Long,
        val totalSize: Long
    )

    suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
        val pdfs = getDirectorySize(booksDir)
        val covers = getDirectorySize(coversDir)
        val cache = getDirectorySize(renderCacheDir) + getDirectorySize(context.cacheDir)
        val dbFile = context.getDatabasePath("books_library.db")
        val dbSize = if (dbFile.exists()) dbFile.length() else 0L

        StorageStats(
            pdfsSize = pdfs,
            coversSize = covers,
            databaseSize = dbSize,
            cacheSize = cache,
            totalSize = pdfs + covers + dbSize + cache
        )
    }

    private fun getDirectorySize(dir: File): Long {
        var size = 0L
        if (dir.exists()) {
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) getDirectorySize(file) else file.length()
            }
        }
        return size
    }
}
