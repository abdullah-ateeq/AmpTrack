package com.example.amptrack.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.amptrack.data.model.AppSettings
import com.example.amptrack.data.model.AppThemeMode
import com.example.amptrack.data.model.CalculationHistoryItem
import com.example.amptrack.data.model.CurrentDisplayMode
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "amptrack_settings")

class AmpTrackPreferences(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.dataStore)

    companion object {
        const val MAX_HISTORY_ITEMS = 50

        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_DEFAULT_POWER_UNIT = stringPreferencesKey("default_power_unit")
        private val KEY_DEFAULT_VOLTAGE_UNIT = stringPreferencesKey("default_voltage_unit")
        private val KEY_DEFAULT_POWER_VALUE = stringPreferencesKey("default_power_value")
        private val KEY_DEFAULT_VOLTAGE_VALUE = stringPreferencesKey("default_voltage_value")
        private val KEY_CURRENT_DISPLAY_MODE = stringPreferencesKey("current_display_mode")
        private val KEY_DECIMAL_PLACES = intPreferencesKey("decimal_places")
        private val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        private val KEY_SHOW_FORMULA = booleanPreferencesKey("show_formula")
        private val KEY_SHOW_QUICK_VALUES = booleanPreferencesKey("show_quick_values")
        private val KEY_HISTORY_DATA = stringPreferencesKey("history_data")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val themeModeStr = prefs[KEY_THEME_MODE] ?: AppThemeMode.SYSTEM.name
            val themeMode = try { AppThemeMode.valueOf(themeModeStr) } catch (e: Exception) { AppThemeMode.SYSTEM }

            val dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true

            val powerUnitStr = prefs[KEY_DEFAULT_POWER_UNIT] ?: PowerUnit.WATT.name
            val defaultPowerUnit = try { PowerUnit.valueOf(powerUnitStr) } catch (e: Exception) { PowerUnit.WATT }

            val voltageUnitStr = prefs[KEY_DEFAULT_VOLTAGE_UNIT] ?: VoltageUnit.VOLT.name
            val defaultVoltageUnit = try { VoltageUnit.valueOf(voltageUnitStr) } catch (e: Exception) { VoltageUnit.VOLT }

            val defaultPowerValue = prefs[KEY_DEFAULT_POWER_VALUE] ?: "100"
            val defaultVoltageValue = prefs[KEY_DEFAULT_VOLTAGE_VALUE] ?: "12"

            val displayModeStr = prefs[KEY_CURRENT_DISPLAY_MODE] ?: CurrentDisplayMode.AUTO.name
            val currentDisplayMode = try { CurrentDisplayMode.valueOf(displayModeStr) } catch (e: Exception) { CurrentDisplayMode.AUTO }

            val decimalPlaces = (prefs[KEY_DECIMAL_PLACES] ?: 2).coerceIn(1, 3)
            val hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true
            val showFormula = prefs[KEY_SHOW_FORMULA] ?: true
            val showQuickValues = prefs[KEY_SHOW_QUICK_VALUES] ?: true

            AppSettings(
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                defaultPowerUnit = defaultPowerUnit,
                defaultVoltageUnit = defaultVoltageUnit,
                defaultPowerValue = defaultPowerValue,
                defaultVoltageValue = defaultVoltageValue,
                currentDisplayMode = currentDisplayMode,
                decimalPlaces = decimalPlaces,
                hapticFeedback = hapticFeedback,
                showFormula = showFormula,
                showQuickValues = showQuickValues
            )
        }

    val historyFlow: Flow<List<CalculationHistoryItem>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val raw = prefs[KEY_HISTORY_DATA]
            HistorySerializer.deserialize(raw)
        }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        dataStore.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    suspend fun updateDefaultPowerUnit(unit: PowerUnit) {
        dataStore.edit { it[KEY_DEFAULT_POWER_UNIT] = unit.name }
    }

    suspend fun updateDefaultVoltageUnit(unit: VoltageUnit) {
        dataStore.edit { it[KEY_DEFAULT_VOLTAGE_UNIT] = unit.name }
    }

    suspend fun updateDefaultPowerValue(value: String) {
        dataStore.edit { it[KEY_DEFAULT_POWER_VALUE] = value }
    }

    suspend fun updateDefaultVoltageValue(value: String) {
        dataStore.edit { it[KEY_DEFAULT_VOLTAGE_VALUE] = value }
    }

    suspend fun updateCurrentDisplayMode(mode: CurrentDisplayMode) {
        dataStore.edit { it[KEY_CURRENT_DISPLAY_MODE] = mode.name }
    }

    suspend fun updateDecimalPlaces(places: Int) {
        val safe = places.coerceIn(1, 3)
        dataStore.edit { it[KEY_DECIMAL_PLACES] = safe }
    }

    suspend fun updateHapticFeedback(enabled: Boolean) {
        dataStore.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun updateShowFormula(enabled: Boolean) {
        dataStore.edit { it[KEY_SHOW_FORMULA] = enabled }
    }

    suspend fun updateShowQuickValues(enabled: Boolean) {
        dataStore.edit { it[KEY_SHOW_QUICK_VALUES] = enabled }
    }

    suspend fun resetSettings() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_THEME_MODE)
            prefs.remove(KEY_DYNAMIC_COLOR)
            prefs.remove(KEY_DEFAULT_POWER_UNIT)
            prefs.remove(KEY_DEFAULT_VOLTAGE_UNIT)
            prefs.remove(KEY_DEFAULT_POWER_VALUE)
            prefs.remove(KEY_DEFAULT_VOLTAGE_VALUE)
            prefs.remove(KEY_CURRENT_DISPLAY_MODE)
            prefs.remove(KEY_DECIMAL_PLACES)
            prefs.remove(KEY_HAPTIC_FEEDBACK)
            prefs.remove(KEY_SHOW_FORMULA)
            prefs.remove(KEY_SHOW_QUICK_VALUES)
        }
    }

    suspend fun saveCalculation(item: CalculationHistoryItem) {
        dataStore.edit { prefs ->
            val existing = HistorySerializer.deserialize(prefs[KEY_HISTORY_DATA])
            // Prepend new item (newest first)
            val updated = mutableListOf(item).apply {
                addAll(existing.filter { it.id != item.id })
            }
            // Enforce max 50 records - remove oldest if size > 50
            val capped = if (updated.size > MAX_HISTORY_ITEMS) {
                updated.take(MAX_HISTORY_ITEMS)
            } else {
                updated
            }
            prefs[KEY_HISTORY_DATA] = HistorySerializer.serialize(capped)
        }
    }

    suspend fun deleteHistoryItem(id: String) {
        dataStore.edit { prefs ->
            val existing = HistorySerializer.deserialize(prefs[KEY_HISTORY_DATA])
            val updated = existing.filter { it.id != id }
            prefs[KEY_HISTORY_DATA] = HistorySerializer.serialize(updated)
        }
    }

    suspend fun clearHistory() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_HISTORY_DATA)
        }
    }
}
