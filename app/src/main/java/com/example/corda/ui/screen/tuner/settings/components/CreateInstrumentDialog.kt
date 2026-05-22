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

@Composable
fun CreateInstrumentDialog(
    ctx: Context,
    onDismiss: () -> Unit,
    onCreate: (name: String, stringCount: Int) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var stringCountText by rememberSaveable { mutableStateOf("") }
    val res = ctx.resources

    val parsedCount = stringCountText.toIntOrNull()
    val isCountValid = parsedCount != null && parsedCount in 2..24
    val isFormValid = name.isNotBlank() && isCountValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(res.getString(R.string.instrument_new)) },
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
                    placeholder = { Text("2-24") },
                    singleLine = true,
                    isError = stringCountText.isNotEmpty() && !isCountValid,
                    supportingText = if (stringCountText.isNotEmpty() && !isCountValid) {
                        { Text(res.getString(R.string.instrument_string_count_error)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, parsedCount!!) },
                enabled = isFormValid,
            ) {
                Text(res.getString(R.string.action_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(res.getString(R.string.action_cancel))
            }
        },
    )
}
