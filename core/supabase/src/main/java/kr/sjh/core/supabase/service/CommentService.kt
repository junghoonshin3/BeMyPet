package kr.sjh.core.supabase.service

import kr.sjh.core.model.Comment

interface CommentService {

    suspend fun getComments(noticeNo: String): List<Comment>

    suspend fun getCommentsByUser(userId: String): List<Comment>

    suspend fun deleteComment(
        commentId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    )

    suspend fun insertComment(comment: Comment)

    suspend fun updateComment(comment: Comment)

    suspend fun getCommentCount(noticeNo: String): Int
}
