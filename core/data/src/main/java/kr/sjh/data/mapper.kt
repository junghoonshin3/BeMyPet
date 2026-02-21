package kr.sjh.data

import PetItem
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.sjh.core.ktor.model.response.SigunguItem
import kr.sjh.core.model.adoption.Pet
import kr.sjh.database.entity.FavouriteEntity
import kr.sjh.database.entity.SigunguEntity

/* =========================
 * Sigungu
 * ========================= */

fun List<SigunguItem>.toEntities(): List<SigunguEntity> =
    map {
        SigunguEntity(
            orgCd = it.orgCd,
            orgdownNm = it.orgdownNm,
            uprCd = it.uprCd
        )
    }

/* =========================
 * PetItem (API) -> Pet (Domain)
 * ========================= */

fun List<PetItem>.toPets(): List<Pet> =
    map { it.toPet() }

fun PetItem.toPet(): Pet =
    Pet(
        desertionNo = desertionNo ?: "",
        noticeNo = noticeNo,
        noticeStartDate = noticeSdt,
        noticeEndDate = noticeEdt,
        happenDate = happenDt,
        happenPlace = happenPlace,

        upKindCode = upKindCd,
        upKindName = upKindNm,
        kindCode = kindCd,
        kindName = kindNm,
        kindFullName = kindFullNm,

        color = colorCd,
        age = age,
        weight = weight,

        sex = sexCd,
        neutered = neuterYn,
        processState = processState,
        specialMark = specialMark,

        thumbnailImageUrl = popfile1
            ?: popfile2
            ?: popfile3
            ?: popfile4,

        imageUrls = listOfNotNull(
            popfile1,
            popfile2,
            popfile3,
            popfile4,
            popfile5,
            popfile6,
            popfile7,
            popfile8
        ),

        careName = careNm,
        careTel = careTel,
        careAddress = careAddr,
        organizationName = orgNm,
        updatedAt = updTm
    )

/* =========================
 * Pet (Domain) -> FavouriteEntity (DB)
 * ========================= */

fun Pet.toFavouriteEntity(): FavouriteEntity =
    FavouriteEntity(
        id = 0,
        filename = organizationName,
        desertionNo = desertionNo,
        happenDt = happenDate,
        happenPlace = happenPlace,
        kindCd = toFavouriteKindLabel(),
        colorCd = color,
        age = age,
        weight = weight,
        noticeNo = noticeNo,
        noticeSdt = noticeStartDate,
        noticeEdt = noticeEndDate,
        popfile = normalizedImageUrls().firstOrNull(),
        processState = processState,
        sexCd = sex,
        neuterYn = neutered,
        specialMark = specialMark.orEmpty(),
        careNm = careName,
        careTel = careTel,
        careAddr = careAddress,
        orgNm = organizationName,
        chargeNm = careName,
        officetel = careTel,
        noticeComment = runCatching { Json.encodeToString(normalizedImageUrls()) }
            .getOrDefault("")
    )

/* =========================
 * FavouriteEntity (DB) -> Pet (Domain)
 * ========================= */

fun FavouriteEntity.toPet(): Pet =
    Pet(
        desertionNo = desertionNo,
        noticeNo = noticeNo,
        noticeStartDate = noticeSdt,
        noticeEndDate = noticeEdt,
        happenDate = happenDt,
        happenPlace = happenPlace,

        upKindCode = null,
        upKindName = null,
        kindCode = kindCd,
        kindName = kindCd.toKindLabel(),
        kindFullName = kindCd.toKindLabel(),

        color = colorCd,
        age = age,
        weight = weight,

        sex = sexCd,
        neutered = neuterYn,
        processState = processState,
        specialMark = specialMark,

        thumbnailImageUrl = restoredImageUrls().firstOrNull(),
        imageUrls = restoredImageUrls(),

        careName = careNm,
        careTel = careTel,
        careAddress = careAddr,
        organizationName = orgNm,
        updatedAt = null
    )

private fun Pet.normalizedImageUrls(): List<String> =
    imageUrls
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .ifEmpty {
            listOfNotNull(thumbnailImageUrl?.trim()?.takeIf { it.isNotBlank() })
        }

private fun Pet.toFavouriteKindLabel(): String? =
    listOf(kindFullName, kindName, kindCode)
        .mapNotNull { it.toKindLabel() }
        .firstOrNull()

private fun String?.toKindLabel(): String? {
    val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    if (value.startsWith("[") && value.contains("]")) {
        return value.substringAfter("]").trim().ifBlank { value }
    }
    return value
}

private fun FavouriteEntity.restoredImageUrls(): List<String> {
    val fromJson = runCatching { Json.decodeFromString<List<String>>(noticeComment.orEmpty()) }
        .getOrDefault(emptyList())
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()

    if (fromJson.isNotEmpty()) return fromJson

    return listOfNotNull(popfile?.trim()?.takeIf { it.isNotBlank() })
}
