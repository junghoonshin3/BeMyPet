package kr.sjh.core.model.adoption

import android.os.Parcelable
import androidx.compose.runtime.Stable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Stable
@Serializable
@Parcelize
data class Pet(

    /** 구조 번호 (고유 식별자) */
    val desertionNo: String? = "",

    /** 공고 번호 */
    val noticeNo: String? = null,

    /** 공고 시작일 */
    val noticeStartDate: String? = null,

    /** 공고 종료일 */
    val noticeEndDate: String? = null,

    /** 접수일 */
    val happenDate: String? = null,

    /** 발견 장소 */
    val happenPlace: String? = "",

    /** 축종 코드 (개/고양이 등) */
    val upKindCode: String? = null,

    /** 축종명 */
    val upKindName: String? = null,

    /** 품종 코드 */
    val kindCode: String? = null,

    /** 품종명 */
    val kindName: String? = null,

    /** 품종 전체 이름 */
    val kindFullName: String? = null,

    /** 색상 */
    val color: String? = null,

    /** 나이 */
    val age: String? = null,

    /** 체중 */
    val weight: String? = null,

    /** 성별 (M/F/Q) */
    val sex: String? = null,

    /** 중성화 여부 (Y/N/U) */
    val neutered: String? = null,

    /** 상태 (보호중, 종료 등) */
    val processState: String? = null,

    /** 특징 및 특이사항 */
    val specialMark: String? = null,

    /** 대표 이미지 */
    val thumbnailImageUrl: String? = null,

    /** 추가 이미지 목록 */
    val imageUrls: List<String> = emptyList(),

    /** 보호소 이름 */
    val careName: String? = null,

    /** 보호소 전화번호 */
    val careTel: String? = null,

    /** 보호소 주소 */
    val careAddress: String? = null,

    /** 관할 기관 */
    val organizationName: String? = null,

    /** 수정일 */
    val updatedAt: String? = null
) : Parcelable {
    val sexCdToText: String
        get() {
            return if (sex == "M") "수컷" else "암컷"
        }
    val neuterYnToText: String
        get() {
            return if (neutered == "Y") "예" else "아니요"
        }
    val isNotice: Boolean
        get() {
            if (noticeStartDate.isNullOrBlank() || noticeEndDate.isNullOrBlank()) {
                return false
            }

            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val today = LocalDate.now()
                val start = LocalDate.parse(noticeStartDate, formatter)
                val end = LocalDate.parse(noticeEndDate, formatter)

                today in start..end
            } catch (e: Exception) {
                false
            }
        }
}

