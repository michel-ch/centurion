package com.century.app.ui.weightlog

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.century.app.data.local.entity.WeightLog
import com.century.app.ui.components.CenturyCard
import com.century.app.ui.components.CenturyTopBar
import com.century.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightLogScreen(
    viewModel: WeightLogViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val weightLogs by viewModel.weightLogs.collectAsState()
    val inputWeight by viewModel.inputWeight.collectAsState()
    val unit = profile?.bodyWeightUnit ?: "kg"
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    var showDeleteDialog by remember { mutableStateOf<WeightLog?>(null) }

    Scaffold(
        topBar = {
            CenturyTopBar(title = "WEIGHT LOG", onBack = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Quick Entry Card
            item {
                CenturyCard {
                    Text(
                        text = "LOG WEIGHT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CenturyRed
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { viewModel.adjustWeight(-0.1f) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DarkBorder
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = inputWeight,
                            onValueChange = { viewModel.updateInputWeight(it) },
                            modifier = Modifier.width(140.dp),
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            ),
                            placeholder = {
                                Text(
                                    text = profile?.bodyWeight?.toString() ?: "70.0",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        textAlign = TextAlign.Center
                                    ),
                                    color = TextTertiary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            suffix = {
                                Text(
                                    text = unit,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextSecondary
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CenturyRed,
                                unfocusedBorderColor = DarkBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        FilledIconButton(
                            onClick = { viewModel.adjustWeight(0.1f) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = DarkBorder
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.logWeight() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CenturyRed),
                        shape = RoundedCornerShape(4.dp),
                        enabled = inputWeight.toFloatOrNull() != null
                    ) {
                        Text(
                            text = "LOG WEIGHT",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // History
            item {
                Text(
                    text = "HISTORY",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (weightLogs.isEmpty()) {
                item {
                    CenturyCard {
                        Text(
                            text = "No weight entries yet.\nLog your first weight above.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            items(weightLogs, key = { it.id }) { log ->
                val previousLog = weightLogs.let { logs ->
                    val index = logs.indexOf(log)
                    if (index < logs.size - 1) logs[index + 1] else null
                }
                val change = previousLog?.let { log.weight - it.weight }

                SwipeToDismissBox(
                    state = rememberSwipeToDismissBoxState(
                        confirmValueChange = { dismissValue ->
                            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                showDeleteDialog = log
                            }
                            false
                        }
                    ),
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(CenturyRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = CenturyRed
                            )
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    CenturyCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = dateFormat.format(Date(log.loggedAt)),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${String.format("%.1f", log.weight)} ${log.unit}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (change != null) {
                                val changeColor = when {
                                    change > 0 -> CenturyOrange
                                    change < 0 -> CenturyGreen
                                    else -> TextSecondary
                                }
                                val prefix = if (change > 0) "+" else ""
                                Text(
                                    text = "$prefix${String.format("%.1f", change)}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = changeColor
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { log ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("DELETE ENTRY", style = MaterialTheme.typography.headlineSmall) },
            text = {
                Text(
                    "Delete weight entry from ${dateFormat.format(Date(log.loggedAt))}?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLog(log)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = CenturyRed)
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("CANCEL")
                }
            }
        )
    }
}
