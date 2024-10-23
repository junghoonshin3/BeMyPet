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
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.xml.xml
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import kr.sjh.core.ktor.BuildConfig
import kr.sjh.core.ktor.repository.AdoptionService
import kr.sjh.core.ktor.repository.impl.AdoptionServiceImpl
import nl.adaptivity.xmlutil.XmlDeclMode
import nl.adaptivity.xmlutil.serialization.XML
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Singleton
    @Provides
    fun provideHttpClient(): HttpClient {
        return HttpClient(Android) {
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
            install(Logging) {
//                logger = Logger.DEFAULT
//                level = LogLevel.ALL
            }
            install(DefaultRequest) {
                url(BuildConfig.BASE_URL)
            }

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
                xml(format = XML {
                    xmlDeclMode = XmlDeclMode.Charset
                })
            }
        }
    }

    @Singleton
    @Provides
    fun provideAdoptionService(httpClient: HttpClient): AdoptionService =
        AdoptionServiceImpl(httpClient)

    @Provides
    fun provideDispatcher(): CoroutineDispatcher = Dispatchers.Default


}