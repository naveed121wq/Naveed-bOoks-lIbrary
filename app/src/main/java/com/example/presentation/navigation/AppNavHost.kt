package com.example.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.BooksApplication
import com.example.presentation.localization.getAppStrings
import com.example.presentation.screens.admin.*
import com.example.presentation.screens.authors.AuthorBooksScreen
import com.example.presentation.screens.authors.AuthorsScreen
import com.example.presentation.screens.categories.CategoriesScreen
import com.example.presentation.screens.categories.CategoryBooksScreen
import com.example.presentation.screens.details.BookDetailsScreen
import com.example.presentation.screens.details.BookDetailsViewModel
import com.example.presentation.screens.favorites.FavoritesScreen
import com.example.presentation.screens.home.HomeScreen
import com.example.presentation.screens.home.HomeViewModel
import com.example.presentation.screens.onboarding.WelcomeScreen
import com.example.presentation.screens.reader.PdfReaderScreen
import com.example.presentation.screens.search.SearchScreen
import com.example.presentation.screens.search.SearchViewModel
import com.example.presentation.screens.settings.SettingsScreen
import com.example.presentation.screens.settings.SettingsViewModel
import com.example.presentation.screens.storage.StorageManagementScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    app: BooksApplication,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val preferencesRepository = app.userPreferencesRepository
    val repository = app.booksRepository

    val currentLanguage by preferencesRepository.languageFlow.collectAsState(initial = "en")
    val isFirstLaunch by preferencesRepository.isFirstLaunchFlow.collectAsState(initial = false)
    val strings = getAppStrings(currentLanguage)

    val layoutDirection = if (currentLanguage == "ur") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val bottomBarRoutes = listOf(
            Screen.Home.route,
            Screen.Categories.route,
            Screen.Search.route,
            Screen.Favorites.route,
            Screen.Settings.route
        )
        val showBottomBar = currentRoute in bottomBarRoutes

        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = strings.home) },
                            label = { Text(strings.home) },
                            selected = currentRoute == Screen.Home.route,
                            onClick = {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Category, contentDescription = strings.categories) },
                            label = { Text(strings.categories) },
                            selected = currentRoute == Screen.Categories.route,
                            onClick = {
                                navController.navigate(Screen.Categories.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Search, contentDescription = strings.search) },
                            label = { Text(strings.search) },
                            selected = currentRoute == Screen.Search.route,
                            onClick = {
                                navController.navigate(Screen.Search.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Favorite, contentDescription = strings.favorites) },
                            label = { Text(strings.favorites) },
                            selected = currentRoute == Screen.Favorites.route,
                            onClick = {
                                navController.navigate(Screen.Favorites.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Settings, contentDescription = strings.settings) },
                            label = { Text(strings.settings) },
                            selected = currentRoute == Screen.Settings.route,
                            onClick = {
                                navController.navigate(Screen.Settings.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (isFirstLaunch) Screen.Welcome.route else Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Welcome.route) {
                    WelcomeScreen(
                        strings = strings,
                        currentLanguage = currentLanguage,
                        onLanguageSelect = { lang ->
                            scope.launch { preferencesRepository.setLanguage(lang) }
                        },
                        onGetStarted = {
                            scope.launch {
                                preferencesRepository.setFirstLaunchCompleted()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Welcome.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return HomeViewModel(repository) as T
                            }
                        }
                    )
                    HomeScreen(
                        strings = strings,
                        viewModel = homeViewModel,
                        onBookClick = { bookId ->
                            navController.navigate(Screen.BookDetails.createRoute(bookId))
                        },
                        onSearchClick = { navController.navigate(Screen.Search.route) },
                        onCategoryClick = { catName ->
                            navController.navigate(Screen.CategoryBooks.createRoute(catName))
                        },
                        onAuthorClick = { authName ->
                            navController.navigate(Screen.AuthorBooks.createRoute(authName))
                        },
                        onAdminClick = { navController.navigate(Screen.AdminLogin.route) },
                        onToggleLanguage = {
                            val newLang = if (currentLanguage == "en") "ur" else "en"
                            scope.launch { preferencesRepository.setLanguage(newLang) }
                        }
                    )
                }

                composable(Screen.Categories.route) {
                    CategoriesScreen(
                        strings = strings,
                        repository = repository,
                        onCategoryClick = { catName ->
                            navController.navigate(Screen.CategoryBooks.createRoute(catName))
                        }
                    )
                }

                composable(Screen.Search.route) {
                    val searchViewModel: SearchViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return SearchViewModel(repository) as T
                            }
                        }
                    )
                    SearchScreen(
                        strings = strings,
                        viewModel = searchViewModel,
                        onBookClick = { bookId ->
                            navController.navigate(Screen.BookDetails.createRoute(bookId))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.Favorites.route) {
                    FavoritesScreen(
                        strings = strings,
                        repository = repository,
                        onBookClick = { bookId ->
                            navController.navigate(Screen.BookDetails.createRoute(bookId))
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return SettingsViewModel(preferencesRepository) as T
                            }
                        }
                    )
                    SettingsScreen(
                        strings = strings,
                        viewModel = settingsViewModel,
                        onStorageManagementClick = { navController.navigate(Screen.StorageManagement.route) },
                        onAuthorsClick = { navController.navigate(Screen.Authors.route) },
                        onAdminClick = { navController.navigate(Screen.AdminLogin.route) }
                    )
                }

                composable(Screen.Authors.route) {
                    AuthorsScreen(
                        strings = strings,
                        repository = repository,
                        onAuthorClick = { authName ->
                            navController.navigate(Screen.AuthorBooks.createRoute(authName))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.StorageManagement.route) {
                    StorageManagementScreen(
                        strings = strings,
                        fileStorageManager = app.fileStorageManager,
                        backupManager = app.backupManager,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminLogin.route) {
                    AdminLoginScreen(
                        strings = strings,
                        userPreferencesRepository = preferencesRepository,
                        onLoginSuccess = {
                            navController.navigate(Screen.AdminDashboard.route) {
                                popUpTo(Screen.AdminLogin.route) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AdminDashboard.route) {
                    AdminDashboardScreen(
                        strings = strings,
                        repository = repository,
                        onAddBookClick = { navController.navigate(Screen.AddBook.route) },
                        onEditBookClick = { bookId ->
                            navController.navigate(Screen.EditBook.createRoute(bookId))
                        },
                        onManageCategoriesClick = { navController.navigate(Screen.ManageCategories.route) },
                        onManageAuthorsClick = { navController.navigate(Screen.ManageAuthors.route) },
                        onStorageManagementClick = { navController.navigate(Screen.StorageManagement.route) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(Screen.AddBook.route) {
                    AddEditBookScreen(
                        bookId = null,
                        strings = strings,
                        repository = repository,
                        fileStorageManager = app.fileStorageManager,
                        onSaveSuccess = { navController.popBackStack() },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.EditBook.route,
                    arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                    AddEditBookScreen(
                        bookId = bookId,
                        strings = strings,
                        repository = repository,
                        fileStorageManager = app.fileStorageManager,
                        onSaveSuccess = { navController.popBackStack() },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.ManageCategories.route
                ) {
                    ManageCategoriesScreen(
                        strings = strings,
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.ManageAuthors.route
                ) {
                    ManageAuthorsScreen(
                        strings = strings,
                        repository = repository,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.CategoryBooks.route,
                    arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                    CategoryBooksScreen(
                        categoryName = categoryName,
                        strings = strings,
                        repository = repository,
                        onBookClick = { bookId ->
                            navController.navigate(Screen.BookDetails.createRoute(bookId))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.AuthorBooks.route,
                    arguments = listOf(navArgument("authorName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val authorName = backStackEntry.arguments?.getString("authorName") ?: ""
                    AuthorBooksScreen(
                        authorName = authorName,
                        strings = strings,
                        repository = repository,
                        onBookClick = { bookId ->
                            navController.navigate(Screen.BookDetails.createRoute(bookId))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.BookDetails.route,
                    arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                    val detailsViewModel: BookDetailsViewModel = viewModel(
                        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return BookDetailsViewModel(bookId, repository) as T
                            }
                        }
                    )
                    BookDetailsScreen(
                        strings = strings,
                        viewModel = detailsViewModel,
                        onBackClick = { navController.popBackStack() },
                        onReadClick = { bId ->
                            navController.navigate(Screen.PdfReader.createRoute(bId))
                        },
                        onRelatedBookClick = { relId ->
                            navController.navigate(Screen.BookDetails.createRoute(relId))
                        }
                    )
                }

                composable(
                    route = Screen.PdfReader.route,
                    arguments = listOf(navArgument("bookId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val bookId = backStackEntry.arguments?.getLong("bookId") ?: 0L
                    PdfReaderScreen(
                        bookId = bookId,
                        repository = repository,
                        strings = strings,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
