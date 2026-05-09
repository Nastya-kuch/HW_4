package com.example.hw_4.data.repository

import android.util.Log
import com.example.hw_4.data.local.CharacterDao
import com.example.hw_4.data.local.CharacterEntity
import com.example.hw_4.data.model.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchCacheRepository @Inject constructor(
    private val characterDao: CharacterDao
) {

    suspend fun saveSearchResult(query: String, characters: List<Character>) {
        withContext(Dispatchers.IO) {
            try {
                val key = query.trim()
                characterDao.deleteByQuery(key)

                val entities = characters.map { character ->
                    CharacterEntity(
                        searchQuery = key,
                        characterId = character.id,
                        name = character.name,
                        status = character.status,
                        species = character.species,
                        type = character.type,
                        gender = character.gender,
                        origin = character.origin,
                        location = character.location,
                        episodeCount = character.episodeCount
                    )
                }
                characterDao.insertAll(entities)
                Log.d("CACHE", "Сохранено ${entities.size} персонажей для запроса '$key'")
            } catch (e: Exception) {
                Log.e("CACHE", "Ошибка сохранения кэша для '$query': ${e.message}", e)
            }
        }
    }

    suspend fun getSearchResult(query: String): List<Character>? {
        return withContext(Dispatchers.IO) {
            try {
                val key = query.trim()
                val entities = characterDao.getByQuery(key)
                if (entities.isEmpty()) null else entities.map { it.toCharacter() }
            } catch (e: Exception) {
                Log.e("CACHE", "Ошибка чтения кэша для '$query': ${e.message}", e)
                null
            }
        }
    }

    suspend fun getLastSearchResult(): Pair<String, List<Character>>? {
        return withContext(Dispatchers.IO) {
            try {
                val lastQuery = characterDao.getLastQuery() ?: return@withContext null
                val entities = characterDao.getByQuery(lastQuery)
                if (entities.isEmpty()) return@withContext null
                val characters = entities.map { it.toCharacter() }
                lastQuery to characters
            } catch (e: Exception) {
                Log.e("CACHE", "Ошибка восстановления последнего кэша: ${e.message}", e)
                null
            }
        }
    }
    suspend fun searchInCache(query: String): List<Character>? {
        return withContext(Dispatchers.IO) {
            try {
                val entities = characterDao.searchByName(query)
                if (entities.isEmpty()) null else entities.map { it.toCharacter() }
            } catch (e: Exception) {
                Log.e("CACHE", "Ошибка поиска в кэше: ${e.message}", e)
                null
            }
        }
    }
}

private fun CharacterEntity.toCharacter() = Character(
    id = characterId,
    name = name,
    status = status,
    species = species,
    type = type,
    gender = gender,
    origin = origin,
    location = location,
    episodeCount = episodeCount
)