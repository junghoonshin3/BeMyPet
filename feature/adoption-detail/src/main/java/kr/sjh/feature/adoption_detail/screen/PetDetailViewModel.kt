package kr.sjh.feature.adoption_detail.screen

import android.util.Log
import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.sjh.core.model.SessionState
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.notification.InterestProfileSyncCoordinator
import kr.sjh.data.repository.CommentRepository
import kr.sjh.data.repository.CompareRepository
import kr.sjh.data.repository.CompareToggleResult
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.GeoLocationRepository
import kr.sjh.data.session.SessionStore
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import kr.sjh.feature.adoption_detail.state.DetailUiState
import javax.inject.Inject

sealed class LocationUiState {
    data object Loading : LocationUiState()
    data class Success(val location: Location) : LocationUiState()
    data class Failure(val e: Exception) : LocationUiState()
}

data class CompareToggleUiEvent(
    val result: CompareToggleResult,
    val action: String,
    val selectedCount: Int
)

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val geoLocationRepository: GeoLocationRepository,
    private val favouriteRepository: FavouriteRepository,
    private val interestProfileSyncCoordinator: InterestProfileSyncCoordinator,
    private val sessionStore: SessionStore,
    private val commentRepository: CommentRepository,
    private val compareRepository: CompareRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    companion object {
        private const val TAG = "PetDetailViewModel"
    }

    private val route = runCatching { savedStateHandle.toRoute<PetDetail>() }.getOrNull()
    private val fromFavourite = route?.fromFavourite ?: false
    private val initialPet = route?.pet ?: savedStateHandle.get<Pet>("pet") ?: Pet()

    private val petState = savedStateHandle.getStateFlow("pet", initialPet)

    val uiState: StateFlow<DetailUiState> =
        petState.onEach { pet ->
            getLocation("${pet.careAddress}")
            refreshCommentCount(pet.noticeNo)
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

    private val _isFavorite = MutableStateFlow(fromFavourite)
    val isFavorite = _isFavorite.asStateFlow()

    val comparedCount = compareRepository.comparedPets()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isCompared = combine(compareRepository.comparedPets(), petState) { comparedPets, pet ->
        comparedPets.any { comparedPet ->
            isSamePet(comparedPet, pet)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _compareToggleEvent = MutableSharedFlow<CompareToggleUiEvent>(extraBufferCapacity = 1)
    val compareToggleEvent = _compareToggleEvent.asSharedFlow()

    init {
        if (!fromFavourite) {
            val desertionNo = initialPet.desertionNo.orEmpty().trim()
            if (desertionNo.isNotBlank()) {
                viewModelScope.launch(Dispatchers.IO) {
                    _isFavorite.value = runCatching { favouriteRepository.isExist(desertionNo) }
                        .getOrDefault(false)
                }
            }
        }
    }

    fun onEvent(event: AdoptionDetailEvent) {
        when (event) {
            is AdoptionDetailEvent.OnFavorite -> {
                if (uiState.value is DetailUiState.Success) {
                    val pet = (uiState.value as DetailUiState.Success).pet
                    val desertionNo = pet.desertionNo.orEmpty()

                    viewModelScope.launch {
                        val previousLike = _isFavorite.value
                        _isFavorite.value = event.isFavorite

                        val result = runCatching {
                            if (event.isFavorite) {
                                favouriteRepository.addPet(pet)
                            } else {
                                favouriteRepository.removePet(desertionNo)
                            }
                        }
                        if (result.isFailure) {
                            _isFavorite.value = previousLike
                            SnackBarManager.showMessage("관심 상태를 변경하지 못했어요. 잠시 후 다시 시도해주세요.")
                        } else {
                            syncInterestProfileFromFavorites()
                        }
                    }
                }
            }

            AdoptionDetailEvent.ToggleCompare -> {
                toggleCompare()
            }
        }
    }

    fun refreshCommentCount() {
        val noticeNo = petState.value.noticeNo
        viewModelScope.launch(Dispatchers.IO) {
            refreshCommentCount(noticeNo)
        }
    }

    private suspend fun refreshCommentCount(noticeNoRaw: String?) {
        val noticeNo = noticeNoRaw.orEmpty().trim()
        if (noticeNo.isBlank()) {
            _commentCount.value = 0
            return
        }
        runCatching {
            commentRepository.getCommentCount(noticeNo)
        }.onSuccess { count ->
            _commentCount.value = count
        }.onFailure { throwable ->
            Log.e(TAG, "failed to refresh comment count", throwable)
        }
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

    private fun toggleCompare() {
        viewModelScope.launch(Dispatchers.IO) {
            val pet = petState.value
            val action = if (isCompared.value) "remove" else "add"
            val result = compareRepository.toggle(pet)
            val selectedCount = compareRepository.comparedPets().value.size

            if (result == CompareToggleResult.LimitExceeded) {
                SnackBarManager.showMessage("비교는 최대 3마리까지 담을 수 있어요.")
            }
            _compareToggleEvent.emit(
                CompareToggleUiEvent(
                    result = result,
                    action = action,
                    selectedCount = selectedCount
                )
            )
        }
    }

    private suspend fun syncInterestProfileFromFavorites() {
        val userId = (sessionStore.session.value as? SessionState.Authenticated)
            ?.user
            ?.id
            ?.trim()
            .orEmpty()
        if (userId.isBlank()) return

        runCatching {
            interestProfileSyncCoordinator.syncFromFavorites(userId)
        }.onFailure { throwable ->
            Log.w(TAG, "Failed to sync interest profile from favorites", throwable)
        }
    }

    private fun isSamePet(first: Pet, second: Pet): Boolean {
        val firstDesertionNo = first.desertionNo.orEmpty().trim()
        val secondDesertionNo = second.desertionNo.orEmpty().trim()
        if (firstDesertionNo.isNotBlank() && secondDesertionNo.isNotBlank()) {
            return firstDesertionNo == secondDesertionNo
        }

        val firstNoticeNo = first.noticeNo.orEmpty().trim()
        val secondNoticeNo = second.noticeNo.orEmpty().trim()
        return firstNoticeNo.isNotBlank() && firstNoticeNo == secondNoticeNo
    }
}
