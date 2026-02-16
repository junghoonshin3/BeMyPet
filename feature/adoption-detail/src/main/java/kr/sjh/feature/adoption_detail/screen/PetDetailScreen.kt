package kr.sjh.feature.adoption_detail.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
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
import kr.sjh.core.common.share.sharePet
import kr.sjh.core.common.snackbar.SnackBarManager
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
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

    val context = LocalContext.current

    PetDetailScreen(
        modifier = modifier,
        uiState = uiState,
        isFavorite = isFavorite,
        onBack = onBack,
        state = location,
        commentCount = commentCount,
        onShare = { pet -> context.sharePet(pet) },
        onFavorite = { like ->
            viewModel.onEvent(AdoptionDetailEvent.OnFavorite(like))
        },
        onNavigateToComments = { noticeNo ->
            when (val state = session) {
                is SessionState.Authenticated -> {
                    onNavigateToComments(noticeNo, state.user.id)
                }

                is SessionState.NoAuthenticated -> {
                    SnackBarManager.showMessage("회원가입 화면으로 이동합니다.")
                    onNavigateToSignUp()
                }

                is SessionState.Banned -> {
                    SnackBarManager.showMessage("${state.bannedUntil}까지 차단된 사용자입니다. 관리자에게 문의해주세요.")
                }

                else -> Unit
            }
        }
    )
}

