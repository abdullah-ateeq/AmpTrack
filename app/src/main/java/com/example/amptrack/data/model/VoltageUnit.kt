package com.example.amptrack.data.model

import java.math.BigDecimal

enum class VoltageUnit(val symbol: String, val displayName: String, val multiplierToVolts: BigDecimal) {
    MILLIVOLT("mV", "Millivolts (mV)", BigDecimal("0.001")),
    VOLT("V", "Volts (V)", BigDecimal("1")),
    KILOVOLT("kV", "Kilovolts (kV)", BigDecimal("1000"));

    companion object {
        fun fromSymbol(symbol: String): VoltageUnit {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: VOLT
        }
    }
}
