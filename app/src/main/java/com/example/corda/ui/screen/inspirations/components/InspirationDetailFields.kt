package com.example.corda.ui.screen.inspirations.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun InspirationDetailFields(
    inspiration: InspirationWithLabels,
) {
    val entity = inspiration.inspiration

    Text(
        text = entity.name,
        style = MaterialTheme.typography.headlineMedium,
    )

    if (inspiration.labels.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            inspiration.labels.forEach { label ->
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text(label.name) },
                )
            }
        }
    }

    if (entity.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.inspiration_description),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = entity.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
