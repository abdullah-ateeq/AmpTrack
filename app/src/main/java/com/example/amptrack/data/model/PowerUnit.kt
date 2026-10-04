package com.example.amptrack.data.model

import java.math.BigDecimal

enum class PowerUnit(val symbol: String, val displayName: String, val multiplierToWatts: BigDecimal) {
    WATT("W", "Watts (W)", BigDecimal("1")),
    KILOWATT("kW", "Kilowatts (kW)", BigDecimal("1000"));

    companion object {
        fun fromSymbol(symbol: String): PowerUnit {
            return entries.firstOrNull { it.symbol.equals(symbol, ignoreCase = true) } ?: WATT
        }
    }
}
