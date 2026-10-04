package com.example.amptrack.data.model

data class CalculationHistoryItem(
    val id: String,
    val timestamp: Long,
    val formattedDateTime: String,
    val powerValue: String,
    val powerUnit: PowerUnit,
    val voltageValue: String,
    val voltageUnit: VoltageUnit,
    val currentFormatted: String,
    val currentDisplayUnit: String,
    val formula: String
)
