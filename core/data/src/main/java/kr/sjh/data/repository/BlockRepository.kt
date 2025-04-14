package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.BlockUser

interface BlockRepository {
    fun getBlockedUsersFlow(blockerId: String): Flow<List<BlockUser>>
    suspend fun deleteBlockedUser(
        blockerId: String,
        blockedId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    suspend fun getBlockUsers(
        blockerId: String,
        onSuccess: (List<BlockUser>) -> Unit,
        onFailure: (Exception) -> Unit
    )
}