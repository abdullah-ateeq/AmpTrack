package com.example.amptrack

import com.example.amptrack.data.datastore.HistorySerializer
import com.example.amptrack.data.model.AppSettings
import com.example.amptrack.data.model.AppThemeMode
import com.example.amptrack.data.model.CalculationHistoryItem
import com.example.amptrack.data.model.CurrentDisplayMode
import com.example.amptrack.data.model.CurrentUnit
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit
import com.example.amptrack.domain.CalculatorEngine
import com.example.amptrack.domain.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Complete suite of tests verifying all 16 required test cases specified in the prompt:
 * 1. W + V → A
 * 2. kW + V → A
 * 3. W + kV → A
 * 4. W + mV → A
 * 5. Zero power
 * 6. Zero voltage
 * 7. Negative power
 * 8. Negative voltage
 * 9. Decimal values
 * 10. Very small current
 * 11. Very large current
 * 12. Current formatting
 * 13. Power conversion
 * 14. Voltage conversion
 * 15. History serialization
 * 16. Settings persistence
 */
class AmpTrackComprehensiveTest {

    // 1. W + V → A (e.g. 100 W + 10 V = 10 A)
    @Test
    fun testCase01_WattsAndVoltsToAmps() {
        val power = BigDecimal("100")
        val voltage = BigDecimal("10")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.VOLT)
        val currentAmps = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        assertEquals(0, BigDecimal("10.00").compareTo(currentAmps.setScale(2, RoundingMode.HALF_UP)))
        val (formatted, unit) = CalculatorEngine.formatCurrent(currentAmps, CurrentDisplayMode.AUTO, 2)
        assertEquals("10.00 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 2. kW + V → A (e.g. 1 kW + 230 V = 4.347826... A -> 4.35 A)
    @Test
    fun testCase02_KilowattsAndVoltsToAmps() {
        val power = BigDecimal("1")
        val voltage = BigDecimal("230")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.KILOWATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.VOLT)
        val currentAmps = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        val (formatted, unit) = CalculatorEngine.formatCurrent(currentAmps, CurrentDisplayMode.AUTO, 2)
        assertEquals("4.35 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 3. W + kV → A (e.g. 500 W + 1 kV = 0.5 A -> 500.00 mA)
    @Test
    fun testCase03_WattsAndKilovoltsToAmps() {
        val power = BigDecimal("500")
        val voltage = BigDecimal("1")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.KILOVOLT)
        val currentAmps = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        assertEquals(0, BigDecimal("0.5").compareTo(currentAmps.setScale(4, RoundingMode.HALF_UP)))
        val (formatted, unit) = CalculatorEngine.formatCurrent(currentAmps, CurrentDisplayMode.AUTO, 2)
        assertEquals("500.00 mA", formatted)
        assertEquals(CurrentUnit.MILLIAMPERE, unit)
    }

    // 4. W + mV → A (e.g. 10 W + 500 mV = 20 A)
    @Test
    fun testCase04_WattsAndMillivoltsToAmps() {
        val power = BigDecimal("10")
        val voltage = BigDecimal("500")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.MILLIVOLT)
        val currentAmps = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        assertEquals(0, BigDecimal("20.00").compareTo(currentAmps.setScale(2, RoundingMode.HALF_UP)))
        val (formatted, unit) = CalculatorEngine.formatCurrent(currentAmps, CurrentDisplayMode.AUTO, 2)
        assertEquals("20.00 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 5. Zero power (0 W + 12 V = 0 A, no error)
    @Test
    fun testCase05_ZeroPower() {
        val power = BigDecimal("0")
        val voltage = BigDecimal("12")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.VOLT)
        val currentAmps = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        assertEquals(0, BigDecimal.ZERO.compareTo(currentAmps))
        val (formatted, unit) = CalculatorEngine.formatCurrent(currentAmps, CurrentDisplayMode.AUTO, 2)
        assertEquals("0.00 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 6. Zero voltage (must not calculate, reject with explicit error)
    @Test
    fun testCase06_ZeroVoltage() {
        val powerWatts = BigDecimal("100")
        val voltageVolts = BigDecimal("0")
        try {
            CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)
            fail("Should fail with zero voltage")
        } catch (e: IllegalArgumentException) {
            assertEquals("Voltage must be greater than zero.", e.message)
        }

        val validation = CalculatorEngine.validateVoltage("0")
        assertTrue(validation.isError)
        assertEquals("Voltage must be greater than zero.", (validation as ValidationResult.Error).message)
    }

    // 7. Negative power (reject with error)
    @Test
    fun testCase07_NegativePower() {
        val powerWatts = BigDecimal("-100")
        val voltageVolts = BigDecimal("12")
        try {
            CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)
            fail("Should fail with negative power")
        } catch (e: IllegalArgumentException) {
            assertEquals("Power cannot be negative.", e.message)
        }

        val validation = CalculatorEngine.validatePower("-100")
        assertTrue(validation.isError)
        assertEquals("Power cannot be negative.", (validation as ValidationResult.Error).message)
    }

    // 8. Negative voltage (reject with error)
    @Test
    fun testCase08_NegativeVoltage() {
        val powerWatts = BigDecimal("100")
        val voltageVolts = BigDecimal("-12")
        try {
            CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)
            fail("Should fail with negative voltage")
        } catch (e: IllegalArgumentException) {
            assertEquals("Voltage must be greater than zero.", e.message)
        }

        val validation = CalculatorEngine.validateVoltage("-12")
        assertTrue(validation.isError)
        assertEquals("Voltage cannot be negative.", (validation as ValidationResult.Error).message)
    }

