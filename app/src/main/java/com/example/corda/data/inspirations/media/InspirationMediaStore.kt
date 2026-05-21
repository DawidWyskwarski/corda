package com.example.corda.data.inspirations.media

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.corda.data.inspirations.local.entities.MediaType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class ImportedMedia(
    val path: String,
    val type: MediaType,
    val thumbnailPath: String?,
    val aspectRatio: Float?,
)

@Singleton
class InspirationMediaStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val mediaDir: File
        get() = File(context.filesDir, INSPIRATIONS_DIR).also { it.mkdirs() }

    fun resolveUri(path: String): Uri = Uri.fromFile(File(path))

    fun thumbnailFileForOriginal(originalPath: String): File {
        val original = File(originalPath)
        return File(original.parentFile, "${original.nameWithoutExtension}.thumb.jpg")
    }

    fun importFromUri(uri: Uri): Result<ImportedMedia> = runCatching {
        val mimeType = context.contentResolver.getType(uri)
            ?: throw IllegalArgumentException("Could not determine media type.")
        val mediaType = mimeTypeToMediaType(mimeType)
            ?: throw IllegalArgumentException("Unsupported media type: $mimeType")
        val extension = extensionForMime(mimeType)
        val destFile = File(mediaDir, "${UUID.randomUUID()}.$extension")
        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalArgumentException("Could not read selected media.")
        if (destFile.length() > LARGE_FILE_WARN_BYTES) {
            Log.w(TAG, "Imported media is large (${destFile.length()} bytes): ${destFile.absolutePath}")
        }

        val thumbFile = File(mediaDir, "${destFile.nameWithoutExtension}.thumb.jpg")
        val thumbnail = InspirationMediaThumbnailer.createThumbnail(
            sourcePath = destFile.absolutePath,
            type = mediaType,
            destFile = thumbFile,
        )

        ImportedMedia(
            path = destFile.absolutePath,
            type = mediaType,
            thumbnailPath = thumbnail.getOrNull()?.path,
            aspectRatio = thumbnail.getOrNull()?.aspectRatio
                ?: InspirationMediaDimensions.aspectRatio(destFile.absolutePath, mediaType),
        )
    }

    fun deleteManagedFiles(mediaPath: String?, thumbnailPath: String?) {
        deleteFile(mediaPath)
        deleteFile(thumbnailPath)
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (!file.exists()) return
        if (!isManagedPath(file)) return
        file.delete()
    }

    fun isManagedPath(file: File): Boolean {
        val inspirationsDir = File(context.filesDir, INSPIRATIONS_DIR).canonicalFile
        return file.canonicalFile.parentFile == inspirationsDir
    }

    private fun mimeTypeToMediaType(mimeType: String): MediaType? = when {
        mimeType.startsWith("image/") -> MediaType.IMAGE
        mimeType.startsWith("video/") -> MediaType.VIDEO
        else -> null
    }

    private fun extensionForMime(mimeType: String): String = when (mimeType) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/heic" -> "heic"
        "image/heif" -> "heif"
        "video/mp4" -> "mp4"
        "video/webm" -> "webm"
        "video/quicktime" -> "mov"
        "video/x-matroska" -> "mkv"
        else -> mimeType.substringAfterLast('/').ifBlank { "bin" }
    }

    companion object {
        private const val TAG = "InspirationMediaStore"
        private const val INSPIRATIONS_DIR = "inspirations"
        private const val LARGE_FILE_WARN_BYTES = 500L * 1024 * 1024
    }
}
