package kr.sjh.bemypet.navigation

import android.os.Bundle
import android.util.Log
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.serialization.json.Json
import kr.sjh.bemypet.BeMyPetAppState
import kr.sjh.bemypet.canGoBack
import kr.sjh.core.common.credential.AccountManager
import kr.sjh.core.model.SessionState
import kr.sjh.feature.adoption.navigation.Adoption
import kr.sjh.feature.adoption.screen.AdoptionRoute
import kr.sjh.feature.adoption_detail.navigation.PetDetail
import kr.sjh.feature.adoption_detail.navigation.navigateToPetDetail
import kr.sjh.feature.adoption_detail.screen.PetDetailRoute
import kr.sjh.feature.block.BlockRoute
import kr.sjh.feature.comments.CommentRoute
import kr.sjh.feature.comments.navigation.Comments
import kr.sjh.feature.favourite.navigation.Favourite
import kr.sjh.feature.favourite.screen.FavouriteRoute
import kr.sjh.feature.navigation.Block
import kr.sjh.feature.report.ReportRoute
import kr.sjh.feature.report.navigation.Report
import kr.sjh.feature.signup.SignInRoute
import kr.sjh.feature.signup.SignInViewModel
import kr.sjh.feature.signup.navigation.SignUp
import kr.sjh.setting.navigation.Setting
import kr.sjh.setting.screen.SettingRoute
import kotlin.reflect.typeOf

@Composable
fun BeMyPetNavHost(
    modifier: Modifier = Modifier,
    appState: BeMyPetAppState,
    bottomPadding: Dp,
    session: SessionState,
    onChangeDarkTheme: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val accountManager = AccountManager(context)

    NavHost(
        modifier = modifier,
        navController = appState.navController, startDestination = Adoption,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
    ) {

        composable<Adoption> {
            AdoptionRoute(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .background(MaterialTheme.colorScheme.background),
                navigateToPetDetail = { pet ->
                    appState.navController.navigateToPetDetail(pet)
                })
        }

        composable<PetDetail>(
            typeMap = PetDetail.typeMap
        ) {
            PetDetailRoute(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .background(MaterialTheme.colorScheme.background), onBack = {
                if (appState.navController.canGoBack()) {
                    appState.navController.popBackStack()
                }
            }, onNavigateToComments = { noticeNo, userId ->
                appState.navController.navigate(Comments(noticeNo, userId))
            }, onNavigateToSignUp = {
                appState.navController.navigate(SignUp)
            })
        }

        composable<Favourite> {
            FavouriteRoute(navigateToPetDetail = { pet ->
                appState.navController.navigateToPetDetail(pet)
            })
        }
        composable<Setting> {
            SettingRoute(modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
                .background(MaterialTheme.colorScheme.background),
                session = session,
                accountManager = accountManager,
                onChangeDarkTheme = onChangeDarkTheme,
                onNavigateToSignIn = {
                    appState.navController.navigate(SignUp)
                },
                onNavigateToAdoption = {
                    appState.navController.navigate(Adoption)
                },
                onNavigateToBlockedUser = { uid ->
                    appState.navController.navigate(Block(uid))
                })
        }

        composable<Comments> {
            CommentRoute(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .navigationBarsPadding(),
                session = session,
                onBack = {
                    appState.navController.popBackStack()
                },
                navigateToReport = { type, comment, user ->
                    appState.navController.navigate(
                        Report(
                            type = type,
                            reportedUserId = comment.userId,
                            reportByUserId = user.id,
                            commentId = comment.id
                        )
                    )
                })
        }

        composable<SignUp>(enterTransition = {  // 아래에서 위로 올라오는 애니메이션
            slideInVertically(initialOffsetY = { 1000 }, animationSpec = tween(700))
        }, exitTransition = { // 뒤로 갈 때 아래로 내려가는 애니메이션
            slideOutVertically(targetOffsetY = { -1000 }, animationSpec = tween(700))
        }, popEnterTransition = {
            slideInVertically(initialOffsetY = { -1000 }, animationSpec = tween(700))
        }, popExitTransition = {
            slideOutVertically(targetOffsetY = { 1000 }, animationSpec = tween(700))
        }) {
            SignInRoute(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .navigationBarsPadding(),
                accountManager = accountManager,
                onSignInSuccess = {
                    appState.navController.navigate(Adoption) {
                        popUpTo(Adoption) {
                            inclusive = true
                        }
                    }
                },
                onBack = {
                    appState.navController.popBackStack()
                })
        }

        composable<Report>(
            typeMap = mapOf(typeOf<Report>() to ReportNavType),
        ) {
            val report = it.toRoute<Report>()
            ReportRoute(report, onBack = {
                appState.navController.popBackStack()
            }, onSuccess = {
                appState.navController.popBackStack()
            })
        }

        composable<Block> {
            BlockRoute(onBack = {
                appState.navController.popBackStack()
            })
        }
    }
}


object ReportNavType : NavType<Report>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Report? {
        return bundle.getString(key)?.let { Json.decodeFromString(it) }
    }

    override fun parseValue(value: String): Report {
        return Json.decodeFromString(value)
    }

    override fun put(bundle: Bundle, key: String, value: Report) {
        bundle.putString(key, Json.encodeToString(Report.serializer(), value))
    }

    override fun serializeAsValue(value: Report): String {
        return Json.encodeToString(Report.serializer(), value)
    }
}