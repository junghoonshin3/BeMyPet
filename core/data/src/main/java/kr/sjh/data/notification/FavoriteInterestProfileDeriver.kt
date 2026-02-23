package kr.sjh.data.notification

import kr.sjh.core.model.adoption.Pet

data class DerivedInterestProfile(
    val regions: List<String>,
    val species: List<String>,
    val sexes: List<String>,
    val sizes: List<String>,
)

object FavoriteInterestProfileDeriver {

    fun derive(pets: List<Pet>): DerivedInterestProfile {
        val regions = pets.mapNotNull { deriveRegionCode(it.noticeNo) }.distinct().sorted()
        val species = pets.mapNotNull { deriveSpecies(it) }.distinct().sorted()
        val sexes = pets.mapNotNull { deriveSex(it.sex) }.distinct().sorted()
        val sizes = pets.mapNotNull { deriveSizeCategory(it.weight) }.distinct().sorted()

        return DerivedInterestProfile(
            regions = regions,
            species = species,
            sexes = sexes,
            sizes = sizes,
        )
    }

    private fun deriveRegionCode(noticeNo: String?): String? {
        val prefix = noticeNo
            ?.substringBefore('-')
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        return REGION_PREFIX_TO_CODE[prefix]
    }

    private fun deriveSpecies(pet: Pet): String? {
        val upKindCode = pet.upKindCode?.trim().orEmpty()
        if (upKindCode.isNotBlank()) {
            return when (upKindCode) {
                "417000" -> "dog"
                "422400" -> "cat"
                else -> upKindCode.lowercase()
            }
        }

        val speciesHint = listOf(pet.kindFullName, pet.kindName, pet.kindCode)
            .joinToString(" ") { it.orEmpty() }
            .lowercase()
            .trim()

        if (speciesHint.isBlank()) return null
        if (speciesHint.contains("고양이") || speciesHint.contains("묘")) return "cat"
        if (speciesHint.contains("강아지") || speciesHint.contains("개") || speciesHint.contains("견")) {
            return "dog"
        }

        return null
    }

    private fun deriveSex(rawSex: String?): String? {
        return rawSex
            ?.trim()
            ?.uppercase()
            ?.takeIf { it == "M" || it == "F" || it == "Q" }
    }

    private fun deriveSizeCategory(weightRaw: String?): String? {
        val normalized = weightRaw?.replace(",", "") ?: return null
        val match = Regex("\\d+(?:\\.\\d+)?").find(normalized) ?: return null
        val value = match.value.toDoubleOrNull() ?: return null

        return when {
            value <= 5.0 -> "SMALL"
            value <= 15.0 -> "MEDIUM"
            else -> "LARGE"
        }
    }

    private val REGION_PREFIX_TO_CODE = mapOf(
        "서울" to "6110000",
        "서울특별시" to "6110000",
        "부산" to "6260000",
        "부산광역시" to "6260000",
        "대구" to "6270000",
        "대구광역시" to "6270000",
        "인천" to "6280000",
        "인천광역시" to "6280000",
        "광주" to "6290000",
        "광주광역시" to "6290000",
        "세종" to "5690000",
        "세종특별자치시" to "5690000",
        "대전" to "6300000",
        "대전광역시" to "6300000",
        "울산" to "6310000",
        "울산광역시" to "6310000",
        "경기" to "6410000",
        "경기도" to "6410000",
        "강원" to "6530000",
        "강원특별자치도" to "6530000",
        "충북" to "6430000",
        "충청북도" to "6430000",
        "충남" to "6440000",
        "충청남도" to "6440000",
        "전북" to "6540000",
        "전북특별자치도" to "6540000",
        "전남" to "6460000",
        "전라남도" to "6460000",
        "경북" to "6470000",
        "경상북도" to "6470000",
        "경남" to "6480000",
        "경상남도" to "6480000",
        "제주" to "6500000",
        "제주특별자치도" to "6500000",
    )
}
