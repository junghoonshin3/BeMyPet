package kr.sjh.data.repository

import kotlinx.coroutines.flow.Flow
import kr.sjh.core.model.adoption.Pet

interface FavouriteRepository {
    fun isExist(desertionNo: String): Boolean
    suspend fun addPet(pet: Pet)
    suspend fun removePet(desertionNo: String)
    fun getFavouritePets(): Flow<List<Pet>>
    suspend fun backfillFavouriteImagesIfNeeded()
}
