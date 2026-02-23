package kr.sjh.data.repository.impl

import kr.sjh.datastore.model.SettingsData
import kotlin.test.Test
import kotlin.test.assertFalse

class SettingRepositoryImplNotificationPrefTest {

    @Test
    fun `default push opt in is false`() {
        val settings = SettingsData()

        assertFalse(settings.pushOptIn)
    }
}
