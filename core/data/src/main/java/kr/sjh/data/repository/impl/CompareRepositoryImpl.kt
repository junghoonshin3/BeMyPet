package kr.sjh.data.repository.impl

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.update
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.repository.CompareRepository
import kr.sjh.data.repository.CompareToggleResult

@Singleton
class CompareRepositoryImpl @Inject constructor() : CompareRepository {
    companion object {
        private const val MAX_COMPARE_SIZE = 3
    }

    private val mutex = Mutex()
    private val comparedPets = MutableStateFlow<List<Pet>>(emptyList())

    override fun comparedPets(): StateFlow<List<Pet>> = comparedPets.asStateFlow()

    override suspend fun toggle(pet: Pet): CompareToggleResult = mutex.withLock {
        val key = pet.compareKey() ?: return CompareToggleResult.LimitExceeded
        val current = comparedPets.value

        val existingIndex = current.indexOfFirst { it.matchesKey(key) }
        if (existingIndex >= 0) {
            comparedPets.update { pets ->
                pets.toMutableList().apply { removeAt(existingIndex) }
            }
            return CompareToggleResult.Removed
        }

        if (current.size >= MAX_COMPARE_SIZE) {
            return CompareToggleResult.LimitExceeded
        }

        comparedPets.update { it + pet }
        CompareToggleResult.Added
    }

    override suspend fun remove(desertionNo: String) = mutex.withLock {
        val key = desertionNo.trim()
        if (key.isBlank()) return@withLock
        comparedPets.update { pets ->
            pets.filterNot { it.matchesKey(key) }
        }
    }

    override suspend fun clear() = mutex.withLock {
        comparedPets.value = emptyList()
    }

    override fun isCompared(desertionNo: String): Flow<Boolean> {
        val key = desertionNo.trim()
        if (key.isBlank()) return comparedPets.map { false }
        return comparedPets.map { pets ->
            pets.any { it.matchesKey(key) }
        }
    }

    private fun Pet.compareKey(): String? {
        return desertionNo?.trim()?.takeIf { it.isNotBlank() }
            ?: noticeNo?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun Pet.matchesKey(key: String): Boolean {
        val desertionKey = desertionNo?.trim()
        val noticeKey = noticeNo?.trim()
        return desertionKey == key || noticeKey == key
    }
}

