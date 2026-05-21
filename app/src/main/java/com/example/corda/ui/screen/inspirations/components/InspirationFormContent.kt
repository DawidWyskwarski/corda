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
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels

/**
 * Scrollable form body for inspiration detail and edit screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InspirationFormContent(
    inspiration: InspirationWithLabels,
    isEditing: Boolean,
    modifier: Modifier = Modifier,
    availableLabels: List<LabelEntity> = emptyList(),
    showDeleteButton: Boolean = false,
    editActions: InspirationEditActions? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        if (isEditing && editActions != null) {
            InspirationEditFields(
                inspiration = inspiration,
                availableLabels = availableLabels,
                showDeleteButton = showDeleteButton,
                actions = editActions,
            )
        } else {
            InspirationDetailFields(inspiration = inspiration)
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
