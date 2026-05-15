package com.example.corda.ui.screen.tuner.addtuning

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.data.tuner.local.entities.Instrument
import com.example.corda.ui.screen.tuner.components.TuningSoundGrid
import com.example.corda.ui.screen.tuner.components.VerticalNoteCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTuningScreen(
    viewModel: AddEditTuningViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tuningName by viewModel.tuningName.collectAsStateWithLifecycle()
    val selectedInstrument by viewModel.selectedInstrument.collectAsStateWithLifecycle()
    val instruments by viewModel.instruments.collectAsStateWithLifecycle()
    val allSounds by viewModel.allSounds.collectAsStateWithLifecycle()
    val selectedStringIndex by viewModel.selectedStringIndex.collectAsStateWithLifecycle()
    val isSaveEnabled by viewModel.isSaveEnabled.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()

    LaunchedEffect(saved) {
        if (saved) onBack()
    }

    val isEditMode = viewModel.isEditMode
    val title = if (isEditMode) "Edit tuning" else "Add tuning"
    val actionLabel = if (isEditMode) "Save" else "Add"

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveTuning() },
                        enabled = isSaveEnabled,
                    ) {
                        Text(actionLabel)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tuningName,
                onValueChange = { viewModel.setTuningName(it) },
                label = { Text("Tuning name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            InstrumentDropdown(
                instruments = instruments,
                selectedInstrument = selectedInstrument,
                enabled = !isEditMode,
                onInstrumentSelected = { viewModel.selectInstrument(it) },
            )

            if (viewModel.stringSounds.isNotEmpty()) {
                TuningSoundGrid(
                    sounds = viewModel.stringSounds,
                    selectedIndex = selectedStringIndex,
                    onIndexSelected = { viewModel.selectString(it) },
                )
            }

            AnimatedVisibility(
                visible = selectedStringIndex != null && allSounds.isNotEmpty(),
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    val currentSound = selectedStringIndex?.let { idx ->
                        viewModel.stringSounds.getOrNull(idx)
                    }

                    if (currentSound != null) {
                        VerticalNoteCarousel(
                            sounds = allSounds,
                            selectedSound = currentSound,
                            onSoundSelected = { viewModel.setNoteForSelectedString(it) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstrumentDropdown(
    instruments: List<Instrument>,
    selectedInstrument: Instrument?,
    enabled: Boolean,
    onInstrumentSelected: (Instrument) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedInstrument?.name ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Instrument") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            instruments.forEach { instrument ->
                DropdownMenuItem(
                    text = { Text(instrument.name) },
                    onClick = {
                        onInstrumentSelected(instrument)
                        expanded = false
                    },
                )
            }
        }
    }
}
