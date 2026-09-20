package com.example.corda.tuner.ui.settings.components

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.core.ui.components.DeleteItemDialog
import com.example.corda.core.ui.components.UserInfo
import com.example.corda.core.ui.system.TransparentNavigationBarEffect
import com.example.corda.tuner.data.local.entities.Instrument

@SuppressLint("LocalContextResourcesRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstrumentManagementBottomSheet(
    instruments: List<Instrument>,
    doesInstrumentHaveTunings: (Int) -> Boolean,
    onCreateInstrument: (Instrument) -> Unit,
    onUpdateInstrument: (Instrument) -> Unit,
    onDeleteInstrument: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Capture the localized context here, inside ProvideAppLocale, so resources are correct.
    // We use localizedContext.resources.getString() in dialogs instead of stringResource(), because AlertDialog creates a separate Android window that doesn't reliably inherit the Compose CompositionLocal overrides.
    val localizedContext = LocalContext.current

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
    )

    var pendingInstrumentToEdit by remember { mutableStateOf<Instrument?>(null) }
    var pendingInstrumentToDelete by remember { mutableStateOf<Instrument?>(null) }
    var showCreateInstrumentDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        TransparentNavigationBarEffect()

        // Wrap the bottom sheet content to ensure LocalContext is localized inside the popup.
        CompositionLocalProvider(LocalContext provides localizedContext) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.instrument_management_title),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                )

                if (instruments.isEmpty()) {
                    UserInfo(
                        mainText = stringResource(R.string.instrument_list_empty),
                        supportingText = stringResource(R.string.instrument_list_empty_supporting),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(vertical = 12.dp)
                    ) {
                        items(
                            items = instruments,
                            key = { it.id },
                        ) { instrument ->
                            InstrumentListItem(
                                instrument = instrument,
                                onEdit = { pendingInstrumentToEdit = instrument },
                                onDelete = {
                                    if (doesInstrumentHaveTunings(instrument.id)) {
                                        Toast.makeText(
                                            localizedContext,
                                            localizedContext.resources.getString(R.string.instrument_delete_blocked_has_tunings),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        pendingInstrumentToDelete = instrument
                                    }
                                },
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                Button(
                    onClick = { showCreateInstrumentDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                ) {
                    Text(stringResource(R.string.instrument_new))
                }
            }
        }
    }

    if (showCreateInstrumentDialog) {
        CreateEditInstrumentDialog(
            instrument = null,
            canEditNotesCount = true,
            onDismiss = { showCreateInstrumentDialog = false },
            onSave = {
                onCreateInstrument(it)
                showCreateInstrumentDialog = false
            },
        )
    }

    pendingInstrumentToEdit?.let { instrument ->
        CreateEditInstrumentDialog(
            instrument = instrument,
            canEditNotesCount = !doesInstrumentHaveTunings(instrument.id),
            onDismiss = { pendingInstrumentToEdit = null },
            onSave = {
                onUpdateInstrument(it)
                pendingInstrumentToEdit = null
            },
        )
    }

    pendingInstrumentToDelete?.let { instrument ->
        DeleteItemDialog(
            titleRes = R.string.instrument_delete_title,
            messageRes = R.string.instrument_delete_message,
            itemName = instrument.name,
            onDelete = {
                onDeleteInstrument(instrument.id)
                pendingInstrumentToDelete = null
            },
            onDismiss = { pendingInstrumentToDelete = null }
        )
    }
}
