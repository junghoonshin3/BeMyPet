package kr.sjh.feature.report

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.PrimaryActionButton
import kr.sjh.core.designsystem.components.SelectableListItem
import kr.sjh.core.designsystem.components.Title
import kr.sjh.core.designsystem.theme.RoundedCorner12
import kr.sjh.core.designsystem.theme.RoundedCorner18
import kr.sjh.core.designsystem.theme.RoundedCornerBottom24
import kr.sjh.core.model.ReportForm
import kr.sjh.core.model.ReportType
import kr.sjh.feature.report.navigation.Report

@Composable
fun ReportRoute(
    report: Report,
    viewModel: ReportViewModel = hiltViewModel(),
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    Log.d("sjh", "$report")
    val reportReasons = when (report.type) {
        ReportType.Comment -> listOf("스팸", "괴롭힘", "혐오 발언", "기타")
        ReportType.User -> listOf("프로필 사진 신고", "사용자 닉네임 신고", "비매너 사용자", "기타")
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReportScreen(
        uiState = uiState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
        onReportSubmit = { reason, description ->
            viewModel.report(
                ReportForm(
                    type = report.type,
                    reportedByUser = report.reportByUserId,
                    reportedUser = report.reportedUserId,
                    commentId = report.commentId,
                    reason = reason,
                    description = description
                )
            )
        },
        reportReasons = reportReasons,
        onBack = onBack,
        onSuccess = onSuccess
    )
}

@Composable
fun ReportScreen(
    uiState: ReportUiState,
    modifier: Modifier = Modifier,
    onReportSubmit: (String, String) -> Unit,
    reportReasons: List<String>,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var selectedReason by remember(reportReasons) { mutableStateOf(reportReasons.first()) }
    var description by remember { mutableStateOf("") }
    var reasonExpanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = uiState.isSuccessful) {
        if (uiState.isSuccessful) {
            onSuccess()
        }
    }

    Column(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
                keyboardController?.hide()
            })
        }
    ) {
        BeMyPetTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary, RoundedCornerBottom24),
            title = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = "신고",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = "신고 사유 선택",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box {
                            SelectableListItem(
                                title = selectedReason,
                                modifier = Modifier.fillMaxWidth(),
                                selected = true,
                                showCheckIcon = false,
                                onClick = { reasonExpanded = true }
                            )

                            DropdownMenu(
                                expanded = reasonExpanded,
                                onDismissRequest = { reasonExpanded = false }
                            ) {
                                reportReasons.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedReason = option
                                            reasonExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            text = "추가 설명",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCorner12
                                )
                                .padding(horizontal = 6.dp)
                        ) {
                            if (!uiState.loading) {
                                TextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    keyboardOptions = KeyboardOptions.Default.copy(
                                        imeAction = ImeAction.Default
                                    ),
                                    placeholder = { Text("추가 설명을 입력하세요.") },
                                    colors = TextFieldDefaults.colors(
                                        cursorColor = MaterialTheme.colorScheme.secondary,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        selectionColors = TextSelectionColors(
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                                        )
                                    ),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                LoadingComponent()
                            }
                        }
                    }
                }
            }

            PrimaryActionButton(
                text = "신고 제출",
                enabled = !uiState.loading,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    onReportSubmit(selectedReason, description)
                }
            )
        }
    }
}
