package com.example.corda.data.inspirations.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Inspiration")
data class InspirationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "inspiration_id")
    val inspirationId: Long = 0,
    val name: String,
    val description: String,
    @ColumnInfo(name = "media_path")
    val mediaPath: String? = null,
    @ColumnInfo(name = "media_type")
    val mediaType: MediaType? = null,
    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String? = null,
    @ColumnInfo(name = "media_aspect_ratio")
    val mediaAspectRatio: Float? = null,
)
