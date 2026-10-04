package com.example.amptrack

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.amptrack.data.datastore.AmpTrackPreferences
import com.example.amptrack.data.model.AppSettings
import com.example.amptrack.data.model.AppThemeMode
import com.example.amptrack.data.model.CalculationHistoryItem
import com.example.amptrack.data.model.CurrentDisplayMode
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsPersistenceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var file: File
    private val scopes = mutableListOf<CoroutineScope>()

    @Before
    fun setUp() {
        file = File(tempFolder.root, "test.preferences_pb")
    }

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
    }

    /** Each call simulates a fresh app process reading the same file. */
    private fun openPreferences(): AmpTrackPreferences {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scopes += scope
        return AmpTrackPreferences(PreferenceDataStoreFactory.create(scope = scope) { file })
    }

    private fun reopen(): AmpTrackPreferences {
        scopes.forEach { it.cancel() }
        scopes.clear()
        // DataStore releases the file asynchronously after its scope is cancelled.
        Thread.sleep(50)
        return openPreferences()
    }

    @Test
    fun defaultsWhenEmpty() = runBlocking {
        assertEquals(AppSettings(), openPreferences().settingsFlow.first())
    }

    @Test
    fun settingsSurviveRestart() = runBlocking {
        val prefs = openPreferences()
        prefs.updateThemeMode(AppThemeMode.DARK)
        prefs.updateDynamicColor(false)
        prefs.updateDefaultPowerUnit(PowerUnit.KILOWATT)
        prefs.updateDefaultVoltageUnit(VoltageUnit.MILLIVOLT)
        prefs.updateDefaultPowerValue("1.5")
        prefs.updateDefaultVoltageValue("230")
        prefs.updateCurrentDisplayMode(CurrentDisplayMode.AMPERES_ONLY)
        prefs.updateDecimalPlaces(3)
        prefs.updateHapticFeedback(false)
        prefs.updateShowFormula(false)
        prefs.updateShowQuickValues(false)

        val restored = reopen().settingsFlow.first()
        assertEquals(
            AppSettings(
                themeMode = AppThemeMode.DARK,
                dynamicColor = false,
                defaultPowerUnit = PowerUnit.KILOWATT,
                defaultVoltageUnit = VoltageUnit.MILLIVOLT,
                defaultPowerValue = "1.5",
                defaultVoltageValue = "230",
                currentDisplayMode = CurrentDisplayMode.AMPERES_ONLY,
                decimalPlaces = 3,
                hapticFeedback = false,
                showFormula = false,
                showQuickValues = false
            ),
            restored
        )
    }

    @Test
    fun decimalPlacesAreClamped() = runBlocking {
        val prefs = openPreferences()
        prefs.updateDecimalPlaces(9)
        assertEquals(3, prefs.settingsFlow.first().decimalPlaces)
    }

    @Test
    fun resetSettingsKeepsHistory() = runBlocking {
        val prefs = openPreferences()
        prefs.updateThemeMode(AppThemeMode.LIGHT)
        prefs.saveCalculation(historyItem("a", 1L))

        prefs.resetSettings()

        assertEquals(AppSettings(), prefs.settingsFlow.first())
        assertEquals(listOf("a"), prefs.historyFlow.first().map { it.id })
    }

    @Test
    fun historySurvivesRestartAndCapsAtFifty() = runBlocking {
        val prefs = openPreferences()
        repeat(51) { i -> prefs.saveCalculation(historyItem("id$i", i.toLong())) }

        val history = reopen().historyFlow.first()
        assertEquals(AmpTrackPreferences.MAX_HISTORY_ITEMS, history.size)
        assertEquals("id50", history.first().id)
        assertFalse(history.any { it.id == "id0" })
    }

    @Test
    fun deleteAndClearHistory() = runBlocking {
        val prefs = openPreferences()
        prefs.saveCalculation(historyItem("a", 1L))
        prefs.saveCalculation(historyItem("b", 2L))

        prefs.deleteHistoryItem("a")
        assertEquals(listOf("b"), prefs.historyFlow.first().map { it.id })

        prefs.clearHistory()
        assertEquals(emptyList<CalculationHistoryItem>(), prefs.historyFlow.first())
    }

    private fun historyItem(id: String, timestamp: Long) = CalculationHistoryItem(
        id = id,
        timestamp = timestamp,
        formattedDateTime = "Oct 4, 2026 • 9:00 AM",
        powerValue = "1",
        powerUnit = PowerUnit.KILOWATT,
        voltageValue = "230",
        voltageUnit = VoltageUnit.VOLT,
        currentFormatted = "4.35 A",
        currentDisplayUnit = "A",
        formula = "1.00 kW ÷ 230.00 V = 4.35 A"
    )
}
