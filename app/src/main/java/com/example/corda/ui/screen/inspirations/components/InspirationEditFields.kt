package com.example.corda.ui.screen.inspirations.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InspirationEditFields(
    inspiration: InspirationWithLabels,
    availableLabels: List<LabelEntity>,
    showDeleteButton: Boolean,
    actions: InspirationEditActions,
) {
    var showLabelPicker by remember { mutableStateOf(false) }
    val entity = inspiration.inspiration
    val selectedLabelIds = inspiration.labels.map { it.labelId }.toSet()

    InspirationTitleTextField(
        value = entity.name,
        onValueChange = actions.onNameChange,
        placeholder = {
            Text(
                text = stringResource(R.string.inspiration_name_hint),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    Spacer(modifier = Modifier.height(6.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        inspiration.labels.forEach { label ->
            InputChip(
                selected = true,
                onClick = { actions.onRemoveLabel(label) },
                label = { Text(label.name) },
            )
        }

        Box {
            FilterChip(
                selected = false,
                onClick = { showLabelPicker = true },
                label = { Text("+") },
            )

            DropdownMenu(
                expanded = showLabelPicker,
                onDismissRequest = { showLabelPicker = false },
            ) {
                val unusedLabels = availableLabels.filter { it.labelId !in selectedLabelIds }

                if (unusedLabels.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.inspiration_no_labels_available)) },
                        onClick = { showLabelPicker = false },
                        enabled = false,
                    )
                } else {
                    unusedLabels.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(label.name) },
                            onClick = {
                                actions.onAddLabel(label)
                                showLabelPicker = false
                            },
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = stringResource(R.string.inspiration_description),
        style = MaterialTheme.typography.titleMedium,
    )

    Spacer(modifier = Modifier.height(4.dp))

    InspirationOutlinedTextField(
        value = entity.description,
        onValueChange = actions.onDescriptionChange,
        placeholder = { Text(stringResource(R.string.inspiration_description_hint)) },
        maxLines = 8,
        modifier = Modifier.height(160.dp),
    )

    if (showDeleteButton) {
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = actions.onDelete,
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.inspiration_delete))
        }
    }
}
