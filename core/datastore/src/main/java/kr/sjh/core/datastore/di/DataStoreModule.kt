package kr.sjh.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.sjh.core.datastore.DataStoreService
import kr.sjh.core.datastore.PetPreferences
import kr.sjh.core.datastore.PetPreferencesSerializer
import kr.sjh.core.datastore.impl.DataStoreServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    internal fun providesPetPreferences(
        @ApplicationContext context: Context,
        petPfSerializer: PetPreferencesSerializer,
    ): DataStore<PetPreferences> =
        DataStoreFactory.create(
            serializer = petPfSerializer,
        ) {
            context.dataStoreFile("favorite_pets_preferences.pb")
        }
}

@Module
@InstallIn(SingletonComponent::class)
interface DataStoreBinder {
    @Binds
    fun provideDataStoreService(impl: DataStoreServiceImpl): DataStoreService
}