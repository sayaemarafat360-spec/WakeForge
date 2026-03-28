package com.wakeforge.app.presentation.create_alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Custom scroll-wheel time picker section.
 *
 * Displays two vertically-scrolling LazyColumns for hour and minute selection.
 * The center item is highlighted with larger text and full opacity, while
 * items further from the center are progressively smaller and dimmer.
 *
 * An AM/PM segmented control is shown below when in 12-hour mode.
 *
 * @param hour          Currently selected hour (0-23).
 * @param minute        Currently selected minute (0-59).
 * @param is24Hour      Whether to display in 24-hour format.
 * @param onHourChange  Callback when the hour selection changes.
 * @param onMinuteChange Callback when the minute selection changes.
 */
@OptIn(FlowPreview::class)
@Composable
fun TimePickerSection(
    hour: Int,
    minute: Int,
    is24Hour: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current
    val haptic = LocalHapticFeedback.current

    val visibleItems = 5
    val centerIndex = visibleItems / 2

    // Track whether the user has interacted (to prevent snapping on initial load)
    var userInteractedHour by remember { mutableIntStateOf(0) }
    var userInteractedMinute by remember { mutableIntStateOf(0) }

    val hourListState = rememberLazyListState(initialFirstVisibleItemIndex = hour - centerIndex)
    val minuteListState = rememberLazyListState(initialFirstVisibleItemIndex = minute - centerIndex)

    // Observe scroll and snap to nearest item
    LaunchedEffect(hourListState) {
        snapshotFlow { hourListState.firstVisibleItemIndex }
            .debounce(80L)
            .distinctUntilChanged()
            .collectLatest { index ->
                if (userInteractedHour > 0) {
                    val adjusted = (index + centerIndex).coerceIn(
                        if (is24Hour) 0 else 1,
                        if (is24Hour) 23 else 12
                    )
                    onHourChange(adjusted)
                }
            }
    }

    LaunchedEffect(minuteListState) {
        snapshotFlow { minuteListState.firstVisibleItemIndex }
            .debounce(80L)
            .distinctUntilChanged()
            .collectLatest { index ->
                if (userInteractedMinute > 0) {
                    val adjusted = (index + centerIndex).coerceIn(0, 59)
                    onMinuteChange(adjusted)
                }
            }
    }

    // Sync external changes to scroll position
    LaunchedEffect(hour, userInteractedHour) {
        if (userInteractedHour == 0) {
            hourListState.scrollToItem(hour - centerIndex)
        }
    }
    LaunchedEffect(minute, userInteractedMinute) {
        if (userInteractedMinute == 0) {
            minuteListState.scrollToItem(minute - centerIndex)
        }
    }

    WFCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Hour column
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LazyColumn(
                        state = hourListState,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 76.dp),
                        flingBehavior = remember { },
                    ) {
                        val hourRange = if (is24Hour) (0..23) else (1..12)
                        items(
                            count = hourRange.count(),
                            key = { hourRange.elementAt(it) }
                        ) { index ->
                            val h = hourRange.elementAt(index)
                            val distance = abs(index - centerIndex)
                            val itemHourListState = rememberLazyListState()

                            TimePickerItem(
                                value = h.toString().padStart(2, '0'),
                                distanceFromCenter = distance,
                                onClick = {
                                    userInteractedHour++
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    hourListState.animateScrollToItem(index - centerIndex)
                                    onHourChange(h)
                                }
                            )
                        }
                    }
                }

                // Colon separator
                Text(
                    text = ":",
                    style = typography.displayLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.primaryAccent,
                        textAlign = TextAlign.Center,
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp),
                )

                // Minute column
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LazyColumn(
                        state = minuteListState,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 76.dp),
                        flingBehavior = remember { },
                    ) {
                        items(
                            count = 60,
                            key = { it }
                        ) { index ->
                            TimePickerItem(
                                value = index.toString().padStart(2, '0'),
                                distanceFromCenter = abs(index - centerIndex),
                                onClick = {
                                    userInteractedMinute++
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    minuteListState.animateScrollToItem(index - centerIndex)
                                    onMinuteChange(index)
                                }
                            )
                        }
                    }
                }
            }

            // Center selection indicator line
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(horizontal = 32.dp)
                    .align(Alignment.Center),
                thickness = 2.dp,
                color = colors.primaryAccent.copy(alpha = 0.5f),
            )

            // Fade-out overlays top and bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.cardSurface,
                                Color.Transparent
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(76.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                colors.cardSurface
                            )
                        )
                    )
            )
        }

        // AM/PM toggle (12h mode only)
        if (!is24Hour) {
            val isPm = hour >= 12
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            ) {
                WFChip(
                    label = "AM",
                    selected = !isPm,
                    onClick = {
                        userInteractedHour++
                        onHourChange(if (isPm) hour - 12 else hour)
                    },
                )
                WFChip(
                    label = "PM",
                    selected = isPm,
                    onClick = {
                        userInteractedHour++
                        onHourChange(if (isPm) hour else hour + 12)
                    },
                )
            }
        }
    }
}

@Composable
private fun TimePickerItem(
    value: String,
    distanceFromCenter: Int,
    onClick: () -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val (fontSize, fontWeight, alpha) = when (distanceFromCenter) {
        0 -> Triple(28.sp, FontWeight.Bold, 1f)
        1 -> Triple(22.sp, FontWeight.SemiBold, 0.7f)
        else -> Triple(18.sp, FontWeight.Normal, 0.3f)
    }

    val textColor = when (distanceFromCenter) {
        0 -> colors.primaryText
        else -> colors.secondaryText
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = value,
            style = typography.titleLarge.copy(
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        )
    }
}

private fun abs(x: Int): Int = if (x < 0) -x else x
