package kr.sjh.bemypet.navigation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun BeMyPetBottomNavigation(
    modifier: Modifier = Modifier,
    destinations: List<TopLevelDestination>,
    navigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    currentTopLevelDestination: TopLevelDestination?,
    currentDestination: NavDestination?
) {
    if (currentTopLevelDestination == null) return
    BottomAppBar(modifier = modifier) {
        destinations.forEach { destination ->
            val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
            BeMyPetNavigationBarItem(icon = {
                Icon(
                    painter = painterResource(id = destination.icon),
                    contentDescription = destination.contentDes
                )
            }, onClick = {
                navigateToTopLevelDestination(destination)
            }, label = {
                Text(text = stringResource(id = destination.title))
            }, selected = selected)
        }
    }
}

@Composable
fun RowScope.BeMyPetNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    selectedContentColor: Color = Color.Red,
    unselectedContentColor: Color = Color.Gray,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = selectedContentColor,
            unselectedIconColor = unselectedContentColor
        )
    )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: TopLevelDestination) =
    this?.hierarchy?.any {
        it.route?.substringAfterLast(".")
            ?.contains(destination.contentDes, ignoreCase = true) == true
    } == true
