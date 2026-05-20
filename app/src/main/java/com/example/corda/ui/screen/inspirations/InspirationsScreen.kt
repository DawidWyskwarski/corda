package com.example.corda.ui.screen.inspirations

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Label
import com.example.corda.ui.screen.inspirations.components.LabelManagementBottomSheet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.ui.components.FilterChipGroup
import com.example.corda.ui.screen.inspirations.components.InspirationCard

/**
 * Main Inspirations list screen.
 *
 * @param openDrawer lambda to open the navigation drawer
 * @param onInspirationClick lambda to navigate to the detail screen for a given inspiration id
 * @param onAddClick lambda to navigate to the add/edit screen for a new inspiration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspirationsScreen(
    modifier: Modifier = Modifier,
    openDrawer: () -> Unit,
    onInspirationClick: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
    viewModel: InspirationsViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    var fabExpanded by remember { mutableStateOf(false) }
    var isLabelSheetOpen by remember { mutableStateOf(false) }

    if (isLabelSheetOpen) {
        LabelManagementBottomSheet(onDismiss = { isLabelSheetOpen = false })
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        modifier = Modifier.fillMaxWidth(),
                        windowInsets = WindowInsets(top = 0.dp),
                        inputField = {
                            SearchBarDefaults.InputField(
                                query = state.searchQuery,
                                onQueryChange = viewModel::setSearchQuery,
                                onSearch = {},
                                expanded = false,
                                onExpandedChange = {},
                                placeholder = { Text(stringResource(R.string.search_inspirations)) },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Search, contentDescription = null)
                                },
                                trailingIcon = {
                                    if (state.searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Rounded.Close, contentDescription = null)
                                        }
                                    }
                                },
                            )
                        },
                        expanded = false,
                        onExpandedChange = {},
                    ) {}
                },
                navigationIcon = {
                    IconButton(onClick = openDrawer) {
                        Icon(Icons.Rounded.Menu, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FabOption(
                            text = stringResource(R.string.manage_labels),
                            icon = Icons.Outlined.Label,
                            onClick = {
                                fabExpanded = false
                                isLabelSheetOpen = true
                            }
                        )
                        FabOption(
                            text = stringResource(R.string.new_inspiration),
                            icon = Icons.Rounded.Add,
                            onClick = {
                                fabExpanded = false
                                onAddClick()
                            }
                        )
                    }
                }
                FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
                    Icon(
                        imageVector = if (fabExpanded) Icons.Rounded.Close else Icons.Rounded.Add,
                        contentDescription = null
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalItemSpacing = 8.dp
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                FilterChipGroup(
                    items = state.availableLabels,
                    selectedItem = state.selectedLabel,
                    onItemSelected = viewModel::setSelectedLabel
                )
            }

            val filtered = if (state.searchQuery.isBlank() && state.selectedLabel == null) {
                state.inspirations
            } else {
                state.inspirations.filter { inspiration ->
                    val matchesQuery = state.searchQuery.isBlank() ||
                            inspiration.name.contains(state.searchQuery, ignoreCase = true) ||
                            inspiration.description.contains(state.searchQuery, ignoreCase = true)
                    val matchesLabel = state.selectedLabel == null ||
                            inspiration.labels.contains(state.selectedLabel)
                    matchesQuery && matchesLabel
                }
            }

            items(
                items = filtered,
                key = { it.id },
                // Logic for videos taking up the full width
                span = { inspiration ->
                    if (inspiration.labels.contains("Video")) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                }
            ) { inspiration ->
                InspirationCard(
                    inspiration = inspiration,
                    onClick = { onInspirationClick(inspiration.id) }
                )
            }
        }
    }
}

@Composable
private fun FabOption(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge
            )
        }
        SmallFloatingActionButton(onClick = onClick) {
            Icon(icon, contentDescription = null)
        }
    }
}
