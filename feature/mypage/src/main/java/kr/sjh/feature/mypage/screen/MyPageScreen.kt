package kr.sjh.feature.mypage.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.sjh.feature.mypage.state.NavigationState

@Composable
fun MyPageRoute(viewModel: MyPageViewModel = hiltViewModel()) {

    val navigationState by viewModel.navigationState.collectAsStateWithLifecycle(NavigationState.None)

    LaunchedEffect(navigationState) {
//        when (navigationState) {
//            NavigationState.NavigationToLogin -> {
//                navigateToLogin()
//            }
//
//            NavigationState.None -> {}
//        }
    }

    MyPageScreen()
}

@Composable
fun MyPageScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
//        Button(onClick = signOut) {
//            Text("로그아웃")
//        }
    }
}