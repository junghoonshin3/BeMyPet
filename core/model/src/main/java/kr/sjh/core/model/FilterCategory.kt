package kr.sjh.core.model


interface FilterCategory {
    val categoryName: String
    val displayName: String
}

enum class FilterBottomSheetState {
    SHOW, HIDE
}