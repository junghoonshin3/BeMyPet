package kr.sjh.feature.favourite.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.EndlessLazyGridColumn
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.core.model.adoption.Pet

private val FavouriteHeaderHeight = 128.dp

@Composable
fun FavouriteRoute(
    navigateToPetDetail: (Pet) -> Unit,
    viewModel: FavouriteViewModel = hiltViewModel(),
) {
    val pets by viewModel.favouritePets.collectAsStateWithLifecycle()

    FavouriteScreen(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        pets = pets,
        navigateToPetDetail = navigateToPetDetail
    )
}

@Composable
private fun FavouriteScreen(
    modifier: Modifier = Modifier, pets: List<Pet>, navigateToPetDetail: (Pet) -> Unit
) {
    Box(modifier = modifier) {
        BeMyPetTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .shadow(4.dp, RoundedCornerBottom24)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
                .clip(RoundedCornerBottom24),
            title = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "내가 저장한 친구들",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "관심동물",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        )

        if (pets.isEmpty()) {
            EmptyFavouriteState(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 28.dp)
            )
        } else {
            EndlessLazyGridColumn(
                userScrollEnabled = true,
                items = pets,
                itemKey = { item -> item.desertionNo ?: item.hashCode() },
                contentPadding = PaddingValues(
                    top = FavouriteHeaderHeight + 12.dp,
                    bottom = 16.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
                loadMore = { }
            ) { pet ->
                FavouritePetCard(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            navigateToPetDetail(pet)
                        },
                    pet = pet
                )
            }
        }
    }
}

private val PetItemTitleStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 9.sp)
private val PetItemContentStyle = TextStyle(fontWeight = FontWeight.Light, fontSize = 9.sp)

@Composable
private fun EmptyFavouriteState(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "아직 저장한 친구가 없어요",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "입양 탭에서 마음에 드는 친구를 저장해보세요.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FavouritePetCard(modifier: Modifier = Modifier, pet: Pet) {
    Card(
        modifier = modifier,
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            SubcomposeAsyncImage(
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                model = pet.thumbnailImageUrl,
                contentDescription = "Pet",
                loading = {
                    LoadingComponent()
                }
            )
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pet.kindFullName?.ifBlank { "품종 정보 없음" }
                        ?: pet.kindName?.ifBlank { "품종 정보 없음" }
                        ?: "품종 정보 없음",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = pet.happenPlace?.ifBlank { "발견 장소 정보 없음" } ?: "발견 장소 정보 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FavouriteMetaChip(text = "성별 ${pet.sexCdToText}")
                    FavouriteMetaChip(text = pet.processState?.ifBlank { "상태 미상" } ?: "상태 미상")
                }
                Text(
                    text = pet.noticeNo?.let { "공고번호 $it" } ?: "공고번호 미확인",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FavouriteMetaChip(text: String) {
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
