package kr.sjh.data.repository.impl

import android.content.Context
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.sjh.data.repository.AuthenticationRepository
import kr.sjh.core.firebase.service.FireStoreRepository
import kr.sjh.core.google.service.GoogleAuthService
import kr.sjh.core.model.Exceptions
import kr.sjh.core.model.firebase.Provider
import kr.sjh.core.model.firebase.User
import kr.sjh.core.model.google.AuthState
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val googleAuthService: GoogleAuthService,
    private val firestoreRepository: FireStoreRepository
) : AuthenticationRepository {
    override fun signInUpWithGoogle(context: Context): Flow<AuthState<FirebaseUser>> = flow {
        googleAuthService.googleSignInUp(context).collect { state ->
            when (state) {
                is AuthState.Error -> {
                    emit(AuthState.Error(state.e))
                }

                AuthState.Idle -> {
                    emit(AuthState.Idle)
                }

                AuthState.Loading -> {
                    emit(AuthState.Loading)
                }

                is AuthState.Success -> {
                    try {
                        Log.d("sjh", "auth success")
                        val isSignIn = addUserToFireStoreIfNewUser(state.data).getOrThrow()
                        if (isSignIn) {
                            state.data.user?.let { user ->
                                emit(AuthState.Success(user))
                            }

                        }
                    } catch (e: Exception) {
                        emit(AuthState.Error(e))
                    }
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun addUserToFireStoreIfNewUser(authResult: AuthResult): Result<Boolean> {
        Log.d("sjh", "addUserToFireStoreIfNewUser")
        return authResult.user?.email?.let { email ->
            Log.d("sjh", "email : $email")
            val isExist = firestoreRepository.isUserInDatabase(email).getOrThrow()
            if (isExist) {
                Result.success(value = true)
            } else {
                authResult.user?.let { user ->
                    addUserToFireStore(user)
                } ?: let {
                    Result.success(value = false)
                }
            }
        } ?: let {
            Result.success(value = false)
        }
    }


    private suspend fun addUserToFireStore(firebaseUser: FirebaseUser): Result<Boolean> {
        firebaseUser.apply {
            val displayName = displayName ?: throw Exceptions.FirebaseDisplayNameNullException()
            val email = email ?: throw Exceptions.FirebaseEmailNullException()
            val photoUrl = photoUrl ?: throw Exceptions.FirebasePhotoUrlNullException()
            val user = User(
                userId = uid,
                fullName = displayName,
                email = email,
                provider = Provider.GOOGLE.name,
                profilePictureUrl = photoUrl.toString(),
            )
            Log.d("sjh", "addUserToFireStore : $uid")
            firestoreRepository.saveUser(user)
        }
        return Result.success(value = true)
    }
}