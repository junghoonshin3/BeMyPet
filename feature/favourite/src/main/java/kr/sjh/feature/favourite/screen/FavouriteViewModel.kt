package kr.sjh.feature.favourite.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kr.sjh.data.repository.FavouriteRepository
import javax.inject.Inject

@HiltViewModel
class FavouriteViewModel @Inject constructor(
    private val favoriteRepository: FavouriteRepository
) : ViewModel() {
    val favouritePets = favoriteRepository.getFavouritePets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
}