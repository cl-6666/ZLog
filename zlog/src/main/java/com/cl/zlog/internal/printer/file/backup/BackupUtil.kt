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

    /**
     * Shift existed backups if needed, and backup the logging file.
     *
     * @param loggingFile    the logging file
     * @param backupStrategy the strategy should be use when backing up
     */
    fun backup(loggingFile: File, backupStrategy: BackupStrategy2) {
        val loggingFileName = loggingFile.name
        val path = loggingFile.parent
        val maxBackupIndex = backupStrategy.maxBackupIndex
        
        if (maxBackupIndex > 0) {
            var backupFile = File(path, backupStrategy.getBackupFileName(loggingFileName, maxBackupIndex))
            if (backupFile.exists()) {
                backupFile.delete()
            }
            
            for (i in maxBackupIndex - 1 downTo 1) {
                backupFile = File(path, backupStrategy.getBackupFileName(loggingFileName, i))
                if (backupFile.exists()) {
                    val nextBackupFile = File(path, backupStrategy.getBackupFileName(loggingFileName, i + 1))
                    backupFile.renameTo(nextBackupFile)
                }
            }
            
            val nextBackupFile = File(path, backupStrategy.getBackupFileName(loggingFileName, 1))
            loggingFile.renameTo(nextBackupFile)
        } else if (maxBackupIndex == BackupStrategy2.NO_LIMIT) {
            for (i in 1 until Int.MAX_VALUE) {
                val nextBackupFile = File(path, backupStrategy.getBackupFileName(loggingFileName, i))
                if (!nextBackupFile.exists()) {
                    loggingFile.renameTo(nextBackupFile)
                    break
                }
            }
        } else {
            // Illegal maxBackIndex, could not come here.
        }
    }

    /**
     * Check if a [BackupStrategy2] is valid, will throw a exception if invalid.
     *
     * @param backupStrategy the backup strategy to be verify
     */
    fun verifyBackupStrategy(backupStrategy: BackupStrategy2) {
        val maxBackupIndex = backupStrategy.maxBackupIndex
        if (maxBackupIndex < 0) {
            throw IllegalArgumentException("Max backup index should not be less than 0")
        } else if (maxBackupIndex == Int.MAX_VALUE) {
            throw IllegalArgumentException("Max backup index too big: $maxBackupIndex")
        }
    }
}