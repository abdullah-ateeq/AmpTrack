package com.example.amptrack.domain

import com.example.amptrack.data.model.CurrentDisplayMode
import com.example.amptrack.data.model.CurrentUnit
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CalculatorEngine {

    private val MATH_CONTEXT = MathContext(34, RoundingMode.HALF_UP)
    private const val CALCULATION_SCALE = 16

    /** Longest accepted input, keeping every derived value small enough to format quickly. */
    const val MAX_INPUT_LENGTH = 15

    /** Values at or above this magnitude are shown in scientific notation to stay readable. */
    private val SCIENTIFIC_THRESHOLD = BigDecimal("1E12")

    /** Plain decimal notation only: optional minus, digits, optional single decimal point. */
    private val DECIMAL_PATTERN = Regex("""-?(\d+\.?\d*|\.\d+)""")

    /**
     * Converts any power value and unit into standard Watts (W).
     */
    fun powerToWatts(power: BigDecimal, unit: PowerUnit): BigDecimal {
        return power.multiply(unit.multiplierToWatts, MATH_CONTEXT)
    }

    /**
     * Converts any voltage value and unit into standard Volts (V).
     */
    fun voltageToVolts(voltage: BigDecimal, unit: VoltageUnit): BigDecimal {
        return voltage.multiply(unit.multiplierToVolts, MATH_CONTEXT)
    }

    /**
     * Calculates electrical current using I = P / V.
     *
     * @param powerWatts Power in watts (>= 0)
     * @param voltageVolts Voltage in volts (> 0)
     * @return Current in amperes (A)
     */
    fun calculateCurrent(powerWatts: BigDecimal, voltageVolts: BigDecimal): BigDecimal {
        if (powerWatts < BigDecimal.ZERO) {
            throw IllegalArgumentException("Power cannot be negative.")
        }
        if (voltageVolts <= BigDecimal.ZERO) {
            throw IllegalArgumentException("Voltage must be greater than zero.")
        }
        if (powerWatts.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO
        }

        // Precision-based division keeps significant digits for very small currents too.
        return powerWatts.divide(voltageVolts, MATH_CONTEXT)
    }

    /**
     * Formats current according to display mode and decimal precision.
     * Auto mode rules:
     * - zero: "0.00 A"
     * - abs < 0.001 A -> µA (microamperes)
     * - abs < 1 A -> mA (milliamperes)
     * - abs < 1000 A -> A (amperes)
     * - abs >= 1000 A -> kA (kiloamperes)
     */
    fun formatCurrent(
        currentAmps: BigDecimal,
        displayMode: CurrentDisplayMode = CurrentDisplayMode.AUTO,
        decimalPlaces: Int = 2
    ): Pair<String, CurrentUnit> {
        val safeDecimalPlaces = decimalPlaces.coerceIn(1, 3)

        if (currentAmps.compareTo(BigDecimal.ZERO) == 0) {
            val formatted = formatDecimal(BigDecimal.ZERO, safeDecimalPlaces) + " " + CurrentUnit.AMPERE.symbol
            return Pair(formatted, CurrentUnit.AMPERE)
        }

        if (displayMode == CurrentDisplayMode.AMPERES_ONLY) {
            val formatted = formatDecimal(currentAmps, safeDecimalPlaces) + " " + CurrentUnit.AMPERE.symbol
            return Pair(formatted, CurrentUnit.AMPERE)
        }

        val absAmps = currentAmps.abs()
        val oneMilli = BigDecimal("0.001")
        val oneAmp = BigDecimal("1.0")
        val oneKilo = BigDecimal("1000.0")

        return when {
            absAmps < oneMilli -> {
                val microamps = currentAmps.multiply(BigDecimal("1000000"), MATH_CONTEXT)
                val formatted = formatDecimal(microamps, safeDecimalPlaces) + " " + CurrentUnit.MICROAMPERE.symbol
                Pair(formatted, CurrentUnit.MICROAMPERE)
            }
            absAmps < oneAmp -> {
                val milliamps = currentAmps.multiply(BigDecimal("1000"), MATH_CONTEXT)
                val formatted = formatDecimal(milliamps, safeDecimalPlaces) + " " + CurrentUnit.MILLIAMPERE.symbol
                Pair(formatted, CurrentUnit.MILLIAMPERE)
            }
            absAmps < oneKilo -> {
                val formatted = formatDecimal(currentAmps, safeDecimalPlaces) + " " + CurrentUnit.AMPERE.symbol
                Pair(formatted, CurrentUnit.AMPERE)
            }
            else -> {
                val kiloamps = currentAmps.divide(BigDecimal("1000"), CALCULATION_SCALE, RoundingMode.HALF_UP)
                val formatted = formatDecimal(kiloamps, safeDecimalPlaces) + " " + CurrentUnit.KILOAMPERE.symbol
                Pair(formatted, CurrentUnit.KILOAMPERE)
            }
        }
    }

    /**
     * Formats a BigDecimal to the specified decimal places using HALF_UP rounding.
     */
    fun formatDecimal(value: BigDecimal, decimalPlaces: Int): String {
        val safeScale = decimalPlaces.coerceIn(1, 3)
        if (value.abs() >= SCIENTIFIC_THRESHOLD) {
            val symbols = DecimalFormatSymbols(Locale.US)
            val pattern = buildString {
                append("0.")
                repeat(safeScale) { append("0") }
                append("E0")
            }
            return DecimalFormat(pattern, symbols).format(value)
        }
        val rounded = value.setScale(safeScale, RoundingMode.HALF_UP)
        val symbols = DecimalFormatSymbols(Locale.US)
        val pattern = buildString {
            append("#,##0.")
            repeat(safeScale) { append("0") }
        }
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(rounded)
    }

    /**
     * Formats power or voltage with units for summary displays.
     */
    fun formatValueWithUnit(value: BigDecimal, unitSymbol: String, decimalPlaces: Int = 2): String {
        return "${formatDecimal(value, decimalPlaces)} $unitSymbol"
    }

    /**
     * Generates a clean formula explanation string.
     */
    fun generateFormulaString(
        powerValue: BigDecimal,
        powerUnit: PowerUnit,
        voltageValue: BigDecimal,
        voltageUnit: VoltageUnit,
        formattedCurrent: String,
        decimalPlaces: Int = 2
    ): String {
        val powerFormatted = "${formatDecimal(powerValue, decimalPlaces)} ${powerUnit.symbol}"
        val voltageFormatted = "${formatDecimal(voltageValue, decimalPlaces)} ${voltageUnit.symbol}"
        return "$powerFormatted ÷ $voltageFormatted = $formattedCurrent"
    }

    /**
     * Validates raw power input string.
     */
    fun validatePower(input: String): ValidationResult<BigDecimal> {
        val parsed = parseDecimal(input)
            ?: return ValidationResult.Error("Enter a valid power value.")

        if (parsed < BigDecimal.ZERO) {
            return ValidationResult.Error("Power cannot be negative.")
        }
        return ValidationResult.Success(parsed)
    }

    /**
     * Validates raw voltage input string.
     */
    fun validateVoltage(input: String): ValidationResult<BigDecimal> {
        val parsed = parseDecimal(input)
            ?: return ValidationResult.Error("Enter a valid voltage value.")

        if (parsed < BigDecimal.ZERO) {
            return ValidationResult.Error("Voltage cannot be negative.")
        }
        if (parsed.compareTo(BigDecimal.ZERO) == 0) {
            return ValidationResult.Error("Voltage must be greater than zero.")
        }
        return ValidationResult.Success(parsed)
    }

    /**
     * Parses plain decimal input. Rejects empty, malformed, exponent-notation
     * and overly long input so extreme values can never stall formatting.
     */
    private fun parseDecimal(input: String): BigDecimal? {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_INPUT_LENGTH) return null
        if (!DECIMAL_PATTERN.matches(trimmed)) return null
        return trimmed.toBigDecimalOrNull()
    }
}

sealed class ValidationResult<out T> {
    data class Success<out T>(val value: T) : ValidationResult<T>()
    data class Error(val message: String) : ValidationResult<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.value
    fun errorMessageOrNull(): String? = (this as? Error)?.message
}
