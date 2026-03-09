package com.century.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.century.app.ui.components.CenturyCard
import com.century.app.ui.components.ProgressRing
import com.century.app.ui.components.StatCard
import com.century.app.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onStartWorkout: (week: Int, day: Int) -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val totalReps by viewModel.totalReps.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val weekProgress by viewModel.weekProgress.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Motto
            Text(
                text = "STRENGTH AND HONOR",
                style = MaterialTheme.typography.labelSmall,
                color = CenturyRed.copy(alpha = 0.7f),
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )

            // Greeting Header
            GreetingHeader(
                name = profile?.name ?: "Warrior",
                currentDay = profile?.currentDay ?: 1,
                profilePhotoUri = profile?.profilePhotoUri
            )

            // Current Week Card
            CurrentWeekCard(
                weekNumber = viewModel.currentWeekNumber,
                weekTitle = viewModel.currentWeek.title,
                pushUpTarget = viewModel.currentWeek.pushUpTarget,
                weekProgress = weekProgress
            )

            // Today's Workout Card
            TodayWorkoutCard(
                dayLabel = viewModel.todayWorkout.label,
                exerciseCount = viewModel.todayWorkout.exercises.size,
                isRestDay = viewModel.todayWorkout.isRestDay,
                onStartWorkout = {
                    onStartWorkout(viewModel.currentWeekNumber, viewModel.currentDayInWeek)
                }
            )

            // Quick Stats Row
            QuickStatsRow(
                totalPushUps = totalReps,
                currentStreak = currentStreak,
                completedWorkouts = completedCount
            )

            // Weekly Progress Ring
            WeeklyProgressSection(
                weekProgress = weekProgress,
                currentDayInWeek = viewModel.currentDayInWeek,
                weekNumber = viewModel.currentWeekNumber
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun GreetingHeader(
    name: String,
    currentDay: Int,
    profilePhotoUri: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Photo
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .border(2.dp, CenturyRed, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profilePhotoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePhotoUri)
                        .crossfade(300)
                        .build(),
                    contentDescription = "Profile photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(28.dp),
                    tint = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = "HEY ${name.uppercase()},",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "DAY $currentDay OF 30",
                style = MaterialTheme.typography.labelLarge,
                color = CenturyRed
            )
        }
    }
}

@Composable
private fun CurrentWeekCard(
    weekNumber: Int,
    weekTitle: String,
    pushUpTarget: String,
    weekProgress: Float
) {
    CenturyCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "WEEK $weekNumber",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = weekTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CenturyRed
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pushUpTarget,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            ProgressRing(
                progress = weekProgress,
                modifier = Modifier.size(64.dp),
                strokeWidth = 6f,
                color = CenturyRed,
                backgroundColor = DarkBorder
            ) {
                Text(
                    text = "${(weekProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = CenturyRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun TodayWorkoutCard(
    dayLabel: String,
    exerciseCount: Int,
    isRestDay: Boolean,
    onStartWorkout: () -> Unit
) {
    CenturyCard {
        Text(
            text = "TODAY'S WORKOUT",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isRestDay) "RECOVERY DAY" else "$exerciseCount EXERCISES",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onStartWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CenturyRed,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                if (isRestDay) Icons.Default.SelfImprovement else Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isRestDay) "START RECOVERY" else "START WORKOUT",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun QuickStatsRow(
    totalPushUps: Int,
    currentStreak: Int,
    completedWorkouts: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = "TOTAL PUSH-UPS",
            value = totalPushUps.toString(),
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = CenturyRed
                )
            }
        )
        StatCard(
            label = "STREAK",
            value = "${currentStreak}d",
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = CenturyOrange
                )
            }
        )
        StatCard(
            label = "COMPLETED",
            value = completedWorkouts.toString(),
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = CenturyGreen
                )
            }
        )
    }
}

@Composable
private fun WeeklyProgressSection(
    weekProgress: Float,
    currentDayInWeek: Int,
    weekNumber: Int
) {
    CenturyCard {
        Text(
            text = "WEEKLY PROGRESS",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            ProgressRing(
                progress = weekProgress,
                modifier = Modifier.size(120.dp),
                strokeWidth = 10f,
                color = CenturyRed,
                backgroundColor = DarkBorder
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DAY $currentDayInWeek",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "OF 7",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Day indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
            dayLabels.forEachIndexed { index, label ->
                val dayNum = index + 1
                val isCompleted = dayNum < currentDayInWeek
                val isCurrent = dayNum == currentDayInWeek

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> CenturyGreen
                                    isCurrent -> CenturyRed
                                    else -> DarkBorder
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isCurrent)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

