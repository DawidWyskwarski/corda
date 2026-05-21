package com.example.corda.ui.screen.inspirations.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.corda.data.inspirations.local.entities.relations.InspirationWithLabels

@Composable
fun InspirationGridCard(
    inspiration: InspirationWithLabels,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entity = inspiration.inspiration

    val mediaModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
        .then(
            when (val ratio = entity.mediaAspectRatio) {
                null -> if (entity.mediaPath != null) {
                    Modifier.aspectRatio(1f)
                } else {
                    Modifier.height(96.dp)
                }
                else -> Modifier.aspectRatio(
                    ratio.coerceIn(GRID_MIN_ASPECT_RATIO, GRID_MAX_ASPECT_RATIO),
                )
            },
        )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column {
            InspirationMediaPreview(
                mediaPath = entity.mediaPath,
                mediaType = entity.mediaType,
                thumbnailPath = entity.thumbnailPath,
                modifier = mediaModifier,
                contentScale = ContentScale.Crop,
                previewSize = InspirationMediaPreviewSize.Grid,
                playVideoWhenVisible = false,
            )

            Text(
                text = entity.name.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            )
        }
    }
}

private const val GRID_MIN_ASPECT_RATIO = 9f / 16f
private const val GRID_MAX_ASPECT_RATIO = 16f / 9f
