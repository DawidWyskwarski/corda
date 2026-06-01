package com.example.corda.data.inspirations.local

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.corda.data.inspirations.local.dao.InspirationsDao
import org.junit.After
import org.junit.Before

abstract class InspirationsDaoTestBase {

    protected lateinit var db: InspirationsDatabase
    protected lateinit var dao: InspirationsDao
    protected val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    open fun setUp() {
        db = InspirationsDatabaseTestHelper.minimal(context)
        dao = db.inspirationsDao
    }

    @After
    open fun tearDown() {
        db.close()
    }
}
