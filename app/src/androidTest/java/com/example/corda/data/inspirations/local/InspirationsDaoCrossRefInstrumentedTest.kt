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
class InspirationsDaoCrossRefInstrumentedTest : InspirationsDaoTestBase() {

    @Test
    fun insertCrossRef_shouldLinkInspirationAndLabel() = runTest {
        val labelId = dao.insertLabel(LabelEntity(name = "Learned"))
        val inspirationId = dao.insertInspiration(
            InspirationEntity(name = "Piece", description = ""),
        )
        dao.insertCrossRef(InspirationLabelCrossRef(inspirationId = inspirationId, labelId = labelId))
        val labels = dao.getInspirationWithLabelsById(inspirationId)!!.labels
        assertEquals(1, labels.size)
        assertEquals("Learned", labels.single().name)
    }

    @Test
    fun insertCrossRefs_batch_shouldLinkAll() = runTest {
        val l1 = dao.insertLabel(LabelEntity(name = "Song"))
        val l2 = dao.insertLabel(LabelEntity(name = "Video"))
        val inspirationId = dao.insertInspiration(
            InspirationEntity(name = "Clip", description = ""),
        )
        dao.insertCrossRefs(
            listOf(
                InspirationLabelCrossRef(inspirationId = inspirationId, labelId = l1),
                InspirationLabelCrossRef(inspirationId = inspirationId, labelId = l2),
            ),
        )
        assertEquals(2, dao.getInspirationWithLabelsById(inspirationId)!!.labels.size)
    }

    @Test
    fun insertCrossRef_duplicate_shouldIgnoreSilently() = runTest {
        val labelId = dao.insertLabel(LabelEntity(name = "Song"))
        val inspirationId = dao.insertInspiration(
            InspirationEntity(name = "Track", description = ""),
        )
        val ref = InspirationLabelCrossRef(inspirationId = inspirationId, labelId = labelId)
        dao.insertCrossRef(ref)
        dao.insertCrossRef(ref)
        assertEquals(1, dao.getInspirationWithLabelsById(inspirationId)!!.labels.size)
    }

    @Test
    fun deleteCrossRefsForInspiration_shouldRemoveAllLinks() = runTest {
        val labelId = dao.insertLabel(LabelEntity(name = "Song"))
        val inspirationId = dao.insertInspiration(
            InspirationEntity(name = "Track", description = ""),
        )
        dao.insertCrossRef(InspirationLabelCrossRef(inspirationId = inspirationId, labelId = labelId))
        dao.deleteCrossRefsForInspiration(inspirationId)
        assertTrue(dao.getInspirationWithLabelsById(inspirationId)!!.labels.isEmpty())
    }
}
