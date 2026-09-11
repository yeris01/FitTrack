package com.fittrack.app.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fittrack.app.billing.BillingManager
import com.fittrack.app.data.FitTrackRepository
import com.fittrack.app.ui.components.BannerAd
import com.fittrack.app.ui.screens.*
import com.fittrack.app.ui.viewmodel.*
import java.net.URLDecoder
import java.net.URLEncoder

private object Routes {
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val ROUTINES = "routines"
    const val PROGRESS = "progress"
    const val CALENDAR = "calendar"
    const val GOALS = "goals"
    const val CREATE_ROUTINE = "create_routine"
    const val HISTORY = "history"
    const val PREMIUM = "premium"
    const val AI_ROUTINE = "ai_routine"
    const val ACTIVE_WORKOUT = "active_workout/{routineId}/{routineName}"
}

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.ROUTINES, "Rutinas", Icons.Default.FitnessCenter),
    BottomTab(Routes.PROGRESS, "Progreso", Icons.Default.ShowChart),
    BottomTab(Routes.CALENDAR, "Calendario", Icons.Default.CalendarMonth),
    BottomTab(Routes.GOALS, "Objetivo", Icons.Default.Flag)
)

@Composable
fun FitTrackNavHost(
    repository: FitTrackRepository,
    billingManager: BillingManager,
    authManager: com.fittrack.app.auth.AuthManager
) {
    val navController = rememberNavController()
    val currentUser by authManager.currentUser.collectAsState()
    val currentUserId by authManager.currentUserId.collectAsState()
    val startDestination = if (currentUser == null) Routes.LOGIN else Routes.ROUTINES

    val factory = ViewModelFactory(repository, currentUserId)

    val routineViewModel: RoutineViewModel = viewModel(factory = factory, key = currentUserId.toString())
    val progressViewModel: ProgressViewModel = viewModel(factory = factory, key = currentUserId.toString())
    val goalViewModel: GoalViewModel = viewModel(factory = factory, key = currentUserId.toString())
    val premiumViewModel: PremiumViewModel = viewModel(factory = factory, key = currentUserId.toString())
    val aiRoutineViewModel: AIRoutineViewModel = viewModel(factory = factory, key = currentUserId.toString())

    val isPremium by premiumViewModel.isPremium.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomTabs.any { it.route == currentRoute }

    // Reaccionar al cambio de usuario (ej: al cerrar sesión)
    androidx.compose.runtime.LaunchedEffect(currentUser) {
        if (currentUser == null && currentRoute != Routes.LOGIN && currentRoute != Routes.SIGN_UP && currentRoute != Routes.WELCOME) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { outerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen de fondo global para toda la app
            Image(
                painter = painterResource(id = com.fittrack.app.R.drawable.app_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(outerPadding)) {
                Box(modifier = Modifier.weight(1f)) {
                    NavHost(navController = navController, startDestination = Routes.WELCOME) {

                        composable(Routes.WELCOME) {
                            WelcomeScreen(
                                onFinished = {
                                    navController.navigate(startDestination) {
                                        popUpTo(Routes.WELCOME) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.LOGIN) {
                            LoginScreen(
                                authManager = authManager,
                                onNavigateToSignUp = { navController.navigate(Routes.SIGN_UP) },
                                onLoginSuccess = {
                                    navController.navigate(Routes.ROUTINES) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.SIGN_UP) {
                            SignUpScreen(
                                authManager = authManager,
                                onBack = { navController.popBackStack() },
                                onSignUpSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Routes.ROUTINES) {
                            RoutinesScreen(
                                viewModel = routineViewModel,
                                isPremium = isPremium,
                                onCreateRoutine = { navController.navigate(Routes.CREATE_ROUTINE) },
                                onStartRoutine = { routine ->
                                    val encodedName = URLEncoder.encode(routine.name, "UTF-8")
                                    navController.navigate("active_workout/${routine.id}/$encodedName")
                                },
                                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                                onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                                onOpenAIRoutine = { navController.navigate(Routes.AI_ROUTINE) },
                                onSignOut = { authManager.signOut() }
                            )
                        }

                        composable(Routes.PROGRESS) {
                            ProgressScreen(
                                viewModel = progressViewModel,
                                isPremium = isPremium,
                                onUpgradeClick = { navController.navigate(Routes.PREMIUM) }
                            )
                        }

                        composable(Routes.CALENDAR) {
                        CalendarScreen(
                            repository = repository,
                            userId = currentUserId,
                            isPremium = isPremium,
                            onOpenHistory = { navController.navigate(Routes.HISTORY) },
                            onOpenPremium = { navController.navigate(Routes.PREMIUM) },
                            onOpenAIRoutine = { navController.navigate(Routes.AI_ROUTINE) },
                            onSignOut = { authManager.signOut() },
                            onBack = { navController.popBackStack() }
                        )
                    }

                        composable(Routes.GOALS) {
                            GoalsScreen(viewModel = goalViewModel)
                        }

                        composable(Routes.CREATE_ROUTINE) {
                            CreateRoutineScreen(
                                viewModel = routineViewModel,
                                onBack = { navController.popBackStack() },
                                onRoutineCreated = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.HISTORY) {
                            HistoryScreen(repository = repository, userId = currentUserId, onBack = { navController.popBackStack() })
                        }

                        composable(Routes.PREMIUM) {
                            PremiumScreen(
                                billingManager = billingManager,
                                isPremium = isPremium,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(Routes.AI_ROUTINE) {
                            AIRoutineScreen(
                                viewModel = aiRoutineViewModel,
                                isPremium = isPremium,
                                onBack = { navController.popBackStack() },
                                onUpgradeClick = { navController.navigate(Routes.PREMIUM) },
                                onDone = {
                                    navController.navigate(Routes.ROUTINES) {
                                        popUpTo(Routes.ROUTINES) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = Routes.ACTIVE_WORKOUT,
                            arguments = listOf(
                                navArgument("routineId") { type = NavType.LongType },
                                navArgument("routineName") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val routineId = backStackEntry.arguments?.getLong("routineId") ?: 0L
                            val encodedName = backStackEntry.arguments?.getString("routineName") ?: ""
                            val routineName = URLDecoder.decode(encodedName, "UTF-8")
                            val workoutViewModel: WorkoutViewModel = viewModel(factory = factory, key = currentUserId.toString())

                            ActiveWorkoutScreen(
                                routineId = routineId,
                                routineName = routineName,
                                routineViewModel = routineViewModel,
                                workoutViewModel = workoutViewModel,
                                onFinished = { navController.popBackStack() }
                            )
                        }
                    }
                }

                // Banner de anuncios solo para usuarios free, anclado abajo de todo el contenido.
                if (!isPremium) {
                    BannerAd()
                }
            }
        }
    }
}
