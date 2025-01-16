package kr.sjh.feature.adoption_detail.screen

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sjh.core.model.Response
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import java.util.Locale
import javax.inject.Inject

sealed class LocationState {
    object Loading : LocationState()
    data class Success(val location: Location) : LocationState()
    data class Failure(val e: Exception) : LocationState()
}

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val geocoder: Geocoder,
    private val favouriteRepository: FavouriteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val pet = savedStateHandle.toRoute<PetDetail>().pet

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
        withContext(Dispatchers.IO) {
            _location.value = LocationState.Loading
            try {
                val location = Location("")
                if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(
                        shelterAddress, 1
                    ) { address ->
                        val lat = address[0].latitude
                        val lon = address[0].longitude
                        _location.value = LocationState.Success(location.apply {
                            latitude = lat
                            longitude = lon
                        })
                    }
                } else {
                    val address = geocoder.getFromLocationName(
                        shelterAddress,
                        1,
                    )
                    if (!address.isNullOrEmpty()) {
                        val lat = address[0].latitude
                        val lon = address[0].longitude
                        _location.value = LocationState.Success(location.apply {
                            latitude = lat
                            longitude = lon
                        })
                    } else {
                        _location.value = LocationState.Failure(Exception("주소를 찾을수 없어요."))
                    }
                }
            } catch (e: Exception) {
                _location.value = LocationState.Failure(Exception("주소를 가져오는데 실패했어요."))
            }
        }
    }
}
