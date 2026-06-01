package com.example.corda.data.inspirations.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.MediaType
import com.example.corda.data.inspirations.local.entities.relations.InspirationLabelCrossRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InspirationsDaoInspirationCrudInstrumentedTest : InspirationsDaoTestBase() {

    @Test
    fun insertInspiration_shouldReturnGeneratedId() = runTest {
        val id = dao.insertInspiration(
            InspirationEntity(name = "Idea", description = "A riff"),
        )
        assertTrue(id > 0)
        assertEquals(1, dao.observeInspirationsWithLabels().first().size)
    }

    @Test
    fun updateInspiration_shouldUpdateMediaFields() = runTest {
        val id = dao.insertInspiration(
            InspirationEntity(name = "Old", description = "Desc"),
        )
        dao.updateInspiration(
            InspirationEntity(
                inspirationId = id,
                name = "New",
                description = "Updated",
                mediaPath = "/files/clip.mp4",
                mediaType = MediaType.VIDEO,
                thumbnailPath = "/files/thumb.jpg",
                mediaAspectRatio = 1.77f,
            ),
        )
        val loaded = dao.getInspirationWithLabelsById(id)!!.inspiration
        assertEquals("New", loaded.name)
        assertEquals(MediaType.VIDEO, loaded.mediaType)
        assertEquals("/files/clip.mp4", loaded.mediaPath)
        assertEquals(1.77f, loaded.mediaAspectRatio!!, 0.01f)
    }

    @Test
    fun deleteInspirationById_shouldRemoveInspirationAndCrossRefs() = runTest {
        val labelId = dao.insertLabel(LabelEntity(name = "Song"))
        val inspirationId = dao.insertInspiration(
            InspirationEntity(name = "Track", description = ""),
        )
        dao.insertCrossRef(InspirationLabelCrossRef(inspirationId = inspirationId, labelId = labelId))
        dao.deleteInspirationById(inspirationId)
        assertTrue(dao.observeInspirationsWithLabels().first().isEmpty())
        assertNull(dao.getInspirationWithLabelsById(inspirationId))
    }

    @Test
    fun insertInspiration_mediaTypeRoundTrip() = runTest {
        val imageId = dao.insertInspiration(
            InspirationEntity(
                name = "Pic",
                description = "",
                mediaPath = "/a.jpg",
                mediaType = MediaType.IMAGE,
            ),
        )
        val videoId = dao.insertInspiration(
            InspirationEntity(
                name = "Vid",
                description = "",
                mediaPath = "/b.mp4",
                mediaType = MediaType.VIDEO,
            ),
        )
        assertEquals(MediaType.IMAGE, dao.getInspirationWithLabelsById(imageId)!!.inspiration.mediaType)
        assertEquals(MediaType.VIDEO, dao.getInspirationWithLabelsById(videoId)!!.inspiration.mediaType)
    }
}
