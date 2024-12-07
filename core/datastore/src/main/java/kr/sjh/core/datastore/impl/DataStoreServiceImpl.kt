package kr.sjh.core.datastore.impl

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kr.sjh.core.datastore.DataStoreService
import kr.sjh.core.datastore.FavouritePet
import kr.sjh.core.datastore.PetPreferences
import javax.inject.Inject

class DataStoreServiceImpl @Inject constructor(
    private val favouritePref: DataStore<PetPreferences>
) : DataStoreService {
    override suspend fun add(pet: FavouritePet) {
        favouritePref.updateData {
            it.toBuilder().addFavouritePets(pet).build()
        }
    }

    override suspend fun remove(pet: FavouritePet) {
        favouritePref.updateData { pref ->
            val index = pref.favouritePetsList.indexOf(pet)
            pref.toBuilder().removeFavouritePets(index).build()
        }
    }

    override suspend fun getPets(): Flow<List<FavouritePet>> = favouritePref.data.map {
        it.favouritePetsList
    }

    override suspend fun clear() {
        favouritePref.updateData {
            it.toBuilder().clearFavouritePets().build()
        }
    }
}