package kr.sjh.core.supabase.service.impl

import io.github.jan.supabase.auth.Auth
import kr.sjh.core.supabase.service.AuthService
import javax.inject.Inject

class AuthServiceImpl @Inject constructor(private val auth: Auth) : AuthService {
    override fun signIn() {
//        auth.signInWith()
    }

    override fun signOut() {
    }

    override fun signUp() {
    }
}