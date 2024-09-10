package kr.sjh.core.firebase.impl

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.dataObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kr.sjh.core.firebase.service.AccountService
import kr.sjh.core.firebase.service.FireStoreRepository
import kr.sjh.core.model.Exceptions.FirebaseUserIsNullException
import kr.sjh.core.model.Exceptions.FireStoreUserNotExistsException
import kr.sjh.core.model.firebase.User
import javax.inject.Inject

class FireStoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore, private val accountService: AccountService
) : FireStoreRepository {

    private val userColRef by lazy { firestore.collection(USER) }
    private val chatColRef by lazy { firestore.collection(CHATS) }
    private val reviewColRef by lazy { firestore.collection(REVIEWS) }

    override suspend fun saveUser(user: User) {
        userColRef.document(user.userId).set(user).await()
    }

    override suspend fun isUserInDatabase(email: String) = runCatching {
        val querySnapshot = userColRef.whereEqualTo("email", email).get().await()
        querySnapshot.documents.isEmpty()
    }

    override suspend fun getUser(): User = accountService.userId?.let { userId ->
        userColRef.document(userId).get().await().toObject(User::class.java)
    } ?: throw FirebaseUserIsNullException()


    override fun getUserFlow(): Flow<User?> = accountService.userId?.let { userId ->
        userColRef.document(userId).dataObjects<User>()
    } ?: throw FirebaseUserIsNullException()

    override suspend fun deleteUser(): Result<User> = runCatching {
        val userId = accountService.userId ?: throw FirebaseUserIsNullException()
        val user = userColRef.document(userId).get().await().toObject(User::class.java)
            ?: throw FireStoreUserNotExistsException()
        userColRef.document(userId).delete().await()
        user
    }

    companion object {
        private const val USER = "users"
        private const val CHATS = "chats"
        private const val REVIEWS = "reviews"
    }

}