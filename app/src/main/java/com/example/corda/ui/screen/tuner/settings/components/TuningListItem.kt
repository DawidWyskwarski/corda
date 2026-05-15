package com.example.corda.ui.screen.tuner.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.corda.data.tuner.local.entities.relations.TuningWithInstrumentAndSounds
import com.example.corda.ui.screen.tuner.components.soundsPreviewAnnotated

/**
 * A single row in the tunings list.
 *
 * Visual anatomy:
 * - Overline  : instrument name (e.g. "Guitar") — contextual label above the headline
 * - Headline  : tuning name (e.g. "Drop D")
 * - Supporting: note string preview (e.g. "D2 A2 D3 G3 B3 E4")
 * - Trailing  : animated check-circle when this tuning is selected
 *
 * When [onEdit] and/or [onDelete] are provided, long-pressing the item shows a context menu
 * at the press location with the corresponding actions.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TuningListItem(
    tuning: TuningWithInstrumentAndSounds,
    shapes: ListItemShapes,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        label = "TuningListItem container color",
    )

    val bodyStyle = MaterialTheme.typography.bodyMedium
    val notesPreview = remember(tuning.sounds, bodyStyle) {
        soundsPreviewAnnotated(sounds = tuning.sounds, baseStyle = bodyStyle)
    }

    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(DpOffset.Zero) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        SegmentedListItem(
            onClick = onClick,
            shapes = shapes,
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val longPress = awaitLongPressOrCancellation(down.id)
                        if (longPress != null) {
                            menuOffset = with(density) {
                                DpOffset(
                                    x = longPress.position.x.toDp(),
                                    y = longPress.position.y.toDp(),
                                )
                            }
                            showMenu = true
                        }
                    }
                },
            colors = ListItemDefaults.colors(containerColor = containerColor),
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

        Box(
            modifier = Modifier
                .offset(
                    x = menuOffset.x,
                    y = menuOffset.y,
                )
                .size(0.dp),
        ) {
            DropdownMenu(
                modifier = Modifier.width(192.dp),
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = tuning.tuningName,
                            style = MaterialTheme.typography.bodyLargeEmphasized,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    onClick = { showMenu = false },
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Edit",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                )
            }
        }
    }
}
