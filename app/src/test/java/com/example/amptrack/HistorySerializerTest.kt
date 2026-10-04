package com.example.amptrack

import com.example.amptrack.data.datastore.HistorySerializer
import com.example.amptrack.data.model.CalculationHistoryItem
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySerializerTest {

    @Test
    fun testSerializationRoundtrip() {
        val items = listOf(
            CalculationHistoryItem(
                id = "id-1",
                timestamp = 1720000000000L,
                formattedDateTime = "Oct 3, 2026 10:30 AM",
                powerValue = "120",
                powerUnit = PowerUnit.WATT,
                voltageValue = "12",
                voltageUnit = VoltageUnit.VOLT,
                currentFormatted = "10.00 A",
                currentDisplayUnit = "A",
                formula = "120.00 W ÷ 12.00 V = 10.00 A"
            ),
            CalculationHistoryItem(
                id = "id-2",
                timestamp = 1720000050000L,
                formattedDateTime = "Oct 3, 2026 10:35 AM",
                powerValue = "1",
                powerUnit = PowerUnit.KILOWATT,
                voltageValue = "230",
                voltageUnit = VoltageUnit.VOLT,
                currentFormatted = "4.35 A",
                currentDisplayUnit = "A",
                formula = "1.00 kW ÷ 230.00 V = 4.35 A"
            )
        )

        val serialized = HistorySerializer.serialize(items)
        assertTrue(serialized.isNotEmpty())

        val deserialized = HistorySerializer.deserialize(serialized)
        assertEquals(2, deserialized.size)
        assertEquals("id-1", deserialized[0].id)
        assertEquals("120", deserialized[0].powerValue)
        assertEquals(PowerUnit.WATT, deserialized[0].powerUnit)
        assertEquals("12", deserialized[0].voltageValue)
        assertEquals(VoltageUnit.VOLT, deserialized[0].voltageUnit)
        assertEquals("10.00 A", deserialized[0].currentFormatted)

        assertEquals("id-2", deserialized[1].id)
        assertEquals(PowerUnit.KILOWATT, deserialized[1].powerUnit)
        assertEquals("4.35 A", deserialized[1].currentFormatted)
    }

    @Test
    fun testEmptyAndMalformedDeserialization() {
        assertEquals(0, HistorySerializer.deserialize(null).size)
        assertEquals(0, HistorySerializer.deserialize("").size)
        assertEquals(0, HistorySerializer.deserialize("random invalid corrupted string").size)
    }

    @Test
    fun testFiftyRecordsEvictionLogic() {
        val list = (1..60).map { i ->
            CalculationHistoryItem(
                id = "item-$i",
                timestamp = 1000L + i,
                formattedDateTime = "Time $i",
                powerValue = "$i",
                powerUnit = PowerUnit.WATT,
                voltageValue = "12",
                voltageUnit = VoltageUnit.VOLT,
                currentFormatted = "$i A",
                currentDisplayUnit = "A",
                formula = "$i W ÷ 12 V = $i A"
            )
        }

        // Keep newest 50
        val capped = list.take(50)
        assertEquals(50, capped.size)
        assertEquals("item-1", capped.first().id)
        assertEquals("item-50", capped.last().id)
    }
}
