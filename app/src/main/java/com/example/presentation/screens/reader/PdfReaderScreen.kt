package com.example.presentation.screens.reader

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.local.entities.BookEntity
import com.example.data.local.entities.BookmarkEntity
import com.example.data.repository.BooksRepository
import com.example.presentation.localization.AppStrings
import com.example.ui.theme.SepiaBackground
import kotlinx.coroutines.launch
import java.io.File

enum class ReaderThemeMode {
    LIGHT, DARK, SEPIA
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    bookId: Long,
    repository: BooksRepository,
    strings: AppStrings,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var book by remember { mutableStateOf<BookEntity?>(null) }
    var pdfRendererHelper by remember { mutableStateOf<PdfRendererHelper?>(null) }
    var totalPages by remember { mutableStateOf(0) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var currentPageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isControlsVisible by remember { mutableStateOf(true) }
    var themeMode by remember { mutableStateOf(ReaderThemeMode.LIGHT) }
    var bookmarks by remember { mutableStateOf<List<BookmarkEntity>>(emptyList()) }
    var isBookmarked by remember { mutableStateOf(false) }

    var showJumpDialog by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }

    // Zoom & Pan state
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // Keep screen awake
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            pdfRendererHelper?.close()
        }
    }

    // Load Book & Bookmarks
    LaunchedEffect(bookId) {
        val b = repository.getBookByIdDirect(bookId)
        if (b != null) {
            book = b
            val pdfFile = File(b.pdfFilePath)
            if (pdfFile.exists()) {
                val helper = PdfRendererHelper(pdfFile)
                pdfRendererHelper = helper
                totalPages = helper.pageCount
                val initialPage = (b.lastReadPage - 1).coerceIn(0, (totalPages - 1).coerceAtLeast(0))
                currentPageIndex = initialPage
            }
        }
    }

    // Observe bookmarks
    LaunchedEffect(bookId) {
        repository.getBookmarksForBook(bookId).collect {
            bookmarks = it
        }
    }

    // Check if current page is bookmarked
    LaunchedEffect(currentPageIndex, bookmarks) {
        isBookmarked = bookmarks.any { it.pageNumber == (currentPageIndex + 1) }
    }

    // Render page when index changes
    LaunchedEffect(currentPageIndex, pdfRendererHelper) {
        val helper = pdfRendererHelper ?: return@LaunchedEffect
        val bmp = helper.renderPage(currentPageIndex)
        currentPageBitmap = bmp
        // Save progress to DB
        book?.let {
            repository.updateReadingProgress(it.id, currentPageIndex + 1, totalPages)
        }
        // Reset zoom on page change
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    val backgroundColor = when (themeMode) {
        ReaderThemeMode.LIGHT -> Color(0xFFF1F5F9)
        ReaderThemeMode.DARK -> Color(0xFF0F172A)
        ReaderThemeMode.SEPIA -> SepiaBackground
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = book?.title ?: strings.readNow,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            if (totalPages > 0) {
                                Text(
                                    text = "${strings.page} ${currentPageIndex + 1} ${strings.ofPages} $totalPages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                repository.toggleBookmark(
                                    bookId,
                                    currentPageIndex + 1,
                                    "Page ${currentPageIndex + 1}"
                                )
                            }
                        }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(onClick = { showBookmarksSheet = true }) {
                            Icon(Icons.Default.Bookmarks, contentDescription = "Bookmarks List")
                        }
                        IconButton(onClick = { showJumpDialog = true }) {
                            Icon(Icons.Default.PinDrop, contentDescription = "Jump to page")
                        }
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isControlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Slider
                        if (totalPages > 1) {
                            Slider(
                                value = (currentPageIndex + 1).toFloat(),
                                onValueChange = { currentPageIndex = it.toInt() - 1 },
                                valueRange = 1f..totalPages.toFloat(),
                                steps = (totalPages - 2).coerceAtLeast(0),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Navigation Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    if (currentPageIndex > 0) currentPageIndex--
                                },
                                enabled = currentPageIndex > 0
                            ) {
                                Icon(Icons.Default.ArrowBackIos, contentDescription = "Previous Page")
                            }

                            // Theme Selector Pills
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = themeMode == ReaderThemeMode.LIGHT,
                                    onClick = { themeMode = ReaderThemeMode.LIGHT },
                                    label = { Text(strings.light) }
                                )
                                FilterChip(
                                    selected = themeMode == ReaderThemeMode.SEPIA,
                                    onClick = { themeMode = ReaderThemeMode.SEPIA },
                                    label = { Text(strings.sepia) }
                                )
                                FilterChip(
                                    selected = themeMode == ReaderThemeMode.DARK,
                                    onClick = { themeMode = ReaderThemeMode.DARK },
                                    label = { Text(strings.dark) }
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (currentPageIndex < totalPages - 1) currentPageIndex++
                                },
                                enabled = currentPageIndex < totalPages - 1
                            ) {
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next Page")
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
                .clickable { isControlsVisible = !isControlsVisible }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 4f)
                        if (scale > 1f) {
                            offsetX += pan.x
                            offsetY += pan.y
                        } else {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val bitmap = currentPageBitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "PDF Page ${currentPageIndex + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }

    // Jump to Page Dialog
    if (showJumpDialog) {
        var pageInput by remember { mutableStateOf("${currentPageIndex + 1}") }
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text(strings.jumpToPage) },
            text = {
                OutlinedTextField(
                    value = pageInput,
                    onValueChange = { pageInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("1 - $totalPages") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val p = pageInput.toIntOrNull()
                    if (p != null && p in 1..totalPages) {
                        currentPageIndex = p - 1
                        showJumpDialog = false
                    }
                }) {
                    Text(strings.jump)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showJumpDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Bookmarks Sheet
    if (showBookmarksSheet) {
        ModalBottomSheet(onDismissRequest = { showBookmarksSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = strings.bookmarks,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (bookmarks.isEmpty()) {
                    Text(
                        text = strings.noBookmarks,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(bookmarks) { bmk ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentPageIndex = bmk.pageNumber - 1
                                        showBookmarksSheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${strings.page} ${bmk.pageNumber}",
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (bmk.note.isNotEmpty()) {
                                            Text(text = bmk.note, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    IconButton(onClick = {
                                        scope.launch { repository.deleteBookmark(bmk) }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Bookmark")
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
