package kr.sjh.core.model


interface FilterCategory {
    val categoryName: String
}

enum class FilterBottomSheetState {
    SHOW, HIDE
}