package kr.sjh.data

import kr.sjh.core.ktor.model.response.AbandonmentPublicResponse
import kr.sjh.core.ktor.model.response.KindResponse
import kr.sjh.core.ktor.model.response.SidoResponse
import kr.sjh.core.ktor.model.response.SigunguResponse
import kr.sjh.core.model.adoption.Pet
import kr.sjh.core.model.adoption.filter.Kind
import kr.sjh.core.model.adoption.filter.Sido
import kr.sjh.core.model.adoption.filter.Sigungu

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
    return this.body.items.item.map {
        Sido(
            orgCd = it.orgCd, orgdownNm = it.orgdownNm
        )
    }
}

fun SigunguResponse.toSigunguList(): List<Sigungu> {
    return this.body.items.item.map {
        Sigungu(
            orgCd = it.orgCd, orgdownNm = it.orgdownNm, uprCd = it.uprCd
        )
    }
}

fun KindResponse.toKindList(): List<Kind> {
    return this.body.items.item.map {
        Kind(
            kindCd = it.kindCd, knm = it.KNm
        )

    }
}