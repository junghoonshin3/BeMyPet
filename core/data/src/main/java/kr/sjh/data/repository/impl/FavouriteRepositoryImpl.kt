package kr.sjh.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.toFavouriteEntity
import kr.sjh.data.toPet
import kr.sjh.database.dao.FavouriteDao
import javax.inject.Inject

class FavouriteRepositoryImpl @Inject constructor(private val dao: FavouriteDao) :
    FavouriteRepository {

    override fun isExist(desertionNo: String): Flow<Boolean> = dao.isExist(desertionNo)
        .flowOn(Dispatchers.IO)

    override suspend fun addPet(pet: Pet) {
        dao.insert(pet.toFavouriteEntity())
    }

    override suspend fun removePet(desertionNo: String) {
        dao.delete(desertionNo)
    }

    override fun getFavouritePets(): Flow<List<Pet>> {
        return dao.getAll().map {
            it.map {
                it.toPet()
            }
        }.flowOn(Dispatchers.IO)
    }
}