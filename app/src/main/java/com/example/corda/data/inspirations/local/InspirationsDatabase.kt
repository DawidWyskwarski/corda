package com.example.corda.data.inspirations.local

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.corda.data.inspirations.local.dao.InspirationsDao
import com.example.corda.data.inspirations.local.entities.InspirationEntity
import com.example.corda.data.inspirations.local.entities.LabelEntity
import com.example.corda.data.inspirations.local.entities.relations.InspirationLabelCrossRef

@Database(
    entities = [
        InspirationEntity::class,
        LabelEntity::class,
        InspirationLabelCrossRef::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(InspirationsTypeConverters::class)
abstract class InspirationsDatabase : RoomDatabase() {

    abstract val inspirationsDao: InspirationsDao

    companion object {

        @Volatile
        private var INSTANCE: InspirationsDatabase? = null

        fun getInstance(ctx: Context): InspirationsDatabase {
            synchronized(this) {
                return INSTANCE ?: Room.databaseBuilder(
                    ctx.applicationContext,
                    InspirationsDatabase::class.java,
                    "inspirations_db",
                )
                    .addMigrations(INSPIRATIONS_MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.beginTransaction()
                            try {
                                populateLabels(db)
                                db.setTransactionSuccessful()
                            } finally {
                                db.endTransaction()
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private fun populateLabels(db: SupportSQLiteDatabase) {
            val defaultLabels = listOf(
                "Want to learn",
                "Song",
                "Learned",
                "Image",
                "Video",
            )
            defaultLabels.forEach { name ->
                db.insert("Label", 0, ContentValues().apply {
                    put("name", name)
                })
            }
        }
    }
}