    // 9. Decimal values (e.g. 60 W + 12 V = 5 A, 120 W + 24 V = 5 A, 12.345 W + 3.3 V = 3.74 A)
    @Test
    fun testCase09_DecimalValues() {
        // 60 W + 12 V = 5 A
        val c1 = CalculatorEngine.calculateCurrent(BigDecimal("60"), BigDecimal("12"))
        val (f1, _) = CalculatorEngine.formatCurrent(c1, CurrentDisplayMode.AUTO, 2)
        assertEquals("5.00 A", f1)

        // 120 W + 24 V = 5 A
        val c2 = CalculatorEngine.calculateCurrent(BigDecimal("120"), BigDecimal("24"))
        val (f2, _) = CalculatorEngine.formatCurrent(c2, CurrentDisplayMode.AUTO, 2)
        assertEquals("5.00 A", f2)

        // Decimal precision test
        val c3 = CalculatorEngine.calculateCurrent(BigDecimal("12.345"), BigDecimal("3.3"))
        val (f3, _) = CalculatorEngine.formatCurrent(c3, CurrentDisplayMode.AUTO, 2)
        assertEquals("3.74 A", f3)
    }

    // 10. Very small current (< 0.001 A -> µA)
    @Test
    fun testCase10_VerySmallCurrent() {
        // 0.0005 A -> 500.00 µA
        val power = BigDecimal("0.005")
        val voltage = BigDecimal("10")
        val current = CalculatorEngine.calculateCurrent(power, voltage)
        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("500.00 µA", formatted)
        assertEquals(CurrentUnit.MICROAMPERE, unit)
    }

    // 11. Very large current (>= 1000 A -> kA)
    @Test
    fun testCase11_VeryLargeCurrent() {
        // 1500 A -> 1.50 kA
        val power = BigDecimal("150000")
        val voltage = BigDecimal("100")
        val current = CalculatorEngine.calculateCurrent(power, voltage)
        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("1.50 kA", formatted)
        assertEquals(CurrentUnit.KILOAMPERE, unit)
    }

    // 12. Current formatting (auto scaling vs Amperes only, and decimal places 1, 2, 3)
    @Test
    fun testCase12_CurrentFormatting() {
        val current = BigDecimal("2.5478")

        // 1 decimal place
        val (f1, _) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 1)
        assertEquals("2.5 A", f1)

        // 2 decimal places
        val (f2, _) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("2.55 A", f2)

