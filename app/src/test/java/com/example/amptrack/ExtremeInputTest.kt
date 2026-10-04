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
import org.junit.Test
import java.math.BigDecimal

class ExtremeInputTest {

    @Test
    fun rejectsExponentAndMalformedInput() {
        listOf("1e999999999", "1E5", "abc", "1.2.3", "--5", ".", "-", " ", "NaN", "Infinity").forEach { input ->
            assertTrue("power '$input'", CalculatorEngine.validatePower(input).isError)
            assertTrue("voltage '$input'", CalculatorEngine.validateVoltage(input).isError)
        }
    }

    @Test
    fun rejectsOverlyLongInput() {
        val tooLong = "1".repeat(CalculatorEngine.MAX_INPUT_LENGTH + 1)
        assertTrue(CalculatorEngine.validatePower(tooLong).isError)
        assertTrue(CalculatorEngine.validatePower("1".repeat(CalculatorEngine.MAX_INPUT_LENGTH)).isSuccess)
    }

    @Test
    fun acceptsLeadingAndTrailingDecimalPoint() {
        assertEquals(0, BigDecimal("0.5").compareTo(CalculatorEngine.validatePower(".5").getOrNull()))
        assertEquals(0, BigDecimal("12").compareTo(CalculatorEngine.validateVoltage("12.").getOrNull()))
    }

    @Test
    fun negativeVoltageAndZeroVoltageMessages() {
        assertEquals("Voltage cannot be negative.", CalculatorEngine.validateVoltage("-1").errorMessageOrNull())
        assertEquals("Voltage must be greater than zero.", CalculatorEngine.validateVoltage("0.000").errorMessageOrNull())
    }

    @Test
    fun tinyCurrentKeepsSignificantDigits() {
        // 0.000000001 W / 999999999 kV is far below the old 16-digit scale.
        val watts = CalculatorEngine.powerToWatts(BigDecimal("0.000000001"), PowerUnit.WATT)
        val volts = CalculatorEngine.voltageToVolts(BigDecimal("999999999"), VoltageUnit.KILOVOLT)
        val amps = CalculatorEngine.calculateCurrent(watts, volts)
        assertTrue(amps.signum() > 0)
    }

    @Test
    fun hugeCurrentUsesReadableScientificNotation() {
        // Largest accepted power with smallest accepted voltage.
        val watts = CalculatorEngine.powerToWatts(BigDecimal("999999999999999"), PowerUnit.KILOWATT)
        val volts = CalculatorEngine.voltageToVolts(BigDecimal("0.000000000001"), VoltageUnit.MILLIVOLT)
        val amps = CalculatorEngine.calculateCurrent(watts, volts)

        val (formatted, unit) = CalculatorEngine.formatCurrent(amps, CurrentDisplayMode.AUTO, 2)
        assertEquals(CurrentUnit.KILOAMPERE, unit)
        assertTrue(formatted, formatted.matches(Regex("""\d\.\d{2}E\d+ kA""")))
        assertFalse(formatted.contains("Infinity") || formatted.contains("NaN"))
    }

    @Test
    fun valuesBelowThresholdStayPlain() {
        assertEquals("999,999.00", CalculatorEngine.formatDecimal(BigDecimal("999999"), 2))
    }
}
