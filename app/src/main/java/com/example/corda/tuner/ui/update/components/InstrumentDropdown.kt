package com.example.corda.tuner.ui.update.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.corda.R
import com.example.corda.tuner.data.local.entities.Instrument
import com.example.corda.tuner.ui.helpers.displayNameOnly
import com.example.corda.tuner.ui.helpers.displayNameWithNoteCount

@Composable
fun InstrumentDropdown( // TODO: Make this more generic
    instruments: List<Instrument>,
    selectedInstrument: Instrument?,
    enabled: Boolean,
    onInstrumentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && enabled,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedInstrument?.displayNameOnly() ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.tuner_tuning_instrument_label)) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded && enabled)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false },
        ) {
            instruments.forEach { instrument ->
                DropdownMenuItem(
                    text = { Text( instrument.displayNameWithNoteCount() ) },
                    onClick = {
                        onInstrumentSelected(instrument.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
