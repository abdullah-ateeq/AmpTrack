package com.example.amptrack.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.amptrack.ui.screens.about.AboutScreen
import com.example.amptrack.ui.screens.calculator.CalculatorScreen
import com.example.amptrack.ui.screens.history.HistoryScreen
import com.example.amptrack.ui.screens.settings.SettingsScreen
import com.example.amptrack.ui.screens.splash.SplashScreen
import com.example.amptrack.ui.viewmodel.AmpTrackViewModel

@Composable
fun AmpTrackApp(
    viewModel: AmpTrackViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        val msg = uiState.userMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    val isSplash = currentRoute == Screen.Splash.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!isSplash) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Splash is popped, so Calculator is the effective root.
                                        popUpTo(Screen.Calculator.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.semantics {
                                contentDescription = "${item.title} tab"
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Calculator.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Calculator.route) {
                CalculatorScreen(
                    uiState = uiState,
                    onPowerChanged = viewModel::onPowerChanged,
                    onPowerUnitChanged = viewModel::onPowerUnitChanged,
                    onVoltageChanged = viewModel::onVoltageChanged,
                    onVoltageUnitChanged = viewModel::onVoltageUnitChanged,
                    onQuickPowerSelected = viewModel::onQuickPowerSelected,
                    onQuickVoltageSelected = viewModel::onQuickVoltageSelected,
                    onCalculate = viewModel::onCalculateExplicit,
                    onReset = viewModel::onResetInputs,
                    onSave = viewModel::onSaveCalculation,
                    snackbarHostState = snackbarHostState
                )
            }

            composable(Screen.History.route) {
                HistoryScreen(
                    historyList = uiState.historyList,
                    onSelectCalculation = { item ->
                        viewModel.onRestoreCalculation(item)
                        navController.navigate(Screen.Calculator.route) {
                            popUpTo(Screen.Calculator.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onDeleteItem = viewModel::onDeleteHistoryItem,
                    onClearAll = viewModel::onClearAllHistory,
                    hapticFeedbackEnabled = uiState.settings.hapticFeedback
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = uiState.settings,
                    onThemeModeChange = viewModel::updateThemeMode,
                    onDynamicColorChange = viewModel::updateDynamicColor,
                    onDefaultPowerUnitChange = viewModel::updateDefaultPowerUnit,
                    onDefaultVoltageUnitChange = viewModel::updateDefaultVoltageUnit,
                    onDefaultPowerValueChange = viewModel::updateDefaultPowerValue,
                    onDefaultVoltageValueChange = viewModel::updateDefaultVoltageValue,
                    onCurrentDisplayModeChange = viewModel::updateCurrentDisplayMode,
                    onDecimalPlacesChange = viewModel::updateDecimalPlaces,
                    onHapticFeedbackChange = viewModel::updateHapticFeedback,
                    onShowFormulaChange = viewModel::updateShowFormula,
                    onShowQuickValuesChange = viewModel::updateShowQuickValues,
                    onResetSettings = viewModel::resetSettings,
                    onClearHistory = viewModel::onClearAllHistory
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
