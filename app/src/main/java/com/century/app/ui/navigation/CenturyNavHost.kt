package com.century.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
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
import com.century.app.ui.theme.*

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

private data class BottomNavItem(
    val label: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem("HOME",      Screen.Home.route,      Icons.Filled.Home,          Icons.Outlined.Home),
    BottomNavItem("PROGRAM",   Screen.Program.route,   Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomNavItem("PROGRESS",  Screen.Progress.route,  Icons.Filled.BarChart,      Icons.Outlined.BarChart),
    BottomNavItem("NUTRITION", Screen.Nutrition.route, Icons.Filled.LocalDining,   Icons.Outlined.LocalDining),
    BottomNavItem("WEIGHT",    Screen.WeightLog.route, Icons.Filled.MonitorWeight, Icons.Outlined.MonitorWeight),
    BottomNavItem("SETTINGS",  Screen.Settings.route,  Icons.Filled.Settings,      Icons.Outlined.Settings)
)

// Routes where the bottom nav should be hidden
private val routesWithoutBottomNav = setOf(
    Screen.Onboarding.route,
    "workout/{week}/{day}"
)

@Composable
fun CenturyNavHost() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = hiltViewModel()
    val hasProfile by homeViewModel.hasProfile.collectAsState()

    val startDestination = if (hasProfile == true) Screen.Home.route else Screen.Onboarding.route

    if (hasProfile == null) return // loading

    // Show splash only on the very first app launch (when there's no profile yet)
    var showSplash by remember { mutableStateOf(hasProfile == false) }
    if (showSplash) {
        SplashScreen(onFinished = { showSplash = false })
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != null && currentRoute !in routesWithoutBottomNav

    val selectedTabIndex = bottomNavItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    contentColor = TextSecondary,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    if (index == selectedTabIndex) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = index == selectedTabIndex,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CenturyRed,
                                selectedTextColor = CenturyRed,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = DarkSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
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
                    }
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
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        delay(1800L)
        visible = false
        delay(700L) // wait for fade-out to complete
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(700)) + scaleIn(tween(700), initialScale = 0.6f),
            exit = fadeOut(tween(700))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "STRENGTH",
                    style = MaterialTheme.typography.displayLarge,
                    color = CenturyRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "AND HONOR",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp
                )
            }
        }
    }
}