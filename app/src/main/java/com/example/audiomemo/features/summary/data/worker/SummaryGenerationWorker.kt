package com.example.audiomemo.features.summary.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.audiomemo.BuildConfig
import com.example.audiomemo.data.db.dao.SummaryDao
import com.example.audiomemo.data.db.dao.TranscriptDao
import com.example.audiomemo.data.db.entities.SummaryEntity
import com.example.audiomemo.data.network.OpenAIApiService
import com.example.audiomemo.data.network.models.ChatCompletionRequest
import com.example.audiomemo.data.network.models.ChatMessage
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.data.mapper.toDomain
import com.example.audiomemo.features.transcript.util.TranscriptStitcher
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SummaryGenerationEntryPoint {
    fun transcriptDao(): TranscriptDao
    fun summaryDao(): SummaryDao
    fun openAIApiService(): OpenAIApiService
}

/**
 * Stitches all chunk transcripts for a session, calls GPT-4o-mini to produce a summary,
 * and persists the result in [SummaryEntity]. Runs after all [WhisperUploadWorker]s finish.
 */
class SummaryGenerationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_SESSION_ID = "session_id"
        const val WORK_NAME_PREFIX = "summary_generation_"
    }

    override suspend fun doWork(): Result {
        val sessionId = inputData.getLong(KEY_SESSION_ID, -1L)
        if (sessionId < 0L) return Result.failure()

        if (BuildConfig.OPENAI_API_KEY.isBlank()) {
            Log.e("SummaryGenerationWorker", "OPENAI_API_KEY is not configured")
            return Result.failure()
        }

        val ep = EntryPointAccessors.fromApplication(
            applicationContext,
            SummaryGenerationEntryPoint::class.java
        )
        val transcriptDao = ep.transcriptDao()
        val summaryDao = ep.summaryDao()
        val apiService = ep.openAIApiService()

        val transcripts = transcriptDao.getTranscriptsForSession(sessionId).first()
            .map { it.toDomain() }
        val stitchedText = TranscriptStitcher().stitch(transcripts)

        if (stitchedText.isBlank()) {
            summaryDao.insert(
                SummaryEntity(
                    sessionId = sessionId,
                    title = "Empty Recording",
                    summary = "No speech was detected in this recording.",
                    status = SummaryStatus.DONE
                )
            )
            return Result.success()
        }

        val existingSummary = summaryDao.getSummaryForSession(sessionId).first()
        val summaryId = if (existingSummary == null) {
            summaryDao.insert(SummaryEntity(sessionId = sessionId, status = SummaryStatus.GENERATING))
        } else {
            summaryDao.update(existingSummary.copy(status = SummaryStatus.GENERATING))
            existingSummary.id
        }

        return try {
            val systemPrompt = """
                You are a concise meeting/lecture assistant. Given a raw transcript, produce:
                1. A short title (max 8 words)
                2. A 2-4 sentence summary
                3. Up to 5 key points (bullet list, one per line starting with "• ")
                4. Up to 3 action items (bullet list, one per line starting with "- ")
                Format your response EXACTLY as:
                TITLE: <title>
                SUMMARY: <summary>
                KEY_POINTS:
                • <point1>
                • <point2>
                ACTION_ITEMS:
                - <item1>
                - <item2>
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = "gpt-4o-mini",
                messages = listOf(
                    ChatMessage(role = "system", content = systemPrompt),
                    ChatMessage(role = "user", content = "Transcript:\n$stitchedText")
                )
            )

            val response = apiService.createChatCompletion(
                authHeader = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content.orEmpty()
            val parsed = parseGptResponse(content)

            summaryDao.update(
                SummaryEntity(
                    id = summaryId,
                    sessionId = sessionId,
                    title = parsed.title,
                    summary = parsed.summary,
                    keyPoints = parsed.keyPoints,
                    actionItems = parsed.actionItems,
                    status = SummaryStatus.DONE
                )
            )
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SummaryGenerationWorker", "Summary generation failed (attempt $runAttemptCount)", e)
            summaryDao.updateStatus(sessionId, SummaryStatus.FAILED)
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    private data class ParsedSummary(
        val title: String,
        val summary: String,
        val keyPoints: String,
        val actionItems: String
    )

    private fun parseGptResponse(content: String): ParsedSummary {
        val lines = content.lines()
        val title = lines.firstOrNull { it.startsWith("TITLE:") }
            ?.removePrefix("TITLE:")?.trim() ?: "Recording Summary"

        val summaryText = lines.firstOrNull { it.startsWith("SUMMARY:") }
            ?.removePrefix("SUMMARY:")?.trim() ?: content.take(300)

        val keyPoints = lines
            .dropWhile { !it.startsWith("KEY_POINTS:") }
            .drop(1)
            .takeWhile { it.startsWith("•") }
            .joinToString("\n")

        val actionItems = lines
            .dropWhile { !it.startsWith("ACTION_ITEMS:") }
            .drop(1)
            .takeWhile { it.startsWith("-") }
            .joinToString("\n")

        return ParsedSummary(
            title = title,
            summary = summaryText,
            keyPoints = keyPoints,
            actionItems = actionItems
        )
    }
}
