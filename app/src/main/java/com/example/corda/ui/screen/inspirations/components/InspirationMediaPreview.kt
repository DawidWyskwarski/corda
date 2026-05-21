package com.example.corda.ui.screen.inspirations.components

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.corda.data.inspirations.local.entities.MediaType
import com.example.corda.data.inspirations.media.InspirationMediaLimits
import java.io.File

enum class InspirationMediaPreviewSize {
    Grid,
    Hero,
}

@Composable
fun InspirationMediaPreview(
    mediaPath: String?,
    mediaType: MediaType?,
    modifier: Modifier = Modifier,
    thumbnailPath: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    previewSize: InspirationMediaPreviewSize = InspirationMediaPreviewSize.Grid,
    playVideoWhenVisible: Boolean = false,
) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (mediaPath.isNullOrBlank() || mediaType == null) {
            return@Box
        }

        val decodePx = when (previewSize) {
            InspirationMediaPreviewSize.Grid -> InspirationMediaLimits.GRID_DECODE_PX
            InspirationMediaPreviewSize.Hero -> InspirationMediaLimits.HERO_DECODE_PX
        }
        val displayPath = when (previewSize) {
            InspirationMediaPreviewSize.Grid -> thumbnailPath ?: mediaPath
            InspirationMediaPreviewSize.Hero -> mediaPath
        }
        val uri = remember(displayPath) { Uri.fromFile(File(displayPath)) }
        val context = LocalContext.current

        when (mediaType) {
            MediaType.IMAGE -> {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .size(Size(decodePx, decodePx))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                )
            }
            MediaType.VIDEO -> {
                if (playVideoWhenVisible) {
                    val player = remember(mediaPath) {
                        ExoPlayer.Builder(context).build().apply {
                            setMediaItem(MediaItem.fromUri(Uri.fromFile(File(mediaPath))))
                            prepare()
                            playWhenReady = true
                            repeatMode = Player.REPEAT_MODE_OFF
                        }
                    }

                    DisposableEffect(player) {
                        onDispose { player.release() }
                    }

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                )
                                this.player = player
                                useController = true
                            }
                        },
                        update = { view -> view.player = player },
                    )
                } else {
                    val posterPath = thumbnailPath
                    if (!posterPath.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.fromFile(File(posterPath)))
                                .size(Size(decodePx, decodePx))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = contentScale,
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
    }
}
