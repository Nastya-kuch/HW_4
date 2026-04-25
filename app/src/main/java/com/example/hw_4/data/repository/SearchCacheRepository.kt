package com.example.hw_4.data.repository

import com.example.hw_4.data.local.AppDatabase
import com.example.hw_4.data.local.SearchCacheEntity
import com.example.hw_4.data.model.Character
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchCacheRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val cacheDao = database.searchCacheDao()
    private val gson = Gson()

    suspend fun saveSearchResult(query: String, characters: List<Character>) {
        withContext(Dispatchers.IO) {
            try {
                val jsonString = gson.toJson(characters)
                val key = query.ifEmpty { "__EMPTY__" }
                cacheDao.saveCache(SearchCacheEntity(key, jsonString))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun getSearchResult(query: String): List<Character>? {
        return withContext(Dispatchers.IO) {
            try {
                val key = query.ifEmpty { "__EMPTY__" }
                val cache = cacheDao.getCache(key)
                cache?.charactersJson?.let { jsonString ->
                    val type = object : TypeToken<List<Character>>() {}.type
                    gson.fromJson(jsonString, type)
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getLastSearchResult(): Pair<String, List<Character>>? {
        return withContext(Dispatchers.IO) {
            try {
                val cache = cacheDao.getLastSearchCache()
                cache?.let {
                    val type = object : TypeToken<List<Character>>() {}.type
                    val characters: List<Character> = gson.fromJson(it.charactersJson, type)
                    val query = if (it.query == "__EMPTY__") "" else it.query
                    query to characters
                }
            } catch (e: Exception) {
                null
            }
        }
    }
}