package kr.sjh.core.ktor.model.response

import kotlinx.serialization.Serializable
import kr.sjh.core.ktor.model.Response


@Serializable
data class KindResponse(
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
            val items: Items,
            val numOfRows: Int,
            val pageNo: Int,
            val totalCount: Int
        ) {
            @Serializable
            data class Items(
                val item: List<Item>
            ) {
                @Serializable
                data class Item(
                    val kindCd: String,
                    val knm: String
                )
            }
        }
    }
}