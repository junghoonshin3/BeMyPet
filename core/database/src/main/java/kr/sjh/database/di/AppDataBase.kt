package kr.sjh.database.di

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import kr.sjh.database.dao.FavouriteDao
import kr.sjh.database.dao.LocationDao
import kr.sjh.database.entity.FavouriteEntity
import kr.sjh.database.entity.SidoEntity
import kr.sjh.database.entity.SigunguEntity

@Database(
    entities = [FavouriteEntity::class, SidoEntity::class, SigunguEntity::class],
    version = AppDataBase.LATEST_VERSION,
    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2),
//        AutoMigration(from = 2, to = 3),
//    ]
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
    abstract fun locationDao(): LocationDao
    companion object {
        const val LATEST_VERSION = 3
    }
}