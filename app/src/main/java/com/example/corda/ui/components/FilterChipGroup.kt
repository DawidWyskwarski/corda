package com.example.corda.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.corda.R

@Composable
fun FilterChipGroup(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedItem: String?,
    onItemSelected: (String?) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp),
    ) {
        item(
            key = "chip_all"
        ) {
            FilterChip(
                selected = selectedItem == null,
                onClick = { onItemSelected(null) },
                label = { Text(stringResource(R.string.all)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }

        items(
            items = items,
            key = { "chip_${it}" }
        ) { item ->
            val isSelected = selectedItem == item

            FilterChip(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                label = {
                    Text(item)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            )
        }
    }
}