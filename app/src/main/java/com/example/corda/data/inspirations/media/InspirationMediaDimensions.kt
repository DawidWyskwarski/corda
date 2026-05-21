package com.example.corda.data.inspirations.media

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.example.corda.data.inspirations.local.entities.MediaType
import java.io.File

object InspirationMediaDimensions {
    fun aspectRatio(path: String, type: MediaType): Float? {
        val file = File(path)
        if (!file.isFile) return null
        return when (type) {
            MediaType.IMAGE -> imageAspectRatio(path)
            MediaType.VIDEO -> videoAspectRatio(path)
        }
    }

    private fun imageAspectRatio(path: String): Float? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        val width = options.outWidth
        val height = options.outHeight
        if (width <= 0 || height <= 0) return null
        return width.toFloat() / height.toFloat()
    }

    private fun videoAspectRatio(path: String): Float? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: return null
            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: return null
            val rotation = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull() ?: 0
            val (w, h) = if (rotation == 90 || rotation == 270) height to width else width to height
            if (w <= 0 || h <= 0) null else w.toFloat() / h.toFloat()
        } finally {
            retriever.release()
        }
    }
}
