package kr.sjh.data.repository.impl

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.repository.CompareToggleResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompareRepositoryImplTest {

    @Test
    fun toggle_same_pet_add_then_remove() = runBlocking {
        val repository = CompareRepositoryImpl()
        val pet = Pet(desertionNo = "D-1", noticeNo = "N-1")

        val first = repository.toggle(pet)
        val second = repository.toggle(pet)

        assertEquals(CompareToggleResult.Added, first)
        assertEquals(CompareToggleResult.Removed, second)
        assertEquals(0, repository.comparedPets().value.size)
    }

    @Test
    fun toggle_more_than_three_returns_limit_exceeded() = runBlocking {
        val repository = CompareRepositoryImpl()

        val first = repository.toggle(Pet(desertionNo = "D-1", noticeNo = "N-1"))
        val second = repository.toggle(Pet(desertionNo = "D-2", noticeNo = "N-2"))
        val third = repository.toggle(Pet(desertionNo = "D-3", noticeNo = "N-3"))
        val fourth = repository.toggle(Pet(desertionNo = "D-4", noticeNo = "N-4"))

        assertEquals(CompareToggleResult.Added, first)
        assertEquals(CompareToggleResult.Added, second)
        assertEquals(CompareToggleResult.Added, third)
        assertEquals(CompareToggleResult.LimitExceeded, fourth)
        assertEquals(3, repository.comparedPets().value.size)
    }

    @Test
    fun isCompared_reflects_current_state() = runBlocking {
        val repository = CompareRepositoryImpl()
        val key = "D-10"

        assertFalse(repository.isCompared(key).first())
        repository.toggle(Pet(desertionNo = key, noticeNo = "N-10"))
        assertTrue(repository.isCompared(key).first())
        repository.remove(key)
        assertFalse(repository.isCompared(key).first())
    }
}

