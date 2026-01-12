package kr.sjh.feature.adoption_detail.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
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
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.model.SessionState
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption_detail.state.AdoptionDetailEvent
import kr.sjh.feature.adoption_detail.state.DetailUiState

@Composable
fun PetDetailRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel(),
    onNavigateToComments: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val location by viewModel.location.collectAsStateWithLifecycle()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    val commentCount by viewModel.commentCount.collectAsStateWithLifecycle()

    val session by viewModel.session.collectAsStateWithLifecycle()

    PetDetailScreen(modifier = modifier,
        uiState = uiState,
        isFavorite = isFavorite,
        onBack = onBack,
        state = location,
        commentCount = commentCount,
        onFavorite = { like ->
            viewModel.onEvent(AdoptionDetailEvent.OnFavorite(like))
        },
        onNavigateToComments = {
            when (val state = session) {
                is SessionState.Authenticated -> {
                    val user = state.user
                    Log.d("sjh", "user.isBanned : ${user.isBanned}")
                    onNavigateToComments(it, state.user.id)
                }

                is SessionState.NoAuthenticated -> {
                    SnackBarManager.showMessage("회원가입 화면으로 이동합니다.")
                    onNavigateToSignUp()
                }

                is SessionState.Banned -> {
                    SnackBarManager.showMessage("${state.bannedUntil}까지 차단된 사용자입니다. 관리자에게 문의해주세요.")
                }

                else -> {}
            }
        })
}

@Composable
private fun PetDetailScreen(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    isFavorite: Boolean,
    commentCount: Int,
    state: LocationUiState,
    onBack: () -> Unit,
    onFavorite: (Boolean) -> Unit,
    onNavigateToComments: (String) -> Unit
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
    Box(modifier = modifier) {
        when (uiState) {
            is DetailUiState.Failure -> {
                uiState.e.printStackTrace()
                Log.d("PetDetailScreen", "Failure")
            }

            DetailUiState.Loading -> {
                LoadingComponent()
            }

            is DetailUiState.Success -> {
                val imageRequest =
                    ImageRequest.Builder(LocalContext.current).data(uiState.pet.thumbnailImageUrl).build()
                Column(modifier = Modifier.fillMaxSize()) {
                    BeMyPetTopAppBar(modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary), title = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                                contentDescription = "back",
                            )
                        }
                    }, iconButton = {
                        IconButton(onClick = {
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
                    PetDetailContent(
                        imageRequest, uiState.pet, commentCount, state, onNavigateToComments
                    )
                }

            }
        }

    }

}

@Composable
private fun PetDetailContent(
    imageReq: ImageRequest,
    pet: Pet,
    commentCount: Int,
    state: LocationUiState,
    onNavigateToComments: (String) -> Unit
) {
    var isDialogShow by remember { mutableStateOf(false) }

    if (isDialogShow) {
        Dialog(properties = DialogProperties(
            decorFitsSystemWindows = false, usePlatformDefaultWidth = false
        ), onDismissRequest = { isDialogShow = false }) {
            PetPinedZoomRoute(imageRequest = imageReq, close = { isDialogShow = false })
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                PetImage(imageReq) {
                    isDialogShow = true
                }
                Title(
                    modifier = Modifier.padding(16.dp),
                    title = "${pet.kindName}",
                    style = MaterialTheme.typography.titleMedium
                )
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 5.dp, bottom = 5.dp),
                    thickness = 1.dp
                )
                TextLine(title = "유기번호", content = "${pet.desertionNo}")
                TextLine(title = "접수일", content = "${pet.happenDate}")
                TextLine(title = "발견장소", content = "${pet.happenPlace}")
                TextLine(title = "품종", content = "${pet.kindName}")
                TextLine(title = "색상", content = pet.color ?: "정보없음")
                TextLine(title = "나이", content = "${pet.age}")
                TextLine(title = "체중", content ="${pet.weight}")
                TextLine(title = "공고기간", content = "${pet.noticeStartDate} ~ ${pet.noticeEndDate}")
                TextLine(title = "중성화", content = pet.neuterYnToText)
                TextLine(title = "상태", content = "${pet.processState}")
                TextLine(title = "성별", content = pet.sexCdToText)
                TextLine(title = "특징", content = pet.specialMark!!)
                TextLine(title = "보호소 이름", content = "${pet.careName}")
                TextLine(title = "보호소 연락처", content = "${pet.careTel}")
                TextLine(title = "보호장소", content = "${pet.careAddress}")
                ShelterMap(
                    modifier = Modifier.fillMaxSize(),
                    mapId = "${pet.careTel}",
                    careNm = "${pet.careName}",
                    state = state
                )
                TextLine(title = "관할기관", content = "${pet.organizationName}")
                pet.careName?.let {
                    TextLine(title = "담당자", content = it)
                }
                TextLine(title = "담당자 연락처", content = "${pet.careTel}")
//                pet.noticeComment?.let {
//                    TextLine(title = "특이사항", content = it)
//                }
            }
        }
//        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
//        CommentSummary(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(60.dp)
//                .clickable(onClick = { onNavigateToComments(pet.noticeNo) }), count = commentCount
//        )
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

@Composable
private fun CommentSummary(modifier: Modifier = Modifier, count: Int) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
            text = "댓글 ${count}개",
            style = MaterialTheme.typography.titleMedium
        )
    }
}