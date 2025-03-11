package kr.sjh.core.supabase.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sjh.core.supabase.service.AuthService
import kr.sjh.core.supabase.service.PostgrestService
import kr.sjh.core.supabase.service.impl.AuthServiceImpl
import kr.sjh.core.supabase.service.impl.PostgrestServiceImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SupaServiceModule {
    @Binds
    @Singleton
    abstract fun provideAuthService(impl: AuthServiceImpl): AuthService

    @Binds
    @Singleton
    abstract fun providePostgrestService(impl: PostgrestServiceImpl): PostgrestService
}