package kr.sjh.data.repository.impl

import kr.sjh.datastore.model.SettingsData
import kotlin.test.Test
import kotlin.test.assertTrue

class SettingRepositoryImplNotificationPrefTest {

    @Test
    fun `default push opt in is true`() {
        val settings = SettingsData()

        assertTrue(settings.pushOptIn)
    }
}
