import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.rememberDismissState
package com.wakeforge.app.presentation.navigation

/**
 * Sealed interface representing every screen (route) in the WakeForge navigation graph.
 *
 * Each entry exposes a [route] string compatible with `NavHost` and, where applicable,
 * a `createRoute` factory that builds parameterised navigation strings.
 */
sealed interface Route {

    val route: String

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Onboarding & Setup
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Initial splash / loading screen shown on cold start. */
    data object Splash : Route {
        override val route: String = "splash"
    }

    /** Walkthrough screens introducing WakeForge features. */
    data class Onboarding(val pageIndex: Int = 0) : Route {
        override val route: String = "onboarding?pageIndex={pageIndex}"

        companion object {
            /** Build a route targeting a specific onboarding page. */
            fun createRoute(pageIndex: Int): String = "onboarding?pageIndex=$pageIndex"
        }
    }

    /** Runtime permission request screen (notifications, alarms, battery). */
    data object PermissionSetup : Route {
        override val route: String = "permission_setup"
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Main Tabs
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Primary home / dashboard tab. */
    data object Home : Route {
        override val route: String = "home"
    }

    /** Alarm list / management tab. */
    data object Alarms : Route {
        override val route: String = "alarms"
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Alarm CRUD
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Screen for creating a brand-new alarm. */
    data object CreateAlarm : Route {
        override val route: String = "create_alarm"
    }

    /** Screen for editing an existing alarm. */
    data class EditAlarm(val alarmId: String) : Route {
        override val route: String = "edit_alarm/{alarmId}"

        companion object {
            /** Build a parameterised route for editing a specific alarm. */
            fun createRoute(alarmId: String): String = "edit_alarm/$alarmId"
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Alarm Lifecycle (Ringing / Mission / Success)
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Full-screen alarm ringing display. */
    data class AlarmRinging(val alarmId: String) : Route {
        override val route: String = "alarm_ringing/{alarmId}"

        companion object {
            fun createRoute(alarmId: String): String = "alarm_ringing/$alarmId"
        }
    }

    /** Mission challenge screen launched during an active alarm. */
    data class MissionChallenge(
        val alarmId: String = "",
        val missionType: String = "",
        val difficulty: String = "",
        val snoozeCount: Int = 0
    ) : Route {
        override val route: String =
            "mission_challenge?alarmId={alarmId}&missionType={missionType}&difficulty={difficulty}&snoozeCount={snoozeCount}"

        companion object {
            fun createRoute(
                alarmId: String,
                missionType: String,
                difficulty: String,
                snoozeCount: Int
            ): String =
                "mission_challenge?alarmId=$alarmId&missionType=$missionType&difficulty=$difficulty&snoozeCount=$snoozeCount"
        }
    }

    /** Success screen shown after a wake-up mission is completed. */
    data class WakeSuccess(
        val alarmId: String,
        val wakeRecordId: String
    ) : Route {
        override val route: String = "wake_success/{alarmId}/{wakeRecordId}"

        companion object {
            fun createRoute(alarmId: String, wakeRecordId: String): String =
                "wake_success/$alarmId/$wakeRecordId"
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Secondary Screens
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /** Statistics & analytics dashboard. */
    data object Stats : Route {
        override val route: String = "stats"
    }

    /** Premium features & subscription management. */
    data object Premium : Route {
        override val route: String = "premium"
    }

    /** Application settings screen. */
    data object Settings : Route {
        override val route: String = "settings"
    }

    /** Sound picker dialog / screen for selecting alarm tones. */
    data object SoundPicker : Route {
        override val route: String = "sound_picker"
    }
}

