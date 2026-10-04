package com.example.amptrack

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

class CalculatorEngineTest {

    // 1. W + V -> A
    @Test
    fun testWattsAndVoltsToAmps() {
        val power = BigDecimal("100")
        val voltage = BigDecimal("10")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.VOLT)
        val current = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)
        assertEquals(0, BigDecimal("10").compareTo(current.setScale(2, RoundingMode.HALF_UP)))

        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("10.00 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 2. kW + V -> A
    @Test
    fun testKilowattsAndVoltsToAmps() {
        // 1 kW + 230 V: 1000 / 230 = 4.347826... -> 4.35 A
        val power = BigDecimal("1")
        val voltage = BigDecimal("230")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.KILOWATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.VOLT)
        val current = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("4.35 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 3. W + kV -> A
    @Test
    fun testWattsAndKilovoltsToAmps() {
        // 500 W + 1 kV (1000 V) = 0.5 A -> 500.00 mA
        val power = BigDecimal("500")
        val voltage = BigDecimal("1")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.KILOVOLT)
        val current = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        assertEquals(0, BigDecimal("0.5").compareTo(current.setScale(4, RoundingMode.HALF_UP)))
        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("500.00 mA", formatted)
        assertEquals(CurrentUnit.MILLIAMPERE, unit)
    }

    // 4. W + mV -> A
    @Test
    fun testWattsAndMillivoltsToAmps() {
        // 10 W + 500 mV (0.5 V) = 20 A
        val power = BigDecimal("10")
        val voltage = BigDecimal("500")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.MILLIVOLT)
        val current = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("20.00 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 5. Zero power
    @Test
    fun testZeroPower() {
        // 0 W + 12 V = 0 A
        val power = BigDecimal("0")
        val voltage = BigDecimal("12")
        val powerWatts = CalculatorEngine.powerToWatts(power, PowerUnit.WATT)
        val voltageVolts = CalculatorEngine.voltageToVolts(voltage, VoltageUnit.VOLT)
        val current = CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)

        assertEquals(0, BigDecimal.ZERO.compareTo(current))
        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("0.00 A", formatted)
        assertEquals(CurrentUnit.AMPERE, unit)
    }

    // 6. Zero voltage
    @Test
    fun testZeroVoltageRejection() {
        val powerWatts = BigDecimal("100")
        val voltageVolts = BigDecimal("0")
        try {
            CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)
            fail("Expected IllegalArgumentException for zero voltage")
        } catch (e: IllegalArgumentException) {
            assertEquals("Voltage must be greater than zero.", e.message)
        }

        // Test validation function
        val validation = CalculatorEngine.validateVoltage("0")
        assertTrue(validation.isError)
        assertEquals("Voltage must be greater than zero.", (validation as ValidationResult.Error).message)
    }

    // 7. Negative power
    @Test
    fun testNegativePowerRejection() {
        val powerWatts = BigDecimal("-50")
        val voltageVolts = BigDecimal("12")
        try {
            CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)
            fail("Expected IllegalArgumentException for negative power")
        } catch (e: IllegalArgumentException) {
            assertEquals("Power cannot be negative.", e.message)
        }

        val validation = CalculatorEngine.validatePower("-50")
        assertTrue(validation.isError)
        assertEquals("Power cannot be negative.", (validation as ValidationResult.Error).message)
    }

    // 8. Negative voltage
    @Test
    fun testNegativeVoltageRejection() {
        val powerWatts = BigDecimal("100")
        val voltageVolts = BigDecimal("-12")
        try {
            CalculatorEngine.calculateCurrent(powerWatts, voltageVolts)
            fail("Expected IllegalArgumentException for negative voltage")
        } catch (e: IllegalArgumentException) {
            assertEquals("Voltage must be greater than zero.", e.message)
        }

        val validation = CalculatorEngine.validateVoltage("-12")
        assertTrue(validation.isError)
        assertEquals("Voltage cannot be negative.", (validation as ValidationResult.Error).message)
    }

    // 9. Decimal values
    @Test
    fun testDecimalValues() {
        // 60 W + 12 V = 5 A
        val current1 = CalculatorEngine.calculateCurrent(BigDecimal("60"), BigDecimal("12"))
        val (formatted1, _) = CalculatorEngine.formatCurrent(current1, CurrentDisplayMode.AUTO, 2)
        assertEquals("5.00 A", formatted1)

        // 120 W + 24 V = 5 A
        val current2 = CalculatorEngine.calculateCurrent(BigDecimal("120"), BigDecimal("24"))
        val (formatted2, _) = CalculatorEngine.formatCurrent(current2, CurrentDisplayMode.AUTO, 2)
        assertEquals("5.00 A", formatted2)

        // 120 W + 12 V = 10 A
        val current3 = CalculatorEngine.calculateCurrent(BigDecimal("120"), BigDecimal("12"))
        val (formatted3, _) = CalculatorEngine.formatCurrent(current3, CurrentDisplayMode.AUTO, 2)
        assertEquals("10.00 A", formatted3)

        // Precision with 12.345 W + 3.3 V = 3.7409... -> 3.74 A
        val current4 = CalculatorEngine.calculateCurrent(BigDecimal("12.345"), BigDecimal("3.3"))
        val (formatted4, _) = CalculatorEngine.formatCurrent(current4, CurrentDisplayMode.AUTO, 2)
        assertEquals("3.74 A", formatted4)
    }

    // 10. Very small current (< 0.001 A -> µA)
    @Test
    fun testVerySmallCurrentMicroamps() {
        // 0.0005 A -> 500 µA (e.g. 0.005 W / 10 V = 0.0005 A)
        val power = BigDecimal("0.005")
        val voltage = BigDecimal("10")
        val current = CalculatorEngine.calculateCurrent(power, voltage)

        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("500.00 µA", formatted)
        assertEquals(CurrentUnit.MICROAMPERE, unit)
    }

    // 11. Very large current (>= 1000 A -> kA)
    @Test
    fun testVeryLargeCurrentKiloamps() {
        // 1500 A -> 1.50 kA (e.g. 150,000 W / 100 V = 1500 A)
        val power = BigDecimal("150000")
        val voltage = BigDecimal("100")
        val current = CalculatorEngine.calculateCurrent(power, voltage)

        val (formatted, unit) = CalculatorEngine.formatCurrent(current, CurrentDisplayMode.AUTO, 2)
        assertEquals("1.50 kA", formatted)
        assertEquals(CurrentUnit.KILOAMPERE, unit)
    }

    // 12. Current formatting (milliamps and Amperes-only mode)
    @Test
    fun testCurrentFormattingModes() {
        // 0.005 A -> 5.00 mA in AUTO
        val smallAmps = BigDecimal("0.005")
        val (autoFormatted, unitAuto) = CalculatorEngine.formatCurrent(smallAmps, CurrentDisplayMode.AUTO, 2)
        assertEquals("5.00 mA", autoFormatted)
        assertEquals(CurrentUnit.MILLIAMPERE, unitAuto)

        // In AMPERES_ONLY mode -> 0.01 A (or 0.005 with 3 decimals: 0.005 A)
        val (ampsOnly, unitAmps) = CalculatorEngine.formatCurrent(smallAmps, CurrentDisplayMode.AMPERES_ONLY, 3)
        assertEquals("0.005 A", ampsOnly)
        assertEquals(CurrentUnit.AMPERE, unitAmps)

        // 2.5 A -> 2.50 A in AUTO
        val normalAmps = BigDecimal("2.5")
        val (normalFormatted, _) = CalculatorEngine.formatCurrent(normalAmps, CurrentDisplayMode.AUTO, 2)
        assertEquals("2.50 A", normalFormatted)
    }

    // 13. Power conversion
    @Test
    fun testPowerConversion() {
        val oneKw = CalculatorEngine.powerToWatts(BigDecimal("1"), PowerUnit.KILOWATT)
        assertEquals(0, BigDecimal("1000").compareTo(oneKw))

        val halfKw = CalculatorEngine.powerToWatts(BigDecimal("0.5"), PowerUnit.KILOWATT)
        assertEquals(0, BigDecimal("500").compareTo(halfKw))

        val hundredW = CalculatorEngine.powerToWatts(BigDecimal("100"), PowerUnit.WATT)
        assertEquals(0, BigDecimal("100").compareTo(hundredW))
    }

    // 14. Voltage conversion
    @Test
    fun testVoltageConversion() {
        val oneKv = CalculatorEngine.voltageToVolts(BigDecimal("1"), VoltageUnit.KILOVOLT)
        assertEquals(0, BigDecimal("1000").compareTo(oneKv))

        val oneMv = CalculatorEngine.voltageToVolts(BigDecimal("1000"), VoltageUnit.MILLIVOLT)
        assertEquals(0, BigDecimal("1").compareTo(oneMv))

        val twelveV = CalculatorEngine.voltageToVolts(BigDecimal("12"), VoltageUnit.VOLT)
        assertEquals(0, BigDecimal("12").compareTo(twelveV))
    }

    @Test
    fun testInputValidations() {
        // Power validation
        assertTrue(CalculatorEngine.validatePower("0").isSuccess)
        assertTrue(CalculatorEngine.validatePower("100.5").isSuccess)
        assertTrue(CalculatorEngine.validatePower("").isError)
        assertTrue(CalculatorEngine.validatePower("abc").isError)
        assertTrue(CalculatorEngine.validatePower("-1").isError)

        // Voltage validation
        assertTrue(CalculatorEngine.validateVoltage("12").isSuccess)
        assertTrue(CalculatorEngine.validateVoltage("0.1").isSuccess)
        assertTrue(CalculatorEngine.validateVoltage("0").isError)
        assertTrue(CalculatorEngine.validateVoltage("-12").isError)
        assertTrue(CalculatorEngine.validateVoltage("").isError)
        assertTrue(CalculatorEngine.validateVoltage("xyz").isError)
    }
}
