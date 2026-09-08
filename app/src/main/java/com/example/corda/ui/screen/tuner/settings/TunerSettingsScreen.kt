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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.data.tuner.local.models.TuningDetails
import com.example.corda.domain.tuner.TuningMode
import com.example.corda.ui.components.DeleteItemDialog
import com.example.corda.ui.components.FABMenu
import com.example.corda.ui.components.FABMenuItem
import com.example.corda.ui.components.FilterChipGroup
import com.example.corda.ui.components.SimpleSingleChoiceButtonGroup
import com.example.corda.ui.components.SingleClickIconButton
import com.example.corda.ui.components.UserInfo
import com.example.corda.ui.screen.tuner.settings.components.InstrumentManagementBottomSheet
import com.example.corda.ui.screen.tuner.settings.components.TuningListItem

/**
 * Screen for the tuner settings.
 *
 * @param viewModel screen-specific ViewModel for search, filter, and instrument list
 * @param onBack lambda reporting an event to `CordaApp` to go back
 * @param onAddTuning lambda to navigate to the Add Tuning screen
 * @param onEditTuning lambda to navigate to the Edit Tuning screen with the tuning ID
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TunerSettingsScreen(
    viewModel: TunerSettingsViewModel,
    onBack: () -> Unit,
    onAddTuning: () -> Unit,
    onEditTuning: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedMode by viewModel.selectedTuningMode.collectAsStateWithLifecycle()
    val instruments by viewModel.instruments.collectAsStateWithLifecycle()
    val modes = remember { TuningMode.entries.toList() }
    var isFabMenuOpen by remember { mutableStateOf(false) }
    var isInstrumentSheetOpen by remember { mutableStateOf(false) }

    val fabMenuItems = remember(onAddTuning) {
        listOf(
            FABMenuItem(
                Icons.AutoMirrored.Rounded.QueueMusic,
                R.string.new_custom_tuning
            ) { 
                isFabMenuOpen = false 
                onAddTuning()
            },
            FABMenuItem(
                Icons.Rounded.Piano,
                R.string.manage_instruments
            ) { 
                isFabMenuOpen = false
                isInstrumentSheetOpen = true
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.markSelectedTuningAsUsed()
        }
    }

    BackHandler(enabled = isInstrumentSheetOpen || isFabMenuOpen) {
        when {
            isInstrumentSheetOpen -> isInstrumentSheetOpen = false
            isFabMenuOpen -> isFabMenuOpen = false
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tuner_settings)) },
                navigationIcon = { SingleClickIconButton(onClick = onBack) }
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
                onItemSelected = { viewModel.setTuningMode(it) }
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

                        val filteredTunings by viewModel.filteredTunings.collectAsStateWithLifecycle()
                        val selectedTuningId by viewModel.selectedTuningId.collectAsStateWithLifecycle()
                        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                        val filterInstrument by viewModel.filterInstrument.collectAsStateWithLifecycle()

                        StandardModeContent(
                            filteredTunings = filteredTunings,
                            selectedTuningId = selectedTuningId,
                            searchQuery = searchQuery,
                            instruments = instruments,
                            filterInstrument = filterInstrument,
                            onSelectTuning = viewModel::setSelectedTuning,
                            onSelectFilterInstrumentId = viewModel::setFilterInstrument,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onDeleteTuning = viewModel::deleteTuning,
                            onEditTuning = onEditTuning
                        )
                    }
                    TuningMode.CHROMATIC -> ChromaticModeContent()
                }
            }
        }
    }

    if (isInstrumentSheetOpen) {
        InstrumentManagementBottomSheet(
            instruments = instruments,
            doesInstrumentHaveTunings = viewModel::hasTunings,
            onCreateInstrument = viewModel::createInstrument,
            onUpdateInstrument = viewModel::updateInstrument,
            onDeleteInstrument = viewModel::deleteInstrument,
            onDismiss = { isInstrumentSheetOpen = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StandardModeContent(
    filteredTunings: List<TuningDetails>,
    selectedTuningId: Int?,
    searchQuery: String,
    instruments: List<Instrument>,
    filterInstrument: Instrument?,
    onSelectTuning: (Int) -> Unit,
    onSelectFilterInstrumentId: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditTuning: (Int) -> Unit,
    onDeleteTuning: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count by remember { derivedStateOf { filteredTunings.size } }
    var pendingTuningToDelete by remember { mutableStateOf<TuningDetails?>(null) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        if (filteredTunings.isEmpty()) {
            UserInfo(
                modifier = Modifier
                    .fillMaxSize(),
                mainText = stringResource(R.string.no_tunings),
                supportingText = "Tap + to add the one you want"
            )
        } else {
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
                        onQueryChange = { onSearchQueryChange(it) },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_tunings)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.clear_search))
                                }
                            }
                        },
                    )
                },
                expanded = false,
                onExpandedChange = { },
            ) {}

            Spacer(modifier = Modifier.height(8.dp))

            FilterChipGroup(
                items = instruments,
                selectedItem = filterInstrument,
                onItemSelected = { onSelectFilterInstrumentId(it.id) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
            ) {
                itemsIndexed(
                    items = filteredTunings,
                    key = { _, tuning -> tuning.tuningId },
                ) { index, tuning ->
                    TuningListItem(
                        tuning = tuning,
                        shapes = ListItemDefaults.segmentedShapes(
                            index = index,
                            count = count
                        ),
                        isSelected = tuning.tuningId == selectedTuningId,
                        onClick = { onSelectTuning(tuning.tuningId) },
                        onEdit = { onEditTuning(tuning.tuningId) },
                        onDelete = { pendingTuningToDelete = tuning },
                    )
                }
            }
        }
    }

    pendingTuningToDelete?.let { tuning ->
        DeleteItemDialog(
            titleRes = R.string.tuning_delete_title,
            messageRes = R.string.tuning_delete_message,
            itemName = tuning.tuningName,
            onDelete = {
                onDeleteTuning(tuning.tuningId)
                pendingTuningToDelete = null
            },
            onDismiss = { pendingTuningToDelete = null }
        )
    }
}

@Composable
private fun ChromaticModeContent(
    modifier: Modifier = Modifier
) {
    UserInfo(
        modifier = modifier
            .fillMaxSize(),
        mainText = stringResource(R.string.chromatic_description),
        supportingText = stringResource(R.string.dont_select_tuning)
    )
}
