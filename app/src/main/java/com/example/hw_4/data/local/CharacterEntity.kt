package com.example.hw_4.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "character_cache")
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val rowId: Long = 0,
    val searchQuery: String,
    val characterId: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: String,
    val location: String,
    val episodeCount: Int,
    val savedAt: Long = System.currentTimeMillis()
)