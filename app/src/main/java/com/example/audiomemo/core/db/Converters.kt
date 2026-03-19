package com.example.audiomemo.data.db

import androidx.room.TypeConverter
import com.example.audiomemo.features.summary.domain.model.SummaryStatus
import com.example.audiomemo.features.transcript.domain.model.ChunkStatus
import com.example.audiomemo.features.transcript.domain.model.SessionState

class Converters {

    @TypeConverter fun fromSessionState(value: SessionState): String = value.name
    @TypeConverter fun toSessionState(value: String): SessionState = SessionState.valueOf(value)

    @TypeConverter fun fromChunkStatus(value: ChunkStatus): String = value.name
    @TypeConverter fun toChunkStatus(value: String): ChunkStatus = ChunkStatus.valueOf(value)

    @TypeConverter fun fromSummaryStatus(value: SummaryStatus): String = value.name
    @TypeConverter fun toSummaryStatus(value: String): SummaryStatus = SummaryStatus.valueOf(value)
}
