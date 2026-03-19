package com.example.audiomemo.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.audiomemo.features.summary.domain.model.SummaryStatus

@Entity(
    tableName = "summaries",
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val title: String = "",
    val summary: String = "",
    val actionItems: String = "[]",  // JSON array
    val keyPoints: String = "[]",    // JSON array
    val status: SummaryStatus = SummaryStatus.PENDING
)
