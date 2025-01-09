package kr.sjh.core.designsystem.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BeMyPetTopAppBar(
    title: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.then(Modifier.heightIn(min = 64.dp))
    ) {
        title()
        content()
    }
}


//class BeMyPetTopAppBarState(
//    initialHeightOffsetLimit: Float, initialHeightOffset: Float, initialContentOffset: Float
//) {
//    /** [heightOffsetLimit]는 상단 앱 바가 접힐 수 있는 최대 높이 오프셋을 나타냅니다.**/
//    var heightOffsetLimit by mutableFloatStateOf(initialHeightOffsetLimit)
//
//    private var _heightOffset = mutableFloatStateOf(initialHeightOffset)
//
//    /** [heightOffset]는 상단 앱 바의 현재 높이 오프셋을 나타냅니다.**/
//    var heightOffset: Float
//        get() = _heightOffset.floatValue
//        set(newOffset) {
//            _heightOffset.floatValue =
//                newOffset.coerceIn(minimumValue = heightOffsetLimit, maximumValue = 0f)
//        }
//
//    var contentOffset by mutableFloatStateOf(initialContentOffset)
//
//    val collapsedFraction: Float
//        get() = if (heightOffsetLimit != 0f) {
//            heightOffset / heightOffsetLimit
//        } else {
//            0f
//        }
//
//    val overlappedFraction: Float
//        get() = if (heightOffsetLimit != 0f) {
//            1 - ((heightOffsetLimit - contentOffset).coerceIn(
//                minimumValue = heightOffsetLimit, maximumValue = 0f
//            ) / heightOffsetLimit)
//        } else {
//            0f
//        }
//
//    companion object {
//        /** The default [Saver] implementation for [TopAppBarState]. */
//        val Saver: Saver<BeMyPetTopAppBarState, *> =
//            listSaver(save = { listOf(it.heightOffsetLimit, it.heightOffset, it.contentOffset) },
//                restore = {
//                    BeMyPetTopAppBarState(
//                        initialHeightOffsetLimit = it[0],
//                        initialHeightOffset = it[1],
//                        initialContentOffset = it[2]
//                    )
//                })
//    }
//}
//
//@Composable
//fun rememberBeMyPetTopAppBarState(
//    initialHeightOffsetLimit: Float = -Float.MAX_VALUE,
//    initialHeightOffset: Float = 0f,
//    initialContentOffset: Float = 0f
//): BeMyPetTopAppBarState {
//    return rememberSaveable(saver = BeMyPetTopAppBarState.Saver) {
//        BeMyPetTopAppBarState(initialHeightOffsetLimit, initialHeightOffset, initialContentOffset)
//    }
//}
//
//private class EnterAlwaysScrollBehavior(
//    override val state: BeMyPetTopAppBarState,
//    override val snapAnimationSpec: AnimationSpec<Float>?,
//    override val flingAnimationSpec: DecayAnimationSpec<Float>?,
//    val canScroll: () -> Boolean = { true }
//) : BeMyPetTopAppBarScrollBehavior {
//    override val isPinned: Boolean = false
//    override var nestedScrollConnection = object : NestedScrollConnection {
//        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
//            if (!canScroll()) return Offset.Zero
//            val prevHeightOffset = state.heightOffset
//            state.heightOffset += available.y
//            return if (prevHeightOffset != state.heightOffset) {
//                // We're in the middle of top app bar collapse or expand.
//                // Consume only the scroll on the Y axis.
//                available.copy(x = 0f)
//            } else {
//                Offset.Zero
//            }
//        }
//
//        override fun onPostScroll(
//            consumed: Offset, available: Offset, source: NestedScrollSource
//        ): Offset {
//            if (!canScroll()) return Offset.Zero
//            state.contentOffset += consumed.y
//            if (state.heightOffset == 0f || state.heightOffset == state.heightOffsetLimit) {
//                if (consumed.y == 0f && available.y > 0f) {
//                    // Reset the total content offset to zero when scrolling all the way down.
//                    // This will eliminate some float precision inaccuracies.
//                    state.contentOffset = 0f
//                }
//            }
//            state.heightOffset += consumed.y
//            return Offset.Zero
//        }
//
//        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
//            val superConsumed = super.onPostFling(consumed, available)
//            return superConsumed + settleAppBar(
//                state, available.y, flingAnimationSpec, snapAnimationSpec
//            )
//        }
//    }
//}
//
//@Stable
//interface BeMyPetTopAppBarScrollBehavior {
//
//    /**
//     * A [TopAppBarState] that is attached to this behavior and is read and updated when scrolling
//     * happens.
//     */
//    val state: BeMyPetTopAppBarState
//
//    /**
//     * Indicates whether the top app bar is pinned.
//     *
//     * A pinned app bar will stay fixed in place when content is scrolled and will not react to any
//     * drag gestures.
//     */
//    val isPinned: Boolean
//
//    /**
//     * An optional [AnimationSpec] that defines how the top app bar snaps to either fully collapsed
//     * or fully extended state when a fling or a drag scrolled it into an intermediate position.
//     */
//    val snapAnimationSpec: AnimationSpec<Float>?
//
//    /**
//     * An optional [DecayAnimationSpec] that defined how to fling the top app bar when the user
//     * flings the app bar itself, or the content below it.
//     */
//    val flingAnimationSpec: DecayAnimationSpec<Float>?
//
//    /**
//     * A [NestedScrollConnection] that should be attached to a [Modifier.nestedScroll] in order to
//     * keep track of the scroll events.
//     */
//    val nestedScrollConnection: NestedScrollConnection
//}
//
//private suspend fun settleAppBar(
//    state: BeMyPetTopAppBarState,
//    velocity: Float,
//    flingAnimationSpec: DecayAnimationSpec<Float>?,
//    snapAnimationSpec: AnimationSpec<Float>?
//): Velocity {
//    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
//    // and just return Zero Velocity.
//    // Note that we don't check for 0f due to float precision with the collapsedFraction
//    // calculation.
//    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
//        return Velocity.Zero
//    }
//    var remainingVelocity = velocity
//    // In case there is an initial velocity that was left after a previous user fling, animate to
//    // continue the motion to expand or collapse the app bar.
//    if (flingAnimationSpec != null && abs(velocity) > 1f) {
//        var lastValue = 0f
//        AnimationState(
//            initialValue = 0f,
//            initialVelocity = velocity,
//        ).animateDecay(flingAnimationSpec) {
//            val delta = value - lastValue
//            val initialHeightOffset = state.heightOffset
//            state.heightOffset = initialHeightOffset + delta
//            val consumed = abs(initialHeightOffset - state.heightOffset)
//            lastValue = value
//            remainingVelocity = this.velocity
//            // avoid rounding errors and stop if anything is unconsumed
//            if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
//        }
//    }
//    // Snap if animation specs were provided.
//    if (snapAnimationSpec != null) {
//        if (state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
//            AnimationState(initialValue = state.heightOffset).animateTo(
//                if (state.collapsedFraction < 0.5f) {
//                    0f
//                } else {
//                    state.heightOffsetLimit
//                }, animationSpec = snapAnimationSpec
//            ) {
//                state.heightOffset = value
//            }
//        }
//    }
//
//    return Velocity(0f, remainingVelocity)
//}
//
//@Composable
//fun enterAlwaysScrollBehavior(
//    state: BeMyPetTopAppBarState = rememberBeMyPetTopAppBarState(),
//    canScroll: () -> Boolean = { true },
//    snapAnimationSpec: AnimationSpec<Float>? = spring(stiffness = Spring.StiffnessMediumLow),
//    flingAnimationSpec: DecayAnimationSpec<Float>? = rememberSplineBasedDecay()
//): BeMyPetTopAppBarScrollBehavior = EnterAlwaysScrollBehavior(
//    state = state,
//    snapAnimationSpec = snapAnimationSpec,
//    flingAnimationSpec = flingAnimationSpec,
//    canScroll = canScroll
//)
