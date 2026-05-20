package com.example.corda.ui.screen.inspirations.addedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.data.inspirations.model.InspirationAttribute
import com.example.corda.ui.screen.inspirations.InspirationsViewModel
import com.example.corda.ui.screen.inspirations.components.InspirationAttributeItem
import com.example.corda.ui.screen.inspirations.components.InspirationImagePlaceholder
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
    var showLabelPicker by remember { mutableStateOf(false) }
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
                    OutlinedTextField(
                        value = dialog.label,
                        onValueChange = { attributeDialog = dialog.copy(label = it) },
                        label = { Text(stringResource(R.string.inspiration_attribute_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = dialog.url,
                        onValueChange = { attributeDialog = dialog.copy(url = it) },
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveInspiration(onSaved) },
                        enabled = !state.isSaving
                    ) {
                        Text(stringResource(R.string.action_confirm))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                InspirationImagePlaceholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .padding(horizontal = 16.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                )
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::updateEditName,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.inspiration_name_hint)) },
                    textStyle = MaterialTheme.typography.headlineMedium,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.labels.forEach { label ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.removeLabel(label) },
                            label = { Text(label) }
                        )
                    }
                    Box {
                        FilterChip(
                            selected = false,
                            onClick = { showLabelPicker = true },
                            label = { Text("+") }
                        )
                        DropdownMenu(
                            expanded = showLabelPicker,
                            onDismissRequest = { showLabelPicker = false }
                        ) {
                            val unusedLabels = state.availableLabels.filter { it !in state.labels }
                            if (unusedLabels.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.inspiration_no_labels_available)) },
                                    onClick = { showLabelPicker = false },
                                    enabled = false
                                )
                            } else {
                                unusedLabels.forEach { label ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.addLabel(label)
                                            showLabelPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.inspiration_description),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::updateEditDescription,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    placeholder = { Text(stringResource(R.string.inspiration_description_hint)) },
                    maxLines = 8
                )

                if (state.attributes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.inspiration_additional_attributes),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.attributes.forEach { attribute ->
                            InspirationAttributeItem(
                                attribute = attribute,
                                isEditing = true,
                                onEditClick = {
                                    attributeDialog = AttributeDialogState(
                                        id = attribute.id,
                                        label = attribute.label,
                                        url = attribute.url
                                    )
                                },
                                onDeleteClick = { viewModel.removeAttribute(attribute.id) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { attributeDialog = AttributeDialogState() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddBox,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(R.string.inspiration_add_attribute))
                }

                if (state.id != null) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(stringResource(R.string.inspiration_delete))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
