package com.example.corda.data.inspirations.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationLabelCrossRef
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InspirationsDaoObserveInstrumentedTest : InspirationsDaoTestBase() {

    @Test
    fun observeInspirations_emptyDb_returnsEmptyList() = runTest {
        assertTrue(dao.observeInspirationsWithLabels().first().isEmpty())
    }

    @Test
    fun observeInspirations_shouldOrderByIdDesc() = runTest {
        val first = dao.insertInspiration(InspirationEntity(name = "First", description = ""))
        val second = dao.insertInspiration(InspirationEntity(name = "Second", description = ""))
        val ordered = dao.observeInspirationsWithLabels().first()
        assertEquals(second, ordered[0].inspiration.inspirationId)
        assertEquals(first, ordered[1].inspiration.inspirationId)
    }

    @Test
    fun observeInspirations_flowUpdatesOnInsert() = runTest {
        assertTrue(dao.observeInspirationsWithLabels().first().isEmpty())
        dao.insertInspiration(InspirationEntity(name = "New", description = ""))
        assertEquals(1, dao.observeInspirationsWithLabels().first().size)
    }

    @Test
    fun observeInspirations_withoutLabels_returnsEmptyLabelList() = runTest {
        dao.insertInspiration(InspirationEntity(name = "Plain", description = ""))
        val item = dao.observeInspirationsWithLabels().first().single()
        assertTrue(item.labels.isEmpty())
    }

    @Test
    fun getInspirationById_shouldReturnEmbeddedLabels() = runTest {
        val labelId = dao.insertLabel(LabelEntity(name = "Image"))
        val inspirationId = dao.insertInspiration(
            InspirationEntity(name = "Photo", description = "Ref"),
        )
        dao.insertCrossRef(InspirationLabelCrossRef(inspirationId = inspirationId, labelId = labelId))
        val result = dao.getInspirationWithLabelsById(inspirationId)
        assertNotNull(result)
        assertEquals("Photo", result!!.inspiration.name)
        assertEquals("Image", result.labels.single().name)
    }

    @Test
    fun getInspirationById_missing_returnsNull() = runTest {
        assertNull(dao.getInspirationWithLabelsById(9999L))
    }

    @Test
    fun observeLabels_flowUpdatesOnInsert() = runTest {
        assertTrue(dao.observeLabels().first().isEmpty())
        dao.insertLabel(LabelEntity(name = "Custom"))
        assertEquals(1, dao.observeLabels().first().size)
    }
}
