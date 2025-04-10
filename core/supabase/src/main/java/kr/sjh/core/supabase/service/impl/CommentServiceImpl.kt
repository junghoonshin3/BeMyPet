package kr.sjh.core.supabase.service.impl

import android.util.Log
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.selectAsFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kr.sjh.core.model.Comment
import kr.sjh.core.supabase.service.CommentService
import javax.inject.Inject

class CommentServiceImpl @Inject constructor(
    postgrest: Postgrest
) : CommentService {

    private val commentTable = postgrest.from("comments")

    @OptIn(SupabaseExperimental::class)
    override fun getComments(postId: String): Flow<List<Comment>> {
        return commentTable.selectAsFlow(
            Comment::id,
            filter = FilterOperation("post_id", FilterOperator.EQ, postId),
        ).map { list ->
            list.sortedByDescending { it.createdAt }
        }
    }


    override suspend fun deleteComment(
        commentId: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit
    ) {
        try {
            commentTable.delete {
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
            commentTable.insert(comment) {
                order(
                    "created_at", Order.ASCENDING
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override suspend fun updateComment(comment: Comment) {
        try {
            commentTable.update(comment) {
                filter {
                    eq("id", comment.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun getCommentCount(postId: String): Int {
        val count = commentTable.select {
            filter {
                eq("post_id", postId)
            }
        }.decodeList<Comment>().size
        return count
    }
}