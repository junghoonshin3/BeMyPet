package kr.sjh.core.firebase.module

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sjh.core.firebase.impl.AccountServiceImpl
import kr.sjh.core.firebase.impl.FireStoreRepositoryImpl
import kr.sjh.core.firebase.impl.StorageRepositoryImpl
import kr.sjh.core.firebase.service.AccountService
import kr.sjh.core.firebase.service.FireStoreRepository
import kr.sjh.core.firebase.service.StorageRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseServiceModule {

    @Provides
    @Singleton
    fun provideStorageRepository(impl: StorageRepositoryImpl): StorageRepository = impl

    @Provides
    @Singleton
    fun provideAccountService(impl: AccountServiceImpl): AccountService = impl

    @Provides
    @Singleton
    fun provideFireStoreRepository(impl: FireStoreRepositoryImpl): FireStoreRepository = impl
}

