package com.example.amptrack.ui.screens.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.amptrack.data.model.PowerUnit
import com.example.amptrack.data.model.VoltageUnit
import com.example.amptrack.ui.components.QuickValueChips
import com.example.amptrack.ui.components.SafetyBanner
import com.example.amptrack.ui.components.UnitSelector
import com.example.amptrack.ui.viewmodel.AmpTrackUiState
import kotlinx.coroutines.launch

private val QUICK_POWERS = listOf("10", "50", "100", "500", "1000", "1500", "2000")
private val QUICK_VOLTAGES = listOf("5", "12", "24", "48", "120", "230", "240")

@Composable
fun CalculatorScreen(
    uiState: AmpTrackUiState,
    onPowerChanged: (String) -> Unit,
    onPowerUnitChanged: (PowerUnit) -> Unit,
    onVoltageChanged: (String) -> Unit,
    onVoltageUnitChanged: (VoltageUnit) -> Unit,
    onQuickPowerSelected: (String) -> Unit,
    onQuickVoltageSelected: (String) -> Unit,
    onCalculate: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    fun performHaptic() {
        if (uiState.settings.hapticFeedback) {
            haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isWide = maxWidth >= 600.dp

        if (isWide) {
            // Tablet / Landscape two-column responsive layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Left column: Header & Inputs
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    CalculatorHeader()
                    Spacer(modifier = Modifier.height(16.dp))
                    InputSection(
                        uiState = uiState,
                        onPowerChanged = onPowerChanged,
                        onPowerUnitChanged = {
                            performHaptic()
                            onPowerUnitChanged(it)
                        },
                        onVoltageChanged = onVoltageChanged,
                        onVoltageUnitChanged = {
                            performHaptic()
                            onVoltageUnitChanged(it)
                        },
                        onQuickPowerSelected = {
                            performHaptic()
                            onQuickPowerSelected(it)
                        },
                        onQuickVoltageSelected = {
                            performHaptic()
                            onQuickVoltageSelected(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ActionButtons(
                        onCalculate = {
                            performHaptic()
                            onCalculate()
                        },
                        onSave = {
                            performHaptic()
                            onSave()
                        },
                        onReset = {
                            performHaptic()
                            onReset()
                        },
                        isCalculationValid = uiState.isCalculationValid
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SafetyBanner()
                }

                // Right column: Result, Formula & Summary
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ResultSection(
                        uiState = uiState,
                        onCopy = { textToCopy ->
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            performHaptic()
                            scope.launch {
                                snackbarHostState.showSnackbar("Current copied")
                            }
                        }
                    )
                    if (uiState.settings.showFormula && uiState.formulaString != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        FormulaSection(formulaString = uiState.formulaString)
                    }
                    if (uiState.isCalculationValid && uiState.summaryPower != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ElectricalSummarySection(
                            powerSummary = uiState.summaryPower,
                            voltageSummary = uiState.summaryVoltage ?: "",
                            currentSummary = uiState.summaryCurrent ?: ""
                        )
                    }
                }
            }
        } else {
            // Phone Portrait single-column layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                CalculatorHeader()

                Spacer(modifier = Modifier.height(14.dp))

                ResultSection(
                    uiState = uiState,
                    onCopy = { textToCopy ->
                        clipboardManager.setText(AnnotatedString(textToCopy))
                        performHaptic()
                        scope.launch {
                            snackbarHostState.showSnackbar("Current copied")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                InputSection(
                    uiState = uiState,
                    onPowerChanged = onPowerChanged,
                    onPowerUnitChanged = {
                        performHaptic()
                        onPowerUnitChanged(it)
                    },
                    onVoltageChanged = onVoltageChanged,
                    onVoltageUnitChanged = {
                        performHaptic()
                        onVoltageUnitChanged(it)
                    },
                    onQuickPowerSelected = {
                        performHaptic()
                        onQuickPowerSelected(it)
                    },
                    onQuickVoltageSelected = {
                        performHaptic()
                        onQuickVoltageSelected(it)
                    }
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (uiState.settings.showFormula && uiState.formulaString != null) {
                    FormulaSection(formulaString = uiState.formulaString)
                    Spacer(modifier = Modifier.height(14.dp))
                }

                if (uiState.isCalculationValid && uiState.summaryPower != null) {
                    ElectricalSummarySection(
                        powerSummary = uiState.summaryPower,
                        voltageSummary = uiState.summaryVoltage ?: "",
                        currentSummary = uiState.summaryCurrent ?: ""
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                ActionButtons(
                    onCalculate = {
                        performHaptic()
                        onCalculate()
                    },
                    onSave = {
                        performHaptic()
                        onSave()
                    },
                    onReset = {
                        performHaptic()
                        onReset()
                    },
                    isCalculationValid = uiState.isCalculationValid
                )

                Spacer(modifier = Modifier.height(14.dp))

                SafetyBanner()

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CalculatorHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AMPTRACK",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = "Calculate current from power and voltage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InputSection(
    uiState: AmpTrackUiState,
    onPowerChanged: (String) -> Unit,
    onPowerUnitChanged: (PowerUnit) -> Unit,
    onVoltageChanged: (String) -> Unit,
    onVoltageUnitChanged: (VoltageUnit) -> Unit,
    onQuickPowerSelected: (String) -> Unit,
    onQuickVoltageSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "INPUT",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .semantics { heading() }
        )

        // Power Input Field + Unit Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.powerInput,
                onValueChange = onPowerChanged,
                label = { Text("POWER") },
                isError = uiState.powerError != null,
                supportingText = {
                    if (uiState.powerError != null) {
                        Text(
                            text = uiState.powerError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Power input"
                    }
            )

            UnitSelector(
                selectedUnit = uiState.powerUnit,
                options = PowerUnit.entries,
                unitLabel = { it.symbol },
                onUnitSelected = onPowerUnitChanged,
                contentDesc = "Power unit"
            )
        }

        // Quick Power Chips
        if (uiState.settings.showQuickValues) {
            QuickValueChips(
                title = "Power",
                values = QUICK_POWERS,
                unitSuffix = "W",
                selectedValue = if (uiState.powerUnit == PowerUnit.WATT) uiState.powerInput else "",
                onValueSelected = onQuickPowerSelected,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Voltage Input Field + Unit Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.voltageInput,
                onValueChange = onVoltageChanged,
                label = { Text("VOLTAGE") },
                isError = uiState.voltageError != null,
                supportingText = {
                    if (uiState.voltageError != null) {
                        Text(
                            text = uiState.voltageError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Voltage input"
                    }
            )

            UnitSelector(
                selectedUnit = uiState.voltageUnit,
                options = VoltageUnit.entries,
                unitLabel = { it.symbol },
                onUnitSelected = onVoltageUnitChanged,
                contentDesc = "Voltage unit"
            )
        }

        // Quick Voltage Chips
        if (uiState.settings.showQuickValues) {
            QuickValueChips(
                title = "Voltage",
                values = QUICK_VOLTAGES,
                unitSuffix = "V",
                selectedValue = if (uiState.voltageUnit == VoltageUnit.VOLT) uiState.voltageInput else "",
                onValueSelected = onQuickVoltageSelected,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun ResultSection(
    uiState: AmpTrackUiState,
    onCopy: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CALCULATED CURRENT",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )

                if (uiState.calculatedCurrentFormatted != null) {
                    IconButton(
                        onClick = { onCopy(uiState.calculatedCurrentFormatted) },
                        modifier = Modifier
                            .size(32.dp)
                            .semantics { contentDescription = "Copy result" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Large Prominent Calculated Current
            if (uiState.calculatedCurrentFormatted != null) {
                Text(
                    text = uiState.calculatedCurrentFormatted,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        contentDescription = "Calculated current ${uiState.calculatedCurrentFormatted}"
                    }
                )
            } else {
                Text(
                    text = "—",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-values (Base Power Watts & Base Voltage Volts)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "POWER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.powerWattsFormatted ?: "—",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "VOLTAGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.voltageVoltsFormatted ?: "—",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun FormulaSection(formulaString: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "CURRENT FORMULA",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "I = P ÷ V",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = formulaString,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ElectricalSummarySection(
    powerSummary: String,
    voltageSummary: String,
    currentSummary: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            SummaryItem(label = "POWER", value = powerSummary)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            SummaryItem(label = "VOLTAGE", value = voltageSummary)
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            SummaryItem(label = "CURRENT", value = currentSummary)
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ActionButtons(
    onCalculate: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    isCalculationValid: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // CALCULATE button (validates / refreshes live calculation)
            Button(
                onClick = onCalculate,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Calculate current" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Calculate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CALCULATE",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // SAVE CALCULATION button
            FilledTonalButton(
                onClick = onSave,
                enabled = isCalculationValid,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Save calculation" }
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "SAVE",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // RESET button
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .semantics { contentDescription = "Reset calculator" }
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "RESET",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
