package kr.sjh.core.ktor.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.xml.xml
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.model.XML
import kr.sjh.core.ktor.service.PetService
import kr.sjh.core.ktor.service.impl.PetServiceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            install(DefaultRequest) {
                url(BuildConfig.BASE_URL)
                url {
                    parameters.append("serviceKey", BuildConfig.SERVICE_KEY)
                    parameters.append("_type", XML)
                }
            }
            install(ContentNegotiation) {
                xml(
                    contentType = ContentType.Text.Xml
                )
            }
        }
    }

    @Singleton
    @Provides
    fun provideAdoptionService(httpClient: HttpClient): PetService = PetServiceImpl(httpClient)

}