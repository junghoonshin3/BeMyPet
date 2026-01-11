package kr.sjh.data

import PetItem
import kr.sjh.core.ktor.model.response.SigunguItem
import kr.sjh.core.model.adoption.Pet
import kr.sjh.database.entity.FavouriteEntity
import kr.sjh.database.entity.SigunguEntity

fun List<SigunguItem>.toEntities(): List<SigunguEntity> {
    return map {
        SigunguEntity(
            orgCd = it.orgCd, orgdownNm = it.orgdownNm, uprCd = it.uprCd
        )
    }
}

fun List<PetItem>.toPets(): List<Pet> {
    return map {
        Pet(
            desertionNo = it.desertionNo,
            rfidCd = it.rfidCd,
            happenDt = it.happenDt,
            happenPlace = it.happenPlace,
            kindCd = it.kindCd,
            colorCd = it.colorCd,
            age = it.age,
            weight = it.weight,
            noticeNo = it.noticeNo,
            noticeSdt = it.noticeSdt,
            noticeEdt = it.noticeEdt,
            popfile1 = it.popfile1,
            popfile2 = it.popfile2,
            processState = it.processState,
            sexCd = it.sexCd,
            neuterYn = it.neuterYn,
            specialMark = it.specialMark,
            careNm = it.careNm,
            careTel = it.careTel,
            careAddr = it.careAddr,
            orgNm = it.orgNm,

        )
    }
}

fun Pet.toFavouriteEntity(): FavouriteEntity = FavouriteEntity(
    id = 0,
    filename = "",
    desertionNo = this.desertionNo,
    happenDt = this.happenDt,
    happenPlace = this.happenPlace,
    kindCd = this.kindCd,
    colorCd = this.colorCd,
    age = this.age,
    weight = this.weight,
    noticeNo = this.noticeNo,
    noticeSdt = this.noticeSdt,
    noticeEdt = this.noticeEdt,
    popfile = this.popfile1!!,
    processState = this.processState,
    sexCd = this.sexCd,
    neuterYn = this.neuterYn,
    specialMark = this.specialMark!!,
    careNm = this.careNm,
    careTel = this.careTel,
    careAddr = this.careAddr,
    orgNm = this.orgNm,
    chargeNm = this.careNm,
    officetel = this.careTel,
    noticeComment =""
)

fun FavouriteEntity.toPet(): Pet {
    return Pet(
        desertionNo = this.desertionNo,
        happenDt = this.happenDt,
        happenPlace = this.happenPlace,
        kindCd = this.kindCd,
        colorCd = this.colorCd,
        age = this.age,
        weight = this.weight,
        noticeNo = this.noticeNo,
        noticeSdt = this.noticeSdt,
        noticeEdt = this.noticeEdt,
        popfile1 =  this.popfile,
        processState = this.processState,
        sexCd = this.sexCd,
        neuterYn = this.neuterYn,
        specialMark = this.specialMark,
        careNm = this.careNm,
        careTel = this.careTel,
        careAddr = this.careAddr,
        orgNm = this.orgNm
    )
}
