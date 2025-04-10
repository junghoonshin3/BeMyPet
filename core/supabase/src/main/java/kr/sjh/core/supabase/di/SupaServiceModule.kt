package kr.sjh.core.supabase.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.sjh.core.supabase.service.AuthService
import kr.sjh.core.supabase.service.BlockService
import kr.sjh.core.supabase.service.CommentService
import kr.sjh.core.supabase.service.ReportService
import kr.sjh.core.supabase.service.impl.AuthServiceImpl
import kr.sjh.core.supabase.service.impl.BlockServiceImpl
import kr.sjh.core.supabase.service.impl.CommentServiceImpl
import kr.sjh.core.supabase.service.impl.ReportServiceImpl
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SupaServiceModule {
    @Binds
    @Singleton
    abstract fun bindAuthService(impl: AuthServiceImpl): AuthService

    @Binds
    @Singleton
    abstract fun bindCommentService(impl: CommentServiceImpl): CommentService

    @Binds
    @Singleton
    abstract fun bindReportService(impl: ReportServiceImpl): ReportService

    @Binds
    @Singleton
    abstract fun bindBlockService(impl: BlockServiceImpl): BlockService
}