package com.example.corda.data.inspirations.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationLabelCrossRef
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InspirationsDaoTransactionInstrumentedTest : InspirationsDaoTestBase() {

    @Test
    fun insertInspirationWithLabels_shouldCreateInspirationAndLinks() = runTest {
        val l1 = dao.insertLabel(LabelEntity(name = "Song"))
        val l2 = dao.insertLabel(LabelEntity(name = "Learned"))
        val id = dao.insertInspirationWithLabels(
            InspirationEntity(name = "My song", description = "Notes"),
            labelIds = listOf(l1, l2),
        )
        val loaded = dao.getInspirationWithLabelsById(id)!!
        assertEquals("My song", loaded.inspiration.name)
        assertEquals(2, loaded.labels.size)
    }

    @Test
    fun updateInspirationWithLabels_shouldReplaceLabels() = runTest {
        val l1 = dao.insertLabel(LabelEntity(name = "Song"))
        val l2 = dao.insertLabel(LabelEntity(name = "Video"))
        val id = dao.insertInspirationWithLabels(
            InspirationEntity(name = "Item", description = ""),
            labelIds = listOf(l1),
        )
        val entity = dao.getInspirationWithLabelsById(id)!!.inspiration
        dao.updateInspirationWithLabels(entity, labelIds = listOf(l2))
        val labels = dao.getInspirationWithLabelsById(id)!!.labels.map { it.name }
        assertEquals(listOf("Video"), labels)
    }

    @Test
    fun updateInspirationWithLabels_emptyLabels_shouldClearAllTags() = runTest {
        val l1 = dao.insertLabel(LabelEntity(name = "Song"))
        val id = dao.insertInspirationWithLabels(
            InspirationEntity(name = "Item", description = ""),
            labelIds = listOf(l1),
        )
        val entity = dao.getInspirationWithLabelsById(id)!!.inspiration
        dao.updateInspirationWithLabels(entity, labelIds = emptyList())
        assertTrue(dao.getInspirationWithLabelsById(id)!!.labels.isEmpty())
    }

    @Test
    fun replaceLabelsForInspiration_shouldSwapTagsWithoutTouchingInspiration() = runTest {
        val l1 = dao.insertLabel(LabelEntity(name = "Song"))
        val l2 = dao.insertLabel(LabelEntity(name = "Image"))
        val id = dao.insertInspiration(
            InspirationEntity(name = "Stable name", description = "Stable desc"),
        )
        dao.insertCrossRef(InspirationLabelCrossRef(inspirationId = id, labelId = l1))
        dao.replaceLabelsForInspiration(id, listOf(l2))
        val loaded = dao.getInspirationWithLabelsById(id)!!
        assertEquals("Stable name", loaded.inspiration.name)
        assertEquals("Image", loaded.labels.single().name)
    }
}
