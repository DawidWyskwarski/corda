package com.example.corda.ui.screen.tuner.settings.components

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.ui.screen.tuner.settings.InstrumentRow
import com.example.corda.ui.screen.tuner.settings.TunerSettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentManagementBottomSheet(
    settingsViewModel: TunerSettingsViewModel,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val deleteBlockedMessage = stringResource(R.string.instrument_delete_blocked_has_tunings)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(Unit) {
        sheetState.partialExpand()
    }

    val instrumentRows by settingsViewModel.instrumentRows.collectAsStateWithLifecycle()

    var editTarget by remember { mutableStateOf<InstrumentRow?>(null) }
    var deleteTarget by remember { mutableStateOf<InstrumentRow?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = "Instruments",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(
                    items = instrumentRows,
                    key = { it.instrument.instrumentId },
                ) { row ->
                    InstrumentListItem(
                        instrument = row.instrument,
                        onEdit = { editTarget = row },
                        onDelete = {
                            if (row.tuningCount > 0) {
                                Toast.makeText(
                                    context,
                                    deleteBlockedMessage,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            } else {
                                deleteTarget = row
                            }
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showCreateDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("New instrument")
            }
        }
    }

    if (showCreateDialog) {
        CreateInstrumentDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, count ->
                settingsViewModel.createInstrument(name, count)
                showCreateDialog = false
            },
        )
    }

    if (editTarget != null) {
        val target = editTarget!!
        EditInstrumentDialog(
            instrument = target.instrument,
            tuningCount = target.tuningCount,
            onDismiss = { editTarget = null },
            onSave = { newName, newCount ->
                settingsViewModel.updateInstrument(target.instrument, newName, newCount)
                editTarget = null
            },
        )
    }

    if (deleteTarget != null) {
        val target = deleteTarget!!
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete instrument") },
            text = {
                Text(
                    "Are you sure you want to delete \"${target.instrument.name}\"? " +
                        "This action cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.deleteInstrument(target.instrument)
                        deleteTarget = null
                    },
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}
