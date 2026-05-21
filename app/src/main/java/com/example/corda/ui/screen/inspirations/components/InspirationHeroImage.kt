package com.example.corda.ui.screen.inspirations.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

internal val InspirationHeroHeight = 320.dp

internal val InspirationHeroBottomShape = RoundedCornerShape(
    bottomStart = 24.dp,
    bottomEnd = 24.dp
)

private val HeroTopScrim = Brush.verticalGradient(
    colors = listOf(
        Color.Black.copy(alpha = 0.45f),
        Color.Transparent
    ),
    startY = 0f,
    endY = 220f
)

/**
 * Full-bleed hero image for inspiration detail and edit screens.
 */
@Composable
fun InspirationHeroImage(
    modifier: Modifier = Modifier,
    showEditOverlay: Boolean = false
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(InspirationHeroHeight)
                .clip(InspirationHeroBottomShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(InspirationHeroHeight)
                .clip(InspirationHeroBottomShape)
                .background(HeroTopScrim)
        )

        if (showEditOverlay) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.35f),
                contentColor = Color.White,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
