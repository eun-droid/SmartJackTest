package com.yes.smartjacktest.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun provideNaverSearchApi(): NaverSearchApi
    = Retrofit.Builder()
        .baseUrl("https://openapi.naver.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(NaverSearchApi::class.java)