package com.iptvapp.player.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvapp.player.domain.model.*
import com.iptvapp.player.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val channels: List<Channel> = emptyList(),
    val movies: List<Movie> = emptyList(),
    val seriesList: List<Series> = emptyList(),
    val favoriteChannels: List<Channel> = emptyList(),
    val favoriteMovies: List<Movie> = emptyList(),
    val isLoadingChannels: Boolean = false,
    val isLoadingMovies: Boolean = false,
    val isLoadingSeries: Boolean = false,
    val error: String? = null,
    val selectedTab: HomeTab = HomeTab.LIVE
)

enum class HomeTab { LIVE, MOVIES, SERIES, FAVORITES }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLiveChannelsUseCase: GetLiveChannelsUseCase,
    private val getMoviesUseCase: GetMoviesUseCase,
    private val getSeriesUseCase: GetSeriesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadAll()
        observeFavorites()
    }

    fun selectTab(tab: HomeTab) = _uiState.update { it.copy(selectedTab = tab) }

    fun loadAll() {
        loadChannels()
        loadMovies()
        loadSeries()
    }

    private fun loadChannels() {
        viewModelScope.launch {
            getLiveChannelsUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoadingChannels = true) }
                    is Result.Success -> _uiState.update {
                        it.copy(channels = result.data, isLoadingChannels = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.message, isLoadingChannels = false)
                    }
                }
            }
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            getMoviesUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoadingMovies = true) }
                    is Result.Success -> _uiState.update {
                        it.copy(movies = result.data, isLoadingMovies = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.message, isLoadingMovies = false)
                    }
                }
            }
        }
    }

    private fun loadSeries() {
        viewModelScope.launch {
            getSeriesUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoadingSeries = true) }
                    is Result.Success -> _uiState.update {
                        it.copy(seriesList = result.data, isLoadingSeries = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.message, isLoadingSeries = false)
                    }
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoritesUseCase.getChannels().collect { channels ->
                _uiState.update { it.copy(favoriteChannels = channels) }
            }
        }
        viewModelScope.launch {
            getFavoritesUseCase.getMovies().collect { movies ->
                _uiState.update { it.copy(favoriteMovies = movies) }
            }
        }
    }

    fun toggleChannelFavorite(channel: Channel) {
        viewModelScope.launch {
            toggleFavoriteUseCase.toggleChannel(channel.streamId, !channel.isFavorite)
        }
    }

    fun toggleMovieFavorite(movie: Movie) {
        viewModelScope.launch {
            toggleFavoriteUseCase.toggleMovie(movie.streamId, !movie.isFavorite)
        }
    }

    fun toggleSeriesFavorite(series: Series) {
        viewModelScope.launch {
            toggleFavoriteUseCase.toggleSeries(series.seriesId, !series.isFavorite)
        }
    }
}
