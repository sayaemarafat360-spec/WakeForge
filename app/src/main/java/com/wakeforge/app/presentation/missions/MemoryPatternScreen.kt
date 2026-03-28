package com.wakeforge.app.presentation.missions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography

/**
 * Memory pattern challenge screen.
 *
 * Phase 1 (Show): Tiles in the pattern light up sequentially with the primary accent color.
 * Phase 2 (Replay): User taps tiles to reproduce the pattern. Correct taps stay lit with
 * success color; wrong taps flash red.
 *
 * @param viewModel The [MissionViewModel] backing this screen.
 */
@Composable
fun MemoryPatternScreen(viewModel: MissionViewModel) {
    val state by viewModel.uiState.collectAsState()
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val mission = state.mission as? com.wakeforge.app.domain.models.Mission.MemoryMission
    val gridSize = mission?.gridSize ?: 3
    val totalTiles = gridSize * gridSize

    // Error flash animation
    var errorFlashVisible by remember { mutableStateOf(false) }
    var errorFlashIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(state.memoryErrorIndex) {
        if (state.memoryErrorIndex >= 0) {
            errorFlashVisible = true
            errorFlashIndex = state.memoryErrorIndex
            kotlinx.coroutines.delay(600L)
            errorFlashVisible = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // ── Status text ────────────────────────────────────────────────

        if (state.isShowingPattern) {
            val revealedCount = state.revealedTileIndex.coerceAtLeast(0) + 1
            Text(
                text = "Memorize the pattern!",
                style = typography.headlineMedium.copy(
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$revealedCount / ${state.memoryPattern.size} tiles revealed",
                style = typography.bodyMedium,
                color = colors.secondaryText,
            )
        } else {
            Text(
                text = "Reproduce the pattern!",
                style = typography.headlineMedium.copy(
                    color = colors.primaryText,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${state.userPattern.size} / ${state.memoryPattern.size} tiles",
                style = typography.bodyMedium,
                color = colors.secondaryText,
            )
        }

        if (state.attempts > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Errors: ${state.attempts}",
                style = typography.labelMedium,
                color = colors.error,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // ── Grid of tiles ──────────────────────────────────────────────

        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(totalTiles) { index ->
                MemoryTile(
                    index = index,
                    isCurrentlyRevealed = state.isShowingPattern && state.revealedTileIndex == index,
                    isUserTappedCorrectly = state.userPattern.contains(index),
                    isErrorTile = errorFlashVisible && errorFlashIndex == index,
                    isShowingPattern = state.isShowingPattern,
                    onTap = { viewModel.onTileTap(index) },
                )
            }
        }
    }
}

/**
 * A single tile in the memory grid.
 */
@Composable
private fun MemoryTile(
    index: Int,
    isCurrentlyRevealed: Boolean,
    isUserTappedCorrectly: Boolean,
    isErrorTile: Boolean,
    isShowingPattern: Boolean,
    onTap: () -> Unit,
) {
    val colors = LocalWakeForgeColors.current

    // Determine tile color with smooth animation
    val tileColor by animateColorAsState(
        targetValue = when {
            isErrorTile -> colors.error
            isCurrentlyRevealed -> colors.primaryAccent
            isUserTappedCorrectly -> colors.success
            else -> colors.surfaceVariant
        },
        animationSpec = tween(300),
        label = "tileColor_$index",
    )

    val contentAlpha = when {
        isCurrentlyRevealed -> 1f
        isUserTappedCorrectly -> 1f
        isErrorTile -> 1f
        !isShowingPattern -> 0.3f
        else -> 0.15f
    }

    val contentColor = when {
        isCurrentlyRevealed -> Color.White
        isUserTappedCorrectly -> Color.White
        isErrorTile -> Color.White
        else -> colors.secondaryText
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(tileColor)
            .then(
                if (!isShowingPattern && !isCurrentlyRevealed) {
                    Modifier.clickable(enabled = !isUserTappedCorrectly, onClick = onTap)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${index + 1}",
            style = LocalWakeForgeTypography.current.titleLarge.copy(
                color = contentColor.copy(alpha = contentAlpha),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
            ),
        )
    }
}
