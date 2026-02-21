package kr.sjh.data.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.sjh.core.ktor.model.request.PetRequest
import kr.sjh.core.ktor.service.PetService
import kr.sjh.core.model.adoption.Pet
import kr.sjh.data.repository.FavouriteRepository
import kr.sjh.data.toPets
import kr.sjh.data.toFavouriteEntity
import kr.sjh.data.toPet
import kr.sjh.database.dao.FavouriteDao
import kr.sjh.database.entity.FavouriteEntity
import java.util.Collections
import javax.inject.Inject

class FavouriteRepositoryImpl @Inject constructor(
    private val dao: FavouriteDao,
    private val service: PetService
) :
    FavouriteRepository {

    private val attemptedBackfillKeys = Collections.synchronizedSet(mutableSetOf<String>())

    override fun isExist(desertionNo: String): Boolean = dao.isExist(desertionNo)

    override suspend fun addPet(pet: Pet) {
        dao.insert(pet.toFavouriteEntity())
    }

    override suspend fun removePet(desertionNo: String) {
        dao.delete(desertionNo)
    }

    override fun getFavouritePets(): Flow<List<Pet>> {
        return dao.getAll().map {
            it.map {
                it.toPet()
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun backfillFavouriteImagesIfNeeded() {
        withContext(Dispatchers.IO) {
            val favourites = runCatching { dao.getAll().first() }.getOrDefault(emptyList())

            favourites.forEach { entity ->
                val currentUrls = resolveStoredImageUrls(entity)
                if (currentUrls.size > 1) return@forEach

                val noticeNo = entity.noticeNo?.trim()?.takeIf { it.isNotBlank() }
                val desertionNo = entity.desertionNo?.trim()?.takeIf { it.isNotBlank() }
                val lookupKey = buildLookupKey(noticeNo = noticeNo, desertionNo = desertionNo)
                    ?: return@forEach

                if (!attemptedBackfillKeys.add(lookupKey)) return@forEach

                val fetchedPet = fetchPetForBackfill(
                    noticeNo = noticeNo,
                    desertionNo = desertionNo
                ) ?: return@forEach

                val fetchedUrls = normalizeImageUrls(
                    imageUrls = fetchedPet.imageUrls,
                    thumbnailImageUrl = fetchedPet.thumbnailImageUrl
                )
                if (fetchedUrls.size <= currentUrls.size) return@forEach

                val updatedEntity = entity.copy(
                    popfile = fetchedUrls.firstOrNull(),
                    noticeComment = runCatching { Json.encodeToString(fetchedUrls) }
                        .getOrElse { entity.noticeComment.orEmpty() }
                )
                dao.insert(updatedEntity)
            }
        }
    }

    private suspend fun fetchPetForBackfill(noticeNo: String?, desertionNo: String?): Pet? {
        return runCatching {
            val response = service.getPets(
                PetRequest(
                    noticeNo = noticeNo,
                    desertionNo = desertionNo,
                    pageNo = 1,
                    numOfRows = 20
                )
            )
            if (response.header.resultCode != "00") return null

            val pets = response.body?.items?.item?.toPets().orEmpty()
            pets.firstOrNull { pet ->
                val isNoticeMatch = !noticeNo.isNullOrBlank() && pet.noticeNo == noticeNo
                val isDesertionMatch =
                    !desertionNo.isNullOrBlank() && pet.desertionNo == desertionNo
                isNoticeMatch || isDesertionMatch
            }
        }.getOrNull()
    }

    private fun resolveStoredImageUrls(entity: FavouriteEntity): List<String> {
        val fromJson = runCatching { Json.decodeFromString<List<String>>(entity.noticeComment.orEmpty()) }
            .getOrDefault(emptyList())
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        if (fromJson.isNotEmpty()) return fromJson

        return listOfNotNull(entity.popfile?.trim()?.takeIf { it.isNotBlank() })
    }

    private fun normalizeImageUrls(imageUrls: List<String>, thumbnailImageUrl: String?): List<String> {
        return imageUrls
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .ifEmpty { listOfNotNull(thumbnailImageUrl?.trim()?.takeIf { it.isNotBlank() }) }
    }

    private fun buildLookupKey(noticeNo: String?, desertionNo: String?): String? {
        if (noticeNo.isNullOrBlank() && desertionNo.isNullOrBlank()) return null
        return "${noticeNo.orEmpty()}|${desertionNo.orEmpty()}"
    }
}
