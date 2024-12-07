package kr.sjh.feature.favourite.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.CheckBoxButton
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.model.adoption.Pet

@Composable
fun FavouriteRoute(
    navigateToPetDetail: (Pet) -> Unit,
    viewModel: FavouriteViewModel = hiltViewModel(),
) {
    val pets by viewModel.favouritePets.collectAsStateWithLifecycle()
    FavouriteScreen(
        Modifier
            .fillMaxSize()
            .background(Color.White), pets, navigateToPetDetail
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouriteScreen(
    modifier: Modifier = Modifier, pets: List<Pet>, navigateToPetDetail: (Pet) -> Unit
) {
    Column(modifier = modifier) {
        TopAppBar(title = { Text(text = stringResource(id = R.string.favourite)) })
        EndlessLazyGridColumn(userScrollEnabled = true,
            items = pets,
            itemKey = { item -> item.hashCode() },
            loadMore = { }) { pet ->
            Pet(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        navigateToPetDetail(pet)
                    }, pet = pet
            )
        }
    }
}

@Composable
private fun Pet(modifier: Modifier = Modifier, pet: Pet) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context).data(pet.popfile).build()
    val fontSize = 9.sp
    Column(
        modifier = modifier
    ) {
        SubcomposeAsyncImage(contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(10.dp)),
            model = imageRequest,
            contentDescription = "Pet",
            loading = {
                LoadingComponent()
            })
        TextLine(
            title = "공고번호",
            content = pet.noticeNo,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "발견장소",
            content = pet.happenPlace,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "품종",
            content = pet.kindCd,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "성별",
            content = pet.sexCdToText,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
        TextLine(
            title = "상태",
            content = pet.processState,
            titleTextStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = fontSize),
            contentTextStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = fontSize)
        )
    }
}

@Composable
@Preview
private fun FavouriteScreenPreview() {
    var list = (0..100).map { index ->
        Pet(
            desertionNo = index.toString(),
            filename = "",
            happenDt = "",
            happenPlace = "",
            kindCd = "",
            colorCd = "",
            age = "",
            weight = "",
            noticeNo = "",
            noticeSdt = "",
            noticeEdt = "",
            popfile = "",
            processState = "",
            sexCd = "",
            neuterYn = "",
            specialMark = "",
            careNm = "",
            careTel = "",
            careAddr = "",
            orgNm = "",
            officetel = ""


        )
    }
    FavouriteScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White), pets = list, navigateToPetDetail = {}
    )
}
