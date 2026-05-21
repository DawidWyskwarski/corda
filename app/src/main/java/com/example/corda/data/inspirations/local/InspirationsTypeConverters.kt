package com.example.corda.data.inspirations.local

import androidx.room.TypeConverter
import com.example.corda.data.inspirations.local.entities.MediaType

class InspirationsTypeConverters {

    @TypeConverter
    fun fromMediaType(value: MediaType?): String? = value?.name

    @TypeConverter
    fun toMediaType(value: String?): MediaType? =
        value?.let { MediaType.valueOf(it) }
}
