package kr.sjh.feature.adoption_detail.screen

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.GeoLocationRepository
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import javax.inject.Inject

sealed class LocationState {
    data object Loading : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Failure(val e: Exception) : LocationState()
}

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val geoLocationRepository: GeoLocationRepository,
    private val favouriteRepository: FavouriteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val pet = savedStateHandle.toRoute<PetDetail>(typeMap = PetDetail.typeMap).pet

    val isLike = favouriteRepository.isExist(pet.desertionNo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    private val _location = MutableStateFlow<LocationState>(LocationState.Loading)
    val location = _location.asStateFlow()

    init {
        viewModelScope.launch {
            getLocation(pet.careAddr)
        }
    }

    private fun addPet() {
        viewModelScope.launch {
            favouriteRepository.addPet(pet)
        }
    }

    private fun removePet() {
        viewModelScope.launch {
            favouriteRepository.removePet(pet.desertionNo)
        }
    }

    fun onEvent(event: AdoptionDetailEvent) {
        when (event) {
            is AdoptionDetailEvent.AddLike -> {
                addPet()
            }

            is AdoptionDetailEvent.RemoveLike -> {
                removePet()
            }
        }
    }

    private suspend fun getLocation(shelterAddress: String) {
        _location.value = LocationState.Loading
        try {
            _location.value = withContext(Dispatchers.IO) {
                val addresses = geoLocationRepository.getFromLocationName(shelterAddress, 1)
                LocationState.Success(Location("").apply {
                    latitude = addresses[0].latitude
                    longitude = addresses[0].longitude
                })
            }

        } catch (e: Exception) {
            _location.value = LocationState.Failure(e)
        }
    }
}
