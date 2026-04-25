package com.example.hw_4.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_cache")
data class SearchCacheEntity(
    @PrimaryKey
    val query: String,
    val charactersJson: String,
    val timestamp: Long = System.currentTimeMillis()
)