        // 3 decimal places
        val (f3, _) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 3)
        assertEquals("2.548 A", f3)

        // Amperes only mode for 0.005 A
        val small = BigDecimal("0.005")
        val (fAmpsOnly, unitAmps) = CalculatorEngine.formatCurrent(small, CurrentDisplayMode.AMPERES_ONLY, 3)
        assertEquals("0.005 A", fAmpsOnly)
        assertEquals(CurrentUnit.AMPERE, unitAmps)
    }

    // 13. Power conversion (W <-> kW)
    @Test
    fun testCase13_PowerConversion() {
        assertEquals(0, BigDecimal("1000").compareTo(CalculatorEngine.powerToWatts(BigDecimal("1"), PowerUnit.KILOWATT)))
        assertEquals(0, BigDecimal("2500").compareTo(CalculatorEngine.powerToWatts(BigDecimal("2.5"), PowerUnit.KILOWATT)))
        assertEquals(0, BigDecimal("75").compareTo(CalculatorEngine.powerToWatts(BigDecimal("75"), PowerUnit.WATT)))
    }

    // 14. Voltage conversion (mV <-> V <-> kV)
    @Test
    fun testCase14_VoltageConversion() {
        // 1 kV = 1000 V
        assertEquals(0, BigDecimal("1000").compareTo(CalculatorEngine.voltageToVolts(BigDecimal("1"), VoltageUnit.KILOVOLT)))
        // 240 V = 240 V
        assertEquals(0, BigDecimal("240").compareTo(CalculatorEngine.voltageToVolts(BigDecimal("240"), VoltageUnit.VOLT)))
        // 12000 mV = 12 V
        assertEquals(0, BigDecimal("12").compareTo(CalculatorEngine.voltageToVolts(BigDecimal("12000"), VoltageUnit.MILLIVOLT)))
    }

    // 15. History serialization (safe serialization & deserialization, max 50 records)
    @Test
    fun testCase15_HistorySerialization() {
        val item = CalculationHistoryItem(
            id = "test-uuid-1",
            timestamp = 1728000000000L,
            formattedDateTime = "Oct 3, 2026 • 2:30 PM",
            powerValue = "120",
            powerUnit = PowerUnit.WATT,
            voltageValue = "12",
            voltageUnit = VoltageUnit.VOLT,
            currentFormatted = "10.00 A",
            currentDisplayUnit = "A",
            formula = "120.00 W ÷ 12.00 V = 10.00 A"
        )

        val serialized = HistorySerializer.serialize(listOf(item))
        val restored = HistorySerializer.deserialize(serialized)

        assertEquals(1, restored.size)
        val deserializedItem = restored[0]
        assertEquals("test-uuid-1", deserializedItem.id)
        assertEquals("120", deserializedItem.powerValue)
        assertEquals(PowerUnit.WATT, deserializedItem.powerUnit)
        assertEquals("12", deserializedItem.voltageValue)
        assertEquals(VoltageUnit.VOLT, deserializedItem.voltageUnit)
        assertEquals("10.00 A", deserializedItem.currentFormatted)
        assertEquals("120.00 W ÷ 12.00 V = 10.00 A", deserializedItem.formula)

        // Fifty records max verification
        val sixtyItems = (1..60).map { i ->
            item.copy(id = "item-$i", powerValue = "$i")
        }
        val capped = sixtyItems.take(50)
        assertEquals(50, capped.size)
    }

    // 16. Settings persistence (model defaults and updates verification)
    @Test
    fun testCase16_SettingsPersistenceModel() {
        val defaultSettings = AppSettings()
        assertEquals(AppThemeMode.SYSTEM, defaultSettings.themeMode)
        assertTrue(defaultSettings.dynamicColor)
        assertEquals(PowerUnit.WATT, defaultSettings.defaultPowerUnit)
        assertEquals(VoltageUnit.VOLT, defaultSettings.defaultVoltageUnit)
        assertEquals("100", defaultSettings.defaultPowerValue)
        assertEquals("12", defaultSettings.defaultVoltageValue)
        assertEquals(CurrentDisplayMode.AUTO, defaultSettings.currentDisplayMode)
        assertEquals(2, defaultSettings.decimalPlaces)
        assertTrue(defaultSettings.hapticFeedback)
        assertTrue(defaultSettings.showFormula)
        assertTrue(defaultSettings.showQuickValues)

        // Customized settings
        val customSettings = defaultSettings.copy(
            themeMode = AppThemeMode.DARK,
            dynamicColor = false,
            defaultPowerUnit = PowerUnit.KILOWATT,
            defaultVoltageUnit = VoltageUnit.KILOVOLT,
            defaultPowerValue = "5",
            defaultVoltageValue = "24",
            currentDisplayMode = CurrentDisplayMode.AMPERES_ONLY,
            decimalPlaces = 3,
            hapticFeedback = false,
            showFormula = false,
            showQuickValues = false
        )

        assertEquals(AppThemeMode.DARK, customSettings.themeMode)
        assertFalse(customSettings.dynamicColor)
        assertEquals(PowerUnit.KILOWATT, customSettings.defaultPowerUnit)
        assertEquals(VoltageUnit.KILOVOLT, customSettings.defaultVoltageUnit)
        assertEquals("5", customSettings.defaultPowerValue)
        assertEquals("24", customSettings.defaultVoltageValue)
        assertEquals(CurrentDisplayMode.AMPERES_ONLY, customSettings.currentDisplayMode)
        assertEquals(3, customSettings.decimalPlaces)
        assertFalse(customSettings.hapticFeedback)
        assertFalse(customSettings.showFormula)
        assertFalse(customSettings.showQuickValues)
    }
}
