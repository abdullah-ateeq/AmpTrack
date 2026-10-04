package com.example.amptrack.data.model

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultPowerUnit: PowerUnit = PowerUnit.WATT,
    val defaultVoltageUnit: VoltageUnit = VoltageUnit.VOLT,
    val defaultPowerValue: String = "100",
    val defaultVoltageValue: String = "12",
    val currentDisplayMode: CurrentDisplayMode = CurrentDisplayMode.AUTO,
    val decimalPlaces: Int = 2,
    val hapticFeedback: Boolean = true,
    val showFormula: Boolean = true,
    val showQuickValues: Boolean = true
)
