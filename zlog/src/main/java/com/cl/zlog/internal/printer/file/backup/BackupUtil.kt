/*
 * Copyright 2021 cl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cl.zlog.internal.printer.file.backup

import com.cl.zlog.printer.file.backup.BackupStrategy2
import java.io.File

object BackupUtil {

    internal data class BackupResult(
        val success: Boolean,
        val backupFiles: Set<File>
    )

    /**
     * Shift existed backups if needed, and backup the logging file.
     *
     * @param loggingFile    the logging file
     * @param backupStrategy the strategy should be use when backing up
     */
    @JvmStatic
    fun backup(loggingFile: File, backupStrategy: BackupStrategy2) {
        backupWithResult(loggingFile, backupStrategy)
    }

    internal fun backupWithResult(
        loggingFile: File,
        backupStrategy: BackupStrategy2
    ): BackupResult {
        val loggingFileName = loggingFile.name
        val folder = loggingFile.parentFile?.canonicalFile
            ?: return BackupResult(false, emptySet())
        val maxBackupIndex = backupStrategy.maxBackupIndex
        val backupFiles = linkedSetOf<File>()

        fun resolveBackupFile(index: Int): File? {
            val fileName = backupStrategy.getBackupFileName(loggingFileName, index)
            val file = File(folder, fileName).canonicalFile
            return file.takeIf { it.parentFile == folder }
        }
        
        if (maxBackupIndex > 0) {
            var backupFile = resolveBackupFile(maxBackupIndex)
                ?: return BackupResult(false, backupFiles)
            if (backupFile.exists() && !backupFile.delete()) {
                return BackupResult(false, backupFiles)
            }
            
            for (i in maxBackupIndex - 1 downTo 1) {
                backupFile = resolveBackupFile(i) ?: return BackupResult(false, backupFiles)
                if (backupFile.exists()) {
                    val nextBackupFile = resolveBackupFile(i + 1)
                        ?: return BackupResult(false, backupFiles)
                    if (!backupFile.renameTo(nextBackupFile)) {
                        return BackupResult(false, backupFiles)
                    }
                    backupFiles.add(nextBackupFile)
                }
            }
            
            val nextBackupFile = resolveBackupFile(1) ?: return BackupResult(false, backupFiles)
            if (!loggingFile.renameTo(nextBackupFile)) {
                return BackupResult(false, backupFiles)
            }
            backupFiles.add(nextBackupFile)
        } else if (maxBackupIndex == BackupStrategy2.NO_LIMIT) {
            for (i in 1 until Int.MAX_VALUE) {
                val nextBackupFile = resolveBackupFile(i) ?: return BackupResult(false, backupFiles)
                if (!nextBackupFile.exists()) {
                    if (!loggingFile.renameTo(nextBackupFile)) {
                        return BackupResult(false, backupFiles)
                    }
                    backupFiles.add(nextBackupFile)
                    return BackupResult(true, backupFiles)
                }
            }
            return BackupResult(false, backupFiles)
        } else {
            // Illegal maxBackIndex, could not come here.
            return BackupResult(false, backupFiles)
        }
        return BackupResult(true, backupFiles)
    }

    /**
     * Check if a [BackupStrategy2] is valid, will throw a exception if invalid.
     *
     * @param backupStrategy the backup strategy to be verify
     */
    @JvmStatic
    fun verifyBackupStrategy(backupStrategy: BackupStrategy2) {
        val maxBackupIndex = backupStrategy.maxBackupIndex
        if (maxBackupIndex < 0) {
            throw IllegalArgumentException("Max backup index should not be less than 0")
        } else if (maxBackupIndex == Int.MAX_VALUE) {
            throw IllegalArgumentException("Max backup index too big: $maxBackupIndex")
        }
    }
}
