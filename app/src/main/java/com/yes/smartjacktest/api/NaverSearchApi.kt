package com.yes.smartjacktest.api

import com.yes.smartjacktest.model.NaverSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverSearchApi {

    @GET("/v1/search/image")
    fun searchImage(
            @Header("X-Naver-Client-Id") naverClientId: String,
            @Header("X-Naver-Client-Secret") naverClientSecret: String,
            @Query("query") query: String,
            @Query("display") display: Int? = null,
            @Query("start") start: Int? = null,
            @Query("sort") sort: String? = null,
            @Query("filter") filter: String? = null
    ): Call<NaverSearchResponse>
}