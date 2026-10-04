package com.example.amptrack.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun QuickValueChips(
    title: String,
    values: List<String>,
    unitSuffix: String,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        values.forEach { value ->
            val isSelected = selectedValue.trim() == value
            AssistChip(
                onClick = { onValueSelected(value) },
                label = {
                    Text(
                        text = "$value $unitSuffix",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceContainer
                    },
                    labelColor = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    }
                ),
                modifier = Modifier.semantics {
                    contentDescription = "Quick $title $value $unitSuffix"
                }
            )
        }
    }
}
