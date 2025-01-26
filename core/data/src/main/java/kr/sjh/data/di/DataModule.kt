package kr.sjh.data.di

import android.content.Context
import android.location.Geocoder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.GeoLocationRepository
import kr.sjh.data.repository.SettingRepository
import kr.sjh.data.repository.impl.AdoptionRepositoryImpl
import kr.sjh.data.repository.impl.FavouriteRepositoryImpl
import kr.sjh.data.repository.impl.GeoLocationRepositoryImpl
import kr.sjh.data.repository.impl.SettingRepositoryImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun provideAdoptionRepository(impl: AdoptionRepositoryImpl): AdoptionRepository

    @Binds
    @Singleton
    abstract fun provideFavouriteRepository(impl: FavouriteRepositoryImpl): FavouriteRepository

    @Binds
    @Singleton
    abstract fun provideSettingRepository(impl: SettingRepositoryImpl): SettingRepository

    @Binds
    @Singleton
    abstract fun provideGeoLocationRepository(impl: GeoLocationRepositoryImpl): GeoLocationRepository
}