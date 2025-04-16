package kr.sjh.core.supabase.service

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.Comment

interface CommentService {

    fun getComments(postId: String): Flow<List<Comment>>

    suspend fun deleteComment(
        commentId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    )

    suspend fun insertComment(comment: Comment)

    suspend fun updateComment(comment: Comment)

    suspend fun getCommentCount(postId: String): Int
}