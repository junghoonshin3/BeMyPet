package kr.sjh.bemypet.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kr.sjh.core.designsystem.R

sealed class BottomNavItem(
    @StringRes val title: Int, val contentDes: String, @DrawableRes val icon: Int
) {
    // 유기 동물
    data object Adoption : BottomNavItem(
        R.string.adoption,
        "Adoption",
        R.drawable.baseline_pets_24,
    )

    // 관심목록
    data object Favourite : BottomNavItem(
        R.string.favourite,
        "Favourite",
        R.drawable.like,
    )

    // 설정
    data object Setting : BottomNavItem(
        R.string.setting,
        "Setting",
        R.drawable.setting_5_svgrepo_com,
    )
}