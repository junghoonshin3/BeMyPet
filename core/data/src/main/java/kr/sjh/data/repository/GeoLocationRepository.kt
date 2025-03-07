package kr.sjh.data.repository

import android.location.Address

interface GeoLocationRepository {
    suspend fun getFromLocationName(address: String, maxResults: Int): List<Address>
}