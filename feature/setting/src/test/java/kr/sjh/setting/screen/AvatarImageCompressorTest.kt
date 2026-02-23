package kr.sjh.setting.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarImageCompressorTest {

    @Test
    fun avatarFileName_isStable() {
        assertEquals("avatar.jpg", AvatarImageCompressor.TARGET_FILE_NAME)
    }
}
