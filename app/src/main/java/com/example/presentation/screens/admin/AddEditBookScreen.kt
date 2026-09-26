package com.example.presentation.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BookEntity
import com.example.data.repository.BooksRepository
import com.example.data.storage.FileStorageManager
import com.example.presentation.localization.AppStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBookScreen(
    bookId: Long?,
    strings: AppStrings,
    repository: BooksRepository,
    fileStorageManager: FileStorageManager,
    onSaveSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEditMode = bookId != null && bookId > 0

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Islamic Studies") }
    var subcategory by remember { mutableStateOf("") }
    var language by remember { mutableStateOf("English") }
    var description by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var publicationYear by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var customNotes by remember { mutableStateOf("") }
    var bookNumber by remember { mutableStateOf("") }

    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCoverUri by remember { mutableStateOf<Uri?>(null) }
    var existingPdfPath by remember { mutableStateOf("") }
    var existingCoverPath by remember { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isDuplicateWarning by remember { mutableStateOf(false) }

    val categories by repository.getAllCategories().collectAsState(initial = emptyList())

    // Load existing book if editing
    LaunchedEffect(bookId) {
        if (isEditMode) {
            val book = repository.getBookByIdDirect(bookId!!)
            if (book != null) {
                title = book.title
                author = book.author
                category = book.category
                subcategory = book.subcategory
                language = book.language
                description = book.description
                publisher = book.publisher
                publicationYear = book.publicationYear
                isbn = book.isbn
                tags = book.tags
                customNotes = book.customNotes
                bookNumber = book.bookNumber
                existingPdfPath = book.pdfFilePath
                existingCoverPath = book.coverImagePath
            }
        }
    }

    // SAF PDF Picker
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPdfUri = uri
            errorMessage = null
        }
    }

    // SAF Cover Picker
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedCoverUri = uri
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) strings.editBook else strings.addNewBook, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // PDF File Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedPdfUri != null) strings.pdfSelected else if (existingPdfPath.isNotEmpty()) "PDF File Attached" else strings.selectPdf,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.testTag("pick_pdf_button")
                        ) {
                            Text(if (selectedPdfUri != null || existingPdfPath.isNotEmpty()) "Replace PDF" else strings.selectPdf)
                        }
                    }
                }
            }

            // Cover Image Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = strings.selectCover, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (selectedCoverUri != null) strings.coverSelected else if (existingCoverPath.isNotEmpty()) "Cover Attached" else "Optional (auto-generated from PDF)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = { coverPickerLauncher.launch("image/*") },
                            modifier = Modifier.testTag("pick_cover_button")
                        ) {
                            Text("Select")
                        }
                    }
                }
            }

            // Book Details Inputs
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    label = { Text("${strings.title} *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_title_input")
                )
            }

            item {
                OutlinedTextField(
                    value = author,
                    onValueChange = {
                        author = it
                        errorMessage = null
                    },
                    label = { Text("${strings.author} *") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("book_author_input")
                )
            }

            // Category Selection Dropdown
            item {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.category) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text("${cat.nameEn} (${cat.nameUr})") },
                                onClick = {
                                    category = cat.nameEn
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Language Selection Chips
            item {
                Text(text = strings.language, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("English", "Urdu", "Arabic").forEach { lang ->
                        FilterChip(
                            selected = language == lang,
                            onClick = { language = lang },
                            label = { Text(lang) }
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = subcategory,
                    onValueChange = { subcategory = it },
                    label = { Text(strings.subcategory) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text(strings.publisher) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = publicationYear,
                        onValueChange = { publicationYear = it },
                        label = { Text(strings.publicationYear) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = isbn,
                        onValueChange = { isbn = it },
                        label = { Text(strings.isbn) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags / Keywords (comma separated)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(strings.description) },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error Display
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Submit Button
            item {
                Button(
                    onClick = {
                        if (title.isBlank()) {
                            errorMessage = "Please enter a book title."
                            return@Button
                        }
                        if (author.isBlank()) {
                            errorMessage = "Please enter an author name."
                            return@Button
                        }
                        if (!isEditMode && selectedPdfUri == null) {
                            errorMessage = "Please select a PDF document."
                            return@Button
                        }

                        isSaving = true
                        scope.launch {
                            try {
                                var pdfPath = existingPdfPath
                                var fileSize = 0L

                                selectedPdfUri?.let { uri ->
                                    val saved = fileStorageManager.savePdfFromUri(uri, "$title.pdf")
                                    pdfPath = saved.first
                                    fileSize = saved.second
                                }

                                var coverPath = existingCoverPath
                                selectedCoverUri?.let { uri ->
                                    coverPath = fileStorageManager.saveCoverFromUri(uri)
                                }

                                if (coverPath.isEmpty() && pdfPath.isNotEmpty()) {
                                    coverPath = fileStorageManager.generateCoverFromPdf(pdfPath)
                                }

                                val pageCount = fileStorageManager.getPdfPageCount(pdfPath)

                                val bookEntity = BookEntity(
                                    id = if (isEditMode) bookId!! else 0,
                                    title = title.trim(),
                                    author = author.trim(),
                                    category = category,
                                    subcategory = subcategory.trim(),
                                    language = language,
                                    description = description.trim(),
                                    publisher = publisher.trim(),
                                    publicationYear = publicationYear.trim(),
                                    isbn = isbn.trim(),
                                    tags = tags.trim(),
                                    customNotes = customNotes.trim(),
                                    bookNumber = bookNumber.trim(),
                                    pdfFilePath = pdfPath,
                                    coverImagePath = coverPath,
                                    fileSize = fileSize,
                                    pageCount = pageCount,
                                    lastModified = System.currentTimeMillis()
                                )

                                if (isEditMode) {
                                    repository.updateBook(bookEntity)
                                } else {
                                    repository.insertBook(bookEntity)
                                }

                                isSaving = false
                                onSaveSuccess()
                            } catch (e: Exception) {
                                e.printStackTrace()
                                errorMessage = "Failed to save: ${e.message}"
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_book_submit_btn"),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (isEditMode) strings.updateBook else strings.saveBook, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
