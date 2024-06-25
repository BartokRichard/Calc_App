package com.example.calculatorapp.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApiService {
    @GET("latest")
    suspend fun getExchangeRates(@Query("apikey") apiKey: String): Response<CurrencyResponse>
}
