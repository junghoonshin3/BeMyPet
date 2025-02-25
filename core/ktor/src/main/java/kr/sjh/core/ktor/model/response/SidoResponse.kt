package kr.sjh.core.ktor.model.response

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class SidoResponse(
    @field:Element(name = "header")
    var header: Header = Header(),
    @field:Element(name = "body")
    var body: Body = Body()
) {
    // 2️⃣ <header> 태그
    @Root(name = "header", strict = false)
    data class Header(
        @field:Element(name = "reqNo")
        var reqNo: Int = 0,

        @field:Element(name = "resultCode")
        var resultCode: String = "",

        @field:Element(name = "resultMsg")
        var resultMsg: String = ""
    )

    // 3️⃣ <body> 태그
    @Root(name = "body", strict = false)
    data class Body(
        @field:Element(name = "numOfRows")
        var numOfRows: Int = 0,

        @field:Element(name = "pageNo")
        var pageNo: Int = 0,

        @field:Element(name = "totalCount")
        var totalCount: Int = 0,

        @field:Element(name = "items")
        var items: Items = Items()
    ) {
        // 4️⃣ <items> 태그
        @Root(name = "items", strict = false)
        data class Items(
            @field:ElementList(inline = true, entry = "item")
            var itemList: List<Item> = mutableListOf()
        ) {
            // 5️⃣ <item> 태그 (리스트 항목)
            @Root(name = "item", strict = false)
            data class Item(
                @field:Element(name = "orgCd")
                var orgCd: String = "",  // 기관 코드

                @field:Element(name = "orgdownNm")
                var orgdownNm: String = "" // 기관명
            )

        }
    }
}




