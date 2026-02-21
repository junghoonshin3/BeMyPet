package kr.sjh.feature.adoption_detail.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.displayBreedName
import kr.sjh.data.repository.CompareRepository

@HiltViewModel
class CompareBoardViewModel @Inject constructor(
    private val compareRepository: CompareRepository
) : ViewModel() {
    val comparedPets = compareRepository.comparedPets()

    fun removePet(compareKey: String) {
        viewModelScope.launch {
            compareRepository.remove(compareKey)
        }
    }

    fun clearComparedPets() {
        viewModelScope.launch {
            compareRepository.clear()
        }
    }
}

private data class CompareRowSpec(
    val label: String,
    val value: (Pet) -> String
)

@Composable
fun CompareBoardRoute(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    viewModel: CompareBoardViewModel = hiltViewModel(),
) {
    val comparedPets by viewModel.comparedPets.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val firebaseAnalytics = remember(context) { FirebaseAnalytics.getInstance(context) }
    val shareTemplate = remember(comparedPets) { buildCompareQuestionTemplate(comparedPets) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CompareBoardHeader(
            title = "비교 보드",
            selectedCount = comparedPets.size,
            onBack = onBack,
            onClear = viewModel::clearComparedPets
        )

        if (comparedPets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "비교할 친구를 2마리 이상 담아보세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 8.dp
                )
            ) {
                item {
                    ComparedPetsOverview(
                        pets = comparedPets,
                        onRemove = { pet ->
                            viewModel.removePet(pet.compareKey())
                        }
                    )
                }
                item {
                    CompareCategoryCard(
                        title = "기본 정보",
                        pets = comparedPets,
                        rows = listOf(
                            CompareRowSpec("품종") { it.displayBreedName },
                            CompareRowSpec("성별") { it.sexCdToText },
                            CompareRowSpec("나이") { it.age.orInfoText() },
                            CompareRowSpec("체중") { it.weight.orInfoText() },
                            CompareRowSpec("중성화") { it.neuterYnToText },
                            CompareRowSpec("상태") { it.processState.orInfoText() }
                        )
                    )
                }
                item {
                    CompareCategoryCard(
                        title = "보호소 정보",
                        pets = comparedPets,
                        rows = listOf(
                            CompareRowSpec("보호소명") { it.careName.orInfoText() },
                            CompareRowSpec("보호소 연락처") { it.careTel.orInfoText() }
                        )
                    )
                }
                item {
                    CompareCategoryCard(
                        title = "특징 비교",
                        pets = comparedPets,
                        rows = listOf(
                            CompareRowSpec("특징") { it.specialMark.orInfoText() }
                        )
                    )
                }
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(50.dp),
            enabled = comparedPets.isNotEmpty(),
            onClick = {
                shareCompareTemplate(context, shareTemplate)
                firebaseAnalytics.logEvent("compare_template_share", Bundle().apply {
                    putInt("selected_count", comparedPets.size)
                })
            },
            shape = RoundedCorner12,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = "질문 템플릿 공유",
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
private fun CompareBoardHeader(
    title: String,
    selectedCount: Int,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24)
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                contentDescription = "back",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = "$selectedCount/3 선택됨",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
        IconButton(onClick = onClear) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.refresh_svgrepo_com),
                contentDescription = "clear",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ComparedPetsOverview(
    pets: List<Pet>,
    onRemove: (Pet) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "비교 대상",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pets.forEachIndexed { index, pet ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCorner12,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "친구 ${index + 1}",
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                IconButton(
                                    modifier = Modifier.size(20.dp),
                                    onClick = { onRemove(pet) }
                                ) {
                                    Icon(
                                        modifier = Modifier.size(14.dp),
                                        imageVector = ImageVector.vectorResource(id = R.drawable.close_svgrepo_com),
                                        contentDescription = "remove",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            ComparePetThumbnail(
                                modifier = Modifier.fillMaxWidth(),
                                imageUrl = pet.thumbnailImageUrl?.trim()?.takeIf { it.isNotBlank() }
                            )
                            Text(
                                text = pet.displayBreedName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparePetThumbnail(
    modifier: Modifier = Modifier,
    imageUrl: String?
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        shape = RoundedCorner12,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        if (imageUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_pets_24),
                    contentDescription = "no image",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "pet thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun CompareCategoryCard(
    title: String,
    pets: List<Pet>,
    rows: List<CompareRowSpec>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCorner18,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            rows.forEach { spec ->
                CompareMatrixRow(
                    label = spec.label,
                    pets = pets,
                    valueSelector = spec.value
                )
            }
        }
    }
}

@Composable
private fun CompareMatrixRow(
    label: String,
    pets: List<Pet>,
    valueSelector: (Pet) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pets.forEachIndexed { index, pet ->
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCorner12,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "친구 ${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = valueSelector(pet).orInfoText(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private fun buildCompareQuestionTemplate(pets: List<Pet>): String {
    val selectedPetText = pets.joinToString(separator = "\n") { pet ->
        "- ${pet.displayBreedName} (공고번호: ${pet.noticeNo.orInfoText()}, 보호소: ${pet.careName.orInfoText()}, 연락처: ${pet.careTel.orInfoText()})"
    }
    return """
        [비교 대상]
        $selectedPetText

        [문의 질문]
        1. 현재 건강 상태와 최근 치료 이력이 어떻게 되나요?
        2. 사람/다른 동물과의 사회성은 어떤 편인가요?
        3. 실내외 생활 적응도는 어떤가요?
        4. 식사/배변/분리불안 관련해서 주의할 점이 있나요?
        5. 입양 절차, 필요 서류, 방문 가능 시간은 어떻게 되나요?
    """.trimIndent()
}

private fun shareCompareTemplate(context: Context, template: String) {
    if (template.isBlank()) return
    runCatching {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, template)
        }
        context.startActivity(Intent.createChooser(sendIntent, "질문 템플릿 공유"))
    }
}

private fun String?.orInfoText(): String = this?.takeIf { it.isNotBlank() } ?: "정보없음"

private fun Pet.compareKey(): String {
    return desertionNo?.trim()?.takeIf { it.isNotBlank() }
        ?: noticeNo?.trim()?.takeIf { it.isNotBlank() }
        ?: ""
}
