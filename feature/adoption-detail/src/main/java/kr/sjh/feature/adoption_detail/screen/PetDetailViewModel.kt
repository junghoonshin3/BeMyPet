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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.repository.AuthRepository
import kr.sjh.data.repository.CommentRepository
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.GeoLocationRepository
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import kr.sjh.feature.adoption_detail.state.DetailUiState
import javax.inject.Inject

sealed class LocationUiState {
    data object Loading : LocationUiState()
    data class Success(val location: Location) : LocationUiState()
    data class Failure(val e: Exception) : LocationUiState()
}

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val geoLocationRepository: GeoLocationRepository,
    private val favouriteRepository: FavouriteRepository,
    private val commentRepository: CommentRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val uiState: StateFlow<DetailUiState> =
        savedStateHandle.getStateFlow("pet", Pet()).onEach { pet ->
            isFavorite("${pet.desertionNo}")
            getLocation("${pet.careAddress}")
//            commentCount(pet.noticeNo)
        }.map { pet ->
            DetailUiState.Success(pet) as DetailUiState
        }.onStart {
            emit(DetailUiState.Loading)
        }.catch { e ->
            emit(DetailUiState.Failure(e))
        }.flowOn(Dispatchers.IO).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DetailUiState.Loading
        )

    private val _location = MutableStateFlow<LocationUiState>(LocationUiState.Loading)

    val location = _location.asStateFlow()

    private val _commentCount = MutableStateFlow(0)
    val commentCount = _commentCount.asStateFlow()

    private val _isFavorite = MutableStateFlow(false)
    val isFavorite = _isFavorite.asStateFlow()

    val session = authRepository.getSessionFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionState.Initializing)

    private fun addPet(pet: Pet) {
        viewModelScope.launch {
            favouriteRepository.addPet(pet)
        }
    }


    private fun removePet(desertionNo: String) {
        viewModelScope.launch {
            favouriteRepository.removePet(desertionNo)
        }
    }

    fun onEvent(event: AdoptionDetailEvent) {
        when (event) {
            is AdoptionDetailEvent.OnFavorite -> {
                if (uiState.value is DetailUiState.Success) {
                    val pet = (uiState.value as DetailUiState.Success).pet
                    if (event.isFavorite) {
                        addPet(pet)
                    } else {
                        removePet("${pet.desertionNo}")
                    }
                }
            }
        }
    }

    private fun isFavorite(desertionNo: String) {
        _isFavorite.value = favouriteRepository.isExist(desertionNo)
    }

    private suspend fun commentCount(postId: String) {
        _commentCount.value = commentRepository.getCommentCount(postId)
    }

    private suspend fun getLocation(shelterAddress: String) {
        _location.value = LocationUiState.Loading
        try {
            _location.value = withContext(Dispatchers.IO) {
                val addresses = geoLocationRepository.getFromLocationName(shelterAddress, 1)
                LocationUiState.Success(
                    Location("").apply {
                        latitude = addresses[0].latitude
                        longitude = addresses[0].longitude
                    },
                )
            }

        } catch (e: Exception) {
            _location.value = LocationUiState.Failure(e)
        }
    }
}
