package com.example.corda.tuner.ui.settings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

/**
 * Context menu for a single tuning, anchored at [offset] relative to the parent's top-start corner.
 *
 * The menu hangs off a zero-size [Box] rather than using [DropdownMenu]'s own `offset` parameter.
 * That parameter is an additive nudge on top of edge-anchored position candidates, all measured from
 * the anchor's top or bottom edge, so a full-size anchor would need a height correction that breaks
 * whenever the position provider falls back to a different candidate near a screen edge. Collapsing
 * the anchor to a point makes its top and bottom both equal the press position.
 */
@Composable
fun TuningDropdownMenu(
    tuningName: String,
    offset: DpOffset,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(offset.x.toPx().toInt(), offset.y.toPx().toInt())
            }
            .size(0.dp),
    ) {
        DropdownMenu(
            modifier = Modifier
                .width(192.dp),
            expanded = true,
            onDismissRequest = onDismiss,
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = tuningName,
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                maxLines = 2,
            )

            MenuItem(
                text = "Edit",
                icon = Icons.Rounded.Edit,
                onClick = onEdit,
            )

            MenuItem(
                text = "Delete",
                icon = Icons.Rounded.Delete,
                onClick = onDelete,
            )
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        modifier = Modifier.height(40.dp),
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        trailingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        onClick = onClick,
    )
}
