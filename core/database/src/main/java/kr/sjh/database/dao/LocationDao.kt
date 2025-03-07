package kr.sjh.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kr.sjh.database.entity.SidoEntity
import kr.sjh.database.entity.SigunguEntity

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SidoEntity::class)
    fun insertAllSido(sidoList: List<SidoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = SigunguEntity::class)
    fun insertAllSigungu(sidoList: List<SigunguEntity>)

    @Query("SELECT * FROM sido")
    fun getSidoList(): Flow<List<SidoEntity>>

    @Query("SELECT * FROM sigungu WHERE uprCd = :uprCd")
    fun getSigunguList(uprCd: String): List<SigunguEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM sigungu WHERE uprCd = :uprCd)")
    suspend fun existSigunguList(uprCd: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM sido)")
    suspend fun existSido(): Boolean
}