package com.example.corda.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleProviderTest {

    @Test
    fun normalizeLanguageTag_polishCode_returnsPl() {
        assertEquals(LANGUAGE_PL, normalizeLanguageTag(LANGUAGE_PL))
    }

    @Test
    fun normalizeLanguageTag_polishLabel_returnsPl() {
        assertEquals(LANGUAGE_PL, normalizeLanguageTag("Polski"))
    }

    @Test
    fun normalizeLanguageTag_null_returnsEn() {
        assertEquals(LANGUAGE_EN, normalizeLanguageTag(null))
    }

    @Test
    fun normalizeLanguageTag_english_returnsEn() {
        assertEquals(LANGUAGE_EN, normalizeLanguageTag(LANGUAGE_EN))
    }

    @Test
    fun normalizeLanguageTag_unknown_returnsEn() {
        assertEquals(LANGUAGE_EN, normalizeLanguageTag("fr"))
    }
}
