package kr.sjh.bemypet.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kr.sjh.core.designsystem.R

sealed class TopLevelDestination(
    @StringRes val title: Int, val contentDes: String, @DrawableRes val icon: Int, val screen: Any
) {
    // 펫 입양
    data object Adoption : TopLevelDestination(
        R.string.adoption,
        "Adoption",
        R.drawable.baseline_pets_24,
        kr.sjh.feature.adoption.navigation.Adoption
    )

    // 입양 후기
    data object Review : TopLevelDestination(
        R.string.review,
        "Review",
        R.drawable.baseline_list_24,
        kr.sjh.feature.review.navigation.Review
    )

    // 채팅
    data object Chat : TopLevelDestination(
        R.string.chat, "Chat", R.drawable.baseline_chat_24, kr.sjh.feature.chat.navigation.Chat
    )


    // 마이 페이지
    data object MyPage : TopLevelDestination(
        R.string.mypage,
        "MyPage",
        R.drawable.baseline_account_circle_24,
        kr.sjh.feature.mypage.navigation.MyPage
    )
}