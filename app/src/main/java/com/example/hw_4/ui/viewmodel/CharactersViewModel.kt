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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import retrofit2.HttpException
import kotlinx.coroutines.delay

data class CharacterListUiState(
    val searchQuery: String = "",
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 1,
    val isEmptySearchResult: Boolean = false,
    val showCacheBanner: Boolean = false
)

@HiltViewModel
class CharacterViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val cacheRepository: SearchCacheRepository
) : ViewModel() {

    var uiState by mutableStateOf(CharacterListUiState(isLoading = true))
        private set

    private var hasMore = true

    private var searchJob: Job? = null

    init {
        loadFirstPage()
    }

    fun loadFirstPage() {
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null,
                showCacheBanner = false,
                isEmptySearchResult = false
            )

            try {
                val characters = repository.getCharactersPage(1)
                val pageInfo = repository.getPagesInfo()
                hasMore = pageInfo.totalPages > 1

                uiState = uiState.copy(
                    isLoading = false,
                    characters = characters,
                    currentPage = 1,
                    hasMorePages = hasMore,
                    searchQuery = "",
                    errorMessage = null,
                    showCacheBanner = false
                )


                cacheRepository.saveSearchResult("", characters)

            } catch (e: Exception) {
                val cached = cacheRepository.getLastSearchResult()
                if (cached != null && cached.second.isNotEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        searchQuery = cached.first,
                        characters = cached.second,
                        hasMorePages = false,
                        errorMessage = null,
                        showCacheBanner = true
                    )
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        errorMessage = "Нет интернета. Проверьте подключение."
                    )
                }
            }
        }
    }

    fun loadNextPage() {
        if (uiState.isLoadingMore || !hasMore) return
        if (uiState.searchQuery.isNotBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoadingMore = true)

            try {
                val nextPage = uiState.currentPage + 1
                val newCharacters = repository.getCharactersPage(nextPage)
                val all = uiState.characters + newCharacters

                val pageInfo = repository.getPagesInfo()
                hasMore = nextPage < pageInfo.totalPages

                uiState = uiState.copy(
                    isLoadingMore = false,
                    characters = all,
                    currentPage = nextPage,
                    hasMorePages = hasMore
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoadingMore = false,
                    errorMessage = "Ошибка загрузки следующей страницы"
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        uiState = uiState.copy(
            searchQuery = query,
            isEmptySearchResult = false,
            errorMessage = null,
            showCacheBanner = false
        )

        searchJob?.cancel()

        if (query.isBlank()) {

            loadFirstPage()
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)
            uiState = uiState.copy(isLoading = true)

            try {
                val results = repository.searchCharacters(query)

                if (results.isEmpty()) {

                    uiState = uiState.copy(
                        isLoading = false,
                        characters = emptyList(),
                        hasMorePages = false,
                        isEmptySearchResult = true,
                        errorMessage = null
                    )
                } else {
                    cacheRepository.saveSearchResult(query, results)
                    uiState = uiState.copy(
                        isLoading = false,
                        characters = results,
                        hasMorePages = false,
                        isEmptySearchResult = false,
                        errorMessage = null
                    )
                }

            } catch (e: HttpException) {
                if (e.code() == 404) {
                    uiState = uiState.copy(
                        isLoading = false,
                        characters = emptyList(),
                        hasMorePages = false,
                        isEmptySearchResult = true,
                        errorMessage = null
                    )
                } else {
                    val cached = cacheRepository.searchInCache(query)
                    if (cached != null && cached.isNotEmpty()) {
                        uiState = uiState.copy(
                            isLoading = false,
                            characters = cached,
                            hasMorePages = false,
                            isEmptySearchResult = false,
                            errorMessage = null,
                            showCacheBanner = true
                        )
                    } else {
                        uiState = uiState.copy(
                            isLoading = false,
                            characters = emptyList(),
                            hasMorePages = false,
                            isEmptySearchResult = true,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                val cached = cacheRepository.searchInCache(query)
                if (cached != null && cached.isNotEmpty()) {
                    uiState = uiState.copy(
                        isLoading = false,
                        characters = cached,
                        hasMorePages = false,
                        isEmptySearchResult = false,
                        errorMessage = null,
                        showCacheBanner = true
                    )
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        characters = emptyList(),
                        hasMorePages = false,
                        isEmptySearchResult = true,
                        errorMessage = null
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