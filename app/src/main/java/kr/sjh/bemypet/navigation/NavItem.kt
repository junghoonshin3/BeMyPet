package kr.sjh.bemypet.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kr.sjh.core.designsystem.R
import kr.sjh.core.model.Screen

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
    data object LikePet : BottomNavItem(
        R.string.like_pet,
        "LikePet",
        R.drawable.like,
    )

    // 마이 페이지
    data object MyPage : BottomNavItem(
        R.string.mypage,
        "MyPage",
        R.drawable.baseline_account_circle_24,
    )
}