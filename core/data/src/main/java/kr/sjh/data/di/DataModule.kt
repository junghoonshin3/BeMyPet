package kr.sjh.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.impl.AdoptionRepositoryImpl
import kr.sjh.data.repository.impl.FavouriteRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideAdoptionRepository(impl: AdoptionRepositoryImpl): AdoptionRepository =
        impl

    @Provides
    @Singleton
    fun provideFavouriteRepository(impl: FavouriteRepositoryImpl): FavouriteRepository =
        impl
}