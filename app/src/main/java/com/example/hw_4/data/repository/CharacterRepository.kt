package com.example.hw_4.data.repository

import com.example.hw_4.data.api.ApiService
import com.example.hw_4.data.model.Character
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getCharactersPage(page: Int): List<Character> = withContext(Dispatchers.IO) {
        val response = apiService.getCharacters(page = page)
        response.results.map { item ->
            Character(
                id = item.id,
                name = item.name,
                status = item.status,
                species = item.species,
                type = item.type,
                gender = item.gender,
                origin = item.origin.name,
                location = item.location.name,
                episodeCount = item.episode.size
            )
        }
    }

    suspend fun searchCharacters(query: String): List<Character> = withContext(Dispatchers.IO) {
            val response = apiService.searchCharacters(name = query)
            response.results.map { item ->
                Character(
                    id = item.id,
                    name = item.name,
                    status = item.status,
                    species = item.species,
                    type = item.type,
                    gender = item.gender,
                    origin = item.origin.name,
                    location = item.location.name,
                    episodeCount = item.episode.size
                )
            }

    }

    suspend fun getPagesInfo(): PageInfo = withContext(Dispatchers.IO) {
        val response = apiService.getCharacters(page = 1)
        PageInfo(
            totalPages = response.info.pages,
            hasNext = response.info.next != null
        )
    }

    suspend fun getCharacterById(id: Int): Character = withContext(Dispatchers.IO) {
        val response = apiService.getCharacterById(id)
        Character(
            id = response.id,
            name = response.name,
            status = response.status,
            species = response.species,
            type = response.type,
            gender = response.gender,
            origin = response.origin.name,
            location = response.location.name,
            episodeCount = response.episode.size
        )
    }
}

data class PageInfo(
    val totalPages: Int,
    val hasNext: Boolean
)