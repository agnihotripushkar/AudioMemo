package com.example.audiomemo.features.transcript.manager

import android.os.StatFs
import java.io.File

/**
 * Checks available storage before and during recording to prevent partial/corrupt chunks
 * caused by running out of disk space.
 */
object StorageGuard {

    /** Minimum free space required to start or continue recording (50 MB). */
    const val MIN_FREE_BYTES = 50L * 1024 * 1024

    fun hasEnoughStorage(dir: File): Boolean = getAvailableBytes(dir) >= MIN_FREE_BYTES

    fun getAvailableBytes(dir: File): Long {
        return try {
            val stat = StatFs(dir.absolutePath)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (e: Exception) {
            0L
        }
    }
}
