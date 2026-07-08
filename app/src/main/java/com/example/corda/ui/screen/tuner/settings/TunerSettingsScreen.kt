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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import com.example.corda.domain.tuner.TuningMode
import com.example.corda.ui.components.FABMenu
import com.example.corda.ui.components.FABMenuItem
import com.example.corda.ui.components.FilterChipGroup
import com.example.corda.ui.components.SimpleSingleChoiceButtonGroup
import com.example.corda.ui.components.SingleClickIconButton
import com.example.corda.ui.components.UserInfo
import com.example.corda.ui.screen.tuner.TunerViewModel
import com.example.corda.ui.screen.tuner.settings.components.InstrumentManagementBottomSheet
import com.example.corda.ui.screen.tuner.settings.components.TuningListItem

/**
 * Screen for the tuner settings.
 *
 * @param sharedViewModel shared ViewModel scoped to the tuner feature (selected tuning, mode)
 * @param settingsViewModel screen-specific ViewModel for search, filter, and instrument list
 * @param onBack lambda reporting an event to `CordaApp` to go back
 * @param onAddTuning lambda to navigate to the Add Tuning screen
 * @param onEditTuning lambda to navigate to the Edit Tuning screen with the tuning ID
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TunerSettingsScreen(
    sharedViewModel: TunerViewModel,
    settingsViewModel: TunerSettingsViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onAddTuning: () -> Unit,
    onEditTuning: (Int) -> Unit,
) {
    val selectedMode by sharedViewModel.selectedMode.collectAsStateWithLifecycle()
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
            sharedViewModel.updateSelectedTuningLastUsed()
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
                onItemSelected = { sharedViewModel.selectMode(it) }
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
                    TuningMode.STANDARD -> TuningsContent(
                        sharedViewModel = sharedViewModel,
                        settingsViewModel = settingsViewModel,
                        onEditTuning = onEditTuning,
                    )
                    TuningMode.CHROMATIC -> ChromaticContent()
                }
            }
        }
    }

    if (isInstrumentSheetOpen) {
        InstrumentManagementBottomSheet(
            settingsViewModel = settingsViewModel,
            onDismiss = { isInstrumentSheetOpen = false },
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TuningsContent(
    sharedViewModel: TunerViewModel,
    settingsViewModel: TunerSettingsViewModel,
    onEditTuning: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredTunings by settingsViewModel.filteredTunings.collectAsStateWithLifecycle()
    val selectedTuning by sharedViewModel.selectedTuning.collectAsStateWithLifecycle()
    val searchQuery by settingsViewModel.searchQuery.collectAsStateWithLifecycle()
    val instruments by settingsViewModel.instruments.collectAsStateWithLifecycle()
    val selectedInstrument by settingsViewModel.selectedInstrument.collectAsStateWithLifecycle()

    val count by remember { derivedStateOf { filteredTunings.size } }

    var deleteTuningId by remember { mutableStateOf<Int?>(null) }

    Column(modifier = modifier.fillMaxSize()) {

        if (filteredTunings.isEmpty() && selectedTuning == null) {

            UserInfo(
                modifier = Modifier
                    .fillMaxSize(),
                mainText = "No tunings found",
                supportingText = "Tap + to add the one want"
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
                        onQueryChange = { settingsViewModel.setSearchQuery(it) },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_tunings)) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { settingsViewModel.setSearchQuery("") }) {
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
                items = instruments.map { it.name },
                selectedItem = selectedInstrument,
                onItemSelected = { settingsViewModel.setSelectedInstrument(it) }
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
                        isSelected = tuning.tuningId == selectedTuning?.tuningId,
                        onClick = { sharedViewModel.selectTuning(tuning) },
                        onEdit = { onEditTuning(tuning.tuningId) },
                        onDelete = { deleteTuningId = tuning.tuningId },
                    )
                }
            }
        }
    }

    if (deleteTuningId != null) {
        val tuningToDelete = filteredTunings.find { it.tuningId == deleteTuningId }
        AlertDialog(
            onDismissRequest = { deleteTuningId = null },
            title = { Text("Delete tuning") },
            text = {
                Text(
                    "Are you sure you want to delete \"${tuningToDelete?.tuningName ?: ""}\"? " +
                            "This action cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTuningId?.let { settingsViewModel.deleteTuning(it) }
                        deleteTuningId = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTuningId = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ChromaticContent(
    modifier: Modifier = Modifier
) {
    UserInfo(
        modifier = modifier
            .fillMaxSize(),
        mainText = stringResource(R.string.chromatic_description),
        supportingText = stringResource(R.string.dont_select_tuning)
    )
}
