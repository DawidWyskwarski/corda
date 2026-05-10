package com.example.corda.ui.screen.tuner.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.ui.components.FABMenu
import com.example.corda.ui.components.FABMenuItem
import com.example.corda.ui.components.SimpleSingleChoiceButtonGroup
import com.example.corda.ui.screen.tuner.TunerViewModel
import com.example.corda.ui.components.TuningListItem

/**
 * Screen for the tuner settings.
 *
 * ### TODO
 * - Make the FAB menu do something
 * - Add tunings to the list
 *
 * @param onBack lambda reporting an event to `CordaApp` to go back
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TunerSettingsScreen(
    viewModel: TunerViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val selectedTuning by viewModel.selectedTuning.collectAsStateWithLifecycle()

    val selectedMode by viewModel.selectedMode.collectAsStateWithLifecycle()
    
    val modes = TuningMode.entries.toList()

    // Search bar state
    var searchQuery by remember { mutableStateOf("") }

    // Instrument filter chip state — null means "All"
    var selectedInstrument by remember { mutableStateOf<String?>(null) }

    // Distinct, sorted instrument names derived from all tunings
    val instruments = remember(viewModel.tunings) {
        viewModel.tunings.map { it.instrument }.distinct().sorted()
    }

    // Filter tunings by search query AND selected instrument chip
    val filteredTunings = remember(searchQuery, selectedInstrument, viewModel.tunings) {
        viewModel.tunings.filter { tuning ->
            val matchesSearch = searchQuery.isBlank() ||
                    tuning.name.contains(searchQuery, ignoreCase = true) ||
                    tuning.instrument.contains(searchQuery, ignoreCase = true)
            val matchesChip = selectedInstrument == null || tuning.instrument == selectedInstrument
            matchesSearch && matchesChip
        }
    }

    // FAB menu state
    var isFabMenuOpen by remember { mutableStateOf(false) }

    val fabMenuItems = remember {
        listOf(
            FABMenuItem(
                Icons.AutoMirrored.Rounded.QueueMusic,
                R.string.new_custom_tuning
            ) { isFabMenuOpen = false },
            FABMenuItem(
                Icons.Rounded.Piano,
                R.string.manage_instruments
            ) { isFabMenuOpen = false }
        )
    }

    // Close FAB menu on back press when open
    BackHandler(isFabMenuOpen) { isFabMenuOpen = false }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tuner_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = selectedMode == TuningMode.STANDARD,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FABMenu(
                    isExpanded = isFabMenuOpen,
                    onExpandedChange = { isFabMenuOpen = it },
                    items = fabMenuItems
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.tuner_mode),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SimpleSingleChoiceButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                selectedItem = selectedMode,
                items = modes,
                onItemSelected = { viewModel.selectMode(it) }
            )

            AnimatedContent(
                targetState = selectedMode,
                modifier = Modifier.weight(1f),
                transitionSpec = {
                    if (targetState == TuningMode.STANDARD) {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    } else {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    }
                },
                label = "Mode Animation"
            ) { mode ->
                when (mode) {
                    TuningMode.STANDARD -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text(
                                modifier = Modifier.padding(vertical = 8.dp),
                                text = stringResource(R.string.tunings),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )

                            SearchBar(
                                modifier = Modifier.fillMaxWidth(),
                                windowInsets = WindowInsets(top = 0.dp),
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = searchQuery,
                                        onQueryChange = { searchQuery = it },
                                        onSearch = { },
                                        expanded = false,
                                        onExpandedChange = { },
                                        placeholder = { Text(stringResource(R.string.search_tunings)) },
                                        leadingIcon = {
                                            Icon(Icons.Rounded.Search, contentDescription = null)
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(
                                                        Icons.Rounded.Close,
                                                        contentDescription = stringResource(R.string.clear_search)
                                                    )
                                                }
                                            }
                                        },
                                    )
                                },
                                expanded = false,
                                onExpandedChange = { },
                            ) {}

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp),
                            ) {
                                item(key = "chip_all") {
                                    val isAllSelected = selectedInstrument == null
                                    FilterChip(
                                        selected = isAllSelected,
                                        onClick = { selectedInstrument = null },
                                        label = { Text(stringResource(R.string.all)) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        ),
                                    )
                                }

                                items(items = instruments, key = { "chip_$it" }) { instrument ->
                                    val isSelected = selectedInstrument == instrument
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedInstrument =
                                                if (isSelected) null else instrument
                                        },
                                        label = { Text(instrument) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                        ),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (filteredTunings.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_tunings),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            } else {

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .selectableGroup(),
                                    verticalArrangement = Arrangement.spacedBy(
                                        ListItemDefaults.SegmentedGap
                                    )
                                ) {
                                    val count = filteredTunings.size
                                    itemsIndexed(
                                        items = filteredTunings,
                                        key = { _, tuning -> "${tuning.instrument}/${tuning.name}" },
                                    ) { index, tuning ->
                                        TuningListItem(
                                            tuning = tuning,
                                            shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
                                            isSelected = tuning == selectedTuning,
                                            onClick = { viewModel.selectTuning(tuning) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    TuningMode.CHROMATIC -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.chromatic_description),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = stringResource(R.string.dont_select_tuning),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
