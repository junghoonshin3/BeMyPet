package kr.sjh.core.supabase.service.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.BlockUser
import kr.sjh.core.supabase.service.BlockService
import javax.inject.Inject

class BlockServiceImpl @Inject constructor(supabaseClient: SupabaseClient) : BlockService {

    private val blockTable = supabaseClient.postgrest.from("blocks")

    override suspend fun blockUser(
        blockUser: BlockUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            blockTable.upsert(
                blockUser
            )
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    override suspend fun unblockUser(
        blockerId: String, blockedId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            blockTable.delete {
                filter {
                    eq("blocker_id", blockerId)
                    eq("blocked_id", blockedId)
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }

    }

    @OptIn(SupabaseExperimental::class)
    override fun getBlockUsers(blockerId: String): Flow<List<BlockUser>> {
        return blockTable.selectAsFlow(
            listOf(
                BlockUser::blockerUser,
                BlockUser::blockedUser
            ),
            filter = FilterOperation("blocker_id", FilterOperator.EQ, blockerId),
        )
    }


}