package kr.sjh.core.firebase.service

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.firebase.User


interface FireStoreRepository {
    suspend fun saveUser(user: User)
    suspend fun isUserInDatabase(email: String): Result<Boolean>
    suspend fun getUser(): User
    fun getUserFlow(): Flow<User?>
    suspend fun deleteUser(): Result<User>


}