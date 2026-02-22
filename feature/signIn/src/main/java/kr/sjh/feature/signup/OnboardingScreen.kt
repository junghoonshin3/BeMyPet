package kr.sjh.feature.signup

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.PrimaryActionButton
import kr.sjh.core.designsystem.theme.RoundedCorner16
import kr.sjh.core.model.SessionState

private data class OnboardingPage(
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String,
)

private val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.fg_ic_image_placeholder,
        title = "몇 분 만에 입양 탐색을 시작해요",
        description = "필터와 카드 목록으로 필요한 조건을 빠르게 좁혀보세요."
    ),
    OnboardingPage(
        imageRes = R.drawable.fg_ic_star_filled,
        title = "마음에 드는 친구를 저장해요",
        description = "관심 목록에 담고 상태 변화를 다시 확인할 수 있어요."
    ),
    OnboardingPage(
        imageRes = R.drawable.baseline_pets_24,
        title = "새 공고 알림으로 다시 만나요",
        description = "관심 동물/지역을 선택하면 새 공고를 빠르게 알려드려요."
    )
)

private val regionOptions = listOf(
    "6110000" to "서울",
    "6410000" to "경기",
    "6260000" to "부산",
)

private val speciesOptions = listOf(
    "dog" to "강아지",
    "cat" to "고양이",
)

@Composable
fun OnboardingRoute(
    modifier: Modifier = Modifier,
    session: SessionState,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
) {
    var pageIndex by remember { mutableIntStateOf(0) }
    val page = onboardingPages[pageIndex]
    val preferenceState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.baseline_arrow_back_24),
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            TextButton(onClick = {
                viewModel.submit(session)
                onSkip()
            }) {
                Text(
                    text = "건너뛰기",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCorner16),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = ImageVector.vectorResource(id = page.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCorner16)
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                DotsIndicator(currentPage = pageIndex, pageCount = onboardingPages.size)

                Text(
                    text = page.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = page.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )

                if (pageIndex == onboardingPages.lastIndex) {
                    OnboardingPreferenceSection(
                        state = preferenceState,
                        onToggleRegion = viewModel::toggleRegion,
                        onToggleSpecies = viewModel::toggleSpecies,
                        onPushToggle = viewModel::setPushOptIn,
                    )
                }
            }
        }

        PrimaryActionButton(
            text = if (pageIndex == onboardingPages.lastIndex) "시작하기" else "다음",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            onClick = {
                if (pageIndex == onboardingPages.lastIndex) {
                    viewModel.submit(session)
                    onComplete()
                } else {
                    pageIndex += 1
                }
            }
        )
    }
}

@Composable
private fun OnboardingPreferenceSection(
    state: OnboardingPreferenceUiState,
    onToggleRegion: (String) -> Unit,
    onToggleSpecies: (String) -> Unit,
    onPushToggle: (Boolean) -> Unit,
) {
    Text(
        text = "관심 지역",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        regionOptions.forEach { (code, label) ->
            FilterChip(
                selected = state.regions.contains(code),
                onClick = { onToggleRegion(code) },
                label = { Text(label) }
            )
        }
    }

    Text(
        text = "관심 동물",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        speciesOptions.forEach { (code, label) ->
            FilterChip(
                selected = state.species.contains(code),
                onClick = { onToggleSpecies(code) },
                label = { Text(label) }
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "새 공고 푸시 알림 받기",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = state.pushOptIn,
            onCheckedChange = onPushToggle,
        )
    }
}

@Composable
private fun DotsIndicator(currentPage: Int, pageCount: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (index == currentPage) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${currentPage + 1}/$pageCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
