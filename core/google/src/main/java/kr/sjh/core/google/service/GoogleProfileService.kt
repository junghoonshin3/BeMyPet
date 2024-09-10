package kr.sjh.core.google.service

import kr.sjh.core.model.google.GoogleResponse

interface GoogleProfileService {
    val displayName: String
    val photoUrl: String
    suspend fun signOut(): SignOutResponse
    suspend fun revokeAccess(): RevokeAccessResponse
}

typealias SignOutResponse = GoogleResponse<Boolean>
typealias RevokeAccessResponse = GoogleResponse<Boolean>
