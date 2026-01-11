package kr.sjh.feature.favourite.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.TextLine
import kr.sjh.core.designsystem.theme.DefaultAppBarHeight
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
            .background(MaterialTheme.colorScheme.background),
        pets,
        navigateToPetDetail
    )
}

@Composable
private fun FavouriteScreen(
    modifier: Modifier = Modifier, pets: List<Pet>, navigateToPetDetail: (Pet) -> Unit
) {
    Box(modifier = modifier) {
        BeMyPetTopAppBar(modifier = Modifier
            .fillMaxWidth()
            .zIndex(1f)
            .background(MaterialTheme.colorScheme.primary), title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = stringResource(R.string.favourite),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        })
        if (pets.isEmpty()) {
            Text(
                "펫이 없어요!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            EndlessLazyGridColumn(userScrollEnabled = true,
                items = pets,
                itemKey = { item -> item.desertionNo },
                contentPadding = PaddingValues(
                    top = DefaultAppBarHeight + 10.dp, bottom = 10.dp, start = 5.dp, end = 5.dp
                ),
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
}

@Composable
private fun Pet(modifier: Modifier = Modifier, pet: Pet) {
    val context = LocalContext.current
    val imageRequest = ImageRequest.Builder(context).data(pet.popfile1).build()
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

@Preview
@Composable
fun FavouriteScreenPreview() {
    MaterialTheme {
        FavouriteScreen(modifier = Modifier.fillMaxSize(), pets = (0..100).map { i ->
            Pet(desertionNo = i.toString())
        }, navigateToPetDetail = {})
    }

}
