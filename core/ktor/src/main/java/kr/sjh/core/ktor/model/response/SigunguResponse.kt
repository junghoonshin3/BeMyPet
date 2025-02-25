package kr.sjh.core.ktor.model.response

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

// 1️⃣ 최상위 <response> 태그
@Root(name = "response", strict = false)
data class SigunguResponse(
    @field:Element(name = "header") var header: Header = Header(),
    @field:Element(name = "body", required = false) var body: Body = Body()
) {

    // 2️⃣ <header> 태그
    @Root(name = "header", strict = false)
    data class Header(
        @field:Element(name = "reqNo") var reqNo: Int = 0,

        @field:Element(name = "resultCode") var resultCode: String = "",

        @field:Element(name = "resultMsg") var resultMsg: String = "",

        @field:Element(name = "errorMsg", required = false) var errorMsg: String = ""
    )

    // 3️⃣ <body> 태그
    @Root(name = "body", strict = false)
    data class Body(
        @field:Element(name = "items",required = false) var items: Items = Items()
    ) {

        // 4️⃣ <items> 태그 (여러 개의 item 포함)
        @Root(name = "items", strict = false)
        data class Items(
            @field:ElementList(
                inline = true,
                entry = "item",
                required = false
            ) var itemList: MutableList<Item> = mutableListOf()
        ) {

            // 5️⃣ <item> 태그 (각 리스트 항목)
            @Root(name = "item", strict = false)
            data class Item(
                @field:Element(name = "uprCd") var uprCd: String = "", // 상위 기관 코드

                @field:Element(name = "orgCd") var orgCd: String = "", // 기관 코드

                @field:Element(name = "orgdownNm") var orgdownNm: String = "" // 기관명
            )
        }
    }

}


