package kr.sjh.core.datastore

import kotlinx.coroutines.flow.Flow


interface DataStoreService {
    suspend fun add(pet: FavouritePet)
    suspend fun remove(pet: FavouritePet)
    suspend fun getPets(): Flow<List<FavouritePet>>
    suspend fun clear()
//    suspend fun getPets(): Flow<String>
//    suspend fun deletePet()
}