package kr.sjh.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sjh.data.repository.AdoptionRepository
import kr.sjh.data.repository.AuthenticationRepository
import kr.sjh.data.repository.impl.AdoptionRepositoryImpl
import kr.sjh.data.repository.impl.AuthenticationRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideAuthenticationRepository(impl: AuthenticationRepositoryImpl): AuthenticationRepository =
        impl

    @Provides
    @Singleton
    fun provideAdoptionRepository(impl: AdoptionRepositoryImpl): AdoptionRepository =
        impl
}