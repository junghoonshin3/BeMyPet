package kr.sjh.core.model.adoption

val Pet.displayBreedName: String
    get() = listOf(kindFullName, kindName, kindCode)
        .mapNotNull { it.toBreedLabelOrNull() }
        .firstOrNull()
        ?: "품종 정보 없음"

private fun String?.toBreedLabelOrNull(): String? {
    val value = this?.trim()?.takeIf { it.isNotBlank() } ?: return null
    if (value.startsWith("[") && value.contains("]")) {
        return value.substringAfter("]").trim().ifBlank { value }
    }
    return value
}
