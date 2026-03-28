package com.wakeforge.app.presentation.create_alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType

/**
 * Difficulty selection section with 5 color-coded chips for each difficulty
 * tier, plus brief descriptions.
 *
 * @param difficulty         Currently selected difficulty.
 * @param onDifficultyChange Callback when difficulty selection changes.
 * @param missionType        Currently selected mission type (for context display).
 */
@Composable
fun DifficultySelectorSection(
    difficulty: MissionDifficulty,
    onDifficultyChange: (MissionDifficulty) -> Unit,
    missionType: MissionType,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    val difficultyColorMap: Map<MissionDifficulty, Color> = mapOf(
        MissionDifficulty.TRIVIAL to Color(0xFF4ADE80),
        MissionDifficulty.EASY to colors.success,
        MissionDifficulty.MEDIUM to colors.primaryAccent,
        MissionDifficulty.HARD to Color(0xFFFFB84D),
        MissionDifficulty.EXTREME to colors.error,
    )

    WFCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Section header
            Text(
                text = "Difficulty",
                style = typography.labelMedium,
                color = colors.secondaryText,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                MissionDifficulty.entries.forEach { diff ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        WFChip(
                            label = diff.displayName.replaceFirstChar { it.uppercase() },
                            selected = difficulty == diff,
                            onClick = { onDifficultyChange(diff) },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Difficulty description
            Text(
                text = difficulty.description,
                style = typography.bodyMedium,
                color = colors.secondaryText,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Color indicator bar
            val selectedColor = difficultyColorMap[difficulty] ?: colors.primaryAccent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .padding(horizontal = 4.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            fraction = difficulty.tier.toFloat() / MissionDifficulty.entries.size.toFloat()
                        )
                        .height(4.dp)
                        .background(selectedColor),
                )
            }
        }
    }
}
