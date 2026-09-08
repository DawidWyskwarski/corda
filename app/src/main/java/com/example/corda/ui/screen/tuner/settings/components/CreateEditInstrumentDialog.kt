package com.example.corda.ui.screen.tuner.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.data.tuner.local.entities.Instrument

/**
 * Dialog for creating a new [Instrument] or editing an existing one.
 *
 * @param instrument the instrument being edited, or `null` to create a new one
 * @param canEditNotesCount whether the note count may be changed; `false` locks the field
 * @param onSave called with the trimmed name and a validated note count
 */
@Composable
fun CreateEditInstrumentDialog(
    instrument: Instrument?,
    canEditNotesCount: Boolean,
    onDismiss: () -> Unit,
    onSave: (Instrument) -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditing = instrument != null

    var name by rememberSaveable(instrument?.id) {
        mutableStateOf(instrument?.name.orEmpty())
    }
    // Held as text so partial and out-of-range input stays visible while typing.
    var notesCountInput by rememberSaveable(instrument?.id) {
        mutableStateOf(instrument?.musicNotesCount?.toString().orEmpty())
    }

    val notesCount = notesCountInput.toByteOrNull()
    val isCountValid = notesCount != null && notesCount in 2..24
    val isFormValid = name.isNotBlank() && isCountValid

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Text(
                text = stringResource(
                    if (isEditing) R.string.instrument_edit_title else R.string.instrument_new
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            text = stringResource(R.string.instrument_name_hint)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = notesCountInput,
                    onValueChange = { notesCountInput = it },
                    label = {
                        Text(
                            text = stringResource(R.string.instrument_string_count_hint)
                        )
                    },
                    placeholder = { Text("2-24") },
                    enabled = canEditNotesCount,
                    singleLine = true,
                    isError = notesCountInput.isNotEmpty() && !isCountValid,
                    supportingText = when {
                        !canEditNotesCount -> {
                            {
                                Text(
                                    stringResource(R.string.instrument_string_count_locked)
                                )
                            }
                        }
                        notesCountInput.isNotEmpty() && !isCountValid -> {
                            {
                                Text(
                                    stringResource(R.string.instrument_string_count_error)
                                )
                            }
                        }
                        else -> null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    notesCount?.let {
                        if (isEditing)
                        {
                            onSave(
                                instrument.copy(
                                    name = name.trim(),
                                    musicNotesCount = it
                                )
                            )
                        } else {
                            onSave(
                                Instrument(
                                    name = name.trim(),
                                    musicNotesCount = it
                                )
                            )
                        }
                    }
                },
                enabled = isFormValid,
            ) {
                Text(
                    text = stringResource(
                        if (isEditing) R.string.action_save else R.string.action_create
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.action_cancel)
                )
            }
        },
    )
}