@Composable
private fun PetDetailScreen(
    modifier: Modifier = Modifier,
    uiState: DetailUiState,
    isFavorite: Boolean,
    commentCount: Int,
    state: LocationUiState,
    onBack: () -> Unit,
    onShare: (Pet) -> Unit,
    onFavorite: (Boolean) -> Unit,
    onNavigateToComments: (String) -> Unit
) {
    var selectedLike by remember(isFavorite) {
        mutableStateOf(isFavorite)
    }

    val selectedColor = if (selectedLike) Color.Red else MaterialTheme.colorScheme.onPrimary
    val context = LocalContext.current

    Box(modifier = modifier) {
        when (uiState) {
            is DetailUiState.Failure -> {
                uiState.e.printStackTrace()
                Log.d("PetDetailScreen", "Failure")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "상세 정보를 불러오지 못했어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DetailUiState.Loading -> {
                LoadingComponent()
            }

            is DetailUiState.Success -> {
                val imageRequest = remember(uiState.pet.thumbnailImageUrl, context) {
                    ImageRequest.Builder(context)
                        .data(uiState.pet.thumbnailImageUrl)
                        .crossfade(true)
                        .build()
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    BeMyPetTopAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerBottom24)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
                            .clip(RoundedCornerBottom24),
                        title = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                                    contentDescription = "back",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Text(
                                text = "입양 상세",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        },
                        iconButton = {
                            Row {
                                IconButton(onClick = { onShare(uiState.pet) }) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_share_24),
                                        contentDescription = "share",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                IconButton(onClick = {
                                    selectedLike = !selectedLike
                                    onFavorite(selectedLike)
                                }) {
                                    Icon(
                                        modifier = Modifier.size(24.dp),
                                        imageVector = ImageVector.vectorResource(id = R.drawable.like),
                                        contentDescription = "like",
                                        tint = selectedColor
                                    )
                                }
                            }
                        }
                    )

                    AdMobBanner()

                    PetDetailContent(
                        imageReq = imageRequest,
                        pet = uiState.pet,
                        commentCount = commentCount,
                        state = state,
                        onNavigateToComments = onNavigateToComments
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
        Dialog(
            properties = DialogProperties(
                decorFitsSystemWindows = false,
                usePlatformDefaultWidth = false
            ),
            onDismissRequest = { isDialogShow = false }
        ) {
            PetPinedZoomRoute(imageRequest = imageReq, close = { isDialogShow = false })
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PetHeroImage(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                imageReq = imageReq,
                onClick = { isDialogShow = true }
            )
        }

        item {
            SummarySection(
                modifier = Modifier.padding(horizontal = 16.dp),
                pet = pet,
                commentCount = commentCount,
                onNavigateToComments = onNavigateToComments
            )
        }

        item {
            InfoSectionCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "발견/공고 정보"
            ) {
                InfoRow(title = "공고번호", value = pet.noticeNo ?: "정보없음")
                InfoRow(title = "유기번호", value = pet.desertionNo ?: "정보없음")
                InfoRow(title = "접수일", value = pet.happenDate ?: "정보없음")
                InfoRow(title = "발견장소", value = pet.happenPlace ?: "정보없음", maxLines = 2)
                InfoRow(
                    title = "공고기간",
                    value = "${pet.noticeStartDate ?: "-"} ~ ${pet.noticeEndDate ?: "-"}",
                    maxLines = 1
                )
            }
        }

        item {
            InfoSectionCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "보호소 정보"
            ) {
                InfoRow(title = "보호소 이름", value = pet.careName ?: "정보없음")
                InfoRow(title = "보호소 연락처", value = pet.careTel ?: "정보없음")
                InfoRow(title = "보호장소", value = pet.careAddress ?: "정보없음", maxLines = 2)
                ShelterMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCorner12),
                    mapId = pet.careTel ?: "map",
                    careNm = pet.careName ?: "보호소",
                    state = state
                )
            }
        }

        item {
            InfoSectionCard(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = "추가 정보"
            ) {
                InfoRow(title = "품종", value = pet.kindFullName ?: pet.kindName ?: "정보없음", maxLines = 2)
                InfoRow(title = "성별", value = pet.sexCdToText)
                InfoRow(title = "상태", value = pet.processState ?: "정보없음")
                InfoRow(title = "중성화", value = pet.neuterYnToText)
                InfoRow(title = "나이", value = pet.age ?: "정보없음")
                InfoRow(title = "체중", value = pet.weight ?: "정보없음")
                InfoRow(title = "색상", value = pet.color ?: "정보없음")
                InfoRow(title = "관할기관", value = pet.organizationName ?: "정보없음", maxLines = 2)
                InfoRow(title = "특징", value = pet.specialMark ?: "정보없음", maxLines = 3)
            }
        }

        item {
            Box(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SummarySection(
    modifier: Modifier = Modifier,
    pet: Pet,
    commentCount: Int,
    onNavigateToComments: (String) -> Unit
) {
    val noticeNo = pet.noticeNo
    val canOpenComment = !noticeNo.isNullOrBlank()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = pet.kindFullName ?: pet.kindName ?: "품종 정보 없음",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = pet.happenPlace ?: "발견 장소 정보 없음",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetaChip(text = "성별 ${pet.sexCdToText}")
                MetaChip(text = pet.processState ?: "상태 미확인")
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canOpenComment) {
                        noticeNo?.let(onNavigateToComments)
                    },
                shape = RoundedCorner12,
                colors = CardDefaults.cardColors(
                    containerColor = if (canOpenComment) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    text = if (canOpenComment) {
                        "댓글 ${commentCount}개 보기"
                    } else {
                        "댓글 정보를 불러올 수 없어요"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (canOpenComment) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun InfoSectionCard(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun InfoRow(
    title: String,
    value: String,
    maxLines: Int = 1
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier.width(72.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MetaChip(text: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCorner12)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PetHeroImage(
    modifier: Modifier = Modifier,
    imageReq: ImageRequest,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            model = imageReq,
            contentDescription = "pet_detail"
        )
    }
}

@Composable
private fun ShelterMap(
    modifier: Modifier = Modifier,
    mapId: String,
    careNm: String,
    state: LocationUiState
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
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
                        LatLng(location.latitude, location.longitude),
                        17f
                    )
                }

                val markerState = rememberMarkerState(
                    key = mapId,
                    position = LatLng(location.latitude, location.longitude)
                )
                val mapProperties = remember {
                    MapProperties(maxZoomPreference = 19f, minZoomPreference = 5f)
                }
                val mapUiSettings = remember {
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
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    properties = mapProperties,
                    uiSettings = mapUiSettings,
                    cameraPositionState = cameraPositionState,
                ) {
                    Marker(
                        state = markerState,
                        title = careNm
                    )
                }
            }
        }
    }
}
