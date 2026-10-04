package com.example.amptrack.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun <T> UnitSelector(
    selectedUnit: T,
    options: List<T>,
    unitLabel: (T) -> String,
    onUnitSelected: (T) -> Unit,
    contentDesc: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .height(56.dp)
                .semantics {
                    contentDescription = "$contentDesc: ${unitLabel(selectedUnit)}"
                },
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = ButtonDefaults.ContentPadding
        ) {
            Text(
                text = unitLabel(selectedUnit),
                style = MaterialTheme.typography.titleMedium
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.padding(start = 2.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = unitLabel(option),
                            style = if (option == selectedUnit) {
                                MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary)
                            } else {
                                MaterialTheme.typography.bodyMedium
                            }
                        )
                    },
                    onClick = {
                        expanded = false
                        onUnitSelected(option)
                    }
                )
            }
        }
    }
}
