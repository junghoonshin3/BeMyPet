package kr.sjh.bemypet.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage
import kr.sjh.bemypet.StartActivity

object PushNotificationPresenter {
    private const val CHANNEL_ID = "new_notice_summary"
    private const val CHANNEL_NAME = "신규 공고 알림"
    private const val CHANNEL_DESCRIPTION = "관심 조건에 맞는 신규 공고 요약 알림"

    fun show(context: Context, message: RemoteMessage, payload: ParsedPushPayload) {
        if (!canNotify(context)) return

        ensureChannel(context)

        val title = message.notification?.title?.takeIf { it.isNotBlank() }
            ?: "새 공고 알림"
        val body = message.notification?.body?.takeIf { it.isNotBlank() }
            ?: payload.matchedCount?.let { "관심 조건에 맞는 신규 공고 ${it}건이 등록됐어요." }
            ?: "새로운 알림이 도착했어요."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(createOpenAppPendingIntent(context))

        NotificationManagerCompat.from(context).notify(createNotificationId(payload), builder.build())
    }

    private fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }

        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = CHANNEL_DESCRIPTION
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun createOpenAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, StartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getActivity(context, 0, intent, flags)
    }

    private fun createNotificationId(payload: ParsedPushPayload): Int {
        val key = payload.batchId.ifBlank {
            payload.noticeNo.ifBlank {
                "fallback"
            }
        }
        return key.hashCode()
    }
}
