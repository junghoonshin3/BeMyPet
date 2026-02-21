package kr.sjh.feature.favourite.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kr.sjh.data.repository.FavouriteRepository
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val favoriteRepository: FavouriteRepository
) : ViewModel() {
    val favouritePets = favoriteRepository.getFavouritePets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { favoriteRepository.backfillFavouriteImagesIfNeeded() }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching { favoriteRepository.backfillFavouriteImagesIfNeeded() }
            runCatching { favoriteRepository.getFavouritePets().first() }
            delay(500)
            _isRefreshing.value = false
        }
    }
}
