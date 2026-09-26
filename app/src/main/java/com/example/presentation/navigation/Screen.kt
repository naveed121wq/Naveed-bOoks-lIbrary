package com.example.presentation.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object Categories : Screen("categories")
    object Search : Screen("search")
    object Favorites : Screen("favorites")
    object Settings : Screen("settings")
    object Authors : Screen("authors")
    object StorageManagement : Screen("storage_management")
    object AdminLogin : Screen("admin_login")
    object AdminDashboard : Screen("admin_dashboard")
    object AddBook : Screen("add_book")
    object ManageCategories : Screen("manage_categories")
    object ManageAuthors : Screen("manage_authors")

    object BookDetails : Screen("book_details/{bookId}") {
        fun createRoute(bookId: Long) = "book_details/$bookId"
    }

    object EditBook : Screen("edit_book/{bookId}") {
        fun createRoute(bookId: Long) = "edit_book/$bookId"
    }

    object CategoryBooks : Screen("category_books/{categoryName}") {
        fun createRoute(categoryName: String) = "category_books/$categoryName"
    }

    object AuthorBooks : Screen("author_books/{authorName}") {
        fun createRoute(authorName: String) = "author_books/$authorName"
    }

    object PdfReader : Screen("pdf_reader/{bookId}") {
        fun createRoute(bookId: Long) = "pdf_reader/$bookId"
    }
}
