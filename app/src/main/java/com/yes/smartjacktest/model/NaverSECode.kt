package com.yes.smartjacktest.model

// Naver Search Error Code
enum class NaverSECode (
        val errorCode: String,
        val errorMessage: String,
        val solutionMessage: String)
{
    SE01("SE01", "Incorrect query request", "검색 API 요청에 오류가 있습니다. 요청 URL, 필수 요청 변수가 정확한지 확인 바랍니다."),
    SE02("SE02", "Invalid display value",  "display 요청 변수값이 허용 범위(1~100)인지 확인해 보세요."),
    SE03("SE03", "Invalid start value", "start 요청 변수값이 허용 범위(1~1000)인지 확인해 보세요."),
    SE04("SE04", "Invalid sort value", "sort 요청 변수 값에 오타가 없는지 확인해 보세요."),
    SE05("SE05", "Invalid search api", "검색 API 대상에 오타가 없는지 확인해 보세요."),
    SE06("SE06","Malformed encoding", "검색어를 UTF-8로 인코딩하세요."),
    SE99("SE99", "System Error (시스템 에러)", "서버 내부 에러가 발생하였습니다. 포럼에 올려주시면 신속히 조치하겠습니다.")
}