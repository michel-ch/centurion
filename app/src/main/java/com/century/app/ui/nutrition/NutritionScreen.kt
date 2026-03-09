package com.century.app.ui.nutrition

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.century.app.data.local.entity.UserProfile
import com.century.app.ui.components.CenturyTopBar
import com.century.app.ui.components.InfoIconButton
import com.century.app.ui.theme.*
import kotlin.math.roundToInt

// ── Nutrition calculations ────────────────────────────────────────────────────

private fun calcBmr(p: UserProfile): Float {
    val base = 10f * p.bodyWeightKg + 6.25f * p.heightCm - 5f * p.age
    return when (p.gender) {
        "Male"   -> base + 5f
        "Female" -> base - 161f
        else     -> base - 78f   // average of M/F
    }
}

private fun calcTdee(bmr: Float, fitnessLevel: String): Float = bmr * when (fitnessLevel) {
    "Beginner"  -> 1.375f   // lightly active
    "Advanced"  -> 1.725f   // very active
    else        -> 1.55f    // Intermediate – moderately active
}

private enum class Goal { CUT, MAINTAIN, BULK }

private fun deriveGoal(p: UserProfile): Goal {
    val goalKg = p.goalWeight?.let {
        if (p.bodyWeightUnit == "kg") it else it * 0.453592f
    } ?: return Goal.MAINTAIN
    return when {
        goalKg < p.bodyWeightKg - 1f -> Goal.CUT
        goalKg > p.bodyWeightKg + 1f -> Goal.BULK
        else -> Goal.MAINTAIN
    }
}

private fun targetCalories(tdee: Float, goal: Goal): Int = when (goal) {
    Goal.CUT      -> (tdee - 400f).roundToInt()
    Goal.BULK     -> (tdee + 250f).roundToInt()
    Goal.MAINTAIN -> tdee.roundToInt()
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CenturyTopBar(title = "Nutrition", onBack = onBack) }
    ) { padding ->
        val p = profile
        if (p == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CenturyRed)
            }
            return@Scaffold
        }

        val bmr      = calcBmr(p)
        val tdee     = calcTdee(bmr, p.fitnessLevel)
        val goal     = deriveGoal(p)
        val calories = targetCalories(tdee, goal)
        val proteinG = (p.bodyWeightKg * 1.8f).roundToInt()
        val fatG     = ((calories * 0.25f) / 9f).roundToInt()
        val carbG    = ((calories - proteinG * 4 - fatG * 9) / 4f).roundToInt().coerceAtLeast(0)
        val waterL   = ((p.bodyWeightKg * 0.035f) * 10).roundToInt() / 10f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CaloriesCard(
                name      = p.name,
                bmr       = bmr.roundToInt(),
                tdee      = tdee.roundToInt(),
                target    = calories,
                goal      = goal
            )
            MacrosCard(
                proteinG  = proteinG,
                carbG     = carbG,
                fatG      = fatG,
                totalCal  = calories
            )
            WaterCard(liters = waterL)
            BmiCard(profile = p)
            TipsCard(fitnessLevel = p.fitnessLevel, goal = goal)
        }
    }
}

// ── Calories card ─────────────────────────────────────────────────────────────

@Composable
private fun CaloriesCard(
    name: String,
    bmr: Int,
    tdee: Int,
    target: Int,
    goal: Goal
) {
    val (goalLabel, goalColor, goalIcon) = when (goal) {
        Goal.CUT      -> Triple("Fat Loss",  Color(0xFF42A5F5), Icons.Default.TrendingDown)
        Goal.BULK     -> Triple("Muscle Gain", Color(0xFF66BB6A), Icons.Default.TrendingUp)
        Goal.MAINTAIN -> Triple("Maintenance", TextSecondary, Icons.Default.Balance)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalFireDepartment, contentDescription = null,
                    tint = CenturyRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("DAILY CALORIES", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                InfoIconButton(
                    title = "Daily Calories",
                    body = "Your daily calorie target is calculated from your BMR and TDEE, then adjusted for your goal (cut, maintain, or bulk).\n\n• BMR – calories your body burns at complete rest\n• TDEE – total calories burned with daily activity\n• TARGET – your adjusted daily intake"
                )
                Spacer(Modifier.weight(1f))
                GoalChip(label = goalLabel, color = goalColor, icon = goalIcon)
            }

            Text(
                text = "$target",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Black,
                color = CenturyRed
            )
            Text("kcal / day", style = MaterialTheme.typography.bodySmall, color = TextTertiary)

            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CalorieDetail("BMR", "$bmr kcal", "Resting burn")
                VerticalDivider(modifier = Modifier.height(48.dp), color = DarkBorder)
                CalorieDetail("TDEE", "$tdee kcal", "Active burn")
                VerticalDivider(modifier = Modifier.height(48.dp), color = DarkBorder)
                CalorieDetail("TARGET", "$target kcal", goalLabel)
            }
        }
    }
}

