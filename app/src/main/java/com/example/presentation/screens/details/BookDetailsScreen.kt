package com.example.presentation.screens.details

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.presentation.components.BookCard
import com.example.presentation.components.RatingBar
import com.example.presentation.components.SectionHeader
import com.example.presentation.localization.AppStrings
import com.example.ui.theme.SapphireBlue
import com.example.ui.theme.Slate800
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    strings: AppStrings,
    viewModel: BookDetailsViewModel,
    onBackClick: () -> Unit,
    onReadClick: (Long) -> Unit,
    onRelatedBookClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val book = uiState.book

    var showRatingDialog by remember { mutableStateOf(false) }
    var showCommentDialog by remember { mutableStateOf(false) }
    var userCommentText by remember { mutableStateOf("") }
    var userNameText by remember { mutableStateOf("Reader") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = strings.bookDetails, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (book != null) {
                        IconButton(onClick = {
                            val file = File(book.pdfFilePath)
                            if (file.exists()) {
                                try {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share ${book.title}"))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (book == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header with large cover
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Slate800, SapphireBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(180.dp)
                                .height(250.dp)
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp
                        ) {
                            if (book.coverImagePath.isNotEmpty() && File(book.coverImagePath).exists()) {
                                AsyncImage(
                                    model = File(book.coverImagePath),
                                    contentDescription = book.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Slate800),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Book,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(64.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Title and Meta
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${strings.author}: ${book.author}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(book.category) }
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(book.language) }
                            )
                            if (book.pageCount > 0) {
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("${book.pageCount} ${strings.pages}") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Rating row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            RatingBar(rating = book.averageRating, starSize = 20.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "%.1f (%d)".format(book.averageRating, book.ratingsCount),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons: Read Now, Like, Favorite
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { onReadClick(book.id) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("read_now_button")
                            ) {
                                Icon(Icons.Default.MenuBook, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (book.lastReadPage > 1) "${strings.continueReading} (p. ${book.lastReadPage})" else strings.readNow,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { viewModel.toggleFavorite() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("details_favorite_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Favorite",
                                    tint = if (uiState.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { viewModel.toggleLike() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("details_like_button")
                            ) {
                                Icon(
                                    imageVector = if (uiState.isLiked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                    contentDescription = "Like",
                                    tint = if (uiState.isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            FilledTonalIconButton(
                                onClick = { showRatingDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("details_rate_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rate",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Info Cards Grid
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (book.description.isNotEmpty()) {
                                Text(
                                    text = strings.description,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = book.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (book.publisher.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "${strings.publisher}: ", fontWeight = FontWeight.SemiBold)
                                    Text(text = book.publisher)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            if (book.publicationYear.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "${strings.publicationYear}: ", fontWeight = FontWeight.SemiBold)
                                    Text(text = book.publicationYear)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            if (book.isbn.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "${strings.isbn}: ", fontWeight = FontWeight.SemiBold)
                                    Text(text = book.isbn)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            if (book.fileSize > 0) {
                                val sizeMb = book.fileSize / (1024f * 1024f)
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "${strings.fileSize}: ", fontWeight = FontWeight.SemiBold)
                                    Text(text = "%.2f MB".format(sizeMb))
                                }
                            }
                        }
                    }
                }

                // Reviews & Comments Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${strings.comments} (${uiState.comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { showCommentDialog = true }) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = strings.addComment)
                        }
                    }
                }

                // Comments List
                if (uiState.comments.isEmpty()) {
                    item {
                        Text(
                            text = "No reviews yet. Be the first to review!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else {
                    items(uiState.comments) { comment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = comment.userName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.commentText,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Related Books Section
                if (uiState.relatedBooks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(title = strings.recommended)
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.relatedBooks) { relBook ->
                                BookCard(
                                    book = relBook,
                                    onClick = { onRelatedBookClick(relBook.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog) {
        var selectedRating by remember { mutableStateOf(5) }
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text(text = strings.rateBook) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    RatingBar(
                        rating = selectedRating.toFloat(),
                        starSize = 36.dp,
                        onRatingChanged = { selectedRating = it }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.rateBook(selectedRating)
                    showRatingDialog = false
                }) {
                    Text(strings.submit)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRatingDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Add Comment Dialog
    if (showCommentDialog) {
        AlertDialog(
            onDismissRequest = { showCommentDialog = false },
            title = { Text(text = strings.addComment) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = userNameText,
                        onValueChange = { userNameText = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = userCommentText,
                        onValueChange = { userCommentText = it },
                        label = { Text(strings.writeComment) },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (userCommentText.isNotBlank()) {
                        viewModel.addComment(userNameText, userCommentText)
                        userCommentText = ""
                        showCommentDialog = false
                    }
                }) {
                    Text(strings.submit)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCommentDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
