package kr.sjh.core.supabase.service.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthServiceImplAvatarPathTest {

    @Test
    fun buildAvatarObjectPath_usesStableUserScopedPath() {
        assertEquals("abc/avatar.jpg", AuthServiceImpl.buildAvatarObjectPathForTest(" abc "))
    }
}
