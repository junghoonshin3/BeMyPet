package kr.sjh.bemypet.notifications

import android.os.Bundle
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kr.sjh.data.repository.NotificationRepository
import kr.sjh.data.repository.SettingRepository
import java.util.TimeZone
import javax.inject.Inject

private const val PUSH_PREF_NAME = "bemypet_push_sync"
private const val KEY_CURRENT_USER_ID = "current_user_id"

@AndroidEntryPoint
class BeMyPetFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var settingRepository: SettingRepository

    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val sharedPref = getSharedPreferences(PUSH_PREF_NAME, MODE_PRIVATE)
        val userId = sharedPref.getString(KEY_CURRENT_USER_ID, null).orEmpty()
        if (userId.isBlank()) return

        ioScope.launch {
            val pushOptIn = settingRepository.getPushOptIn().first()
            notificationRepository.upsertSubscription(
                userId = userId,
                token = token,
                pushOptIn = pushOptIn,
                timezone = TimeZone.getDefault().id.ifBlank { "Asia/Seoul" },
            )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val parsedPayload = PushPayloadParser.parse(message.data)
        logPushReceived(parsedPayload)
        PushNotificationPresenter.show(applicationContext, message, parsedPayload)
    }

    private fun logPushReceived(payload: ParsedPushPayload) {
        FirebaseAnalytics.getInstance(applicationContext).logEvent("push_received", Bundle().apply {
            putString("campaign_type", payload.campaignType.ifBlank { "unknown" })
            payload.noticeNo.takeIf { it.isNotBlank() }?.let { putString("notice_no", it) }
            payload.matchedCount?.let { putLong("matched_count", it.toLong()) }
            payload.batchId.takeIf { it.isNotBlank() }?.let { putString("batch_id", it.take(40)) }
        })
    }
}
