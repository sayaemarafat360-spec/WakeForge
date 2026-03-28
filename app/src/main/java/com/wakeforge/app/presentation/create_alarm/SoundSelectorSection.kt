package com.wakeforge.app.presentation.create_alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.wakeforge.app.core.components.WFBottomSheet
import com.wakeforge.app.core.components.WFCard
import com.wakeforge.app.core.components.WFChip
import com.wakeforge.app.core.theme.LocalWakeForgeColors
import com.wakeforge.app.core.theme.LocalWakeForgeTypography
import com.wakeforge.app.data.sound.SoundCatalog
import com.wakeforge.app.data.sound.SoundCategory

/**
 * Sound selection section with a card showing the current sound and a bottom
 * sheet picker for browsing the full sound catalog.
 *
 * @param currentSound    ID of the currently selected sound.
 * @param onSoundSelected Callback when a new sound is selected.
 * @param previewSound    Callback to preview a sound (5-second play).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundSelectorSection(
    currentSound: String,
    onSoundSelected: (String) -> Unit,
    previewSound: (String) -> Unit,
) {
    val colors = LocalWakeForgeColors.current
    val typography = LocalWakeForgeTypography.current

    var showBottomSheet by remember { mutableStateOf(false) }
    val currentSoundItem = SoundCatalog.getSoundById(currentSound)
        ?: SoundCatalog.getDefaultSound()

    // Current sound card
    WFCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = colors.primaryAccent,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = currentSoundItem.name,
                    style = typography.titleLarge,
                    color = colors.primaryText,
                )
                Text(
                    text = currentSoundItem.description,
                    style = typography.bodyMedium,
                    color = colors.secondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Preview button
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Preview sound",
                tint = colors.secondaryAccent,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(4.dp),
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Category filter chips
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SoundCategory.entries.forEach { category ->
            WFChip(
                label = category.displayName,
                selected = false,
                onClick = { showBottomSheet = true },
            )
        }
    }

    // Bottom sheet picker
    if (showBottomSheet) {
        WFBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            title = "Select Alarm Sound",
        ) {
            // Group sounds by category
            SoundCategory.entries.forEach { category ->
                val sounds = SoundCatalog.getSoundsByCategory(category)
                if (sounds.isNotEmpty()) {
                    Text(
                        text = category.displayName,
                        style = typography.labelMedium,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    )

                    sounds.forEach { sound ->
                        val isSelected = sound.id == currentSound
                        WFCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            borderColor = if (isSelected) colors.primaryAccent else null,
                            cornerRadius = 8.dp,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text = sound.name,
                                        style = typography.bodyLarge,
                                        color = if (isSelected) colors.primaryAccent else colors.primaryText,
                                    )
                                    Text(
                                        text = sound.description,
                                        style = typography.bodyMedium,
                                        color = colors.secondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Preview",
                                    tint = colors.secondaryAccent,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .padding(2.dp),
                                )
                            }
                        }

                        // Click handler on the card content
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {}
                    }
                }
            }
        }
    }
}
