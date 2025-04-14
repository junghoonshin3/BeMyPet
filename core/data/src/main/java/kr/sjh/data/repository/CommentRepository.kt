package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.BlockUser
import kr.sjh.core.model.Comment
import kr.sjh.core.model.ReportForm

interface CommentRepository {
    fun getComments(postId: String, userId: String): Flow<List<Comment>>
    suspend fun deleteComment(
        commentId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    )

    suspend fun insertComment(comment: Comment)
    suspend fun updateComment(comment: Comment)
    suspend fun getCommentCount(postId: String): Int
    suspend fun reportUsers(report: ReportForm)
    suspend fun blockUser(blockUser: BlockUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
    fun getBlockUsers(blockerId: String): Flow<List<BlockUser>>
}