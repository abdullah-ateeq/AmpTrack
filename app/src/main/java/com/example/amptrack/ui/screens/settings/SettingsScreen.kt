package com.example.amptrack.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amptrack.data.model.AppSettings
import com.example.amptrack.data.model.AppThemeMode
import com.example.amptrack.data.model.CurrentDisplayMode
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onThemeModeChange: (AppThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDefaultPowerUnitChange: (PowerUnit) -> Unit,
    onDefaultVoltageUnitChange: (VoltageUnit) -> Unit,
    onDefaultPowerValueChange: (String) -> String?,
    onDefaultVoltageValueChange: (String) -> String?,
    onCurrentDisplayModeChange: (CurrentDisplayMode) -> Unit,
    onDecimalPlacesChange: (Int) -> Unit,
    onHapticFeedbackChange: (Boolean) -> Unit,
    onShowFormulaChange: (Boolean) -> Unit,
    onShowQuickValuesChange: (Boolean) -> Unit,
    onResetSettings: () -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    // Local text state so typing is not interrupted by DataStore round-trips;
    // only valid values are persisted.
    var defaultPowerText by rememberSaveable { mutableStateOf(settings.defaultPowerValue) }
    var defaultVoltageText by rememberSaveable { mutableStateOf(settings.defaultVoltageValue) }
    var defaultPowerError by rememberSaveable { mutableStateOf<String?>(null) }
    var defaultVoltageError by rememberSaveable { mutableStateOf<String?>(null) }

    fun triggerHaptic() {
        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings?") },
            text = { Text("All settings will be restored to their initial defaults.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        triggerHaptic()
                        onResetSettings()
                        val defaults = AppSettings()
                        defaultPowerText = defaults.defaultPowerValue
                        defaultVoltageText = defaults.defaultVoltageValue
                        defaultPowerError = null
                        defaultVoltageError = null
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History?") },
            text = { Text("This will permanently remove all saved calculations.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearHistoryDialog = false
                        triggerHaptic()
                        onClearHistory()
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Group
        SettingsCard(title = "APPEARANCE") {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AppThemeMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.themeMode == mode,
                        onClick = {
                            triggerHaptic()
                            onThemeModeChange(mode)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = AppThemeMode.entries.size)
                    ) {
                        Text(mode.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dynamic Color",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Use system wallpaper colors on Android 12+",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.dynamicColor,
                    onCheckedChange = {
                        triggerHaptic()
                        onDynamicColorChange(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Calculation Defaults Group
        SettingsCard(title = "CALCULATION DEFAULTS") {
            // Default Power Unit
            Text(
                text = "Default Power Unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                PowerUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = settings.defaultPowerUnit == unit,
                        onClick = {
                            triggerHaptic()
                            onDefaultPowerUnitChange(unit)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = PowerUnit.entries.size)
                    ) {
                        Text(unit.symbol)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Default Voltage Unit
            Text(
                text = "Default Voltage Unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                VoltageUnit.entries.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = settings.defaultVoltageUnit == unit,
                        onClick = {
                            triggerHaptic()
                            onDefaultVoltageUnitChange(unit)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = VoltageUnit.entries.size)
                    ) {
                        Text(unit.symbol)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Default Values Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = defaultPowerText,
                    onValueChange = {
                        defaultPowerText = it
                        defaultPowerError = onDefaultPowerValueChange(it)
                    },
                    label = { Text("Default Power") },
                    suffix = { Text(settings.defaultPowerUnit.symbol) },
                    isError = defaultPowerError != null,
                    supportingText = defaultPowerError?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = defaultVoltageText,
                    onValueChange = {
                        defaultVoltageText = it
                        defaultVoltageError = onDefaultVoltageValueChange(it)
                    },
                    label = { Text("Default Voltage") },
                    suffix = { Text(settings.defaultVoltageUnit.symbol) },
                    isError = defaultVoltageError != null,
                    supportingText = defaultVoltageError?.let { error -> { Text(error) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Display & Precision Group
        SettingsCard(title = "DISPLAY & PRECISION") {
            // Current Display Mode
            Text(
                text = "Current Display Scaling",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val modes = listOf(CurrentDisplayMode.AUTO, CurrentDisplayMode.AMPERES_ONLY)
                modes.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = settings.currentDisplayMode == mode,
                        onClick = {
                            triggerHaptic()
                            onCurrentDisplayModeChange(mode)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                    ) {
                        Text(if (mode == CurrentDisplayMode.AUTO) "Auto (µA/mA/A/kA)" else "Amperes Only (A)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Decimal Places
            Text(
                text = "Decimal Places",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val decimals = listOf(1, 2, 3)
                decimals.forEachIndexed { index, places ->
                    SegmentedButton(
                        selected = settings.decimalPlaces == places,
                        onClick = {
                            triggerHaptic()
                            onDecimalPlacesChange(places)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = decimals.size)
                    ) {
                        Text("$places")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Interaction & Toggles Group
        SettingsCard(title = "PREFERENCES") {
            // Haptic Feedback
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Haptic Feedback",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Light vibration on buttons and selection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.hapticFeedback,
                    onCheckedChange = {
                        triggerHaptic()
                        onHapticFeedbackChange(it)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Show Formula
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Show Formula",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Display I = P ÷ V breakdown on the calculator",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.showFormula,
                    onCheckedChange = {
                        triggerHaptic()
                        onShowFormulaChange(it)
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            // Show Quick Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Show Quick Values",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Show shortcut chips for common wattage and voltages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = settings.showQuickValues,
                    onCheckedChange = {
                        triggerHaptic()
                        onShowQuickValuesChange(it)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset Settings and Clear History Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset Settings")
            }

            OutlinedButton(
                onClick = { showClearHistoryDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear History", color = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
