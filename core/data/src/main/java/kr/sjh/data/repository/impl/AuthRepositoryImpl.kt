package kr.sjh.data.repository.impl

import kr.sjh.core.supabase.service.AuthService
import kr.sjh.data.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
) : AuthRepository {
    override fun signIn() {
        authService.signIn()
    }

    override fun signUp() {
        authService.signUp()
    }

    override fun signOut() {
        authService.signOut()
    }
}