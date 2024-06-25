package com.example.calculatorapp.api

data class CurrencyResponse(
    val data: Map<String, CurrencyData>
)

data class CurrencyData(
    val code: String,
    val value: Double
)
