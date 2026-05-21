package com.example.corda.ui.screen.inspirations.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.corda.data.inspirations.model.InspirationAttribute

/**
 * Scrollable form body for inspiration detail and edit screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InspirationFormContent(
    name: String,
    description: String,
    labels: List<String>,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    availableLabels: List<String> = emptyList(),
    showDeleteButton: Boolean = false,
    editActions: InspirationEditActions? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp
            )
    ) {
        if (isEditing && editActions != null) {
            InspirationEditFields(
                name = name,
                description = description,
                labels = labels,
                availableLabels = availableLabels,
                showDeleteButton = showDeleteButton,
                actions = editActions
            )
        } else {
            InspirationDetailFields(
                name = name,
                description = description,
                labels = labels
            )
        }

        Spacer(
            modifier = Modifier
                .height(12.dp)
        )
    }
}
