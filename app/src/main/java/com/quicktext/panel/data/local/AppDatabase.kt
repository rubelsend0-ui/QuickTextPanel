package com.quicktext.panel.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BoxEntity::class, TemplateEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun boxDao(): BoxDao
    abstract fun templateDao(): TemplateDao
}
