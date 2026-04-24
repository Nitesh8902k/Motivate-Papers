package com.aiapps.motivatepapersapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    val number: Int,
    val quote: String,
    val category: String
)
