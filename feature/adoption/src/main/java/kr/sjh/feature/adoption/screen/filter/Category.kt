package kr.sjh.feature.adoption.screen.filter

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class Category(
    val type: CategoryType,
    val isSelected: MutableState<Boolean> = mutableStateOf(false),
    val displayNm: MutableState<String> = mutableStateOf("")
)


enum class CategoryType(val title: String) {
    DATE_RANGE("기간"), NEUTER("중성화"), LOCATION("지역"), UP_KIND("품종")
}