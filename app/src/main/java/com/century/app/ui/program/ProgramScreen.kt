package com.century.app.ui.program

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.century.app.domain.model.ProgramDay
import com.century.app.domain.model.ProgramWeek
import com.century.app.ui.components.CenturyTopBar
import com.century.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramScreen(
    viewModel: ProgramViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onDayClick: (week: Int, day: Int) -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val completedDays by viewModel.completedDays.collectAsState()
    val weeks = viewModel.allWeeks

    val currentDay = profile?.currentDay ?: 1
    val currentWeekNumber = ((currentDay - 1) / 7) + 1
    val currentDayInWeek = ((currentDay - 1) % 7) + 1

    Scaffold(
        topBar = {
            CenturyTopBar(
                title = "PROGRAM",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Program header
            Text(
                text = "28-DAY PUSH-UP PROGRAM",
                style = MaterialTheme.typography.labelLarge,
                color = CenturyRed,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            weeks.forEach { week ->
                WeekAccordionCard(
                    week = week,
                    completedDays = completedDays,
                    currentWeekNumber = currentWeekNumber,
                    currentDayInWeek = currentDayInWeek,
                    initiallyExpanded = week.weekNumber == currentWeekNumber,
                    onDayClick = onDayClick
                )
            }
        }
    }
}

@Composable
private fun WeekAccordionCard(
    week: ProgramWeek,
    completedDays: Set<Pair<Int, Int>>,
    currentWeekNumber: Int,
    currentDayInWeek: Int,
    initiallyExpanded: Boolean,
    onDayClick: (week: Int, day: Int) -> Unit
) {
    var expanded by remember(initiallyExpanded) { mutableStateOf(initiallyExpanded) }

    val completedInWeek = week.days.count { day ->
        completedDays.contains(week.weekNumber to day.dayNumber)
    }
    val isCurrentWeek = week.weekNumber == currentWeekNumber
    val borderColor = if (isCurrentWeek) CenturyRed else MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isCurrentWeek) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            ),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {
            // Week header - clickable to expand/collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WEEK ${week.weekNumber}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isCurrentWeek) CenturyRed else MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = week.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "TARGET: ${week.pushUpTarget}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$completedInWeek/${week.days.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (completedInWeek == week.days.size) CenturyGreen else TextSecondary
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = TextSecondary
                    )
                }
            }

            // Expandable day list
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline,
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    week.days.forEach { day ->
                        val isCompleted = completedDays.contains(week.weekNumber to day.dayNumber)
                        val isToday = week.weekNumber == currentWeekNumber &&
                                day.dayNumber == currentDayInWeek

                        DayRow(
                            day = day,
                            isCompleted = isCompleted,
                            isToday = isToday,
                            onClick = { onDayClick(week.weekNumber, day.dayNumber) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayRow(
    day: ProgramDay,
    isCompleted: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when {
        isCompleted -> CenturyGreen
        isToday -> CenturyRed
        else -> TextTertiary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (isToday) CenturyRed.copy(alpha = 0.08f) else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted) CenturyGreen else Color.Transparent
                )
                .border(
                    width = 2.dp,
                    color = statusColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Completed",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
            } else {
                Text(
                    text = "${day.dayNumber}",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Day info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "DAY ${day.dayNumber}",
                style = MaterialTheme.typography.labelMedium,
                color = if (isToday) CenturyRed else MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = day.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Status label
        if (day.isRestDay) {
            Icon(
                Icons.Default.SelfImprovement,
                contentDescription = "Rest Day",
                modifier = Modifier.size(18.dp),
                tint = TextTertiary
            )
        } else {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = "Workout",
                modifier = Modifier.size(18.dp),
                tint = statusColor
            )
        }

        // Status text
        Text(
            text = when {
                isCompleted -> "DONE"
                isToday -> "TODAY"
                else -> ""
            },
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
    }
}
