package com.example.amptrack.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.amptrack.data.datastore.AmpTrackPreferences
import com.example.amptrack.data.model.AppSettings
import com.example.amptrack.data.model.AppThemeMode
import com.example.amptrack.data.model.CalculationHistoryItem
import com.example.amptrack.data.model.CurrentDisplayMode
import com.example.amptrack.data.model.CurrentUnit
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit
import com.example.amptrack.domain.CalculatorEngine
import com.example.amptrack.domain.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AmpTrackViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = AmpTrackPreferences(application)

    private val _uiState = MutableStateFlow(AmpTrackUiState())
    val uiState: StateFlow<AmpTrackUiState> = _uiState.asStateFlow()

    private var initialLoaded = false

    init {
        // Collect Settings
        viewModelScope.launch {
            preferences.settingsFlow.collectLatest { settings ->
                _uiState.update { current ->
                    val updatedState = if (!initialLoaded) {
                        initialLoaded = true
                        current.copy(
                            powerInput = settings.defaultPowerValue,
                            powerUnit = settings.defaultPowerUnit,
                            voltageInput = settings.defaultVoltageValue,
                            voltageUnit = settings.defaultVoltageUnit,
                            settings = settings
                        )
                    } else {
                        current.copy(settings = settings)
                    }
                    computeCalculation(updatedState)
                }
            }
        }

        // Collect History
        viewModelScope.launch {
            preferences.historyFlow.collectLatest { history ->
                _uiState.update { it.copy(historyList = history) }
            }
        }
    }

    fun onPowerChanged(newPower: String) {
        _uiState.update { current ->
            val updated = current.copy(powerInput = newPower)
            computeCalculation(updated)
        }
    }

    fun onPowerUnitChanged(unit: PowerUnit) {
        _uiState.update { current ->
            val updated = current.copy(powerUnit = unit)
            computeCalculation(updated)
        }
    }

    fun onVoltageChanged(newVoltage: String) {
        _uiState.update { current ->
            val updated = current.copy(voltageInput = newVoltage)
            computeCalculation(updated)
        }
    }

    fun onVoltageUnitChanged(unit: VoltageUnit) {
        _uiState.update { current ->
            val updated = current.copy(voltageUnit = unit)
            computeCalculation(updated)
        }
    }

    fun onQuickPowerSelected(powerWattsValue: String) {
        _uiState.update { current ->
            val updated = current.copy(
                powerInput = powerWattsValue,
                powerUnit = PowerUnit.WATT
            )
            computeCalculation(updated)
        }
    }

    fun onQuickVoltageSelected(voltageVoltsValue: String) {
        _uiState.update { current ->
            val updated = current.copy(
                voltageInput = voltageVoltsValue,
                voltageUnit = VoltageUnit.VOLT
            )
            computeCalculation(updated)
        }
    }

    fun onCalculateExplicit() {
        _uiState.update { computeCalculation(it) }
    }

    fun onResetInputs() {
        val settings = _uiState.value.settings
        _uiState.update { current ->
            val resetState = current.copy(
                powerInput = settings.defaultPowerValue,
                powerUnit = settings.defaultPowerUnit,
                voltageInput = settings.defaultVoltageValue,
                voltageUnit = settings.defaultVoltageUnit,
                userMessage = "Calculator reset"
            )
            computeCalculation(resetState)
        }
    }

    fun onSaveCalculation() {
        val state = _uiState.value
        if (!state.isCalculationValid || state.calculatedCurrentFormatted == null) {
            _uiState.update { it.copy(userMessage = "Cannot save invalid calculation") }
            return
        }

        val powerValidation = CalculatorEngine.validatePower(state.powerInput)
        val voltageValidation = CalculatorEngine.validateVoltage(state.voltageInput)

        if (powerValidation !is ValidationResult.Success || voltageValidation !is ValidationResult.Success) {
            _uiState.update { it.copy(userMessage = "Cannot save invalid calculation") }
            return
        }

        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(now))

        val historyItem = CalculationHistoryItem(
            id = UUID.randomUUID().toString(),
            timestamp = now,
            formattedDateTime = formattedDate,
            powerValue = state.powerInput,
            powerUnit = state.powerUnit,
            voltageValue = state.voltageInput,
            voltageUnit = state.voltageUnit,
            currentFormatted = state.calculatedCurrentFormatted,
            currentDisplayUnit = state.currentUnit.symbol,
            formula = state.formulaString ?: "I = P ÷ V"
        )

        viewModelScope.launch {
            preferences.saveCalculation(historyItem)
            _uiState.update { it.copy(userMessage = "Calculation saved to history") }
        }
    }

    fun onRestoreCalculation(item: CalculationHistoryItem) {
        _uiState.update { current ->
            val restored = current.copy(
                powerInput = item.powerValue,
                powerUnit = item.powerUnit,
                voltageInput = item.voltageValue,
                voltageUnit = item.voltageUnit,
                userMessage = "Restored calculation from history"
            )
            computeCalculation(restored)
        }
    }

    fun onDeleteHistoryItem(id: String) {
        viewModelScope.launch {
            preferences.deleteHistoryItem(id)
            _uiState.update { it.copy(userMessage = "Record deleted") }
        }
    }

    fun onClearAllHistory() {
        viewModelScope.launch {
            preferences.clearHistory()
            _uiState.update { it.copy(userMessage = "History cleared") }
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferences.updateThemeMode(mode)
        }
    }

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateDynamicColor(enabled)
        }
    }

    fun updateDefaultPowerUnit(unit: PowerUnit) {
        viewModelScope.launch {
            preferences.updateDefaultPowerUnit(unit)
        }
    }

    fun updateDefaultVoltageUnit(unit: VoltageUnit) {
        viewModelScope.launch {
            preferences.updateDefaultVoltageUnit(unit)
        }
    }

    /** Persists the default power only when valid; returns the validation error otherwise. */
    fun updateDefaultPowerValue(value: String): String? {
        val validation = CalculatorEngine.validatePower(value)
        if (validation is ValidationResult.Success) {
            viewModelScope.launch {
                preferences.updateDefaultPowerValue(value.trim())
            }
        }
        return validation.errorMessageOrNull()
    }

    /** Persists the default voltage only when valid; returns the validation error otherwise. */
    fun updateDefaultVoltageValue(value: String): String? {
        val validation = CalculatorEngine.validateVoltage(value)
        if (validation is ValidationResult.Success) {
            viewModelScope.launch {
                preferences.updateDefaultVoltageValue(value.trim())
            }
        }
        return validation.errorMessageOrNull()
    }

    fun updateCurrentDisplayMode(mode: CurrentDisplayMode) {
        viewModelScope.launch {
            preferences.updateCurrentDisplayMode(mode)
        }
    }

    fun updateDecimalPlaces(places: Int) {
        viewModelScope.launch {
            preferences.updateDecimalPlaces(places)
        }
    }

    fun updateHapticFeedback(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateHapticFeedback(enabled)
        }
    }

    fun updateShowFormula(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateShowFormula(enabled)
        }
    }

    fun updateShowQuickValues(enabled: Boolean) {
        viewModelScope.launch {
            preferences.updateShowQuickValues(enabled)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            preferences.resetSettings()
            _uiState.update { it.copy(userMessage = "Settings reset to defaults") }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun computeCalculation(state: AmpTrackUiState): AmpTrackUiState {
        val powerValidation = CalculatorEngine.validatePower(state.powerInput)
        val voltageValidation = CalculatorEngine.validateVoltage(state.voltageInput)

        val powerError = (powerValidation as? ValidationResult.Error)?.message
        val voltageError = (voltageValidation as? ValidationResult.Error)?.message

        if (powerValidation !is ValidationResult.Success || voltageValidation !is ValidationResult.Success) {
            return state.copy(
                powerError = powerError,
                voltageError = voltageError,
                calculatedCurrentFormatted = null,
                powerWattsFormatted = null,
                voltageVoltsFormatted = null,
                formulaString = null,
                summaryPower = null,
                summaryVoltage = null,
                summaryCurrent = null,
                isCalculationValid = false
            )
        }

        val powerRaw = powerValidation.value
        val voltageRaw = voltageValidation.value

        return try {
            val powerWatts = CalculatorEngine.powerToWatts(powerRaw, state.powerUnit)
            val voltageVolts = CalculatorEngine.voltageToVolts(voltageRaw, state.voltageUnit)
            val currentAmps = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

            val decimalPlaces = state.settings.decimalPlaces
            val (formattedCurrent, unit) = CalculatorEngine.formatCurrent(
                currentAmps = currentAmps,
                displayMode = state.settings.currentDisplayMode,
                decimalPlaces = decimalPlaces
            )

            val powerWattsFormatted = CalculatorEngine.formatValueWithUnit(powerWatts, "W", decimalPlaces)
            val voltageVoltsFormatted = CalculatorEngine.formatValueWithUnit(voltageVolts, "V", decimalPlaces)

            val formulaString = CalculatorEngine.generateFormulaString(
                powerValue = powerRaw,
                powerUnit = state.powerUnit,
                voltageValue = voltageRaw,
                voltageUnit = state.voltageUnit,
                formattedCurrent = formattedCurrent,
                decimalPlaces = decimalPlaces
            )

            val summaryPower = "${CalculatorEngine.formatDecimal(powerRaw, decimalPlaces)} ${state.powerUnit.symbol}"
            val summaryVoltage = "${CalculatorEngine.formatDecimal(voltageRaw, decimalPlaces)} ${state.voltageUnit.symbol}"

            state.copy(
                powerError = null,
                voltageError = null,
                calculatedCurrentFormatted = formattedCurrent,
                currentUnit = unit,
                powerWattsFormatted = powerWattsFormatted,
                voltageVoltsFormatted = voltageVoltsFormatted,
                formulaString = formulaString,
                summaryPower = summaryPower,
                summaryVoltage = summaryVoltage,
                summaryCurrent = formattedCurrent,
                isCalculationValid = true
            )
        } catch (e: Exception) {
            state.copy(
                powerError = null,
                voltageError = e.message ?: "Calculation error",
                calculatedCurrentFormatted = null,
                powerWattsFormatted = null,
                voltageVoltsFormatted = null,
                formulaString = null,
                summaryPower = null,
                summaryVoltage = null,
                summaryCurrent = null,
                isCalculationValid = false
            )
        }
    }
}
