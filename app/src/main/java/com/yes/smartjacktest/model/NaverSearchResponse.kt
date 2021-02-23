package com.yes.smartjacktest.model

import com.google.gson.annotations.SerializedName

data class NaverSearchResponse(
    var lastBuildDate: String = "",     // 검색 결과 생성 시간
    var total: Int = 0,                 // 검색 결과 문서의 총 개수
    var start: Int = 0,                 // 검색 결과 문서 중, 문서의 시작점
    var display: Int = 0,               // 검색된 검색 결과의 개수
    var items: List<ImageItem>              // XML 포맷에서는 item 태그, JSON 포맷에서는 items 속성으로 표현됨. 개별 검색 결과임
)

data class ImageItem(
    var title: String = "",         // 검색 결과 이미지의 제목
    var link: String = "",          // 검색 결과 이미지의 하이퍼텍스트 link
    var thumbnail: String = "",     // 검색 결과 이미지의 썸네일 link
    @SerializedName("sizeheight") var sizeHeight: String = "",    // 검색 결과 이미지의 높이
    @SerializedName("sizewidth") var sizeWidth: String = ""      // 검색 결과 이미지의 너비. 단위는 px
)