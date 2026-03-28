package com.wakeforge.app.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.wakeforge.app.presentation.alarms.AlarmsScreen
import com.wakeforge.app.presentation.create_alarm.CreateAlarmScreen
import com.wakeforge.app.presentation.edit_alarm.EditAlarmScreen
import com.wakeforge.app.presentation.alarm_ringing.AlarmRingingScreen
import com.wakeforge.app.presentation.home.HomeScreen
import com.wakeforge.app.presentation.missions.MissionChallengeScreen
import com.wakeforge.app.presentation.onboarding.OnboardingScreen
import com.wakeforge.app.presentation.permissions.PermissionScreen
import com.wakeforge.app.presentation.premium.PremiumScreen
import com.wakeforge.app.presentation.settings.SettingsScreen
import com.wakeforge.app.presentation.splash.SplashScreen
import com.wakeforge.app.presentation.stats.StatsScreen
import com.wakeforge.app.presentation.wake_success.WakeSuccessScreen

@Composable
fun WakeForgeNavGraph(
    navController: NavHostController,
    startDestination: String
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = setOf(
        Route.Home.route,
        Route.Alarms.route,
        Route.Stats.route,
        Route.Settings.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                WakeForgeBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash
            composable(route = Route.Splash.route) {
                SplashScreen(navController = navController)
            }

            // Onboarding
            composable(
                route = Route.Onboarding.route,
                arguments = listOf(
                    navArgument("pageIndex") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                )
            ) {
                OnboardingScreen(navController = navController)
            }

            // Permission Setup
            composable(route = Route.PermissionSetup.route) {
                PermissionScreen(navController = navController)
            }

            // Home (bottom tab)
            composable(route = Route.Home.route) {
                HomeScreen(navController = navController)
            }

            // Alarms (bottom tab)
            composable(route = Route.Alarms.route) {
                AlarmsScreen(navController = navController)
            }

            // Create Alarm
            composable(route = Route.CreateAlarm.route) {
                CreateAlarmScreen(navController = navController)
            }

            // Edit Alarm
            composable(
                route = Route.EditAlarm.route,
                arguments = listOf(
                    navArgument("alarmId") { type = NavType.StringType }
                )
            ) {
                EditAlarmScreen(navController = navController)
            }

            // Alarm Ringing (fullscreen, no bottom bar)
            composable(
                route = Route.AlarmRinging.route,
                arguments = listOf(
                    navArgument("alarmId") { type = NavType.StringType }
                ),
                enterTransition = { NavigationAnimations.enterFromBottom() },
                exitTransition = { NavigationAnimations.exitToBottom() },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                AlarmRingingScreen(navController = navController)
            }

            // Mission Challenge (fullscreen, no bottom bar)
            composable(
                route = Route.MissionChallenge.route,
                arguments = listOf(
                    navArgument("alarmId") { type = NavType.StringType; defaultValue = "" },
                    navArgument("missionType") { type = NavType.StringType; defaultValue = "" },
                    navArgument("difficulty") { type = NavType.StringType; defaultValue = "" },
                    navArgument("snoozeCount") { type = NavType.IntType; defaultValue = 0 }
                ),
                enterTransition = { NavigationAnimations.enterFromBottom() },
                exitTransition = { NavigationAnimations.exitToBottom() },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None }
            ) {
                MissionChallengeScreen(navController = navController)
            }

            // Wake Success
            composable(
                route = Route.WakeSuccess.route,
                arguments = listOf(
                    navArgument("alarmId") { type = NavType.StringType },
                    navArgument("wakeRecordId") { type = NavType.StringType }
                ),
                enterTransition = { NavigationAnimations.scaleIn() },
                exitTransition = { NavigationAnimations.scaleOut() }
            ) {
                WakeSuccessScreen(navController = navController)
            }

            // Stats (bottom tab)
            composable(route = Route.Stats.route) {
                StatsScreen(navController = navController)
            }

            // Premium
            composable(route = Route.Premium.route) {
                PremiumScreen(navController = navController)
            }

            // Settings (bottom tab)
            composable(route = Route.Settings.route) {
                SettingsScreen(navController = navController)
            }
        }
    }
}
