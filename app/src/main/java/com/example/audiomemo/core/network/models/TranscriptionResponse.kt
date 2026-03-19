package com.example.audiomemo.data.network.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TranscriptionResponse(
    @SerialName("text") val text: String
)
