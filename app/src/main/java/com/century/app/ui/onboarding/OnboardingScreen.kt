package com.century.app.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.century.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveError by viewModel.saveError.collectAsState()

    val isStepValid = remember(state, currentStep) { viewModel.isCurrentStepValid() }
    val totalSteps = OnboardingViewModel.TOTAL_STEPS

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column {
                // Progress indicator
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / totalSteps },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = CenturyRed,
                    trackColor = MaterialTheme.colorScheme.outline
                )
                // Step counter
                Text(
                    text = "STEP ${currentStep + 1} / $totalSteps",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    textAlign = TextAlign.End
                )
            }
        },
        bottomBar = {
            OnboardingBottomBar(
                currentStep = currentStep,
                totalSteps = totalSteps,
                isStepValid = isStepValid,
                isSaving = isSaving,
                onBack = { viewModel.prevStep() },
                onNext = { viewModel.nextStep() },
                onFinish = { viewModel.saveProfile(onComplete) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(300))
                    } else {
                        slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(300)
                        ) + fadeIn(tween(300)) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(300)
                                ) + fadeOut(tween(300))
                    }
                },
                label = "onboarding_step"
            ) { step ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    when (step) {
                        0 -> NameStep(state, viewModel)
                        1 -> BodyWeightStep(state, viewModel)
                        2 -> HeightStep(state, viewModel)
                        3 -> AgeStep(state, viewModel)
                        4 -> GenderStep(state, viewModel)
                        5 -> FitnessLevelStep(state, viewModel)
                        6 -> MaxPushUpsStep(state, viewModel)
                        7 -> GoalWeightStep(state, viewModel)
                        8 -> ReminderTimeStep(state, viewModel)
                        9 -> SummaryStep(state)
                    }
                }
            }

            // Error snackbar
            if (saveError != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("DISMISS", color = CenturyRed)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    Text(saveError ?: "")
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Bottom navigation bar
// ────────────────────────────────────────────────────────────────────

@Composable
private fun OnboardingBottomBar(
    currentStep: Int,
    totalSteps: Int,
    isStepValid: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            if (currentStep > 0) {
                OutlinedButton(
                    onClick = onBack,
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("BACK", style = MaterialTheme.typography.labelLarge)
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            // Next / Finish button
            if (currentStep < totalSteps - 1) {
                Button(
                    onClick = onNext,
                    enabled = isStepValid,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CenturyRed,
                        contentColor = Color.White,
                        disabledContainerColor = CenturyRed.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text("NEXT", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Button(
                    onClick = onFinish,
                    enabled = isStepValid && !isSaving,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CenturyRed,
                        contentColor = Color.White,
                        disabledContainerColor = CenturyRed.copy(alpha = 0.3f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (isSaving) "SAVING..." else "FINISH",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (!isSaving) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Finish",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Shared helpers
// ────────────────────────────────────────────────────────────────────

@Composable
private fun StepTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun StepSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
    )
}

@Composable
private fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffix: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true
) {
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelMedium
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() }
        ),
        suffix = if (suffix != null) {
            { Text(suffix, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        } else null,
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        shape = RoundedCornerShape(4.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CenturyRed,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            cursorColor = CenturyRed,
            focusedLabelColor = CenturyRed
        )
    )
}

@Composable
private fun UnitToggle(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .clip(
                        when (option) {
                            options.first() -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                            options.last() -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
                    .background(if (isSelected) CenturyRed else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) CenturyRed else MaterialTheme.colorScheme.outline,
                        shape = when (option) {
                            options.first() -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                            options.last() -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                            else -> RoundedCornerShape(0.dp)
                        }
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SelectableChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) CenturyRed else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (isSelected) CenturyRed else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ────────────────────────────────────────────────────────────────────
// Step 0 - Name
// ────────────────────────────────────────────────────────────────────

@Composable
private fun NameStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    StepTitle("What's your name?")
    StepSubtitle("We'll use this to personalize your experience.")
    OnboardingTextField(
        value = state.name,
        onValueChange = { viewModel.updateName(it) },
        label = "Name",
        isError = state.name.isNotEmpty() && state.name.isBlank(),
        errorMessage = "Name cannot be empty"
    )
}

// ────────────────────────────────────────────────────────────────────
// Step 1 - Body Weight
// ────────────────────────────────────────────────────────────────────

@Composable
private fun BodyWeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    val weightVal = state.bodyWeight.toFloatOrNull()
    val showError = state.bodyWeight.isNotEmpty() && (weightVal == null || weightVal <= 0f)

    StepTitle("Body Weight")
    StepSubtitle("Enter your current body weight.")

    UnitToggle(
        options = listOf("kg", "lbs"),
        selected = state.bodyWeightUnit,
        onSelect = { viewModel.updateBodyWeightUnit(it) }
    )

    Spacer(Modifier.height(16.dp))

    OnboardingTextField(
        value = state.bodyWeight,
        onValueChange = { viewModel.updateBodyWeight(it) },
        label = "Weight",
        keyboardType = KeyboardType.Decimal,
        suffix = state.bodyWeightUnit,
        isError = showError,
        errorMessage = "Enter a valid weight"
    )
}

// ────────────────────────────────────────────────────────────────────
// Step 2 - Height
// ────────────────────────────────────────────────────────────────────

@Composable
private fun HeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    StepTitle("Height")
    StepSubtitle("How tall are you?")

    UnitToggle(
        options = listOf("cm", "ft"),
        selected = state.heightUnit,
        onSelect = { viewModel.updateHeightUnit(it) }
    )

    Spacer(Modifier.height(16.dp))

    if (state.heightUnit == "cm") {
        val heightVal = state.height.toFloatOrNull()
        val showError = state.height.isNotEmpty() && (heightVal == null || heightVal <= 0f)

        OnboardingTextField(
            value = state.height,
            onValueChange = { viewModel.updateHeight(it) },
            label = "Height",
            keyboardType = KeyboardType.Decimal,
            suffix = "cm",
            isError = showError,
            errorMessage = "Enter a valid height"
        )
    } else {
        val feetVal = state.height.toFloatOrNull()
        val inchesVal = state.heightInches.toIntOrNull()
        val showFeetError = state.height.isNotEmpty() && (feetVal == null || feetVal < 0f)
        val showInchesError = state.heightInches.isNotEmpty() && (inchesVal == null || inchesVal !in 0..11)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OnboardingTextField(
                value = state.height,
                onValueChange = { viewModel.updateHeight(it) },
                label = "Feet",
                keyboardType = KeyboardType.Number,
                suffix = "ft",
                isError = showFeetError,
                errorMessage = "Invalid",
                modifier = Modifier.weight(1f)
            )
            OnboardingTextField(
                value = state.heightInches,
                onValueChange = { viewModel.updateHeightInches(it) },
                label = "Inches",
                keyboardType = KeyboardType.Number,
                suffix = "in",
                isError = showInchesError,
                errorMessage = "0-11",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Step 3 - Age
// ────────────────────────────────────────────────────────────────────

@Composable
private fun AgeStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    val ageVal = state.age.toIntOrNull()
    val showError = state.age.isNotEmpty() && (ageVal == null || ageVal !in 5..120)

    StepTitle("How old are you?")
    StepSubtitle("Your age helps us tailor your training program.")

    OnboardingTextField(
        value = state.age,
        onValueChange = { viewModel.updateAge(it) },
        label = "Age",
        keyboardType = KeyboardType.Number,
        suffix = "years",
        isError = showError,
        errorMessage = "Enter an age between 5 and 120"
    )
}

// ────────────────────────────────────────────────────────────────────
// Step 4 - Gender
// ────────────────────────────────────────────────────────────────────

@Composable
private fun GenderStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    StepTitle("Gender")
    StepSubtitle("This helps calculate calories and adjust exercises.")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf("Male", "Female").forEach { gender ->
            SelectableChip(
                label = gender,
                isSelected = state.gender == gender,
                onClick = { viewModel.updateGender(gender) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Step 5 - Fitness Level
// ────────────────────────────────────────────────────────────────────

@Composable
private fun FitnessLevelStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    StepTitle("Fitness Level")
    StepSubtitle("Choose the level that best describes you right now.")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        data class LevelInfo(val label: String, val description: String)

        val levels = listOf(
            LevelInfo("Beginner", "New to push-ups or can do fewer than 15"),
            LevelInfo("Intermediate", "Can do 15-40 push-ups comfortably"),
            LevelInfo("Advanced", "Can do 40+ push-ups with good form")
        )

        levels.forEach { level ->
            val isSelected = state.fitnessLevel == level.label
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) CenturyRed.copy(alpha = 0.1f) else Color.Transparent)
                    .border(
                        width = 2.dp,
                        color = if (isSelected) CenturyRed else MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { viewModel.updateFitnessLevel(level.label) }
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        text = level.label.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) CenturyRed else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = level.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ────────────────────────────────────────────────────────────────────
// Step 6 - Current Max Push-Ups
// ────────────────────────────────────────────────────────────────────

@Composable
private fun MaxPushUpsStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    val pushVal = state.currentMaxPushUps.toIntOrNull()
    val showError = state.currentMaxPushUps.isNotEmpty() && (pushVal == null || pushVal < 0)

    StepTitle("Max Push-Ups")
    StepSubtitle("How many push-ups can you do in one set right now?")

    OnboardingTextField(
        value = state.currentMaxPushUps,
        onValueChange = { viewModel.updateCurrentMaxPushUps(it) },
        label = "Push-ups",
        keyboardType = KeyboardType.Number,
        suffix = "reps",
        isError = showError,
        errorMessage = "Enter a valid number"
    )
}

// ────────────────────────────────────────────────────────────────────
// Step 7 - Goal Weight
// ────────────────────────────────────────────────────────────────────

@Composable
private fun GoalWeightStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    val goalVal = state.goalWeight.toFloatOrNull()
    val showError = state.goalWeight.isNotEmpty() && (goalVal == null || goalVal <= 0f)

    StepTitle("Goal Weight")
    StepSubtitle("Optional. Set a target body weight to track progress.")

    OnboardingTextField(
        value = state.goalWeight,
        onValueChange = { viewModel.updateGoalWeight(it) },
        label = "Goal weight",
        keyboardType = KeyboardType.Decimal,
        suffix = state.bodyWeightUnit,
        isError = showError,
        errorMessage = "Enter a valid weight or leave blank"
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = "Leave blank to skip",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// ────────────────────────────────────────────────────────────────────
// Step 8 - Reminder Time
// ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeStep(state: OnboardingState, viewModel: OnboardingViewModel) {
    val parts = state.reminderTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 7
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    // Sync picker changes back to view model
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        val h = timePickerState.hour.toString().padStart(2, '0')
        val m = timePickerState.minute.toString().padStart(2, '0')
        viewModel.updateReminderTime("$h:$m")
    }

    StepTitle("Daily Reminder")
    StepSubtitle("When should we remind you to train?")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        TimePicker(
            state = timePickerState,
            colors = TimePickerDefaults.colors(
                clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
                selectorColor = CenturyRed,
                containerColor = MaterialTheme.colorScheme.surface,
                clockDialSelectedContentColor = Color.White,
                clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
                timeSelectorSelectedContainerColor = CenturyRed,
                timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                timeSelectorSelectedContentColor = Color.White,
                timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

    Spacer(Modifier.height(12.dp))

    Text(
        text = "Reminder set for ${state.reminderTime}",
        style = MaterialTheme.typography.bodyMedium,
        color = CenturyRed,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

// ────────────────────────────────────────────────────────────────────
// Step 9 - Summary
// ────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryStep(state: OnboardingState) {
    StepTitle("All Set!")
    StepSubtitle("Review your profile before we begin.")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            Text(
                text = state.name.uppercase(),
                style = MaterialTheme.typography.headlineMedium,
                color = CenturyRed
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outline
            )

            SummaryRow(label = "WEIGHT", value = "${state.bodyWeight} ${state.bodyWeightUnit}")

            val heightDisplay = if (state.heightUnit == "cm") {
                "${state.height} cm"
            } else {
                "${state.height} ft ${state.heightInches} in"
            }
            SummaryRow(label = "HEIGHT", value = heightDisplay)

            SummaryRow(label = "AGE", value = "${state.age} years")

            SummaryRow(label = "GENDER", value = state.gender)

            SummaryRow(label = "FITNESS LEVEL", value = state.fitnessLevel)

            SummaryRow(label = "MAX PUSH-UPS", value = "${state.currentMaxPushUps} reps")

            if (state.goalWeight.isNotBlank()) {
                SummaryRow(label = "GOAL WEIGHT", value = "${state.goalWeight} ${state.bodyWeightUnit}")
            }

            SummaryRow(label = "REMINDER", value = state.reminderTime)
        }
    }

    Spacer(Modifier.height(16.dp))

    Text(
        text = "Tap FINISH to save your profile and start training.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
    }
}
