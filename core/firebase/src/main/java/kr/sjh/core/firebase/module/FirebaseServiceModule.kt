package kr.sjh.core.firebase.module

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sjh.core.firebase.impl.AccountServiceImpl
import kr.sjh.core.firebase.impl.FireStoreRepositoryImpl
import kr.sjh.core.firebase.impl.StorageRepositoryImpl
import kr.sjh.core.firebase.service.AccountService
import kr.sjh.core.firebase.service.FireStoreRepository
import kr.sjh.core.firebase.service.StorageRepository

@Module
@InstallIn(SingletonComponent::class)
interface FirebaseServiceModule {
    @Binds
    fun provideAccountService(impl: AccountServiceImpl): AccountService

    @Binds
    fun provideFireStoreService(impl: FireStoreRepositoryImpl): FireStoreRepository

    @Binds
    fun provideStorageService(impl: StorageRepositoryImpl): StorageRepository
}