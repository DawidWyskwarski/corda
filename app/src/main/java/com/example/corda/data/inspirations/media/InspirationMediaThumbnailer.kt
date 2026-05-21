package com.example.corda.data.inspirations.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.os.Build
import com.example.corda.data.inspirations.local.entities.MediaType
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class ThumbnailResult(
    val path: String,
    val aspectRatio: Float,
)

/**
 * Builds small JPEG previews from large originals without decoding full 8K bitmaps.
 */
object InspirationMediaThumbnailer {

    fun createThumbnail(
        sourcePath: String,
        type: MediaType,
        destFile: File,
    ): Result<ThumbnailResult> = runCatching {
        destFile.parentFile?.mkdirs()
        when (type) {
            MediaType.IMAGE -> createImageThumbnail(sourcePath, destFile)
            MediaType.VIDEO -> createVideoThumbnail(sourcePath, destFile)
        }
    }

    private fun createImageThumbnail(sourcePath: String, destFile: File): ThumbnailResult {
        val bounds = readImageBounds(sourcePath)
            ?: error("Could not read image dimensions.")
        val aspectRatio = bounds.width.toFloat() / bounds.height.toFloat()
        val bitmap = decodeSampledBitmap(sourcePath, bounds, InspirationMediaLimits.THUMB_MAX_LONG_EDGE)
            ?: decodeWithImageDecoder(sourcePath, InspirationMediaLimits.THUMB_MAX_LONG_EDGE)
            ?: error("Could not decode image for thumbnail.")
        try {
            writeJpeg(destFile, bitmap)
        } finally {
            if (!bitmap.isRecycled) bitmap.recycle()
        }
        return ThumbnailResult(destFile.absolutePath, aspectRatio)
    }

    private fun createVideoThumbnail(sourcePath: String, destFile: File): ThumbnailResult {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(sourcePath)
            val width = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull() ?: error("Missing video width.")
            val height = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull() ?: error("Missing video height.")
            val rotation = retriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                ?.toIntOrNull() ?: 0
            val (w, h) = if (rotation == 90 || rotation == 270) height to width else width to height
            val aspectRatio = w.toFloat() / h.toFloat()
            val frame = retriever.getFrameAtTime(1_000_000)
                ?: error("Could not extract video frame.")
            val scaled = scaleToMaxLongEdge(frame, InspirationMediaLimits.THUMB_MAX_LONG_EDGE)
            try {
                writeJpeg(destFile, scaled)
            } finally {
                if (scaled !== frame && !scaled.isRecycled) scaled.recycle()
                if (!frame.isRecycled) frame.recycle()
            }
            return ThumbnailResult(destFile.absolutePath, aspectRatio)
        } finally {
            retriever.release()
        }
    }

    private fun readImageBounds(path: String): ImageBounds? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, options)
        if (options.outWidth > 0 && options.outHeight > 0) {
            return ImageBounds(options.outWidth, options.outHeight)
        }
        return null
    }

    private fun decodeSampledBitmap(
        path: String,
        bounds: ImageBounds,
        maxLongEdge: Int,
    ): Bitmap? {
        val sampleSize = calculateInSampleSize(bounds.width, bounds.height, maxLongEdge)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        return BitmapFactory.decodeFile(path, options)
    }

    private fun decodeWithImageDecoder(path: String, maxLongEdge: Int): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        val bounds = readImageBounds(path) ?: return null
        val scale = maxLongEdge.toFloat() / max(bounds.width, bounds.height)
        val targetW = (bounds.width * scale).roundToInt().coerceAtLeast(1)
        val targetH = (bounds.height * scale).roundToInt().coerceAtLeast(1)
        val source = ImageDecoder.createSource(File(path))
        return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.setTargetSize(targetW, targetH)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    }

    private fun scaleToMaxLongEdge(bitmap: Bitmap, maxLongEdge: Int): Bitmap {
        val longEdge = max(bitmap.width, bitmap.height)
        if (longEdge <= maxLongEdge) return bitmap
        val scale = maxLongEdge.toFloat() / longEdge
        val targetW = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val targetH = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    private fun writeJpeg(file: File, bitmap: Bitmap) {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, InspirationMediaLimits.THUMB_JPEG_QUALITY, out)
        }
    }

    private fun calculateInSampleSize(width: Int, height: Int, maxLongEdge: Int): Int {
        var inSampleSize = 1
        val longEdge = max(width, height)
        while (longEdge / inSampleSize > maxLongEdge * 2) {
            inSampleSize *= 2
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private data class ImageBounds(val width: Int, val height: Int)
}
