package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kr.sjh.core.model.adoption.Pet

enum class CompareToggleResult {
    Added,
    Removed,
    LimitExceeded
}

interface CompareRepository {
    fun comparedPets(): StateFlow<List<Pet>>
    suspend fun toggle(pet: Pet): CompareToggleResult
    suspend fun remove(desertionNo: String)
    suspend fun clear()
    fun isCompared(desertionNo: String): Flow<Boolean>
}

