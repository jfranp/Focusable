package com.example.focusable.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FocusSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FocusDatabase : RoomDatabase() {
    abstract fun focusSessionDao(): FocusSessionDao
}
