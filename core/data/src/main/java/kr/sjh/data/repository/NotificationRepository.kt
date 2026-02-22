package kr.sjh.data.repository

interface NotificationRepository {
    suspend fun upsertInterestProfile(
        userId: String,
        regions: List<String>,
        species: List<String>,
        sexes: List<String>,
        sizes: List<String>,
        pushEnabled: Boolean,
    )

    suspend fun upsertSubscription(
        userId: String,
        token: String,
        pushOptIn: Boolean,
        timezone: String,
    )

    suspend fun touchLastActive(userId: String)
}
