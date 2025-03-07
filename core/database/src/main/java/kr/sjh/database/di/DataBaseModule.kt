package kr.sjh.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.sjh.database.dao.FavouriteDao
import kr.sjh.database.dao.LocationDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Singleton
    @Provides
    fun provideFavouriteDao(db: AppDataBase): FavouriteDao = db.favouriteDao()

    @Singleton
    @Provides
    fun provideLocationDao(db: AppDataBase): LocationDao = db.locationDao()

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDataBase =
        Room.databaseBuilder(
            context, AppDataBase::class.java, "bemypet_db"
        ).fallbackToDestructiveMigration().build()
}