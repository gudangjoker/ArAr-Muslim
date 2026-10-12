package com.arar.muslim.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.arar.muslim.core.theme.IslamicGold
import com.arar.muslim.core.theme.IslamicGreenPrimary
import com.arar.muslim.presentation.asmaulhusna.AsmaulHusnaScreen
import com.arar.muslim.presentation.asmaulhusna.AsmaulHusnaViewModel
import com.arar.muslim.presentation.doa.DzikirScreen
import com.arar.muslim.presentation.doa.DzikirViewModel
import com.arar.muslim.presentation.qibla.QiblaScreen
import com.arar.muslim.presentation.qibla.QiblaViewModel
import com.arar.muslim.presentation.quran.AyahViewerScreen
import com.arar.muslim.presentation.quran.QuranScreen
import com.arar.muslim.presentation.quran.QuranViewModel

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.arar.muslim.R

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @DrawableRes val iconRes: Int? = null
) {
    object Quran : Screen("quran", "Al-Qur'an", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, R.drawable.ic_nav_quran)
    object Qibla : Screen("qibla", "Kiblat", Icons.Filled.Explore, Icons.Outlined.Explore, R.drawable.ic_nav_qibla)
    object Dzikir : Screen("dzikir", "Dzikir", Icons.Filled.AutoStories, Icons.Outlined.AutoStories, R.drawable.ic_nav_doa)
    object AsmaulHusna : Screen("asmaul_husna", "99 Asma", Icons.Filled.FormatListNumbered, Icons.Outlined.FormatListNumbered, R.drawable.ic_nav_asmaulhusna)
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    quranViewModel: QuranViewModel,
    qiblaViewModel: QiblaViewModel,
    asmaulHusnaViewModel: AsmaulHusnaViewModel,
    dzikirViewModel: DzikirViewModel,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Screen.Quran,
        Screen.Qibla,
        Screen.Dzikir,
        Screen.AsmaulHusna
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation bar on AyahViewerScreen
    val showBottomBar = items.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = IslamicGreenPrimary
                ) {
                    items.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                if (screen.iconRes != null) {
                                    Image(
                                        painter = painterResource(id = screen.iconRes),
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = IslamicGreenPrimary,
                                selectedTextColor = IslamicGreenPrimary,
                                indicatorColor = IslamicGold.copy(alpha = 0.25f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Quran.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(Screen.Quran.route) {
                QuranScreen(
                    viewModel = quranViewModel,
                    onSurahClick = { surahId ->
                        navController.navigate("ayah_viewer/$surahId")
                    }
                )
            }

            composable(
                route = "ayah_viewer/{surahId}",
                arguments = listOf(navArgument("surahId") { type = NavType.IntType })
            ) { backStackEntry ->
                val surahId = backStackEntry.arguments?.getInt("surahId") ?: 1
                AyahViewerScreen(
                    surahId = surahId,
                    viewModel = quranViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Qibla.route) {
                QiblaScreen(viewModel = qiblaViewModel)
            }

            composable(Screen.Dzikir.route) {
                DzikirScreen(viewModel = dzikirViewModel)
            }

            composable(Screen.AsmaulHusna.route) {
                AsmaulHusnaScreen(viewModel = asmaulHusnaViewModel)
            }
        }
    }
}
