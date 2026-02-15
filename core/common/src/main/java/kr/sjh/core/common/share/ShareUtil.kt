package kr.sjh.core.common.share

import android.content.Context
import android.content.Intent
import kr.sjh.core.model.adoption.Pet

fun buildShareText(pet: Pet): String {
    return buildString {
        appendLine("[BeMyPet] 입양 공고 안내")
        appendLine()
        pet.kindFullName?.let { appendLine("품종: $it") }
        appendLine("성별: ${pet.sexCdToText}")
        pet.age?.let { appendLine("나이: $it") }
        pet.happenPlace?.let { appendLine("발견장소: $it") }
        pet.processState?.let { appendLine("상태: $it") }
        pet.specialMark?.let { appendLine("특징: $it") }
        appendLine()
        pet.careName?.let { appendLine("보호소: $it") }
        pet.careTel?.let { appendLine("연락처: $it") }
    }
}

fun Context.sharePet(pet: Pet) {
    val text = buildShareText(pet)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "[BeMyPet] ${pet.kindFullName ?: "입양 공고"}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(intent, "공유하기"))
}
