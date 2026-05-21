package com.example.corda.ui.screen.inspirations.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.ui.screen.inspirations.InspirationsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabelManagementBottomSheet(
    onDismiss: () -> Unit,
    viewModel: InspirationsViewModel = hiltViewModel()
) {
    // Capture the localized context here, inside ProvideAppLocale, so resources are correct.
    // We use localizedContext.resources.getString() in dialogs instead of stringResource(), because AlertDialog creates a separate Android window that doesn't reliably inherit the Compose CompositionLocal overrides.
    val localizedContext = LocalContext.current

    val state by viewModel.listState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(Unit) {
        sheetState.partialExpand()
    }

    var editTarget by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<String?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        // Wrap the bottom sheet content to ensure LocalContext is localized inside the popup.
        CompositionLocalProvider(LocalContext provides localizedContext) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = stringResource(R.string.labels_title),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(
                        items = state.availableLabels,
                        key = { it.labelId },
                    ) { label ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = label.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            Row {
                                IconButton(onClick = { editTarget = label.name }) {
                                    Icon(Icons.Outlined.Edit, contentDescription = null)
                                }
                                IconButton(onClick = { deleteTarget = label.name }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = null)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.label_new))
                }
            }
        }
    }

    editTarget?.let { label ->
        EditLabelDialog(
            currentName = label,
            ctx = localizedContext,
            onDismiss = { editTarget = null },
            onSave = { newName ->
                viewModel.updateLabel(label, newName)
                editTarget = null
            }
        )
    }

    deleteTarget?.let { label ->
        val res = localizedContext.resources
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(res.getString(R.string.label_delete_title)) },
            text = { Text(res.getString(R.string.label_delete_message, label)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLabel(label)
                    deleteTarget = null
                }) { Text(res.getString(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(res.getString(R.string.action_cancel))
                }
            }
        )
    }

    if (showCreateDialog) {
        CreateLabelDialog(
            ctx = localizedContext,
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createLabel(name)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun EditLabelDialog(
    currentName: String,
    ctx: Context,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf(currentName) }
    val res = ctx.resources

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(res.getString(R.string.label_edit_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(res.getString(R.string.label_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                enabled = name.isNotBlank() && name != currentName
            ) { Text(res.getString(R.string.label_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(res.getString(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun CreateLabelDialog(
    ctx: Context,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    val res = ctx.resources

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(res.getString(R.string.label_new)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(res.getString(R.string.label_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank()
            ) { Text(res.getString(R.string.label_create)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(res.getString(R.string.action_cancel)) }
        }
    )
}
