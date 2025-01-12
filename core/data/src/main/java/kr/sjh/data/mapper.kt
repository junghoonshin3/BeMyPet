package kr.sjh.data

import kr.sjh.core.ktor.model.response.AbandonmentPublicResponse
import kr.sjh.core.ktor.model.response.KindResponse
import kr.sjh.core.ktor.model.response.SidoResponse
import kr.sjh.core.ktor.model.response.SigunguResponse
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Kind
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu
import kr.sjh.core.model.setting.Setting
import kr.sjh.database.entity.FavouriteEntity

fun AbandonmentPublicResponse.Body.Items.toPets(): List<Pet> {
    return this.item.map {
        Pet(
            desertionNo = it.desertionNo,
            filename = it.filename,
            happenDt = it.happenDt,
            happenPlace = it.happenPlace,
            kindCd = it.kindCd,
            colorCd = it.colorCd,
            age = it.age,
            weight = it.weight,
            noticeNo = it.noticeNo,
            noticeSdt = it.noticeSdt,
            noticeEdt = it.noticeEdt,
            popfile = it.popfile,
            processState = it.processState,
            sexCd = it.sexCd,
            neuterYn = it.neuterYn,
            specialMark = it.specialMark,
            careNm = it.careNm,
            careTel = it.careTel,
            careAddr = it.careAddr,
            orgNm = it.orgNm,
            chargeNm = it.chargeNm,
            officetel = it.officetel,
        )
    }
}

fun SidoResponse.toSidoList(): List<Sido> {
    return this.body?.items?.item?.map {
        Sido(
            orgCd = it.orgCd, orgdownNm = it.orgdownNm
        )
    } ?: emptyList()
}

fun SigunguResponse.toSigunguList(): List<Sigungu> {
    return this.body?.items?.item?.map {
        Sigungu(
            orgCd = it.orgCd, orgdownNm = it.orgdownNm, uprCd = it.uprCd
        )
    } ?: emptyList()
}

fun KindResponse.toKindList(): List<Kind> {
    return this.body?.items?.item?.map {
        Kind(
            kindCd = it.kindCd, knm = it.KNm
        )
    } ?: emptyList()
}

fun Pet.toFavouriteEntity(): FavouriteEntity = FavouriteEntity(
    id = 0,
    desertionNo = this.desertionNo,
    filename = this.filename,
    happenDt = this.happenDt,
    happenPlace = this.happenPlace,
    kindCd = this.kindCd,
    colorCd = this.colorCd,
    age = this.age,
    weight = this.weight,
    noticeNo = this.noticeNo,
    noticeSdt = this.noticeSdt,
    noticeEdt = this.noticeEdt,
    popfile = this.popfile,
    processState = this.processState,
    sexCd = this.sexCd,
    neuterYn = this.neuterYn,
    specialMark = this.specialMark,
    careNm = this.careNm,
    careTel = this.careTel,
    careAddr = this.careAddr,
    orgNm = this.orgNm,
    chargeNm = this.chargeNm,
    officetel = this.officetel,
    noticeComment = this.noticeComment
)

fun FavouriteEntity.toPet(): Pet {
    return Pet(
        desertionNo = this.desertionNo,
        filename = this.filename,
        happenDt = this.happenDt,
        happenPlace = this.happenPlace,
        kindCd = this.kindCd,
        colorCd = this.colorCd,
        age = this.age,
        weight = this.weight,
        noticeNo = this.noticeNo,
        noticeSdt = this.noticeSdt,
        noticeEdt = this.noticeEdt,
        popfile = this.popfile,
        processState = this.processState,
        sexCd = this.sexCd,
        neuterYn = this.neuterYn,
        specialMark = this.specialMark,
        careNm = this.careNm,
        careTel = this.careTel,
        careAddr = this.careAddr,
        orgNm = this.orgNm,
        chargeNm = this.chargeNm,
        officetel = this.officetel,
        noticeComment = this.noticeComment
    )
}
