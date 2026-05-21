package com.example.corda.ui.screen.inspirations.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.data.inspirations.model.InspirationAttribute
import com.example.corda.ui.screen.inspirations.InspirationsViewModel
import com.example.corda.ui.screen.inspirations.components.InspirationEditActions
import com.example.corda.ui.screen.inspirations.components.InspirationFormContent
import com.example.corda.ui.screen.inspirations.components.InspirationFormShell
import com.example.corda.ui.screen.inspirations.components.InspirationOutlinedTextField
import java.util.UUID

private data class AttributeDialogState(
    val id: String? = null,
    val label: String = "",
    val url: String = ""
)

/**
 * Inspiration add/edit screen.
 *
 * @param id null when creating a new inspiration, non-null when editing an existing one
 * @param onBack navigate back without saving
 * @param onSaved navigate back after saving
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspirationEditScreen(
    id: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspirationsViewModel = hiltViewModel()
) {
    val state by viewModel.editState.collectAsStateWithLifecycle()
    var attributeDialog by remember { mutableStateOf<AttributeDialogState?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(id) {
        viewModel.startEditing(id)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.inspiration_delete)) },
            text = { Text(stringResource(R.string.inspiration_delete_message, state.name)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteInspiration(state.id!!, onSaved)
                }) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    attributeDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { attributeDialog = null },
            title = {
                Text(
                    stringResource(
                        if (dialog.id == null) R.string.inspiration_add_attribute
                        else R.string.inspiration_edit_attribute
                    )
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InspirationOutlinedTextField(
                        value = dialog.label,
                        onValueChange = { attributeDialog = dialog.copy(label = it) },
                        label = { Text(stringResource(R.string.inspiration_attribute_label)) },
                        singleLine = true
                    )
                    InspirationOutlinedTextField(
                        value = dialog.url,
                        onValueChange = { attributeDialog = dialog.copy(url = it) },
                        label = { Text("URL") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val attr = InspirationAttribute(
                            id = dialog.id ?: UUID.randomUUID().toString(),
                            label = dialog.label,
                            url = dialog.url
                        )
                        if (dialog.id == null) viewModel.addAttribute(attr)
                        else viewModel.updateAttribute(attr)
                        attributeDialog = null
                    },
                    enabled = dialog.label.isNotBlank()
                ) { Text(stringResource(R.string.action_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { attributeDialog = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    InspirationFormShell(
        modifier = modifier,
        isEditing = true,
        onBack = onBack,
        onSave = { viewModel.saveInspiration(onSaved) },
        saveEnabled = !state.isSaving,
        showHeroEditOverlay = true
    ) {
        InspirationFormContent(
            name = state.name,
            description = state.description,
            labels = state.labels,
            isEditing = true,
            availableLabels = state.availableLabels,
            showDeleteButton = state.id != null,
            editActions = InspirationEditActions(
                onNameChange = viewModel::updateEditName,
                onDescriptionChange = viewModel::updateEditDescription,
                onRemoveLabel = viewModel::removeLabel,
                onAddLabel = viewModel::addLabel,
                onAttributeEdit = { attribute ->
                    attributeDialog = AttributeDialogState(
                        id = attribute.id,
                        label = attribute.label,
                        url = attribute.url
                    )
                },
                onAttributeDelete = viewModel::removeAttribute,
                onAddAttribute = { attributeDialog = AttributeDialogState() },
                onDelete = { showDeleteDialog = true }
            )
        )
    }
}
