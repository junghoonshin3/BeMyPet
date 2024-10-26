package kr.sjh.feature.adoption_detail

import android.graphics.BlendMode
import android.graphics.PorterDuff
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.navigation.PinchZoom

@Composable
fun PetDetailRoute(
    detail: PetDetail, onBack: () -> Unit, navigateToPinchZoom: (PinchZoom) -> Unit
) {
    PetDetailScreen(
        pet = detail.petInfo, onBack = onBack, navigateToPinchZoom = navigateToPinchZoom
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PetDetailScreen(
    pet: Pet, onBack: () -> Unit, navigateToPinchZoom: (PinchZoom) -> Unit
) {

    val imageRequest = ImageRequest.Builder(LocalContext.current).data(pet.popfile).build()

    var selectedLike by remember { mutableStateOf(false) }

    val selectedColor by remember(selectedLike) {
        derivedStateOf {
            if (selectedLike) Color.Red else Color.Unspecified
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        stickyHeader {
            TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White, scrolledContainerColor = Color.White
            ), title = {}, modifier = Modifier
                .fillMaxWidth()
                .alpha(0.8f), navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back"
                    )
                }
            }, actions = {
                IconButton(onClick = {
                    selectedLike = !selectedLike
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
            PetDetailContent(imageRequest, pet, navigateToPinchZoom)
        }
    }
}

@Composable
private fun PetDetailContent(
    imageReq: ImageRequest, pet: Pet, navigateToPinchZoom: (PinchZoom) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PetImage(imageReq) {
            navigateToPinchZoom(PinchZoom(pet.popfile))
        }
        Title(title = pet.kindCd)
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
        TextLine(title = "색상", content = pet.colorCd)
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