package com.example.corda.tuner.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import com.example.corda.tuner.ui.components.annotateMusicNotes
import com.example.corda.tuner.ui.settings.data.TuningListItem

/**
 * A single row in the tunings list.
 *
 * Visual anatomy:
 * - Overline  : instrument name (e.g. "Guitar") — contextual label above the headline
 * - Headline  : tuning name (e.g. "Drop D")
 * - Supporting: note string preview (e.g. "D2 A2 D3 G3 B3 E4")
 * - Trailing  : animated check-circle when this tuning is selected
 *
 * Long-pressing the item shows a context menu at the press location offering [onEdit] and [onDelete].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TuningListItem(
    tuning: TuningListItem,
    shapes: ListItemShapes,
    isSelected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bodyStyle = MaterialTheme.typography.bodyMedium
    val notesPreview = remember(tuning.notes, bodyStyle) {
        annotateMusicNotes(tuning.notes, bodyStyle)
    }

    var isMenuVisible by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current

    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect {
            if (it is PressInteraction.Press) {
                menuOffset = with(density) {
                    DpOffset(
                        it.pressPosition.x.toDp(),
                        it.pressPosition.y.toDp(),
                    )
                }
            }
        }
    }

    Box(modifier = modifier) {
        SegmentedListItem(
            onClick = onClick,
            onLongClick = { isMenuVisible = true },
            interactionSource = interactionSource,
            shapes = shapes,
            verticalAlignment = Alignment.CenterVertically,
            colors = ListItemDefaults.colors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            ),
            overlineContent = {
                Text(
                    text = tuning.instrumentName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = {
                Text(
                    text = notesPreview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingContent = {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMedium,
                        ),
                    ) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            },
        ) {
            Text(
                text = tuning.tuningName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if ( isMenuVisible ) {
            TuningDropdownMenu(
                tuningName = tuning.tuningName,
                offset = menuOffset,
                onDismiss = { isMenuVisible = false },
                onEdit = {
                    isMenuVisible = false
                    onEdit()
                },
                onDelete = {
                    isMenuVisible = false
                    onDelete()
                }
            )
        }
    }
}
