package kr.sjh.core.google.module

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.sjh.core.google.BuildConfig.WEB_CLIENT_ID
import kr.sjh.core.google.impl.GoogleAuthServiceImpl
import kr.sjh.core.google.impl.GoogleProfileServiceImpl
import kr.sjh.core.google.service.GoogleAuthService
import kr.sjh.core.google.service.GoogleProfileService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class GoogleAuthModule {
    @Singleton
    @Provides
    fun provideGetGoogleIdOption() =
        GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID).build()

    @Singleton
    @Provides
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
        CredentialManager.create(context)

    @Singleton
    @Provides
    fun provideAuthService(
        auth: FirebaseAuth, googleIdOption: GetGoogleIdOption, credentialManager: CredentialManager
    ): GoogleAuthService = GoogleAuthServiceImpl(
        auth = auth, googleIdOption = googleIdOption, credentialManager = credentialManager
    )

    @Singleton
    @Provides
    fun provideProfileService(
        auth: FirebaseAuth,
        credentialManager: CredentialManager,
    ): GoogleProfileService = GoogleProfileServiceImpl(
        auth = auth, credentialManager = credentialManager
    )
}