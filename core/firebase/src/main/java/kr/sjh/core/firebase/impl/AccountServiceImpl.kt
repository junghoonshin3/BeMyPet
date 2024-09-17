package kr.sjh.core.firebase.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kr.sjh.core.firebase.service.AccountService
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

    override fun signOut() {
        auth.signOut()
    }

    override suspend fun revokeAccess(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
    }
}