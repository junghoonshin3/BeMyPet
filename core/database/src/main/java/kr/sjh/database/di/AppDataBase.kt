package kr.sjh.database.di

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.sjh.database.dao.FavouriteDao
import kr.sjh.database.dao.SettingDao
import kr.sjh.database.entity.FavouriteEntity
import kr.sjh.database.entity.SettingEntity

@Database(
    entities = [FavouriteEntity::class, SettingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDataBase : RoomDatabase() {
    abstract fun favouriteDao(): FavouriteDao
    abstract fun settingDao(): SettingDao
}