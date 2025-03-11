package kr.sjh.data.repository

interface AuthRepository {
    fun signIn()
    fun signUp()
    fun signOut()
}