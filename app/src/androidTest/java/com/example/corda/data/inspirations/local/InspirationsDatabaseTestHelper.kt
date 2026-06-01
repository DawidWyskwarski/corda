package com.example.corda.data.inspirations.local

import android.content.ContentValues
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

object InspirationsDatabaseTestHelper {

    fun minimal(context: Context): InspirationsDatabase =
        Room.inMemoryDatabaseBuilder(context, InspirationsDatabase::class.java)
            .allowMainThreadQueries()
            .build()

    fun withDefaultLabels(context: Context): InspirationsDatabase =
        Room.inMemoryDatabaseBuilder(context, InspirationsDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(DefaultLabelsCallback())
            .build()

    private class DefaultLabelsCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            db.beginTransaction()
            try {
                listOf(
                    "Want to learn",
                    "Song",
                    "Learned",
                    "Image",
                    "Video",
                ).forEach { name ->
                    db.insert("Label", 0, ContentValues().apply {
                        put("name", name)
                    })
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
