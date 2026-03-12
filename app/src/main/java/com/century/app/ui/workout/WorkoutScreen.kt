package com.century.app.ui.workout

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.century.app.ui.components.*
import com.century.app.ui.theme.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            if (!uiState.isCompleted) {
                CenturyTopBar(
                    title = "WEEK ${uiState.weekNumber} / DAY ${uiState.dayNumber}",
                    onBack = onBack
                )
            }
        },
        bottomBar = {
            // Rest timer bar at bottom when resting
            AnimatedVisibility(
                visible = uiState.isResting,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                RestTimerBar(
                    remainingSeconds = uiState.restSeconds,
                    totalSeconds = uiState.totalRestSeconds,
                    onSkip = { viewModel.skipRest() }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CenturyRed)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "LOADING WORKOUT...",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = CenturyRed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(onClick = onBack) {
                            Text("GO BACK", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            uiState.isCompleted -> {
                WorkoutCompletionScreen(
                    uiState = uiState,
                    onDone = onComplete,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            else -> {
                WorkoutContent(
                    uiState = uiState,
                    onCompleteSet = { viewModel.completeSet(it) },
                    onCompleteExercise = { viewModel.completeExercise(it) },
                    onUncompleteExercise = { viewModel.uncompleteExercise(it) },
                    onCompleteMaxTest = { index, count -> viewModel.completeMaxTest(index, count) },
                    onStartTimer = { viewModel.startTimedExercise(it) },
                    onPauseTimer = { viewModel.pauseTimedExercise(it) },
                    onResetTimer = { viewModel.resetTimedExercise(it) },
                    onFinishWorkout = { viewModel.finishWorkout() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun WorkoutContent(
    uiState: WorkoutUiState,
    onCompleteSet: (Int) -> Unit,
    onCompleteExercise: (Int) -> Unit,
    onUncompleteExercise: (Int) -> Unit,
    onCompleteMaxTest: (Int, Int) -> Unit,
    onStartTimer: (Int) -> Unit,
    onPauseTimer: (Int) -> Unit,
    onResetTimer: (Int) -> Unit,
    onFinishWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = uiState.exerciseStates.count { it.isCompleted }
    val totalCount = uiState.exercises.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Day label header
        item {
            Text(
                text = uiState.dayLabel.uppercase(),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Progress bar
        item {
            WorkoutProgressBar(
                completedCount = completedCount,
                totalCount = totalCount,
                progress = progress
            )
        }

        // Exercise cards
        itemsIndexed(uiState.exercises) { index, exercise ->
            val exerciseState = uiState.exerciseStates.getOrNull(index)
                ?: ExerciseState(exerciseIndex = index)
            val timedState = uiState.timedExerciseStates[index]

            ExerciseCard(
                exercise = exercise,
                exerciseState = exerciseState,
                timedState = timedState,
                onSetTap = { onCompleteSet(index) },
                onMaxTestComplete = { count -> onCompleteMaxTest(index, count) },
                onCompleteToggle = { isChecked ->
                    if (isChecked) onCompleteExercise(index) else onUncompleteExercise(index)
                },
                onStartTimer = { onStartTimer(index) },
                onPauseTimer = { onPauseTimer(index) },
                onResetTimer = { onResetTimer(index) }
            )
        }

        // Finish button
        item {
            val allDone = uiState.exerciseStates.all { it.isCompleted }
            Button(
                onClick = onFinishWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = allDone,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (allDone) CenturyRed else DarkBorder,
                    contentColor = Color.White,
                    disabledContainerColor = DarkBorder,
                    disabledContentColor = TextTertiary
                )
            ) {
                Text(
                    text = if (allDone) "FINISH WORKOUT" else "COMPLETE ALL EXERCISES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WorkoutProgressBar(
    completedCount: Int,
    totalCount: Int,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "workout_progress"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PROGRESS",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            Text(
                text = "$completedCount / $totalCount",
                style = MaterialTheme.typography.labelLarge,
                color = CenturyRed,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = CenturyRed,
            trackColor = DarkBorder
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: com.century.app.domain.model.ProgramExercise,
    exerciseState: ExerciseState,
    timedState: TimedExerciseState?,
    onSetTap: () -> Unit,
    onMaxTestComplete: (Int) -> Unit,
    onCompleteToggle: (Boolean) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResetTimer: () -> Unit
) {
    val cardAlpha by animateFloatAsState(
        targetValue = if (exerciseState.isCompleted) 0.6f else 1f,
        animationSpec = tween(300),
        label = "card_alpha"
    )
    var showMaxTestDialog by remember { mutableStateOf(false) }

    if (showMaxTestDialog) {
        MaxTestInputDialog(
            exerciseName = exercise.name,
            onConfirm = { count ->
                showMaxTestDialog = false
                onMaxTestComplete(count)
            },
            onDismiss = { showMaxTestDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (exerciseState.isCompleted) {
                DarkSurface
            } else {
                DarkSurfaceVariant
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                colors = if (exerciseState.isCompleted) {
                    listOf(CenturyGreen.copy(alpha = 0.5f), CenturyGreen.copy(alpha = 0.5f))
                } else {
                    listOf(DarkBorder, DarkBorder)
                }
            )
        )
    ) {
        Column(modifier = Modifier.padding(0.dp)) {
            // Exercise image
            ExerciseImageCard(
                illustrationId = exercise.illustrationId,
                exerciseName = exercise.name
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Exercise name
                Text(
                    text = exercise.name.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Sets x Reps in red + REST tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sets x Reps
                    Text(
                        text = "${exercise.sets} x ${exercise.reps}".uppercase(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = CenturyRed,
                        fontWeight = FontWeight.Bold
                    )

                    // Rest tag
                    if (exercise.restBetweenSetsSec > 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = DarkBorder
                        ) {
                            Text(
                                text = "REST: ${exercise.restBetweenSetsSec}S",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Form note
                if (exercise.formNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = exercise.formNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timed exercise: inline countdown timer
                if (exercise.isTimed && timedState != null) {
                    TimedExerciseTimer(
                        timedState = timedState,
                        onStart = onStartTimer,
                        onPause = onPauseTimer,
                        onReset = onResetTimer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Set tracker circles (not shown for timed exercises with 1 set)
                if (exercise.sets > 1 || !exercise.isTimed) {
                    Text(
                        text = "SETS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SetTrackerRow(
                        totalSets = exercise.sets,
                        completedSets = exerciseState.setsCompleted,
                        onSetTap = {
                            if (exercise.isMaxTest) {
                                showMaxTestDialog = true
                            } else {
                                onSetTap()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Complete exercise checkbox
                HorizontalDivider(color = DarkBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (exercise.isMaxTest && !exerciseState.isCompleted) {
                                showMaxTestDialog = true
                            } else {
                                onCompleteToggle(!exerciseState.isCompleted)
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = exerciseState.isCompleted,
                        onCheckedChange = { isChecked ->
                            if (exercise.isMaxTest && isChecked && !exerciseState.isCompleted) {
                                showMaxTestDialog = true
                            } else {
                                onCompleteToggle(isChecked)
                            }
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CenturyGreen,
                            uncheckedColor = TextTertiary,
                            checkmarkColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (exerciseState.isCompleted) "COMPLETED" else "MARK COMPLETE",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (exerciseState.isCompleted) CenturyGreen else TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimedExerciseTimer(
    timedState: TimedExerciseState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    val progress = if (timedState.totalSeconds > 0) {
        timedState.remainingSeconds.toFloat() / timedState.totalSeconds
    } else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(4.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(4.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TIMER",
            style = MaterialTheme.typography.labelMedium,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Large time display
        val minutes = timedState.remainingSeconds / 60
        val seconds = timedState.remainingSeconds % 60
        val timeText = if (minutes > 0) {
            String.format("%d:%02d", minutes, seconds)
        } else {
            "${seconds}s"
        }

        Text(
            text = timeText,
            style = MaterialTheme.typography.displayLarge,
            color = when {
                timedState.isFinished -> CenturyGreen
                timedState.isRunning -> CenturyRed
                else -> MaterialTheme.colorScheme.onBackground
            },
            fontWeight = FontWeight.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = when {
                timedState.isFinished -> CenturyGreen
                else -> CenturyRed
            },
            trackColor = DarkBorder
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (timedState.isFinished) {
                // Reset only
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Icon(
                        Icons.Default.Replay,
                        contentDescription = "Reset",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET", style = MaterialTheme.typography.labelMedium)
                }
            } else if (timedState.isRunning) {
                // Pause
                OutlinedButton(
                    onClick = onPause,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CenturyYellow)
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Pause",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PAUSE", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                // Start
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CenturyRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("START", style = MaterialTheme.typography.labelMedium)
                }

                // Reset (visible when paused and not at start)
                if (timedState.remainingSeconds != timedState.totalSeconds) {
                    OutlinedButton(
                        onClick = onReset,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = "Reset",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RESET", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ==================== MAX TEST INPUT DIALOG ====================

@Composable
private fun MaxTestInputDialog(
    exerciseName: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = exerciseName.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column {
                Text(
                    text = "How many did you complete?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        isError = false
                    },
                    label = { Text("Count") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text("Enter a valid number", color = CenturyRed) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CenturyRed
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val count = input.trim().toIntOrNull()
                    if (count != null && count > 0) {
                        onConfirm(count)
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = CenturyRed)
            ) {
                Text("CONFIRM", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}

// ==================== COMPLETION CELEBRATION SCREEN ====================

@Composable
private fun WorkoutCompletionScreen(
    uiState: WorkoutUiState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val durationMinutes = (uiState.totalDurationMs / 60000).toInt()
    val durationSeconds = ((uiState.totalDurationMs % 60000) / 1000).toInt()
    val durationText = if (durationMinutes > 0) {
        "${durationMinutes}m ${durationSeconds}s"
    } else {
        "${durationSeconds}s"
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Confetti particles
        ConfettiOverlay()

        // Main content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Spacer(modifier = Modifier.height(80.dp))

                // Trophy / check icon
                val pulseAnim = rememberInfiniteTransition(label = "pulse")
                val scale by pulseAnim.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                        .background(CenturyRed.copy(alpha = 0.15f), CircleShape)
                        .border(3.dp, CenturyRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = "Workout Complete",
                        modifier = Modifier.size(56.dp),
                        tint = CenturyRed
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title
                Text(
                    text = "WORKOUT\nCOMPLETE",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    lineHeight = 42.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "WEEK ${uiState.weekNumber} / DAY ${uiState.dayNumber} - ${uiState.dayLabel}".uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Stats grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "TOTAL REPS",
                        value = "${uiState.totalReps}",
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                Icons.Default.FitnessCenter,
                                contentDescription = null,
                                tint = CenturyRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                    StatCard(
                        label = "DURATION",
                        value = durationText,
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = CenturyRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "CALORIES",
                        value = "${uiState.estimatedCalories.toInt()}",
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = CenturyOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                    StatCard(
                        label = "EXERCISES",
                        value = "${uiState.exerciseStates.count { it.isCompleted }}",
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CenturyGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Done button
                Button(
                    onClick = onDone,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CenturyRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "DONE",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 4.sp
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// ==================== CONFETTI ANIMATION ====================

private data class ConfettiParticle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color
)

@Composable
private fun ConfettiOverlay() {
    val confettiColors = listOf(
        CenturyRed,
        CenturyGreen,
        CenturyYellow,
        CenturyOrange,
        CenturyRedLight,
        Color.White
    )

    val particles = remember {
        List(40) { i ->
            ConfettiParticle(
                id = i,
                startX = Random.nextFloat(),
                startY = Random.nextFloat() * -0.5f - 0.1f,
                velocityX = (Random.nextFloat() - 0.5f) * 0.3f,
                velocityY = Random.nextFloat() * 0.4f + 0.2f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = Random.nextFloat() * 8f + 4f,
                color = confettiColors[Random.nextInt(confettiColors.size)]
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val animProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_progress"
    )

    androidx.compose.foundation.Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        particles.forEach { particle ->
            val t = (animProgress + particle.startY + 0.5f) % 1f

            val x = (particle.startX + particle.velocityX * t) * size.width
            val y = t * size.height * 1.3f
            val rotation = particle.rotation + particle.rotationSpeed * t

            val alpha = when {
                t < 0.1f -> t / 0.1f
                t > 0.8f -> (1f - t) / 0.2f
                else -> 1f
            }.coerceIn(0f, 0.8f)

            drawContext.canvas.save()
            drawContext.canvas.translate(x, y)
            drawContext.canvas.rotate(rotation)

            // Draw small rectangles as confetti
            drawRect(
                color = particle.color.copy(alpha = alpha),
                topLeft = androidx.compose.ui.geometry.Offset(
                    -particle.size / 2,
                    -particle.size / 4
                ),
                size = androidx.compose.ui.geometry.Size(particle.size, particle.size / 2)
            )

            drawContext.canvas.restore()
        }
    }
}

