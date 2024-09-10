package kr.sjh.core.firebase.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kr.sjh.core.firebase.service.AccountService
import kr.sjh.core.firebase.service.ReloadUserResponse
import javax.inject.Inject

class AccountServiceImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AccountService {
    override val userId: String?
        get() = auth.currentUser?.uid
    override val email: String?
        get() = auth.currentUser?.email
    override val firebaseUser: FirebaseUser?
        get() = auth.currentUser

    override suspend fun reloadFirebaseUser(): ReloadUserResponse {
        TODO("Not yet implemented")
    }

    override fun signOut() {
        TODO("Not yet implemented")
    }

    override suspend fun revokeAccess(): Result<Unit> {
        TODO("Not yet implemented")
    }


}