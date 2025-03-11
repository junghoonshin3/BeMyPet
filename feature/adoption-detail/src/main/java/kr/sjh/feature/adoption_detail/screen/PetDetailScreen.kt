package kr.sjh.feature.adoption_detail.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kr.sjh.core.common.ads.AdMobBanner
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import kr.sjh.feature.adoption_detail.state.DetailUiState

@Composable
fun PetDetailRoute(
    onBack: () -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel(),
) {
    val location by viewModel.location.collectAsStateWithLifecycle()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    PetDetailScreen(modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background),
        uiState = uiState,
        isFavorite = isFavorite,
        onBack = onBack,
        state = location,
        onFavorite = { like ->
            viewModel.onEvent(AdoptionDetailEvent.OnFavorite(like))
        })
}

@Composable
private fun PetDetailScreen(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    isFavorite: Boolean,
    state: LocationUiState,
    onBack: () -> Unit,
    onFavorite: (Boolean) -> Unit,
) {

    var selectedLike by remember(isFavorite) {
        mutableStateOf(isFavorite)
    }

    val color = MaterialTheme.colorScheme.onPrimary

    val selectedColor by remember(selectedLike) {
        derivedStateOf {
            if (selectedLike) Color.Red else color
        }
    }
    Column(modifier = modifier) {
        BeMyPetTopAppBar(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
            title = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back",
                    )
                }
            },
            iconButton = {
                IconButton(
                    onClick = {
                        selectedLike = !selectedLike
                        onFavorite(selectedLike)
                    }) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.like),
                        contentDescription = "like",
                        tint = selectedColor
                    )
                }
            })
        AdMobBanner()
        when (uiState) {
            is DetailUiState.Failure -> {
                Log.d("PetDetailScreen", "Failure")
            }

            DetailUiState.Loading -> {
                Log.d("PetDetailScreen", "Loading")
                LoadingComponent()
            }

            is DetailUiState.Success -> {
                val imageRequest =
                    ImageRequest.Builder(LocalContext.current).data(uiState.pet.popfile).build()
                PetDetailContent(imageRequest, uiState.pet, state)
            }
        }
    }
}

@Composable
private fun PetDetailContent(
    imageReq: ImageRequest,
    pet: Pet,
    state: LocationUiState,
) {
    var isDialogShow by remember { mutableStateOf(false) }

    if (isDialogShow) {
        Dialog(properties = DialogProperties(
            decorFitsSystemWindows = false,
            usePlatformDefaultWidth = false
        ),
            onDismissRequest = { isDialogShow = false }) {
            PetPinedZoomRoute(imageRequest = imageReq, close = { isDialogShow = false })
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            PetImage(imageReq) {
                isDialogShow = true
            }
            Title(
                modifier = Modifier.padding(16.dp),
                title = pet.kindCd,
                style = MaterialTheme.typography.titleMedium
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp, bottom = 5.dp),
                thickness = 1.dp
            )
            TextLine(title = "유기번호", content = pet.desertionNo)
            TextLine(title = "접수일", content = pet.happenDt)
            TextLine(title = "발견장소", content = pet.happenPlace)
            TextLine(title = "품종", content = pet.kindCd)
            TextLine(title = "색상", content = pet.colorCd ?: "정보없음")
            TextLine(title = "나이", content = pet.age)
            TextLine(title = "체중", content = pet.weight)
            TextLine(title = "공고기간", content = "${pet.noticeSdt} ~ ${pet.noticeEdt}")
            TextLine(title = "중성화", content = pet.neuterYnToText)
            TextLine(title = "상태", content = pet.processState)
            TextLine(title = "성별", content = pet.sexCdToText)
            TextLine(title = "나이", content = pet.age)
            TextLine(title = "특징", content = pet.specialMark)
            TextLine(title = "보호소 이름", content = pet.careNm)
            TextLine(title = "보호소 연락처", content = pet.careTel)
            TextLine(title = "보호장소", content = pet.careAddr)
            ShelterMap(
                modifier = Modifier.fillMaxSize(),
                mapId = pet.careAddr,
                careNm = pet.careNm,
                state = state
            )
            TextLine(title = "관할기관", content = pet.orgNm)
            pet.chargeNm?.let {
                TextLine(title = "담당자", content = it)
            }
            TextLine(title = "담당자 연락처", content = pet.officetel)
            pet.noticeComment?.let {
                TextLine(title = "특이사항", content = it)
            }
        }
    }
}

@Composable
private fun PetImage(imageReq: ImageRequest, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clickable { onClick() }, contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            model = imageReq,
            contentDescription = "pet_detail"
        )
    }
}

@Composable
private fun ShelterMap(
    modifier: Modifier = Modifier, mapId: String, careNm: String, state: LocationUiState
) {
    Box(
        modifier = Modifier.aspectRatio(1f), contentAlignment = Alignment.Center
    ) {
        when (state) {
            is LocationUiState.Failure -> {
                Text(text = "지도를 읽어오는데 실패했습니다")
            }

            LocationUiState.Loading -> {
                LoadingComponent()
            }

            is LocationUiState.Success -> {
                val location = state.location
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(location.latitude, location.longitude), 17f
                    )
                }

                val markerState = rememberMarkerState(
                    key = mapId, position = LatLng(location.latitude, location.longitude)
                )
                val mapProperties by remember {
                    mutableStateOf(
                        MapProperties(maxZoomPreference = 19f, minZoomPreference = 5f)
                    )
                }
                val mapUiSettings by remember {
                    mutableStateOf(
                        MapUiSettings(
                            compassEnabled = false,
                            indoorLevelPickerEnabled = false,
                            mapToolbarEnabled = false,
                            myLocationButtonEnabled = false,
                            rotationGesturesEnabled = false,
                            scrollGesturesEnabled = false,
                            scrollGesturesEnabledDuringRotateOrZoom = false,
                            tiltGesturesEnabled = false,
                            zoomControlsEnabled = true,
                            zoomGesturesEnabled = true,
                        )
                    )
                }

                GoogleMap(
                    modifier = modifier,
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    cameraPositionState = cameraPositionState,
                ) {
                    Marker(
                        state = markerState, title = careNm
                    )
                }
            }
        }
    }

}