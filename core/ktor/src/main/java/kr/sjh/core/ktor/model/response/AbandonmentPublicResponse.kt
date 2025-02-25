import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "response", strict = false)
data class AbandonmentPublicResponse(
    @field:Element(name = "header") var header: Header = Header(),

    @field:Element(name = "body", required = false) var body: Body? = null  // body는 null일 수도 있음
) {

    @Root(name = "header", strict = false)
    data class Header(
        @field:Element(name = "reqNo")
        var reqNo: Int = 0,

        @field:Element(name = "resultCode")
        var resultCode: String = "",

        @field:Element(name = "resultMsg")
        var resultMsg: String = "",

        @field:Element(name = "errorMsg", required = false)
        var errorMsg: String = ""
    )

    @Root(name = "body", strict = false)
    data class Body(
        @field:Element(name = "items") var items: Items = Items(),

        @field:Element(name = "numOfRows") var numOfRows: Int = 0,

        @field:Element(name = "pageNo") var pageNo: Int = 0,

        @field:Element(name = "totalCount") var totalCount: Int = 0
    ) {
        @Root(name = "items", strict = false)
        data class Items(
            @field:ElementList(
                entry = "item",
                inline = true,
                required = false
            ) var item: List<Item>? = null  // 리스트가 없을 수도 있음
        ) {
            @Root(name = "item", strict = false)
            data class Item(
                @field:Element(name = "desertionNo") var desertionNo: String = "",

                @field:Element(name = "filename") var filename: String = "",

                @field:Element(name = "happenDt") var happenDt: String = "",

                @field:Element(name = "happenPlace") var happenPlace: String = "",

                @field:Element(name = "kindCd") var kindCd: String = "",

                @field:Element(name = "colorCd", required = false) var colorCd: String? = null,

                @field:Element(name = "age") var age: String = "",

                @field:Element(name = "weight") var weight: String = "",

                @field:Element(name = "noticeNo") var noticeNo: String = "",

                @field:Element(name = "noticeSdt") var noticeSdt: String = "",

                @field:Element(name = "noticeEdt") var noticeEdt: String = "",

                @field:Element(name = "popfile") var popfile: String = "",

                @field:Element(name = "processState") var processState: String = "",

                @field:Element(name = "sexCd") var sexCd: String = "",

                @field:Element(name = "neuterYn") var neuterYn: String = "",

                @field:Element(name = "specialMark") var specialMark: String = "",

                @field:Element(name = "careNm") var careNm: String = "",

                @field:Element(name = "careTel") var careTel: String = "",

                @field:Element(name = "careAddr") var careAddr: String = "",

                @field:Element(name = "orgNm") var orgNm: String = "",

                @field:Element(name = "chargeNm", required = false) var chargeNm: String? = null,

                @field:Element(name = "officetel") var officetel: String = "",

                @field:Element(
                    name = "noticeComment",
                    required = false
                ) var noticeComment: String? = null
            )

        }

    }

}