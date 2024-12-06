package kr.sjh.feature.adoption_detail.screen

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favouriteRepository: FavouriteRepository, savedStateHandle: SavedStateHandle
) : ViewModel() {

    val pet = Json.decodeFromString<Pet>(savedStateHandle.get<PetDetail>("petInfo").toString())

    val isExist = favouriteRepository.isExist(pet.desertionNo)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), false)

    private val _location = MutableStateFlow<Response<Location>>(Response.Loading)
    val location = _location.asStateFlow()

    lateinit var geocoder: Geocoder

    init {
        viewModelScope.launch {
            getLocation(pet.careAddr)
        }
    }

    private fun addPet(pet: Pet) {
        viewModelScope.launch {
            favouriteRepository.addPet(pet)
        }
    }

    private fun removePet(pet: Pet) {
        viewModelScope.launch {
            favouriteRepository.removePet(pet.desertionNo)
        }
    }

    fun onEvent(event: AdoptionDetailEvent) {
        when (event) {
            is AdoptionDetailEvent.AddLike -> {
                addPet(pet)
            }

            is AdoptionDetailEvent.RemoveLike -> {
                removePet(pet)
            }
        }
    }

   private suspend fun getLocation(shelterAddress: String) {
        withContext(Dispatchers.IO) {
            _location.value = Response.Loading
            try {
                if (!::geocoder.isInitialized) {
                    geocoder = Geocoder(context, Locale.getDefault())
                }
                if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(
                        shelterAddress,
                        1
                    ) { address ->
                        val lat = address[0].latitude
                        val lon = address[0].longitude
                        _location.value = Response.Success(Location("").apply {
                            latitude = lat
                            longitude = lon
                        })
                    }
                } else {
                    val address = geocoder.getFromLocationName(
                        shelterAddress,
                        1,
                    )
                    if (!address.isNullOrEmpty()) {//The address can be null or empty
                        val lat = address[0].latitude
                        val lon = address[0].longitude
                        _location.value = Response.Success(Location("").apply {
                            latitude = lat
                            longitude = lon
                        })
                    } else {
                        _location.value = Response.Failure(Exception("주소를 찾을수 없어요."))
                    }
                }
            } catch (e: Exception) {
                _location.value = Response.Failure(Exception("주소를 가져오는데 실패했어요."))
            }
        }

    }
}
