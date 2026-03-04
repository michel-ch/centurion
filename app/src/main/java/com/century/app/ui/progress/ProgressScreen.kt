package com.century.app.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.century.app.data.local.entity.PushUpTest
import com.century.app.data.local.entity.WeightLog
import com.century.app.ui.components.CenturyCard
import com.century.app.ui.components.CenturyTopBar
import com.century.app.ui.components.StatCard
import com.century.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val weightLogs by viewModel.weightLogs.collectAsState(initial = emptyList())
    val pushUpTests by viewModel.pushUpTests.collectAsState(initial = emptyList())
    val completedSessions by viewModel.completedSessions.collectAsState(initial = emptyList())
    val totalReps by viewModel.totalReps.collectAsState()
    val totalCalories by viewModel.totalCalories.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val longestStreak by viewModel.longestStreak.collectAsState()

    val bmi = viewModel.calculateBmi()
    val weightChange = viewModel.calculateWeightChange(weightLogs)
    val estimatedBodyFat = viewModel.estimateBodyFat()

    val totalDays = 28
    val completionPercent = if (totalDays > 0) {
        (completedSessions.size.toFloat() / totalDays * 100f)
    } else 0f

    Scaffold(
        topBar = {
            CenturyTopBar(
                title = "PROGRESS",
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ===== BMI Card =====
            if (bmi != null && bmi > 0f) {
                BmiCard(bmi = bmi, profile = profile)
            }

            // ===== Body Stats Row =====
            if (profile != null) {
                BodyStatsRow(
                    currentWeight = profile!!.bodyWeight,
                    weightUnit = profile!!.bodyWeightUnit,
                    startingWeight = weightLogs
                        .sortedBy { it.loggedAt }
                        .firstOrNull()?.weight ?: profile!!.bodyWeight,
                    weightChange = weightChange,
                    estimatedBodyFat = estimatedBodyFat
                )
            }

            // ===== Streak & Consistency Card =====
            StreakConsistencyCard(
                currentStreak = streak,
                longestStreak = longestStreak,
                totalWorkouts = completedSessions.size,
                completionPercent = completionPercent
            )

            // ===== Total Reps & Calories Row =====
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "TOTAL REPS",
                    value = formatNumber(totalReps),
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = CenturyRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                StatCard(
                    label = "CALORIES BURNED",
                    value = formatNumber(totalCalories.toInt()),
                    modifier = Modifier.weight(1f),
                    icon = {
                        Icon(
                            Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CenturyOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }

            // ===== Push-Up Test Results =====
            if (pushUpTests.isNotEmpty()) {
                PushUpTestCard(tests = pushUpTests)
            }

            // ===== Weight Log Summary =====
            if (weightLogs.isNotEmpty()) {
                WeightLogCard(logs = weightLogs)
            }
        }
    }
}

@Composable
private fun BmiCard(
    bmi: Float,
    profile: com.century.app.data.local.entity.UserProfile?
) {
    val bmiCategory = profile?.bmiCategory ?: "Unknown"
    val bmiColor = when (bmiCategory) {
        "Underweight" -> BmiUnderweight
        "Normal" -> BmiNormal
        "Overweight" -> BmiOverweight
        "Obese" -> BmiObese
        else -> TextSecondary
    }

    CenturyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BMI",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format("%.1f", bmi),
                    style = MaterialTheme.typography.displayMedium,
                    color = bmiColor,
                    fontWeight = FontWeight.Black
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bmiColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = bmiCategory.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = bmiColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BodyStatsRow(
    currentWeight: Float,
    weightUnit: String,
    startingWeight: Float,
    weightChange: Float?,
    estimatedBodyFat: Float?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "CURRENT",
            value = "${String.format("%.1f", currentWeight)} $weightUnit",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = "START",
            value = "${String.format("%.1f", startingWeight)} $weightUnit",
            modifier = Modifier.weight(1f)
        )
        if (weightChange != null) {
            val changePrefix = if (weightChange >= 0) "+" else ""
            val changeColor = if (weightChange <= 0) CenturyGreen else CenturyOrange
            StatCard(
                label = "CHANGE",
                value = "$changePrefix${String.format("%.1f", weightChange)}",
                modifier = Modifier.weight(1f)
            )
        } else if (estimatedBodyFat != null) {
            StatCard(
                label = "EST. BF%",
                value = "${String.format("%.1f", estimatedBodyFat)}%",
                modifier = Modifier.weight(1f)
            )
        } else {
            StatCard(
                label = "CHANGE",
                value = "--",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StreakConsistencyCard(
    currentStreak: Int,
    longestStreak: Int,
    totalWorkouts: Int,
    completionPercent: Float
) {
    CenturyCard {
        Text(
            text = "STREAK & CONSISTENCY",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StreakStatItem(
                label = "CURRENT\nSTREAK",
                value = "$currentStreak",
                modifier = Modifier.weight(1f)
            )
            StreakStatItem(
                label = "LONGEST\nSTREAK",
                value = "$longestStreak",
                modifier = Modifier.weight(1f)
            )
            StreakStatItem(
                label = "TOTAL\nWORKOUTS",
                value = "$totalWorkouts",
                modifier = Modifier.weight(1f)
            )
            StreakStatItem(
                label = "COMPLETION",
                value = "${String.format("%.0f", completionPercent)}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StreakStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            color = CenturyRed,
            fontWeight = FontWeight.Black
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary,
            lineHeight = MaterialTheme.typography.labelSmall.fontSize * 1.3f,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun PushUpTestCard(tests: List<PushUpTest>) {
    CenturyCard {
        Text(
            text = "PUSH-UP TEST RESULTS",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        val sortedTests = tests.sortedBy { it.weekNumber }
        sortedTests.forEach { test ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WEEK ${test.weekNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${test.maxReps} REPS",
                    style = MaterialTheme.typography.headlineSmall,
                    color = CenturyRed,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatDate(test.testedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
            }
            if (test != sortedTests.last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = 1.dp
                )
            }
        }

        // Show improvement if multiple tests
        if (sortedTests.size >= 2) {
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            val improvement = sortedTests.last().maxReps - sortedTests.first().maxReps
            val improvementPercent = if (sortedTests.first().maxReps > 0) {
                (improvement.toFloat() / sortedTests.first().maxReps * 100f)
            } else 0f
            Text(
                text = "IMPROVEMENT: +$improvement REPS (${String.format("%.0f", improvementPercent)}%)",
                style = MaterialTheme.typography.labelMedium,
                color = CenturyGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun WeightLogCard(logs: List<WeightLog>) {
    CenturyCard {
        Text(
            text = "WEIGHT LOG",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        val sortedLogs = logs.sortedByDescending { it.loggedAt }
        val displayLogs = sortedLogs.take(5) // Show last 5 entries

        displayLogs.forEach { log ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(log.loggedAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${String.format("%.1f", log.weight)} ${log.unit}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            }
            if (log != displayLogs.last()) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline,
                    thickness = 1.dp
                )
            }
        }

        if (sortedLogs.size > 5) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "+${sortedLogs.size - 5} MORE ENTRIES",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
    }
}

private fun formatNumber(value: Int): String {
    return when {
        value >= 1_000_000 -> "${String.format("%.1f", value / 1_000_000f)}M"
        value >= 10_000 -> "${String.format("%.1f", value / 1_000f)}K"
        value >= 1_000 -> String.format("%,d", value)
        else -> "$value"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.US)
    return sdf.format(Date(timestamp)).uppercase()
}
