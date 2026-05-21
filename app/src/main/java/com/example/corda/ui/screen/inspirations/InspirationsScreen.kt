package com.example.corda.ui.screen.inspirations

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.ui.components.FABMenu
import com.example.corda.ui.components.FABMenuItem
import com.example.corda.ui.components.FilterChipGroup
import com.example.corda.ui.components.UserInfo
import com.example.corda.ui.screen.inspirations.components.LabelManagementBottomSheet

/**
 * Main Inspirations list screen.
 *
 * @param openDrawer lambda to open the navigation drawer
 * @param onInspirationClick lambda to navigate to the detail screen for a given inspiration id
 * @param onAddClick lambda to navigate to the add/edit screen for a new inspiration
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InspirationsScreen(
    modifier: Modifier = Modifier,
    openDrawer: () -> Unit,
    onInspirationClick: (String) -> Unit = {},
    onAddClick: () -> Unit = {},
    viewModel: InspirationsViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var isFabMenuOpen by remember { mutableStateOf(false) }
    var isLabelSheetOpen by remember { mutableStateOf(false) }
    var isFilterPanelOpen by remember { mutableStateOf(false) }

    LaunchedEffect(isFilterPanelOpen) {
        if (isFilterPanelOpen) {
            focusManager.clearFocus()
        }
    }

    val fabMenuItems = remember(onAddClick) {
        listOf(
            FABMenuItem(
                Icons.Rounded.Add, R.string.new_inspiration
            ) {
                isFabMenuOpen = false
                onAddClick()
            },
            FABMenuItem(
                Icons.Outlined.Label, R.string.manage_labels
            ) {
                isFabMenuOpen = false
                isLabelSheetOpen = true
            },
        )
    }

    val hasOverlay = isLabelSheetOpen || isFabMenuOpen

    BackHandler(enabled = hasOverlay) {
        when {
            isLabelSheetOpen -> isLabelSheetOpen = false
            isFabMenuOpen -> isFabMenuOpen = false
        }
    }

    if (isLabelSheetOpen) {
        LabelManagementBottomSheet(onDismiss = { isLabelSheetOpen = false })
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
                },
                actions = {
                    val filterContentDescription = if (isFilterPanelOpen) {
                        stringResource(R.string.hide_filters)
                    } else {
                        stringResource(R.string.show_filters)
                    }
                    BadgedBox(
                        badge = {
                            if (state.selectedLabel != null) {
                                Badge()
                            }
                        },
                    ) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                isFilterPanelOpen = !isFilterPanelOpen
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = filterContentDescription,
                                tint = if (isFilterPanelOpen) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FABMenu(
                isExpanded = isFabMenuOpen,
                onExpandedChange = { isFabMenuOpen = it },
                items = fabMenuItems,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            AnimatedVisibility(
                visible = isFilterPanelOpen,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            ) {
                FilterChipGroup(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    items = state.availableLabels,
                    selectedItem = state.selectedLabel,
                    onItemSelected = viewModel::setSelectedLabel,
                )
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    UserInfo(
                        mainText = "No inspirations found",
                        supportingText = "Please add some to get started!"
                    )
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = if (isFilterPanelOpen) 0.dp else 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalItemSpacing = 8.dp,
                ) {
                    items(
                        items = filtered,
                        key = { it.id },
                        span = { inspiration ->
                            if (inspiration.labels.contains("Video")) {
                                StaggeredGridItemSpan.FullLine
                            } else {
                                StaggeredGridItemSpan.SingleLane
                            }
                        },
                    ) { inspiration ->

                    }
                }
            }
        }
    }
}
