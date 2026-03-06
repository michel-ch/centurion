package com.century.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.century.app.ui.home.HomeScreen
import com.century.app.ui.home.HomeViewModel
import com.century.app.ui.onboarding.OnboardingScreen
import com.century.app.ui.onboarding.OnboardingViewModel
import com.century.app.ui.workout.WorkoutScreen
import com.century.app.ui.workout.WorkoutViewModel
import com.century.app.ui.program.ProgramScreen
import com.century.app.ui.program.ProgramViewModel
import com.century.app.ui.progress.ProgressScreen
import com.century.app.ui.progress.ProgressViewModel
import com.century.app.ui.weightlog.WeightLogScreen
import com.century.app.ui.weightlog.WeightLogViewModel
import com.century.app.ui.settings.SettingsScreen
import com.century.app.ui.settings.SettingsViewModel
import com.century.app.ui.nutrition.NutritionScreen
import com.century.app.ui.nutrition.NutritionViewModel

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Workout : Screen("workout/{week}/{day}") {
        fun createRoute(week: Int, day: Int) = "workout/$week/$day"
    }
    data object Program : Screen("program")
    data object Progress : Screen("progress")
    data object Nutrition : Screen("nutrition")
    data object WeightLog : Screen("weight_log")
    data object Settings : Screen("settings")
}

@Composable
fun CenturyNavHost() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val hasProfile by homeViewModel.hasProfile.collectAsState()

    val startDestination = if (hasProfile == true) Screen.Home.route else Screen.Onboarding.route

    if (hasProfile == null) return // loading

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel()
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onStartWorkout = { week, day ->
                    navController.navigate(Screen.Workout.createRoute(week, day))
                },
                onNavigateToProgram = { navController.navigate(Screen.Program.route) },
                onNavigateToProgress = { navController.navigate(Screen.Progress.route) },
                onNavigateToNutrition = { navController.navigate(Screen.Nutrition.route) },
                onNavigateToWeightLog = { navController.navigate(Screen.WeightLog.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(
            Screen.Workout.route,
            arguments = listOf(
                navArgument("week") { type = NavType.IntType },
                navArgument("day") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val week = backStackEntry.arguments?.getInt("week") ?: 1
            val day = backStackEntry.arguments?.getInt("day") ?: 1
            val viewModel: WorkoutViewModel = hiltViewModel()
            LaunchedEffect(week, day) { viewModel.loadWorkout(week, day) }
            WorkoutScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onComplete = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Program.route) {
            val viewModel: ProgramViewModel = hiltViewModel()
            ProgramScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onDayClick = { week, day ->
                    navController.navigate(Screen.Workout.createRoute(week, day))
                }
            )
        }

        composable(Screen.Progress.route) {
            val viewModel: ProgressViewModel = hiltViewModel()
            ProgressScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Nutrition.route) {
            val viewModel: NutritionViewModel = hiltViewModel()
            NutritionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.WeightLog.route) {
            val viewModel: WeightLogViewModel = hiltViewModel()
            WeightLogScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onResetComplete = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
