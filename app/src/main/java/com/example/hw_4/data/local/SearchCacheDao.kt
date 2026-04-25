package com.example.hw_4.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SearchCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCache(cache: SearchCacheEntity)

    @Query("SELECT * FROM search_cache WHERE query = :query")
    suspend fun getCache(query: String): SearchCacheEntity?

    @Query("SELECT * FROM search_cache ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSearchCache(): SearchCacheEntity?
}