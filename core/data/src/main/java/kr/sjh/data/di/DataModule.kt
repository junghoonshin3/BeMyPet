package kr.sjh.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.repository.AuthRepository
import kr.sjh.data.repository.BlockRepository
import kr.sjh.data.repository.CommentRepository
import kr.sjh.data.repository.CompareRepository
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.repository.GeoLocationRepository
import kr.sjh.data.repository.SettingRepository
import kr.sjh.data.repository.impl.AdoptionRepositoryImpl
import kr.sjh.data.repository.impl.AuthRepositoryImpl
import kr.sjh.data.repository.impl.BlockRepositoryImpl
import kr.sjh.data.repository.impl.CommentRepositoryImpl
import kr.sjh.data.repository.impl.CompareRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun provideCommentRepository(impl: CommentRepositoryImpl): CommentRepository

    @Binds
    @Singleton
    abstract fun provideCompareRepository(impl: CompareRepositoryImpl): CompareRepository

    @Binds
    @Singleton
    abstract fun provideBlockRepository(impl: BlockRepositoryImpl): BlockRepository

}
