package com.example.hw_4.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CharacterEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}