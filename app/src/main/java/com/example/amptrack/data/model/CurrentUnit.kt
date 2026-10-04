package com.example.amptrack.data.model

import java.math.BigDecimal

enum class CurrentUnit(val symbol: String, val displayName: String, val multiplierToAmps: BigDecimal) {
    MICROAMPERE("µA", "Microamperes (µA)", BigDecimal("0.000001")),
    MILLIAMPERE("mA", "Milliamperes (mA)", BigDecimal("0.001")),
    AMPERE("A", "Amperes (A)", BigDecimal("1")),
    KILOAMPERE("kA", "Kiloamperes (kA)", BigDecimal("1000"));

    companion object {
        fun fromSymbol(symbol: String): CurrentUnit {
            return entries.firstOrNull { it.symbol == symbol } ?: AMPERE
        }
    }
}
