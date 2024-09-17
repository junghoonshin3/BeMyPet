package kr.sjh.data

import kr.sjh.core.ktor.model.response.AbandonmentPublic
import kr.sjh.core.ktor.model.response.AbandonmentPublicResponse
import kr.sjh.core.model.adoption.Pet

fun AbandonmentPublic.toPets(): List<Pet> {
    return this.body.items.item.map {
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
            officetel = it.officetel
        )
    }
}