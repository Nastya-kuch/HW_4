package com.example.hw_4.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CharacterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Query("SELECT * FROM character_cache WHERE searchQuery = :query ORDER BY characterId ASC")
    suspend fun getByQuery(query: String): List<CharacterEntity>

    @Query("SELECT DISTINCT searchQuery FROM character_cache ORDER BY savedAt DESC LIMIT 1")
    suspend fun getLastQuery(): String?
    @Query("DELETE FROM character_cache WHERE searchQuery = :query")
    suspend fun deleteByQuery(query: String)
    @Query("DELETE FROM character_cache")
    suspend fun clearAll()

    @Query("SELECT * FROM character_cache WHERE name LIKE '%' || :query || '%' GROUP BY characterId ORDER BY characterId ASC")
    suspend fun searchByName(query: String): List<CharacterEntity>
}