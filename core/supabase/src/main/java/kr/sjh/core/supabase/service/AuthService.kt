package kr.sjh.core.supabase.service

interface AuthService {
    fun signIn()
    fun signOut()
    fun signUp()
}