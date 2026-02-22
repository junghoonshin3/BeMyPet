package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.sjh.core.model.Comment
import kr.sjh.core.supabase.service.CommentService
import java.time.OffsetDateTime
import javax.inject.Inject

class CommentServiceImpl @Inject constructor(
    postgrest: Postgrest
) : CommentService {

    private val commentTable = postgrest.from("comments")
    private val commentFeedView = postgrest.from("comment_feed")

    @OptIn(SupabaseExperimental::class)
    override fun getComments(noticeNo: String): Flow<List<Comment>> {
        return commentFeedView.selectAsFlow(
            Comment::id,
            filter = FilterOperation("notice_no", FilterOperator.EQ, noticeNo),
        ).map { list ->
            list.sortedByDescending { it.createdAt }
        }
    }

    override suspend fun getCommentsByUser(userId: String): List<Comment> {
        val normalizedUserId = userId.trim()
        if (normalizedUserId.isBlank()) return emptyList()
        return commentFeedView.select {
            filter {
                eq("user_id", normalizedUserId)
            }
        }.decodeList<Comment>().sortedByDescending { it.createdAt }
    }


    override suspend fun deleteComment(
        commentId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            commentTable.update(
                mapOf("deleted_at" to OffsetDateTime.now().toString())
            ) {
                Log.d("sjh", "commentId : ${commentId}")
                filter { eq("id", commentId) }
            }
            onSuccess(commentId)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    override suspend fun insertComment(
        comment: Comment
    ) {
        try {
            commentTable.insert(
                mapOf(
                    "id" to comment.id,
                    "user_id" to comment.userId,
                    "notice_no" to comment.noticeNo,
                    "content" to comment.content
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override suspend fun updateComment(comment: Comment) {
        try {
            commentTable.update(
                mapOf(
                    "content" to comment.content,
                    "updated_at" to OffsetDateTime.now().toString()
                )
            ) {
                filter {
                    eq("id", comment.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getCommentCount(noticeNo: String): Int {
        val count = commentFeedView.select {
            filter {
                eq("notice_no", noticeNo)
            }
        }.decodeList<Comment>().size
        return count
    }
}
