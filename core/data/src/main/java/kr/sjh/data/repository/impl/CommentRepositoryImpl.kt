package kr.sjh.data.repository.impl

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.BlockUser
import kr.sjh.core.model.Comment
import kr.sjh.core.model.ReportForm
import kr.sjh.core.supabase.service.BlockService
import kr.sjh.core.supabase.service.CommentService
import kr.sjh.core.supabase.service.ReportService
import kr.sjh.data.repository.CommentRepository
import javax.inject.Inject

class CommentRepositoryImpl @Inject constructor(
    private val commentService: CommentService,
    private val reportService: ReportService,
    private val blockService: BlockService
) : CommentRepository {

    override fun getComments(noticeNo: String, userId: String) =
        commentService.getComments(noticeNo)

    override suspend fun deleteComment(
        commentId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        commentService.deleteComment(commentId, onSuccess, onFailure)
    }

    override suspend fun insertComment(
        comment: Comment,
    ) {
        commentService.insertComment(comment)
    }

    override suspend fun updateComment(
        comment: Comment,
    ) {
        commentService.updateComment(comment)
    }

    override suspend fun getCommentCount(noticeNo: String): Int {
        return commentService.getCommentCount(noticeNo)
    }

    override suspend fun reportUsers(report: ReportForm) {
        reportService.reportUsers(report)
    }

    override suspend fun blockUser(
        blockUser: BlockUser, onSuccess: () -> Unit, onFailure: (Exception) -> Unit
    ) {
        blockService.blockUser(blockUser, onSuccess, onFailure)
    }

    override fun getBlockUsers(blockerId: String): Flow<List<BlockUser>> {
        return blockService.getBlockUsersFlow(blockerId)
    }
}
