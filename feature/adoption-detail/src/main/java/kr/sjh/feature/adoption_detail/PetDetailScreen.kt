package kr.sjh.feature.adoption_detail

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kr.sjh.core.designsystem.components.ImageDialog
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.model.adoption.Pet
import kr.sjh.feature.adoption_detail.navigation.PetDetail

@Composable
fun PetDetailRoute(detail: PetDetail) {
    PetDetailScreen(pet = detail.petInfo)
}

@Composable
private fun PetDetailScreen(pet: Pet) {
    val scrollState = rememberScrollState()

    val imageRequest = ImageRequest.Builder(LocalContext.current).data(pet.popfile).build()

    var isShow by remember {
        mutableStateOf(false)
    }

    ImageDialog(isShow = isShow,
        close = { isShow = false },
        onDismissRequest = { isShow = false }) {
        AsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.Center),
            contentScale = ContentScale.None,
            model = imageRequest,
            contentDescription = "pet_detail"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        PetImage(imageRequest) {
            isShow = true
        }
        Title(title = pet.kindCd)
        Text(text = pet.colorCd)
        Text(text = pet.happenDt)
        Text(text = pet.happenPlace)
        Text(text = pet.age)
        Text(text = pet.weight)
        Text(text = pet.sexCd)
        Text(text = pet.neuterYn)
        Text(text = pet.processState)
        Text(text = "${pet.noticeSdt}~${pet.noticeEdt}")
        Text(text = pet.specialMark)
        Text(text = pet.careNm)
        Text(text = pet.careTel)
        Text(text = pet.careAddr)
        Text(text = pet.orgNm)
        pet.chargeNm?.let { chargeNm ->
            Text(text = chargeNm)
        }
        Text(text = pet.officetel)
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

