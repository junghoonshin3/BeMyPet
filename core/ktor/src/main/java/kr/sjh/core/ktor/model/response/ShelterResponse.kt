package kr.sjh.core.ktor.model.response

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class ShelterResponse(
    @field:Element(name = "header")
    var header: Header = Header(),

    @field:Element(name = "body")
    var body: Body = Body()
) {
    @Root(name = "header", strict = false)
    data class Header(
        @field:Element(name = "reqNo", required = false)
        var reqNo: Int = 0,

        @field:Element(name = "resultCode", required = false)
        var resultCode: String = "",

        @field:Element(name = "resultMsg", required = false)
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
            var itemList: List<Item> = listOf()
        ) {
            @Root(name = "item", strict = false)
            data class Item(
                @field:Element(name = "careRegNo", required = false)
                var careRegNo: String = "",

                @field:Element(name = "careNm", required = false)
                var careNm: String = ""
            )
        }
    }
}