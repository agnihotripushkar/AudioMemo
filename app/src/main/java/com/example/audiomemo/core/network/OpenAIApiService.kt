package com.example.audiomemo.data.network

import com.example.audiomemo.data.network.models.ChatCompletionRequest
import com.example.audiomemo.data.network.models.ChatCompletionResponse
import com.example.audiomemo.data.network.models.TranscriptionResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAIApiService {

    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse

    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
        @Part model: MultipartBody.Part
    ): TranscriptionResponse
}
