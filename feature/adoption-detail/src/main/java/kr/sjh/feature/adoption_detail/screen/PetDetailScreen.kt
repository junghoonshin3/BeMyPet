package kr.sjh.feature.adoption_detail.screen

import android.location.Location
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
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
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.PinchZoomComponent
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.model.Response
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent

@Composable
fun PetDetailRoute(
    onBack: () -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel(),
) {

    val location by viewModel.location.collectAsStateWithLifecycle()

    val isLike by viewModel.isLike.collectAsStateWithLifecycle()

    PetDetailScreen(
        pet = viewModel.pet,
        isLike = isLike,
        onBack = onBack,
        state = location,
        onLike = { like ->
            if (like) {
                viewModel.onEvent(AdoptionDetailEvent.AddLike)
            } else {
                viewModel.onEvent(AdoptionDetailEvent.RemoveLike)
            }
        },
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PetDetailScreen(
    pet: Pet,
    isLike: Boolean,
    state: LocationState,
    onBack: () -> Unit,
    onLike: (Boolean) -> Unit,
) {
    val imageRequest = ImageRequest.Builder(LocalContext.current).data(pet.popfile).build()

    var selectedLike by remember(isLike) {
        mutableStateOf(isLike)
    }
    val color = MaterialTheme.colorScheme.onPrimary
    val selectedColor by remember(selectedLike) {
        derivedStateOf {
            if (selectedLike) Color.Red else color
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        stickyHeader {
            BeMyPetTopAppBar(modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary.copy(0.8f))
                .zIndex(1f), title = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back",
                    )
                }
            }, iconButton = {
                IconButton(onClick = {
                    selectedLike = !selectedLike
                    onLike(selectedLike)
                }) {
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.like),
                        contentDescription = "like",
                        tint = selectedColor
                    )
                }
            })
        }
        item {
            PetDetailContent(imageRequest, pet, state)
        }
    }
}

@Composable
private fun PetDetailContent(
    imageReq: ImageRequest,
    pet: Pet,
    state: LocationState,
) {
    var isDialogShow by remember { mutableStateOf(false) }

    if (isDialogShow) {
        Dialog(properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { isDialogShow = false }) {
            PetPinedZoomRoute(imageRequest = imageReq, close = { isDialogShow = false })
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        PetImage(imageReq) {
            isDialogShow = true
        }
        Title(title = pet.kindCd)
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp, bottom = 5.dp), thickness = 1.dp
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
        when (state) {
            is LocationState.Failure -> {

            }

            LocationState.Loading -> {

            }

            is LocationState.Success -> {
                val location = state.location
                ShelterMap(mapId = pet.careAddr, careNm = pet.careNm, location = location)
            }
        }
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
private fun ShelterMap(mapId: String, careNm: String, location: Location) {

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(location.latitude, location.longitude), 17f)
    }

    val markerState = rememberMarkerState(
        key = mapId, position = LatLng(location.latitude, location.longitude)
    )
    val mapProperties by remember {
        mutableStateOf(
            MapProperties(maxZoomPreference = 14f, minZoomPreference = 5f)
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
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false,
            )
        )
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
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