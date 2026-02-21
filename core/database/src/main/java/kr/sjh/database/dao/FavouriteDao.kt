package kr.sjh.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kr.sjh.database.entity.FavouriteEntity

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favouriteEntity: FavouriteEntity)

    @Query("SELECT * FROM favourite_pet order by id desc")
    fun getAll(): Flow<List<FavouriteEntity>>

    @Query("DELETE FROM favourite_pet WHERE desertionNo = :desertionNo")
    suspend fun delete(desertionNo: String)

    @Query("DELETE FROM favourite_pet")
    suspend fun clear()

    @Query("SELECT EXISTS(SELECT * FROM favourite_pet WHERE desertionNo = :desertionNo)")
    fun isExist(desertionNo: String): Boolean
}
