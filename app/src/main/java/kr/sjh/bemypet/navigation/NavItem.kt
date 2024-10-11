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
        R.drawable.heart_svgrepo_com,
    )

    // 마이 페이지
    data object MyPage : BottomNavItem(
        R.string.mypage,
        "MyPage",
        R.drawable.baseline_account_circle_24,
    )
}

sealed class TopNavItem(
    @StringRes val title: Int,
    val contentDes: String,
    @DrawableRes val leftRes: Int? = null,
    @DrawableRes val rightRes: Int? = null
) {
    // 유기 동물
    data object Adoption : TopNavItem(
        R.string.adoption, "Adoption", null, null
    )

    data object PetDetail : TopNavItem(
        R.string.pet_detail,
        "PetDetail",
        R.drawable.baseline_arrow_back_24,
        R.drawable.heart_svgrepo_com
    )

    data object LikePet : TopNavItem(
        R.string.like_pet, "LikePet", null, null
    )

    data object MyPage : TopNavItem(
        R.string.mypage, "MyPage", null, null
    )
}