package com.example.corda.data.inspirations.local

import com.example.corda.data.inspirations.local.entities.MediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InspirationsTypeConvertersTest {

    private val converters = InspirationsTypeConverters()

    @Test
    fun fromMediaType_image_returnsImageString() {
        assertEquals("IMAGE", converters.fromMediaType(MediaType.IMAGE))
    }

    @Test
    fun toMediaType_imageString_returnsImage() {
        assertEquals(MediaType.IMAGE, converters.toMediaType("IMAGE"))
    }

    @Test
    fun fromMediaType_null_returnsNull() {
        assertNull(converters.fromMediaType(null))
    }

    @Test
    fun toMediaType_null_returnsNull() {
        assertNull(converters.toMediaType(null))
    }
}
