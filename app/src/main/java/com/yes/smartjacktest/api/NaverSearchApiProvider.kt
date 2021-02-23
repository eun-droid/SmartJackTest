package com.yes.smartjacktest.api

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

fun provideNaverSearchApi(): NaverSearchApi
    = Retrofit.Builder()
        .baseUrl("https://openapi.naver.com")
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync()) // 받은 응답을 옵서버블 형태로 변환, 비동기 방식으로 API 호출
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NaverSearchApi::class.java)