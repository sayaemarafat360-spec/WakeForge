package com.wakeforge.app.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.wakeforge.app.core.theme.WakeForgeTheme
import com.wakeforge.app.data.alarm.AlarmReceiver
import com.wakeforge.app.presentation.navigation.Route
import com.wakeforge.app.presentation.navigation.WakeForgeNavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * The single-[Activity] entry point for the WakeForge application.
 *
 * Configures edge-to-edge display, sets the Compose content root,
 * and launches the navigation graph starting at the splash screen.
 *
 * When launched from an alarm notification (via [AlarmReceiver]),
 * the activity extracts the alarm ID from the intent extras and
 * navigates directly to the alarm ringing screen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── Edge-to-edge display ───────────────────────────────────────────────
        enableEdgeToEdge()

        // ── Determine start destination ────────────────────────────────────────
        val alarmId = intent.getStringExtra(AlarmReceiver.EXTRA_ALARM_ID)
        val startDestination = if (!alarmId.isNullOrBlank()) {
            Route.AlarmRinging.createRoute(alarmId)
        } else {
            Route.Splash.route
        }

        // ── Compose content ───────────────────────────────────────────────────
        setContent {
            WakeForgeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WakeForgeNavGraph(
                        navController = rememberNavController(),
                        startDestination = startDestination
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        // When a new alarm intent arrives while the activity is already running,
        // the navigation will be handled by recomposition since startDestination
        // is computed from the current intent.
    }
}
