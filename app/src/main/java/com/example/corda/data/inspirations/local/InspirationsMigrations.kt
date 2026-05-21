package com.example.corda.data.inspirations.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val INSPIRATIONS_MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Inspiration ADD COLUMN thumbnail_path TEXT")
        db.execSQL("ALTER TABLE Inspiration ADD COLUMN media_aspect_ratio REAL")
    }
}
