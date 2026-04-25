package com.example.hw_4.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_4.data.repository.CharacterRepository
import com.example.hw_4.data.repository.SearchCacheRepository
import com.example.hw_4.data.model.Character
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterListUiState(
    val searchQuery: String = "",
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 1
)

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val cacheRepository: SearchCacheRepository
) : ViewModel() {

    var uiState by mutableStateOf(CharacterListUiState(isLoading = true))
        private set

    init {
        loadLastCache()
    }

    private fun loadLastCache() {
        viewModelScope.launch {
            val lastSearch = cacheRepository.getLastSearchResult()
            if (lastSearch != null) {
                val (query, characters) = lastSearch
                uiState = uiState.copy(
                    searchQuery = query,
                    characters = characters,
                    isLoading = false,
                    hasMorePages = false
                )
            } else {
                loadFirstPage()
            }
        }
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            try {
                val characters = repository.getCharactersPage(1)
                val pageInfo = repository.getPagesInfo()

                cacheRepository.saveSearchResult("", characters)

                uiState = uiState.copy(
                    isLoading = false,
                    characters = characters,
                    currentPage = 1,
                    hasMorePages = pageInfo.totalPages > 1
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    fun loadNextPage() {
        if (uiState.isLoadingMore || !uiState.hasMorePages) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoadingMore = true)

            try {
                val nextPage = uiState.currentPage + 1
                val newCharacters = repository.getCharactersPage(nextPage)
                val allCurrent = uiState.characters.toMutableList()
                allCurrent.addAll(newCharacters)

                val pageInfo = repository.getPagesInfo()

                uiState = uiState.copy(
                    isLoadingMore = false,
                    characters = allCurrent,
                    currentPage = nextPage,
                    hasMorePages = nextPage < pageInfo.totalPages
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoadingMore = false,
                    errorMessage = e.message ?: "Error loading more"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        uiState = uiState.copy(searchQuery = query, isLoading = true)

        viewModelScope.launch {
            try {
                val results = repository.searchCharacters(query)
                cacheRepository.saveSearchResult(query, results)

                uiState = uiState.copy(
                    isLoading = false,
                    characters = results,
                    hasMorePages = false
                )
            } catch (e: Exception) {
                val cached = cacheRepository.getSearchResult(query)
                if (cached != null) {
                    uiState = uiState.copy(
                        isLoading = false,
                        characters = cached,
                        hasMorePages = false,
                        errorMessage = "Ошибка сети, показаны кэшированные данные"
                    )
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Search error"
                    )
                }
            }
        }
    }

    fun retry() {
        if (uiState.searchQuery.isNotBlank()) {
            updateSearchQuery(uiState.searchQuery)
        } else {
            loadFirstPage()
        }
    }
}