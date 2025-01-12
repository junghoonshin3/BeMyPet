package kr.sjh.database.di

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import kr.sjh.database.dao.FavouriteDao
import kr.sjh.database.entity.FavouriteEntity

@Database(
    entities = [FavouriteEntity::class],
    version = AppDataBase.LATEST_VERSION,
    exportSchema = true,
//    autoMigrations = [
//        AutoMigration(from = 1, to = 2),
//        AutoMigration(from = 2, to = 3),
//    ]
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao

    companion object {
        const val LATEST_VERSION = 3
    }
}