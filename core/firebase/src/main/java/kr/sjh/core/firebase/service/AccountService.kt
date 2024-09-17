package kr.sjh.core.firebase.service

import com.google.firebase.auth.FirebaseUser
import kr.sjh.core.model.Response

typealias ReloadUserResponse = Response<Boolean>


interface AccountService {
    val userId: String?
    val email: String?
    val firebaseUser: FirebaseUser?
    fun signOut()
    suspend fun revokeAccess(): Result<Unit>
}