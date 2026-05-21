package com.example.corda.ui.screen.inspirations.addedit

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.corda.R
import com.example.corda.ui.screen.inspirations.InspirationsViewModel
import com.example.corda.ui.screen.inspirations.components.InspirationEditActions
import com.example.corda.ui.screen.inspirations.components.InspirationFormContent
import com.example.corda.ui.screen.inspirations.components.InspirationFormShell

/**
 * Inspiration add/edit screen.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InspirationEditScreen(
    id: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InspirationsViewModel = hiltViewModel(),
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val edit by viewModel.editInspiration.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val mediaImportError by viewModel.mediaImportError.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) viewModel.importEditMediaFromPicker(uri)
    }

    val launchMediaPicker = remember(pickMediaLauncher) {
        {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo),
            )
        }
    }

    LaunchedEffect(id) {
        viewModel.startEditing(id)
    }

    if (mediaImportError) {
        AlertDialog(
            onDismissRequest = viewModel::clearMediaImportError,
            title = { Text(stringResource(R.string.inspiration_media_import_failed)) },
            confirmButton = {
                TextButton(onClick = viewModel::clearMediaImportError) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.inspiration_delete)) },
            text = {
                Text(
                    stringResource(
                        R.string.inspiration_delete_message,
                        edit.inspiration.name,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInspiration(edit.inspiration.inspirationId, onDeleted)
                    },
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    val entity = edit.inspiration
    val hasMedia = entity.mediaPath != null && entity.mediaType != null

    InspirationFormShell(
        modifier = modifier,
        isEditing = true,
        onBack = onBack,
        onSave = { viewModel.saveInspiration(onSaved) },
        saveEnabled = !isSaving,
        showHeroEditOverlay = true,
        mediaPath = entity.mediaPath,
        mediaType = entity.mediaType,
        onEditMediaClick = launchMediaPicker,
        onEditMediaLongClick = if (hasMedia) {
            { viewModel.clearEditMedia() }
        } else {
            null
        },
    ) {
        InspirationFormContent(
            inspiration = edit,
            isEditing = true,
            availableLabels = listState.availableLabels,
            showDeleteButton = entity.inspirationId != 0L,
            editActions = InspirationEditActions(
                onNameChange = viewModel::updateEditName,
                onDescriptionChange = viewModel::updateEditDescription,
                onRemoveLabel = viewModel::removeLabel,
                onAddLabel = viewModel::addLabel,
                onDelete = { showDeleteDialog = true },
            ),
        )
    }
}
