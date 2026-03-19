package com.example.audiomemo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.audiomemo.data.db.dao.ChunkDao
import com.example.audiomemo.data.db.dao.SessionDao
import com.example.audiomemo.data.db.dao.SummaryDao
import com.example.audiomemo.data.db.dao.TranscriptDao
import com.example.audiomemo.data.db.entities.ChunkEntity
import com.example.audiomemo.data.db.entities.SessionEntity
import com.example.audiomemo.data.db.entities.SummaryEntity
import com.example.audiomemo.data.db.entities.TranscriptEntity

@Database(
    entities = [
        SessionEntity::class,
        ChunkEntity::class,
        TranscriptEntity::class,
        SummaryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AudioMemoDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun chunkDao(): ChunkDao
    abstract fun transcriptDao(): TranscriptDao
    abstract fun summaryDao(): SummaryDao
}