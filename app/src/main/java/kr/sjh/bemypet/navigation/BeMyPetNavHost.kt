package kr.sjh.bemypet.navigation

import android.os.Bundle
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
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
import kr.sjh.feature.adoption_detail.navigation.CompareBoard
import kr.sjh.feature.adoption_detail.navigation.navigateToCompareBoard
import kr.sjh.feature.adoption_detail.navigation.navigateToPetDetail
import kr.sjh.feature.adoption_detail.screen.CompareBoardRoute
import kr.sjh.feature.adoption_detail.screen.PetDetailRoute
import kr.sjh.feature.block.BlockRoute
import kr.sjh.feature.comments.CommentRoute
import kr.sjh.feature.comments.navigation.Comments
import kr.sjh.feature.favourite.navigation.Favourite
import kr.sjh.feature.favourite.screen.FavouriteRoute
import kr.sjh.feature.navigation.Block
import kr.sjh.feature.report.ReportRoute
import kr.sjh.feature.report.navigation.Report
import kr.sjh.feature.signup.OnboardingRoute
import kr.sjh.feature.signup.SignInRoute
import kr.sjh.feature.signup.navigation.Onboarding
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
    hasSeenOnboarding: Boolean,
    onChangeDarkTheme: (Boolean) -> Unit,
    onCompleteOnboarding: () -> Unit,
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
                    appState.navController.navigateToPetDetail(
                        pet = pet,
                        fromFavourite = false
                    )
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
            }, session = session, onNavigateToComments = { noticeNo, userId ->
                appState.navController.navigate(Comments(noticeNo, userId))
            }, onNavigateToSignUp = {
                appState.navController.navigate(SignUp)
            }, onNavigateToCompareBoard = {
                appState.navController.navigateToCompareBoard()
            })
        }

        composable<CompareBoard> {
            CompareBoardRoute(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                onBack = {
                    appState.navController.popBackStack()
                }
            )
        }

        composable<Favourite> {
            FavouriteRoute(navigateToPetDetail = { pet ->
                appState.navController.navigateToPetDetail(
                    pet = pet,
                    fromFavourite = true
                )
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

        composable<SignUp>(enterTransition = {
            slideInVertically(
                initialOffsetY = { it / 6 },
                animationSpec = tween(260)
            ) + fadeIn(animationSpec = tween(220))
        }, exitTransition = {
            slideOutVertically(
                targetOffsetY = { -it / 12 },
                animationSpec = tween(220)
            ) + fadeOut(animationSpec = tween(180))
        }, popEnterTransition = {
            slideInVertically(
                initialOffsetY = { -it / 12 },
                animationSpec = tween(240)
            ) + fadeIn(animationSpec = tween(200))
        }, popExitTransition = {
            slideOutVertically(
                targetOffsetY = { it / 6 },
                animationSpec = tween(220)
            ) + fadeOut(animationSpec = tween(180))
        }) {
            SignInRoute(modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .navigationBarsPadding(),
                accountManager = accountManager,
                onSignInSuccess = {
                    if (hasSeenOnboarding) {
                        appState.navController.navigate(Adoption) {
                            popUpTo(SignUp) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    } else {
                        appState.navController.navigate(Onboarding) {
                            popUpTo(SignUp) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onBack = {
                    appState.navController.popBackStack()
                })
        }

        composable<Onboarding> {
            OnboardingRoute(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .navigationBarsPadding(),
                onBack = {
                    appState.navController.popBackStack()
                },
                onComplete = {
                    onCompleteOnboarding()
                    appState.navController.navigate(Adoption) {
                        popUpTo(Onboarding) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onSkip = {
                    onCompleteOnboarding()
                    appState.navController.navigate(Adoption) {
                        popUpTo(Onboarding) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
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
