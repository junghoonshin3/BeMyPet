package kr.sjh.bemypet.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

@Composable
fun BeMyPetBottomNavigation(
    modifier: Modifier = Modifier,
    destinations: List<BottomNavItem>,
    navigateToTopLevelDestination: (BottomNavItem) -> Unit,
    currentBottomNavItem: BottomNavItem?,
    currentDestination: NavDestination?
) {
    val visible by remember(currentBottomNavItem) {
        derivedStateOf {
            when (currentBottomNavItem) {
                BottomNavItem.Adoption, BottomNavItem.Favourite, BottomNavItem.Setting -> true
                null -> false
            }
        }
    }
    val animationTime = 400

    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = animationTime,
                easing = LinearEasing // interpolator
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = animationTime,
                easing = LinearEasing // interpolator
            )
        )
    ) {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            destinations.forEach { destination ->
                val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
                BeMyPetNavigationBarItem(icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = destination.icon),
                        contentDescription = destination.contentDes
                    )
                }, onClick = {
                    navigateToTopLevelDestination(destination)
                }, label = {
                    Text(text = stringResource(id = destination.title))
                }, selected = selected
                )
            }
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
            selectedTextColor = selectedContentColor,
            unselectedIconColor = unselectedContentColor,
            unselectedTextColor = unselectedContentColor
        )
    )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: BottomNavItem) =
    this?.hierarchy?.any {
        it.route?.substringAfterLast(".")
            ?.contains(destination.contentDes, ignoreCase = true) == true
    } == true
