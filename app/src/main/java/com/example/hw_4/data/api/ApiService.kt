package com.example.hw_4.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int = 1
    ): CharacterResponse

    @GET("character")
    suspend fun searchCharacters(
        @Query("name") name: String
    ): CharacterResponse

    @GET("character/{id}")
    suspend fun getCharacterById(
        @Path("id") id: Int
    ): CharacterResponseSingle

    
}

data class CharacterResponse(
    val info: Info,
    val results: List<CharacterFromApi>
)

data class CharacterResponseSingle(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: OriginFromApi,
    val location: LocationFromApi,
    val episode: List<String>
)

data class Info(
    val count: Int,
    val pages: Int,
    val next: String?,
    val prev: String?
)

data class CharacterFromApi(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: OriginFromApi,
    val location: LocationFromApi,
    val episode: List<String>
)

data class OriginFromApi(val name: String, val url: String)
data class LocationFromApi(val name: String, val url: String)