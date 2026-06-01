package com.example.corda.data.inspirations.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InspirationsDaoSeedDataInstrumentedTest : InspirationsDaoTestBase() {

    @Test
    fun observeLabels_withDefaultSeed_returnsFiveSortedLabels() = runTest {
        val seededDb = InspirationsDatabaseTestHelper.withDefaultLabels(context)
        val seededDao = seededDb.inspirationsDao
        try {
            val names = seededDao.observeLabels().first().map { it.name }
            assertEquals(
                listOf("Image", "Learned", "Song", "Video", "Want to learn"),
                names,
            )
        } finally {
            seededDb.close()
        }
    }
}
