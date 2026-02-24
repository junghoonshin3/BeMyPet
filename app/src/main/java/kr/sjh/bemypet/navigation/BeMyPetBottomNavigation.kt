package kr.sjh.bemypet.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import kr.sjh.core.designsystem.theme.RoundedCorner24

@Composable
fun BeMyPetBottomNavigation(
    modifier: Modifier = Modifier,
    destinations: List<BottomNavItem>,
    navigateToTopLevelDestination: (BottomNavItem) -> Unit,
    currentBottomNavItem: BottomNavItem?,
    currentDestination: NavDestination?
) {
    val visible by remember(currentBottomNavItem) {
        mutableStateOf(
            when (currentBottomNavItem) {
                BottomNavItem.Adoption, BottomNavItem.Favourite, BottomNavItem.Setting -> true
                null -> false
            }
        )
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = visible, enter = slideInVertically(
            initialOffsetY = { it },
        ), exit = slideOutVertically(
            targetOffsetY = { it },
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCorner24,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                shadowElevation = 2.dp
            ) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    destinations.forEach { destination ->
                        val selected =
                            currentDestination.isTopLevelDestinationInHierarchy(destination)
                        BeMyPetNavigationBarItem(
                            icon = {
                                Icon(
                                    modifier = Modifier.size(22.dp),
                                    painter = painterResource(id = destination.icon),
                                    contentDescription = destination.contentDes
                                )
                            },
                            onClick = {
                                navigateToTopLevelDestination(destination)
                            },
                            label = {
                                Text(text = stringResource(id = destination.title))
                            },
                            selected = selected
                        )
                    }
                }
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
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.secondary,
            selectedTextColor = MaterialTheme.colorScheme.secondary,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(destination: BottomNavItem) =
    this?.hierarchy?.any {
        it.route?.substringAfterLast(".")
            ?.contains(destination.contentDes, ignoreCase = true) == true
    } == true
