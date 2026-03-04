package com.century.app.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.century.app.ui.components.CenturyCard
import com.century.app.ui.components.CenturyTopBar
import com.century.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onResetComplete: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var editWeight by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenturyTopBar(title = "SETTINGS", onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Profile Section
            item {
                Text(
                    text = "PROFILE",
                    style = MaterialTheme.typography.labelLarge,
                    color = CenturyRed
                )
            }

            // Name
            item {
                CenturyCard {
                    if (editName) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CenturyRed
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { editName = false }) {
                                Text("CANCEL")
                            }
                            TextButton(
                                onClick = {
                                    if (nameInput.length in 2..30) {
                                        viewModel.updateName(nameInput)
                                        editName = false
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = CenturyRed)
                            ) {
                                Text("SAVE")
                            }
                        }
                    } else {
                        SettingsRow(
                            label = "Name",
                            value = profile?.name ?: "",
                            onClick = {
                                nameInput = profile?.name ?: ""
                                editName = true
                            }
                        )
                    }
                }
            }

            // Weight
            item {
                CenturyCard {
                    if (editWeight) {
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = { weightInput = it },
                            label = { Text("Weight (${profile?.bodyWeightUnit ?: "kg"})") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CenturyRed
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { editWeight = false }) {
                                Text("CANCEL")
                            }
                            TextButton(
                                onClick = {
                                    weightInput.toFloatOrNull()?.let {
                                        viewModel.updateWeight(it)
                                        editWeight = false
                                    }
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = CenturyRed)
                            ) {
                                Text("SAVE")
                            }
                        }
                    } else {
                        SettingsRow(
                            label = "Body Weight",
                            value = "${String.format("%.1f", profile?.bodyWeight ?: 0f)} ${profile?.bodyWeightUnit ?: "kg"}",
                            onClick = {
                                weightInput = String.format("%.1f", profile?.bodyWeight ?: 0f)
                                editWeight = true
                            }
                        )
                    }
                }
            }

            // Units
            item {
                Text(
                    text = "PREFERENCES",
                    style = MaterialTheme.typography.labelLarge,
                    color = CenturyRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                CenturyCard {
                    SettingsRow(
                        label = "Units",
                        value = if (profile?.bodyWeightUnit == "kg") "Metric (kg/cm)" else "Imperial (lbs/ft)",
                        onClick = { viewModel.toggleUnits() }
                    )
                }
            }

            // Rest Timers
            item {
                Text(
                    text = "REST TIMERS",
                    style = MaterialTheme.typography.labelLarge,
                    color = CenturyRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                CenturyCard {
                    Text(
                        text = "Between Sets: ${profile?.restBetweenSetsSec ?: 60}s",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 45, 60, 90, 120).forEach { sec ->
                            FilterChip(
                                selected = profile?.restBetweenSetsSec == sec,
                                onClick = { viewModel.updateRestBetweenSets(sec) },
                                label = { Text("${sec}s") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CenturyRed,
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            item {
                CenturyCard {
                    Text(
                        text = "Between Exercises: ${profile?.restBetweenExercisesSec ?: 90}s",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(60, 90, 120, 150).forEach { sec ->
                            FilterChip(
                                selected = profile?.restBetweenExercisesSec == sec,
                                onClick = { viewModel.updateRestBetweenExercises(sec) },
                                label = { Text("${sec}s") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CenturyRed,
                                    selectedLabelColor = TextPrimary
                                )
                            )
                        }
                    }
                }
            }

            item {
                CenturyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Auto-Reduce Rest by Week",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Progressively shortens rest each week",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = profile?.autoReduceRest ?: true,
                            onCheckedChange = { viewModel.toggleAutoReduceRest() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CenturyRed,
                                checkedTrackColor = CenturyRedDark
                            )
                        )
                    }
                }
            }

            // Notifications
            item {
                Text(
                    text = "NOTIFICATIONS",
                    style = MaterialTheme.typography.labelLarge,
                    color = CenturyRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                CenturyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Daily Reminder",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "at ${profile?.reminderTime ?: "07:00"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = profile?.reminderEnabled ?: true,
                            onCheckedChange = { viewModel.toggleReminder() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CenturyRed,
                                checkedTrackColor = CenturyRedDark
                            )
                        )
                    }
                }
            }

            // Theme
            item {
                Text(
                    text = "APPEARANCE",
                    style = MaterialTheme.typography.labelLarge,
                    color = CenturyRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                CenturyCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dark Theme",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = profile?.useDarkTheme ?: true,
                            onCheckedChange = { viewModel.toggleDarkTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CenturyRed,
                                checkedTrackColor = CenturyRedDark
                            )
                        )
                    }
                }
            }

            // Data
            item {
                Text(
                    text = "DATA",
                    style = MaterialTheme.typography.labelLarge,
                    color = CenturyRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                CenturyCard {
                    SettingsRow(
                        label = "Export Data",
                        value = "CSV",
                        onClick = {
                            viewModel.exportData()?.let {
                                context.startActivity(Intent.createChooser(it, "Export Century Data"))
                            }
                        }
                    )
                }
            }

            item {
                CenturyCard {
                    SettingsRow(
                        label = "Reset Program",
                        value = "Start over",
                        valueColor = CenturyRed,
                        onClick = { showResetDialog = true }
                    )
                }
            }

            // About
            item {
                Text(
                    text = "ABOUT",
                    style = MaterialTheme.typography.labelLarge,
                    color = CenturyRed,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                CenturyCard {
                    Text(
                        text = "CENTURY",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "100 Push-Up Challenge",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    "RESET PROGRAM",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CenturyRed
                )
            },
            text = {
                Text(
                    "This will reset your 30-day program progress. Your weight log and workout history will be kept. Are you sure?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetProgram()
                        showResetDialog = false
                        onResetComplete()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CenturyRed)
                ) {
                    Text("RESET")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = TextSecondary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor
            )
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = TextTertiary
            )
        }
    }
}
