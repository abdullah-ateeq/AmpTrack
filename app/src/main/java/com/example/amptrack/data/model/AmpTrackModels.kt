package com.example.amptrack.data.model

import java.math.BigDecimal

data class AmpTrackInput(
    val power: BigDecimal,
    val powerUnit: PowerUnit,
    val voltage: BigDecimal,
    val voltageUnit: VoltageUnit
)

data class AmpTrackResult(
    val powerWatts: BigDecimal,
    val voltageVolts: BigDecimal,
    val currentAmps: BigDecimal,
    val formattedCurrent: String,
    val formulaString: String,
    val currentUnit: CurrentUnit
)
