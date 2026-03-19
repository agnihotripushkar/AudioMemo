package com.example.audiomemo.data.network.models

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatCompletionRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<ChatMessage>
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatMessage(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatCompletionResponse(
    @SerialName("id") val id: String,
    @SerialName("choices") val choices: List<ChatChoice>
)

@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ChatChoice(
    @SerialName("index") val index: Int,
    @SerialName("message") val message: ChatMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)
