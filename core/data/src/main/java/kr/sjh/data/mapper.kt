package kr.sjh.data

import PetItem
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
        kindCd = kindCode,
        colorCd = color,
        age = age,
        weight = weight,
        noticeNo = noticeNo,
        noticeSdt = noticeStartDate,
        noticeEdt = noticeEndDate,
        popfile = thumbnailImageUrl.orEmpty(),
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
        noticeComment = ""
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
        kindName = null,
        kindFullName = null,

        color = colorCd,
        age = age,
        weight = weight,

        sex = sexCd,
        neutered = neuterYn,
        processState = processState,
        specialMark = specialMark,

        thumbnailImageUrl = popfile,
        imageUrls = listOfNotNull(popfile),

        careName = careNm,
        careTel = careTel,
        careAddress = careAddr,
        organizationName = orgNm,
        updatedAt = null
    )
