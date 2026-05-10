package com.example.corda.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex

import com.example.corda.R

data class FABMenuItem(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit,
)

/**
 * Custom component for a floating action button menu.
 *
 * ### TODO
 * - add documentation (yes i stole the code from the official docs)
 * - move the isExpanded Box to the TunerSettingsScreen to eliminate the weird gap
 *
 * @param isExpanded Whether the menu is expanded.
 * @param onExpandedChange Callback to invoke when the menu is expanded or collapsed.
 * @param items The list of menu items.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FABMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<FABMenuItem>
) {
    val listState = rememberLazyListState()
    val fabVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || !listState.canScrollForward
        }
    }
    val focusRequester = remember { FocusRequester() }

    val toggleMenuDescription = stringResource(R.string.toggle_menu)
    val expandedDescription = stringResource(R.string.expanded)
    val collapsedDescription = stringResource(R.string.collapsed)
    val closeMenuDescription = stringResource(R.string.close_menu)

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { onExpandedChange(false) }
                    }
            )
        }

        FloatingActionButtonMenu(
            modifier = Modifier
                .align(Alignment.BottomEnd),
            expanded = isExpanded,
            button = {
                TooltipBox(
                    positionProvider =
                        TooltipDefaults.rememberTooltipPositionProvider(
                            if (isExpanded) {
                                TooltipAnchorPosition.Start
                            } else {
                                TooltipAnchorPosition.Above
                            }
                        ),
                    tooltip = { PlainTooltip { Text(toggleMenuDescription) } },
                    state = rememberTooltipState()
                ) {
                    ToggleFloatingActionButton(
                        modifier = Modifier
                            .semantics {
                                traversalIndex = -1f
                                stateDescription =
                                    if (isExpanded) {
                                        expandedDescription
                                    } else {
                                        collapsedDescription
                                    }
                                contentDescription = toggleMenuDescription
                            }
                            .animateFloatingActionButton(
                                visible = fabVisible || isExpanded,
                                alignment = Alignment.BottomEnd,
                            )
                            .focusRequester(focusRequester),
                        checked = isExpanded,
                        onCheckedChange = onExpandedChange,
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress }),
                        )
                    }
                }
            }
        ) {
            items.forEachIndexed { i, item ->
                FloatingActionButtonMenuItem(
                    modifier = Modifier
                        .semantics {
                            isTraversalGroup = true
                            // Add a custom a11y action to allow closing the menu when focusing
                            // the last menu item, since the close button comes before the first
                            // menu item in the traversal order.
                            if (i == items.size - 1) {
                                customActions =
                                    listOf(
                                        CustomAccessibilityAction(
                                            label = closeMenuDescription,
                                            action = {
                                                onExpandedChange(false)
                                                true
                                            },
                                        )
                                    )
                            }
                        }
                        .then(
                            if (i == 0) {
                                Modifier.onKeyEvent {
                                    // Navigating back from the first item should go back to the
                                    // FAB menu button.
                                    if (
                                        it.type == KeyEventType.KeyDown &&
                                        (it.key == Key.DirectionUp ||
                                                (it.isShiftPressed && it.key == Key.Tab))
                                    ) {
                                        focusRequester.requestFocus()
                                        return@onKeyEvent true
                                    }
                                    return@onKeyEvent false
                                }
                            } else {
                                Modifier
                            }
                        ),
                    onClick = item.onClick,
                    icon = { Icon(item.icon, contentDescription = null) },
                    text = { Text(text = stringResource(item.labelRes)) },
                )
            }
        }
    }
}