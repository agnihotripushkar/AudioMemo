package com.example.audiomemo.features.summary.domain.model

enum class SummaryStatus { PENDING, GENERATING, DONE, FAILED }

data class Summary(
    val id: Long = 0,
    val sessionId: Long,
    val title: String = "",
    val summary: String = "",
    val actionItems: String = "[]",
    val keyPoints: String = "[]",
    val status: SummaryStatus = SummaryStatus.PENDING
)
