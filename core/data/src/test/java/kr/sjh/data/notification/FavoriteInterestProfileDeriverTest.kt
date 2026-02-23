package kr.sjh.data.notification

import kr.sjh.core.model.adoption.Pet
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteInterestProfileDeriverTest {

    @Test
    fun derive_extracts_region_species_sex_and_size_from_favorites() {
        val pets = listOf(
            Pet(
                noticeNo = "전남-함평-2026-00071",
                upKindCode = "417000",
                sex = "M",
                weight = "4.3(Kg)",
            ),
            Pet(
                noticeNo = "경기-수원-2026-00011",
                upKindCode = "422400",
                sex = "F",
                weight = "8kg",
            ),
        )

        val result = FavoriteInterestProfileDeriver.derive(pets)

        assertEquals(listOf("6410000", "6460000"), result.regions)
        assertEquals(listOf("cat", "dog"), result.species)
        assertEquals(listOf("F", "M"), result.sexes)
        assertEquals(listOf("MEDIUM", "SMALL"), result.sizes)
    }

    @Test
    fun derive_uses_kind_text_when_upkind_code_is_missing() {
        val pets = listOf(
            Pet(kindName = "코리안숏헤어 고양이"),
            Pet(kindFullName = "믹스견"),
        )

        val result = FavoriteInterestProfileDeriver.derive(pets)

        assertEquals(listOf("cat", "dog"), result.species)
    }

    @Test
    fun derive_deduplicates_and_ignores_blank_values() {
        val pets = listOf(
            Pet(noticeNo = "서울-중구-2026-00001", upKindCode = "417000", sex = "M", weight = "2kg"),
            Pet(noticeNo = "서울-강남-2026-00002", upKindCode = "417000", sex = "M", weight = "2kg"),
            Pet(noticeNo = "", upKindCode = "", sex = "", weight = ""),
        )

        val result = FavoriteInterestProfileDeriver.derive(pets)

        assertEquals(listOf("6110000"), result.regions)
        assertEquals(listOf("dog"), result.species)
        assertEquals(listOf("M"), result.sexes)
        assertEquals(listOf("SMALL"), result.sizes)
    }
}
