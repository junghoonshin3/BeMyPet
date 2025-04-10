package kr.sjh.core.supabase.service

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.BlockUser

interface BlockService {
    suspend fun blockUser(
        blockUser: BlockUser, onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    suspend fun unblockUser(
        blockerId: String, blockedId: String, onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    )

    fun getBlockUsers(blockerId: String): Flow<List<BlockUser>>
}