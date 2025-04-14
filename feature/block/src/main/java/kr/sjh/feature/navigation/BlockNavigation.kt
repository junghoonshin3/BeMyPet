package kr.sjh.feature.navigation

import kotlinx.serialization.Serializable
import kr.sjh.core.model.BlockUser

@Serializable
data class Block(val userId: String)

sealed class BlockEvent {
    data object DeleteBlockUser : BlockEvent()
    data class SelectedBlockedUser(val blockedUser: BlockUser) : BlockEvent()
}