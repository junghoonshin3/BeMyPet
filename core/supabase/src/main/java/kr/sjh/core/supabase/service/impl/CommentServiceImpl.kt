package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.postgrest.Postgrest
import kr.sjh.core.model.Comment
import kr.sjh.core.supabase.service.CommentService
import java.time.OffsetDateTime
import java.util.UUID
import javax.inject.Inject

class CommentServiceImpl @Inject constructor(
    postgrest: Postgrest
) : CommentService {

    private val commentTable = postgrest.from("comments")
    private val commentFeedView = postgrest.from("comment_feed")

    override suspend fun getComments(noticeNo: String): List<Comment> {
        return commentFeedView.select {
            filter {
                eq("notice_no", noticeNo)
            }
        }.decodeList<Comment>().sortedByDescending { it.createdAt }
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
        val normalizedId = commentId.trim()
        if (!isValidCommentId(normalizedId)) {
            onFailure(IllegalArgumentException("Invalid comment id for delete"))
            return
        }
        try {
            commentTable.update(
                mapOf("deleted_at" to OffsetDateTime.now().toString())
            ) {
                Log.d("CommentServiceImpl", "deleteComment id=$normalizedId")
                filter { eq("id", normalizedId) }
            }
            onSuccess(normalizedId)
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
        val normalizedId = comment.id.trim()
        if (!isValidCommentId(normalizedId)) {
            Log.w("CommentServiceImpl", "skip updateComment: invalid id")
            return
        }
        try {
            commentTable.update(
                mapOf(
                    "content" to comment.content,
                    "updated_at" to OffsetDateTime.now().toString()
                )
            ) {
                filter {
                    eq("id", normalizedId)
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

    private fun isValidCommentId(commentId: String): Boolean {
        if (commentId.isBlank()) return false
        if (commentId.equals("null", ignoreCase = true)) return false
        return runCatching {
            UUID.fromString(commentId)
            true
        }.getOrElse { false }
    }
}
