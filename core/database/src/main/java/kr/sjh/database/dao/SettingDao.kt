package kr.sjh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kr.sjh.database.entity.SettingEntity

@Dao
interface SettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setting: SettingEntity)

    @Query("SELECT * FROM setting WHERE id = 1")
    fun select(): Flow<SettingEntity>

    @Query("SELECT EXISTS(SELECT * FROM setting WHERE id = 1)")
    suspend fun isSettingExists(): Boolean


}