package kr.sjh.core.ktor.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ShelterResponse(
    val response: Response
) {
    @Serializable
    data class Response(
        val header: Header,
        val body: Body
    ) {
        @Serializable
        data class Header(
            val reqNo: Int,
            val resultCode: String,
            val resultMsg: String
        )

        @Serializable
        data class Body(
            val items: Items
        ) {
            @Serializable
            data class Items(
                val item: List<Item>
            ) {
                @Serializable
                data class Item(
                    val careRegNo: String,
                    val careNm: String
                )
            }
        }
    }
}