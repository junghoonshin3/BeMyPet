package kr.sjh.core.supabase.service.impl

import org.junit.Assert.assertEquals
import org.junit.Test

class AuthServiceImplAvatarPathTest {

    @Test
    fun buildAvatarObjectPath_usesStableUserScopedPath() {
        assertEquals("abc/avatar.jpg", AuthServiceImpl.buildAvatarObjectPathForTest(" abc "))
    }

    @Test
    fun appendCacheBuster_addsVersionQueryParam() {
        assertEquals(
            "https://example.com/avatar.jpg?v=123",
            AuthServiceImpl.appendCacheBusterForTest("https://example.com/avatar.jpg", 123)
        )
    }

    @Test
    fun appendCacheBuster_preservesExistingQuery() {
        assertEquals(
            "https://example.com/avatar.jpg?foo=1&v=123",
            AuthServiceImpl.appendCacheBusterForTest("https://example.com/avatar.jpg?foo=1", 123)
        )
    }
}
