package com.example.amptrack.ui.viewmodel

import com.example.amptrack.data.model.AppSettings
import com.example.amptrack.data.model.CalculationHistoryItem
import com.example.amptrack.data.model.CurrentUnit
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit

data class AmpTrackUiState(
    val powerInput: String = "100",
    val powerUnit: PowerUnit = PowerUnit.WATT,
    val voltageInput: String = "12",
    val voltageUnit: VoltageUnit = VoltageUnit.VOLT,
    val calculatedCurrentFormatted: String? = "8.33 A",
    val currentUnit: CurrentUnit = CurrentUnit.AMPERE,
    val powerWattsFormatted: String? = "100.00 W",
    val voltageVoltsFormatted: String? = "12.00 V",
    val formulaString: String? = "100.00 W ÷ 12.00 V = 8.33 A",
    val summaryPower: String? = "100 W",
    val summaryVoltage: String? = "12 V",
    val summaryCurrent: String? = "8.33 A",
    val powerError: String? = null,
    val voltageError: String? = null,
    val isCalculationValid: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val historyList: List<CalculationHistoryItem> = emptyList(),
    val userMessage: String? = null
)
