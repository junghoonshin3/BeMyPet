package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kr.sjh.core.model.BlockUser
import kr.sjh.core.supabase.service.BlockService
import javax.inject.Inject

class BlockServiceImpl @Inject constructor(supabaseClient: SupabaseClient) : BlockService {

    private val blockTable = supabaseClient.postgrest.from("blocks")
    private val blockFeedView = supabaseClient.postgrest.from("block_feed")

    override suspend fun blockUser(
        blockUser: BlockUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            blockTable.upsert(
                mapOf(
                    "blocker_id" to blockUser.blockerUser,
                    "blocked_id" to blockUser.blockedUser
                )
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
                Log.d("sjh", "blockerId : $blockerId, blockedId : $blockedId")
                filter {
                    and {
                        eq("blocker_id", blockerId)
                        eq("blocked_id", blockedId)
                    }
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    @OptIn(SupabaseExperimental::class)
    override fun getBlockUsersFlow(blockerId: String): Flow<List<BlockUser>> {
        return blockTable.selectAsFlow(
            primaryKeys = listOf(BlockUser::blockerUser, BlockUser::blockedUser),
            filter = FilterOperation("blocker_id", FilterOperator.EQ, blockerId)
        ).map {
            fetchBlockUsers(blockerId)
        }.onStart {
            emit(fetchBlockUsers(blockerId))
        }.catch { throwable ->
            Log.w(TAG, "Realtime subscription failed for blocks. Falling back to latest fetch.", throwable)
            emit(fetchBlockUsers(blockerId))
        }
    }

    override suspend fun getBlockUsers(
        blockerId: String,
        onSuccess: (List<BlockUser>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            onSuccess(fetchBlockUsers(blockerId))
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private suspend fun fetchBlockUsers(blockerId: String): List<BlockUser> {
        return blockFeedView.select {
            filter {
                eq("blocker_id", blockerId)
            }
        }.decodeList<BlockUser>()
    }

    private companion object {
        const val TAG = "BlockServiceImpl"
    }
}
