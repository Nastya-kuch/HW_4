package com.example.hw_4.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_4.data.repository.CharacterRepository
import com.example.hw_4.data.model.Character
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterDetailUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val character: Character? = null
)

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: CharacterRepository,

) : ViewModel() {
    private var lastLoadedId: Int? = null
    var uiState by mutableStateOf(CharacterDetailUiState(isLoading = true))
        private set

    fun loadCharacter(characterId: Int) {
        lastLoadedId = characterId
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            try {
                val character = repository.getCharacterById(characterId)
                uiState = uiState.copy(
                    isLoading = false,
                    character = character
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun retry() {
        val id = lastLoadedId ?: return
        loadCharacter(id)
    }
}