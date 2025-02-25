package kr.sjh.core.ktor.model.response

import kr.sjh.core.ktor.model.Header
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class KindResponse(
    @field:Element(name = "header")
    var header: Header = Header(),

    @field:Element(name = "body", required = false)
    var body: Body? = null
) {
    @Root(name = "header", strict = false)
    data class Header(
        @field:Element(name = "reqNo")
        var reqNo: Int = 0,

        @field:Element(name = "resultCode")
        var resultCode: String = "",

        @field:Element(name = "resultMsg")
        var resultMsg: String = ""

    )

    @Root(name = "body", strict = false)
    data class Body(
        @field:Element(name = "items")
        var items: Items = Items()
    ) {
        @Root(name = "items", strict = false)
        data class Items(
            @field:ElementList(entry = "item", inline = true, required = false)
            var item: List<Item>? = null
        ) {
            @Root(name = "item", strict = false)
            data class Item(
                @field:Element(name = "kindCd")
                var kindCd: String = "",

                @field:Element(name = "KNm")
                var KNm: String = ""
            )
        }
    }
}