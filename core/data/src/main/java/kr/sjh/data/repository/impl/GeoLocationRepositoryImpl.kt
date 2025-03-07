package kr.sjh.data.repository.impl

import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kr.sjh.data.repository.GeoLocationRepository
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GeoLocationRepositoryImpl @Inject constructor(private val geocoder: Geocoder) :
    GeoLocationRepository {
    override suspend fun getFromLocationName(
        address: String, maxResults: Int
    ) = suspendCoroutine<List<Address>> { continuation ->
        try {
            if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocationName(
                    address, 1
                ) { shelterAddress ->
                    continuation.resume(shelterAddress)
                }
            } else {
                val shelterAddress = geocoder.getFromLocationName(
                    address,
                    1,
                )
                if (!shelterAddress.isNullOrEmpty()) {
                    continuation.resume(shelterAddress)
                } else {
                    continuation.resumeWithException(Exception("주소를 찾을수 없어요."))
                }
            }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

}