@Composable
private fun GoalChip(label: String, color: Color, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun CalorieDetail(label: String, value: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(subtitle, style = MaterialTheme.typography.labelSmall,
            color = TextTertiary, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

// ── Macros card ───────────────────────────────────────────────────────────────

@Composable
private fun MacrosCard(proteinG: Int, carbG: Int, fatG: Int, totalCal: Int) {
    val proteinCal = proteinG * 4
    val carbCal    = carbG * 4
    val fatCal     = fatG * 9
    val total      = (proteinCal + carbCal + fatCal).coerceAtLeast(1).toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PieChart, contentDescription = null,
                    tint = CenturyRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("MACRONUTRIENTS", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                InfoIconButton(
                    title = "Macronutrients",
                    body = "Macros are the three main nutrients your body uses for energy:\n\n• Protein (4 kcal/g) – builds and repairs muscle. Aim for ~1.8g per kg of bodyweight.\n• Carbohydrates (4 kcal/g) – your body's primary fuel source, especially during workouts.\n• Fat (9 kcal/g) – supports hormones and joint health. Keep around 25% of total calories."
                )
            }

            // Split bar
            MacroSplitBar(
                proteinFraction = proteinCal / total,
                carbFraction    = carbCal / total,
                fatFraction     = fatCal / total
            )

            MacroRow(
                label     = "Protein",
                grams     = proteinG,
                kcal      = proteinCal,
                fraction  = proteinCal / total,
                color     = Color(0xFF42A5F5),
                note      = "≈ 1.8 g/kg bodyweight — muscle repair & growth"
            )
            MacroRow(
                label     = "Carbohydrates",
                grams     = carbG,
                kcal      = carbCal,
                fraction  = carbCal / total,
                color     = Color(0xFFFFCA28),
                note      = "Primary fuel for intense training sessions"
            )
            MacroRow(
                label     = "Fat",
                grams     = fatG,
                kcal      = fatCal,
                fraction  = fatCal / total,
                color     = Color(0xFFEF5350),
                note      = "Hormones, joints, vitamins absorption"
            )
        }
    }
}

@Composable
private fun MacroSplitBar(proteinFraction: Float, carbFraction: Float, fatFraction: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        Box(Modifier.weight(proteinFraction.coerceAtLeast(0.01f)).fillMaxHeight()
            .background(Color(0xFF42A5F5)))
        Box(Modifier.weight(carbFraction.coerceAtLeast(0.01f)).fillMaxHeight()
            .background(Color(0xFFFFCA28)))
        Box(Modifier.weight(fatFraction.coerceAtLeast(0.01f)).fillMaxHeight()
            .background(Color(0xFFEF5350)))
    }
}

@Composable
private fun MacroRow(
    label: String,
    grams: Int,
    kcal: Int,
    fraction: Float,
    color: Color,
    note: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
                Text(label, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${grams}g", style = MaterialTheme.typography.bodyMedium,
                    color = color, fontWeight = FontWeight.Bold)
                Text("$kcal kcal", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier  = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color     = color,
            trackColor = DarkBorder
        )
        Text(note, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
    }
}

// ── Water card ────────────────────────────────────────────────────────────────

