package kr.sjh.data.repository.impl

import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.BlockUser
import kr.sjh.core.supabase.service.BlockService
import kr.sjh.data.repository.BlockRepository

class BlockRepositoryImpl @Inject constructor(private val blockService: BlockService) :
    BlockRepository {

    override fun getBlockedUsersFlow(blockerId: String): Flow<List<BlockUser>> =
        blockService.getBlockUsersFlow(blockerId)

    override suspend fun deleteBlockedUser(
        blockerId: String, blockedId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        blockService.unblockUser(blockerId, blockedId, onSuccess, onFailure)
    }

    override suspend fun getBlockUsers(
        blockerId: String, onSuccess: (List<BlockUser>) -> Unit, onFailure: (Exception) -> Unit
    ) {
        blockService.getBlockUsers(blockerId, onSuccess, onFailure)
    }
}