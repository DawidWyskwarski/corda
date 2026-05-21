package com.example.corda.ui.screen.inspirations.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R

@Composable
internal fun InspirationFormTopBar(
    isEditing: Boolean,
    onBack: () -> Unit,
    onEdit: (() -> Unit)?,
    onSave: (() -> Unit)?,
    saveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InspirationOverlayIconButton(
            onClick = onBack,
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null
        )

        Spacer(modifier = Modifier.weight(1f))

        when {
            isEditing && onSave != null -> {
                TextButton(
                    onClick = onSave,
                    enabled = saveEnabled,
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.action_confirm),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }
            }
            !isEditing && onEdit != null -> {
                InspirationOverlayIconButton(
                    onClick = onEdit,
                    icon = Icons.Outlined.Edit,
                    contentDescription = null
                )
            }
        }
    }
}
