package com.example.amptrack.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Calculator : Screen("calculator")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = Screen.Calculator.route,
        title = "Calculator",
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate
    ),
    BottomNavItem(
        route = Screen.History.route,
        title = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    ),
    BottomNavItem(
        route = Screen.Settings.route,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    ),
    BottomNavItem(
        route = Screen.About.route,
        title = "About",
        selectedIcon = Icons.Filled.Info,
        unselectedIcon = Icons.Outlined.Info
    )
)
