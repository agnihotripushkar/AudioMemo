package com.example.audiomemo.features.transcript.util

import com.example.audiomemo.features.transcript.domain.model.Transcript

/**
 * Stitches individual chunk transcripts into one coherent text for a session.
 * Chunks are sorted by chunkIndex and joined with a space.
 */
class TranscriptStitcher {

    fun stitch(transcripts: List<Transcript>): String =
        transcripts
            .sortedBy { it.chunkIndex }
            .joinToString(" ") { it.text.trim() }
            .trim()
}
