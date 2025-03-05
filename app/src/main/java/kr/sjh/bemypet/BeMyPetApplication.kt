package kr.sjh.bemypet

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers

@HiltAndroidApp
class BeMyPetApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader(this).newBuilder().memoryCachePolicy(CachePolicy.ENABLED)
            .dispatcher(Dispatchers.IO)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCache {
                MemoryCache.Builder(this).maxSizePercent(0.20)
                    .build()
            }.diskCachePolicy(CachePolicy.ENABLED).diskCache {
                DiskCache.Builder().maxSizePercent(0.03).directory(cacheDir).build()
            }.build()
    }
}