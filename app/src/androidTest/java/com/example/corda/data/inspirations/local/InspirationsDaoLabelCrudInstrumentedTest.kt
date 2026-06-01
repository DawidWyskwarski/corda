package com.example.corda.data.inspirations.local

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationLabelCrossRef
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InspirationsDaoLabelCrudInstrumentedTest : InspirationsDaoTestBase() {

    @Test
    fun insertLabel_shouldPersistUniqueName() = runTest {
        val id = dao.insertLabel(LabelEntity(name = "Practice"))
        assertTrue(id > 0)
        assertEquals("Practice", dao.getLabelById(id)!!.name)
    }

    @Test
    fun insertLabel_duplicateName_shouldAbort() = runTest {
        dao.insertLabel(LabelEntity(name = "Song"))
        try {
            dao.insertLabel(LabelEntity(name = "Song"))
            fail("Expected SQLiteConstraintException for duplicate label name")
        } catch (_: SQLiteConstraintException) {
        }
    }

    @Test
    fun updateLabel_shouldPersistChanges() = runTest {
        val id = dao.insertLabel(LabelEntity(name = "Old"))
        dao.updateLabel(LabelEntity(labelId = id, name = "Renamed"))
        assertEquals("Renamed", dao.getLabelById(id)!!.name)
    }

    @Test
    fun updateLabelName_shouldRenameLabel() = runTest {
        val id = dao.insertLabel(LabelEntity(name = "Tag"))
        dao.updateLabelName(id, "Renamed Tag")
        assertEquals("Renamed Tag", dao.getLabelByName("Renamed Tag")!!.name)
    }

    @Test
    fun deleteLabelById_shouldCascadeCrossRefsNotInspirations() = runTest {
        val labelId = dao.insertLabel(LabelEntity(name = "Song"))
        val inspirationId = dao.insertInspiration(
            InspirationEntity(name = "Keep me", description = ""),
        )
        dao.insertCrossRef(InspirationLabelCrossRef(inspirationId = inspirationId, labelId = labelId))
        dao.deleteLabelById(labelId)
        val inspiration = dao.getInspirationWithLabelsById(inspirationId)!!
        assertTrue(inspiration.labels.isEmpty())
        assertEquals("Keep me", inspiration.inspiration.name)
    }

    @Test
    fun getLabelByName_existing_returnsLabel() = runTest {
        dao.insertLabel(LabelEntity(name = "Want to learn"))
        val label = dao.getLabelByName("Want to learn")
        assertEquals("Want to learn", label!!.name)
    }

    @Test
    fun getLabelByName_missing_returnsNull() = runTest {
        assertNull(dao.getLabelByName("Missing"))
    }

    @Test
    fun getLabelById_shouldReturnCorrectLabel() = runTest {
        val id = dao.insertLabel(LabelEntity(name = "Video"))
        assertEquals("Video", dao.getLabelById(id)!!.name)
    }
}
