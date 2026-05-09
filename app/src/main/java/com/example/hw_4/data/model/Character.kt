package com.example.hw_4.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val origin: String,
    val location: String,
    val episodeCount: Int
)