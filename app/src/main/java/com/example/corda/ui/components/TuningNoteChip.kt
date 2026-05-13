package com.example.corda.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TuningNoteChip(
    note: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isTuned: Boolean = false
) {
    FilterChip(
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        horizontalArrangement = Arrangement.Center,
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                text = note,
                style = MaterialTheme.typography.titleMedium
            )
        },
        trailingIcon = if (isTuned) {
            {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Tuned",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null
    )
}
