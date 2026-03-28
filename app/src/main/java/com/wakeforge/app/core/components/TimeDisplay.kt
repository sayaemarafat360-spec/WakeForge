package com.wakeforge.app.core.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.extensions.toFormattedTime
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.core.utils.TimeUtils

/**
 * Display size for [TimeDisplay].
 */
enum class TimeDisplayStyle {
    /** 36 sp bold — home screen hero clock. */
    Large,
    /** 24 sp semi-bold — card headers, upcoming alarm cards. */
    Medium,
    /** 18 sp medium — inline time references. */
    Small,
}

/**
 * Custom time display composable that renders an hour:minute time using
 * WakeForge's typography scale.
 *
 * @param hour       Hour of day (0–23).
 * @param minute     Minute of hour (0–59).
 * @param modifier   Outer modifier.
 * @param is24Hour   Whether to use 24-hour format; defaults to system setting.
 * @param style      Display size variant.
 * @param color      Override color; defaults to palette's primaryText.
 */
@Composable
fun TimeDisplay(
    hour: Int,
    minute: Int,
    modifier: Modifier = Modifier,
    is24Hour: Boolean? = null,
    style: TimeDisplayStyle = TimeDisplayStyle.Medium,
    color: Color? = null,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val resolvedColor = color ?: colors.primaryText
    val context = LocalContext.current
    val use24Hour = is24Hour ?: TimeUtils.is24HourFormat(context)

    // Convert minutes-since-midnight to formatted string
    val totalMinutes = (hour.coerceIn(0, 23) * 60) + minute.coerceIn(0, 59)
    val timeString = totalMinutes.toFormattedTime(use24Hour)

    val textStyle: TextStyle = when (style) {
        TimeDisplayStyle.Large -> typography.displayLarge.copy(
            color = resolvedColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        TimeDisplayStyle.Medium -> typography.headlineLarge.copy(
            color = resolvedColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Start,
        )
        TimeDisplayStyle.Small -> typography.titleLarge.copy(
            color = resolvedColor,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = timeString,
            style = textStyle,
            textAlign = textStyle.textAlign,
        )
    }
}
