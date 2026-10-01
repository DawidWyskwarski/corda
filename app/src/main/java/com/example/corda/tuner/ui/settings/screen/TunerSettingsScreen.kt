package com.example.corda.tuner.ui.settings.screen

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Piano
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.core.tuner.TuningMode
import com.example.corda.core.ui.components.DeleteItemDialog
import com.example.corda.core.ui.components.FABMenu
import com.example.corda.core.ui.components.FABMenuItem
import com.example.corda.core.ui.components.NavigateBackButton
import com.example.corda.core.ui.components.SimpleSingleChoiceButtonGroup
import com.example.corda.core.ui.components.UserInfo
import com.example.corda.core.ui.state.UiState
import com.example.corda.tuner.data.local.entities.Instrument
import com.example.corda.tuner.ui.settings.components.InstrumentFilterChipGroup
import com.example.corda.tuner.ui.settings.components.InstrumentManagementBottomSheet
import com.example.corda.tuner.ui.settings.components.TuningListItem
import com.example.corda.tuner.ui.settings.data.TuningListItem

/**
 * Screen for the tuner settings.
 *
 * @param onBack lambda reporting an event to `CordaApp` to go back
 * @param onAddTuning lambda to navigate to the Add Tuning screen
 * @param onEditTuning lambda to navigate to the Edit Tuning screen with the tuning ID
 * @param modifier applied to the screen's [Scaffold]
 * @param viewModel screen-specific ViewModel for search, filter, and instrument list
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TunerSettingsScreen(
    onBack: () -> Unit,
    onAddTuning: () -> Unit,
    onEditTuning: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TunerSettingsViewModel = hiltViewModel(),
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
                R.string.tuner_settings_fab_new_tuning_label
            ) {
                isFabMenuOpen = false
                onAddTuning()
            },
            FABMenuItem(
                Icons.Rounded.Piano,
                R.string.tuner_settings_fab_manage_instruments_label
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

    BackHandler(enabled = isFabMenuOpen) { isFabMenuOpen = false }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tuner_settings_title)) },
                navigationIcon = { NavigateBackButton(onClick = onBack) }
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
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.tuner_settings_mode_label),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SimpleSingleChoiceButtonGroup(
                selectedItem = selectedMode,
                items = modes,
                onItemSelected = { viewModel.setTuningMode(it) },
                modifier = Modifier.fillMaxWidth(),
                itemLabel = { mode ->
                    when (mode) {
                        TuningMode.STANDARD -> stringResource(R.string.tuner_settings_mode_standard_option)
                        TuningMode.CHROMATIC -> stringResource(R.string.tuner_settings_mode_chromatic_option)
                    }
                },
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

                        val tuningsUiState by viewModel.tuningsUiState.collectAsStateWithLifecycle()
                        val selectedTuningId by viewModel.selectedTuningId.collectAsStateWithLifecycle()
                        val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                        val filterInstrumentId by viewModel.filterInstrumentId.collectAsStateWithLifecycle()

                        StandardModeContent(
                            tuningsUiState = tuningsUiState,
                            selectedTuningId = selectedTuningId,
                            searchQuery = searchQuery,
                            instruments = instruments,
                            filterInstrumentId = filterInstrumentId,
                            onSelectTuning = viewModel::setSelectedTuning,
                            onSelectFilterInstrumentId = viewModel::setFilterInstrument,
                            onSearchQueryChange = viewModel::setSearchQuery,
                            onDeleteTuning = viewModel::deleteTuning,
                            onEditTuning = onEditTuning
                        )
                    }

                    TuningMode.CHROMATIC -> ChromaticModeContent()
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            LoadingIndicator()
                        }
                    }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StandardModeContent(
    tuningsUiState: UiState<List<TuningListItem>>,
    selectedTuningId: Int?,
    instruments: List<Instrument>?,
    filterInstrumentId: Int?,
    searchQuery: String,
    onSelectTuning: (Int) -> Unit,
    onSelectFilterInstrumentId: (Int) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEditTuning: (Int) -> Unit,
    onDeleteTuning: (Int) -> Unit,
) {
    var pendingTuningToDelete by remember { mutableStateOf<TuningListItem?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            modifier = Modifier.padding(vertical = 8.dp),
            text = stringResource(R.string.tuner_settings_tunings_header),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.tuner_settings_search_placeholder)) },
            leadingIcon = {
                Icon(Icons.Rounded.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.tuner_settings_search_clear_description)
                        )
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )

        instruments?.let {
            InstrumentFilterChipGroup(
                instruments = it,
                selectedId = filterInstrumentId,
                onInstrumentSelected = { id -> onSelectFilterInstrumentId(id) },
                modifier = Modifier
                    .padding(vertical = 8.dp)
            )
        }

        when (tuningsUiState) {
            is UiState.Loading -> {
                Box (
                    contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                ) {
                    LoadingIndicator() //TODO: Replace with a shimmer effect
                }
            }
            is UiState.Error -> {
                UserInfo(
                    mainText = stringResource(R.string.core_error_generic_title),
                    supportingText = stringResource(tuningsUiState.errorRes),
                    modifier = Modifier
                        .fillMaxSize(),
                )
            }
            is UiState.Loaded -> {
                val tunings by remember(tuningsUiState) { mutableStateOf(tuningsUiState.data) }

                if (tunings.isEmpty()) {
                    UserInfo(
                        mainText = stringResource(R.string.tuner_settings_tunings_empty_message),
                        supportingText = stringResource(R.string.tuner_settings_tunings_empty_supporting),
                        modifier = Modifier
                            .fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                    ) {
                        itemsIndexed(
                            items = tunings,
                            key = { _, tuning -> tuning.tuningId },
                        ) { index, tuning ->

                            TuningListItem(
                                tuning = tuning,
                                shapes = ListItemDefaults.segmentedShapes(
                                    index = index,
                                    count = tunings.size,
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
        }


    }

    pendingTuningToDelete?.let { tuning ->
        DeleteItemDialog(
            titleRes = R.string.tuner_tuning_delete_title,
            messageRes = R.string.core_delete_confirmation_message,
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
private fun ChromaticModeContent() {
    UserInfo(
        mainText = stringResource(R.string.tuner_settings_chromatic_info_title),
        supportingText = stringResource(R.string.tuner_settings_chromatic_info_supporting),
        modifier = Modifier
            .fillMaxSize(),
    )
}
