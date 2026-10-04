package com.example.amptrack.data.datastore

import com.example.amptrack.data.model.CalculationHistoryItem
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit

object HistorySerializer {

    private const val RECORD_SEPARATOR = "\u001E" // Record Separator
    private const val FIELD_SEPARATOR = "\u001F"  // Unit Separator

    /**
     * Serializes a list of calculation history items into a string.
     * Escapes separators safely.
     */
    fun serialize(items: List<CalculationHistoryItem>): String {
        return items.joinToString(RECORD_SEPARATOR) { item ->
            listOf(
                escape(item.id),
                item.timestamp.toString(),
                escape(item.formattedDateTime),
                escape(item.powerValue),
                item.powerUnit.name,
                escape(item.voltageValue),
                item.voltageUnit.name,
                escape(item.currentFormatted),
                escape(item.currentDisplayUnit),
                escape(item.formula)
            ).joinToString(FIELD_SEPARATOR)
        }
    }

    /**
     * Deserializes a string into a list of calculation history items.
     * Gracefully ignores corrupted or invalid items.
     */
    fun deserialize(raw: String?): List<CalculationHistoryItem> {
        if (raw.isNullOrBlank()) return emptyList()

        val records = raw.split(RECORD_SEPARATOR)
        val result = mutableListOf<CalculationHistoryItem>()

        for (record in records) {
            if (record.isBlank()) continue
            val fields = record.split(FIELD_SEPARATOR)
            if (fields.size >= 10) {
                try {
                    val id = unescape(fields[0])
                    val timestamp = fields[1].toLongOrNull() ?: System.currentTimeMillis()
                    val formattedDateTime = unescape(fields[2])
                    val powerValue = unescape(fields[3])
                    val powerUnit = try { PowerUnit.valueOf(fields[4]) } catch (e: Exception) { PowerUnit.WATT }
                    val voltageValue = unescape(fields[5])
                    val voltageUnit = try { VoltageUnit.valueOf(fields[6]) } catch (e: Exception) { VoltageUnit.VOLT }
                    val currentFormatted = unescape(fields[7])
                    val currentDisplayUnit = unescape(fields[8])
                    val formula = unescape(fields[9])

                    result.add(
                        CalculationHistoryItem(
                            id = id,
                            timestamp = timestamp,
                            formattedDateTime = formattedDateTime,
                            powerValue = powerValue,
                            powerUnit = powerUnit,
                            voltageValue = voltageValue,
                            voltageUnit = voltageUnit,
                            currentFormatted = currentFormatted,
                            currentDisplayUnit = currentDisplayUnit,
                            formula = formula
                        )
                    )
                } catch (_: Exception) {
                    // Skip malformed record
                }
            }
        }
        return result
    }

    private fun escape(s: String): String {
        return s.replace("\\", "\\\\")
            .replace(RECORD_SEPARATOR, "")
            .replace(FIELD_SEPARATOR, "")
    }

    private fun unescape(s: String): String {
        return s.replace("\\\\", "\\")
    }
}
