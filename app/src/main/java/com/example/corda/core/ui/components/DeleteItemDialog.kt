package com.example.corda.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.corda.R

/**
 * Confirmation dialog for a destructive action.
 *
 * @param titleRes title naming what is being deleted, e.g. `R.string.tuner_tuning_delete_title`
 * @param messageRes body text taking the item name as its single format argument, e.g. the
 * shared `R.string.core_delete_confirmation_message`
 * @param itemName name substituted into [messageRes]
 */
@Composable
fun DeleteItemDialog(
    @StringRes titleRes: Int,
    @StringRes messageRes: Int,
    itemName: String,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(titleRes)) },
        text = { Text(stringResource(messageRes, itemName)) },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.core_action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.core_action_cancel))
            }
        },
        modifier = modifier
    )
}
