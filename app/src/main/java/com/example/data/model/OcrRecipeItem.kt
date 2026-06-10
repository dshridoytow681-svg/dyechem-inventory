package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OcrRecipeItem(
    val productName: String,
    val lotNumber: String = "",
    val quantity: Double = 0.0
)
