package kr.sjh.feature.report

import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sjh.core.designsystem.R
import kr.sjh.core.designsystem.components.BeMyPetTopAppBar
import kr.sjh.core.designsystem.components.LoadingComponent
import kr.sjh.core.designsystem.components.Title
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
        ReportType.Comment -> {
            listOf("스팸", "괴롭힘", "혐오 발언", "기타")
        }

        ReportType.User -> {
            listOf("프로필 사진 신고", "사용자 닉네임 신고", "비매너 사용자", "기타")
        }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ReportScreen(
        uiState = uiState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
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
    var selectedReason by remember { mutableStateOf(reportReasons.first()) }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(key1 = uiState.isSuccessful) {
        if (uiState.isSuccessful) {
            onSuccess()
        }
    }

    Column(modifier = modifier) {
        BeMyPetTopAppBar(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
            title = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = "back",
                    )
                }
                Title(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    title = "신고",
                    style = MaterialTheme.typography.headlineSmall
                )
            })
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "신고 사유 선택", style = MaterialTheme.typography.titleMedium)
            DropdownMenuBox(options = reportReasons,
                selected = selectedReason,
                onOptionSelected = { selectedReason = it })
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "추가 설명", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.LightGray)
                    .padding(start = 5.dp, end = 5.dp),
            ) {
                if (!uiState.loading) {
                    TextField(
                        value = description,
                        onValueChange = {
                            description = it
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default),
                        placeholder = { Text("추가 설명를 입력하세요.") },
                        colors = TextFieldDefaults.colors(
                            cursorColor = Color.Black,
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            selectionColors = TextSelectionColors(Color.Black, Color.Black)
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 8.dp)
                    )
                } else {
                    LoadingComponent()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = !uiState.loading,
                onClick = { onReportSubmit(selectedReason, description) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(text = "신고 제출", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun DropdownMenuBox(options: List<String>, selected: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { expanded = true }) {
            Text(selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = {
                    onOptionSelected(option)
                    expanded = false
                })
            }
        }
    }
}