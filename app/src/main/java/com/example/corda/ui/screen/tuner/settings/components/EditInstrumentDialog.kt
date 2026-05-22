package com.example.corda.ui.screen.tuner.settings.components

import android.content.Context
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.data.tuner.local.entities.Instrument

@Composable
fun EditInstrumentDialog(
    instrument: Instrument,
    tuningCount: Int,
    ctx: Context,
    onDismiss: () -> Unit,
    onSave: (newName: String, newStringCount: Int) -> Unit,
) {
    val hasTunings = tuningCount > 0
    val res = ctx.resources

    var name by rememberSaveable { mutableStateOf(instrument.name) }
    var stringCountText by rememberSaveable {
        mutableStateOf(instrument.soundsCount.toString())
    }

    val parsedCount = stringCountText.toIntOrNull()
    val isCountValid = parsedCount != null && parsedCount in 2..24
    val isFormValid = name.isNotBlank() && isCountValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(res.getString(R.string.instrument_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(res.getString(R.string.instrument_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = stringCountText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            stringCountText = input
                        }
                    },
                    label = { Text(res.getString(R.string.instrument_string_count_hint)) },
                    placeholder = { Text("2–24") },
                    singleLine = true,
                    enabled = !hasTunings,
                    isError = stringCountText.isNotEmpty() && !isCountValid,
                    supportingText = when {
                        hasTunings -> {
                            { Text(res.getString(R.string.instrument_string_count_locked)) }
                        }
                        stringCountText.isNotEmpty() && !isCountValid -> {
                            { Text(res.getString(R.string.instrument_string_count_error)) }
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
                onClick = { onSave(name, parsedCount!!) },
                enabled = isFormValid,
            ) {
                Text(res.getString(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(res.getString(R.string.action_cancel))
            }
        },
    )
}
