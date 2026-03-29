package com.wakeforge.app.presentation.navigation
import androidx.navigation.NavGraph.Companion.findStartDestination

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import com.wakeforge.app.presentation.alarm_ringing.AlarmRingingScreen
import com.wakeforge.app.presentation.alarms.AlarmsScreen
import com.wakeforge.app.presentation.create_alarm.CreateAlarmScreen
import com.wakeforge.app.presentation.edit_alarm.EditAlarmScreen
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
        Routes.HOME,
        Routes.ALARMS,
        Routes.STATS,
        Routes.SETTINGS
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
                                // saveState = true
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
            composable(route = Routes.SPLASH) {
                SplashScreen(navController = navController)
            }

            // Onboarding
            composable(
                route = Routes.ONBOARDING,
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
            composable(route = Routes.PERMISSION_SETUP) {
                PermissionScreen(navController = navController)
            }

            // Home (bottom tab)
            composable(route = Routes.HOME) {
                HomeScreen(navController = navController)
            }

            // Alarms (bottom tab)
            composable(route = Routes.ALARMS) {
                AlarmsScreen(navController = navController)
            }

            // Create Alarm
            composable(route = Routes.CREATE_ALARM) {
                CreateAlarmScreen(navController = navController)
            }

            // Edit Alarm
            composable(
                route = Routes.EDIT_ALARM,
                arguments = listOf(
                    navArgument("alarmId") { type = NavType.StringType }
                )
            ) {
                EditAlarmScreen(navController = navController)
            }

            // Alarm Ringing (fullscreen, no bottom bar)
            composable(
                route = Routes.ALARM_RINGING,
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
                route = Routes.MISSION_CHALLENGE,
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
                route = Routes.WAKE_SUCCESS,
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
            composable(route = Routes.STATS) {
                StatsScreen(navController = navController)
            }

            // Premium
            composable(route = Routes.PREMIUM) {
                PremiumScreen(navController = navController)
            }

            // Settings (bottom tab)
            composable(route = Routes.SETTINGS) {
                SettingsScreen(navController = navController)
            }
        }
    }
}