@Composable
private fun WaterCard(liters: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.WaterDrop, contentDescription = null,
                tint = Color(0xFF29B6F6), modifier = Modifier.size(36.dp))
            Column {
                Text("DAILY HYDRATION", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                Text("${liters} L / day",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF29B6F6))
                Text("35 ml per kg bodyweight — add more on training days",
                    style = MaterialTheme.typography.labelSmall, color = TextTertiary)
            }
        }
    }
}

// ── BMI card ──────────────────────────────────────────────────────────────────

@Composable
private fun BmiCard(profile: UserProfile) {
    val bmiColor = when (profile.bmiCategory) {
        "Underweight" -> BmiUnderweight
        "Normal"      -> BmiNormal
        "Overweight"  -> BmiOverweight
        else          -> BmiObese
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null,
                    tint = CenturyRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("BODY MASS INDEX", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                InfoIconButton(
                    title = "BMI — Body Mass Index",
                    body = "BMI is a simple measure of body weight relative to height.\n\nFormula: weight (kg) ÷ height² (m²)\n\n• Underweight: < 18.5\n• Normal: 18.5 – 24.9\n• Overweight: 25 – 29.9\n• Obese: ≥ 30\n\nNote: BMI doesn't distinguish muscle from fat, so athletic people may score higher."
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "%.1f".format(profile.bmi),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = bmiColor
                    )
                    Text(profile.bmiCategory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = bmiColor, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    val weightStr = if (profile.bodyWeightUnit == "kg")
                        "%.1f kg".format(profile.bodyWeightKg)
                    else "%.1f lbs".format(profile.bodyWeight)
                    val heightStr = if (profile.heightUnit == "cm")
                        "%.0f cm".format(profile.heightCm)
                    else "%d'%d\"".format(profile.height.toInt(), profile.heightInches)
                    Text(weightStr, style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary)
                    Text(heightStr, style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary)
                    Text("${profile.age} yrs · ${profile.gender}",
                        style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                }
            }
            // BMI scale bar
            BmiScaleBar(bmi = profile.bmi)
        }
    }
}

@Composable
private fun BmiScaleBar(bmi: Float) {
    val fraction = ((bmi - 15f) / 25f).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            // Gradient zones
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(0.14f).fillMaxHeight().background(BmiUnderweight))
                Box(Modifier.weight(0.4f).fillMaxHeight().background(BmiNormal))
                Box(Modifier.weight(0.2f).fillMaxHeight().background(BmiOverweight))
                Box(Modifier.weight(0.26f).fillMaxHeight().background(BmiObese))
            }
            // Marker
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(3.dp)
                    .offset(x = (fraction * 1000).dp * 0f) // positional trick via padding
                    .align(Alignment.CenterStart)
                    .padding(start = (fraction * 300).coerceIn(0f, 297f).dp)
                    .background(Color.White)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("15", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
            Text("18.5", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
            Text("25", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
            Text("30", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
            Text("40", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
        }
    }
}

// ── Tips card ─────────────────────────────────────────────────────────────────

@Composable
private fun TipsCard(fitnessLevel: String, goal: Goal) {
    val tips = buildList {
        add("Eat protein within 2 hours post-workout to maximize muscle protein synthesis.")
        add("Prioritize whole foods — chicken, eggs, fish, legumes, rice, oats, vegetables.")
        when (goal) {
            Goal.CUT -> {
                add("On a cut, keep protein high to preserve lean muscle while losing fat.")
                add("Eat fiber-rich foods to stay full on fewer calories.")
            }
            Goal.BULK -> {
                add("On a bulk, add a second daily protein shake if hitting targets is difficult.")
                add("Time carbs around your workouts for better performance and recovery.")
            }
            Goal.MAINTAIN -> {
                add("Maintenance calories support performance without weight change.")
                add("Focus on diet quality — micronutrients and sleep matter as much as macros.")
            }
        }
        if (fitnessLevel == "Advanced") {
            add("Consider creatine monohydrate (3–5 g/day) for strength and endurance gains.")
        }
        add("Limit processed sugar and alcohol — they spike calories with no nutritional value.")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, contentDescription = null,
                    tint = Color(0xFFFFCA28), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("NUTRITION TIPS", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
            }
            tips.forEach { tip ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("•", color = CenturyRed, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium)
                    Text(tip, style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
