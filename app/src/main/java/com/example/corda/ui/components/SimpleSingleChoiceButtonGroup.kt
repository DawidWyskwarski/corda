package com.example.corda.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

/**
 * Generic component for a simple single choice button group.
 *
 * @param selectedItem The currently selected item.
 * @param onItemSelected Callback to invoke when an item is selected.
 * @param items The list of items to display.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> SimpleSingleChoiceButtonGroup(
    modifier: Modifier = Modifier,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    items: List<T>,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        items.forEachIndexed { index, item ->
            ToggleButton(
                modifier = Modifier
                    .weight(1f)
                    .semantics { role = Role.RadioButton }
                ,
                checked = item == selectedItem,
                onCheckedChange = {
                    onItemSelected(item)
                },
                shapes =
                    // Choose different shapes based on the index of the item
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        items.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                colors = ToggleButtonDefaults.colors(
                    checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            ) {
                Text(
                    text = item.toString()
                )
            }
        }
    }
}