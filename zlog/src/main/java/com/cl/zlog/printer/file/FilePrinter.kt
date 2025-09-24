/*
 * Copyright 2015 cl
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

package com.cl.zlog.printer.file

import com.cl.zlog.flattener.Flattener
import com.cl.zlog.flattener.Flattener2
import com.cl.zlog.internal.DefaultsFactory
import com.cl.zlog.internal.Platform
import com.cl.zlog.internal.printer.file.backup.BackupStrategyWrapper
import com.cl.zlog.internal.printer.file.backup.BackupUtil
import com.cl.zlog.printer.Printer
import com.cl.zlog.printer.file.backup.BackupStrategy
import com.cl.zlog.printer.file.backup.BackupStrategy2
import com.cl.zlog.printer.file.clean.CleanStrategy
import com.cl.zlog.printer.file.naming.FileNameGenerator
import com.cl.zlog.printer.file.writer.Writer
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

/**
 * Log [Printer] using file system. When print a log, it will print it to the specified file.
 *
 * Use the [Builder] to construct a [FilePrinter] object.
 */
class FilePrinter internal constructor(builder: Builder) : Printer {

    /**
     * The folder path of log file.
     */
    private val folderPath: String

    /**
     * The file name generator for log file.
     */
    private val fileNameGenerator: FileNameGenerator

    /**
     * The backup strategy for log file.
     */
    private val backupStrategy: BackupStrategy2

    /**
     * The clean strategy for log file.
     */
    private val cleanStrategy: CleanStrategy

    /**
     * The flattener when print a log.
     */
    private var flattener: Flattener2

    /**
     * Log writer.
     */
    private var writer: Writer

    @Volatile
    private var worker: Worker? = null

    init {
        folderPath = builder.folderPath
        fileNameGenerator = builder.fileNameGenerator
        backupStrategy = builder.backupStrategy
        cleanStrategy = builder.cleanStrategy
        flattener = builder.flattener
        writer = builder.writer

        if (USE_WORKER) {
            worker = Worker()
        }

        checkLogFolder()
    }

    /**
     * Make sure the folder of log file exists.
     */
    private fun checkLogFolder() {
        val folder = File(folderPath)
        if (!folder.exists()) {
            folder.mkdirs()
        }
    }

    override fun println(logLevel: Int, tag: String, msg: String) {
        val timeMillis = System.currentTimeMillis()
        if (USE_WORKER) {
            worker?.let { w ->
                if (!w.isStarted()) {
                    w.start()
                }
                w.enqueue(LogItem(timeMillis, logLevel, tag, msg))
            }
        } else {
            doPrintln(timeMillis, logLevel, tag, msg)
        }
    }

    /**
     * Do the real job of writing log to file.
     */
    private fun doPrintln(timeMillis: Long, logLevel: Int, tag: String, msg: String) {
        val lastFileName = writer.openedFileName
        val isWriterClosed = !writer.isOpened
        if (lastFileName == null || isWriterClosed || fileNameGenerator.isFileNameChangeable) {
            val newFileName = fileNameGenerator.generateFileName(logLevel, System.currentTimeMillis())
            if (newFileName.isNullOrBlank()) {
                Platform.get().error("File name should not be empty, ignore log: $msg")
                return
            }
            if (newFileName != lastFileName || isWriterClosed) {
                writer.close()
                cleanLogFilesIfNecessary()
                if (!writer.open(File(folderPath, newFileName))) {
                    return
                }
            }
        }

        val lastFile = writer.openedFile
        if (backupStrategy.shouldBackup(lastFile)) {
            // Backup the log file, and create a new log file.
            writer.close()
            BackupUtil.backup(lastFile, backupStrategy)
            if (!writer.open(File(folderPath, writer.openedFileName ?: return))) {
                return
            }
        }
        val flattenedLog = flattener.flatten(timeMillis, logLevel, tag, msg).toString()
        writer.appendLog(flattenedLog)
    }

    /**
     * Clean log files if should clean follow strategy
     */
    private fun cleanLogFilesIfNecessary() {
        val logDir = File(folderPath)
        val files = logDir.listFiles() ?: return
        for (file in files) {
            if (cleanStrategy.shouldClean(file)) {
                file.delete()
            }
        }
    }

    /**
     * Builder for [FilePrinter].
     */
    class Builder(
        /**
         * The folder path of log file.
         */
        internal var folderPath: String
    ) {

        /**
         * The file name generator for log file.
         */
        internal var fileNameGenerator: FileNameGenerator = DefaultsFactory.createFileNameGenerator()

        /**
         * The backup strategy for log file.
         */
        internal var backupStrategy: BackupStrategy2 = DefaultsFactory.createBackupStrategy()

        /**
         * The clean strategy for log file.
         */
        internal var cleanStrategy: CleanStrategy = DefaultsFactory.createCleanStrategy()

        /**
         * The flattener when print a log.
         */
        internal var flattener: Flattener2 = DefaultsFactory.createFlattener2()

        /**
         * The writer to write log into log file.
         */
        internal var writer: Writer = DefaultsFactory.createWriter()

        /**
         * Set the file name generator for log file.
         *
         * @param fileNameGenerator the file name generator for log file
         * @return the builder
         */
        fun fileNameGenerator(fileNameGenerator: FileNameGenerator): Builder {
            this.fileNameGenerator = fileNameGenerator
            return this
        }

        /**
         * Set the backup strategy for log file.
         *
         * @param backupStrategy the backup strategy for log file
         * @return the builder
         */
        fun backupStrategy(backupStrategy: BackupStrategy): Builder {
            this.backupStrategy = if (backupStrategy is BackupStrategy2) {
                backupStrategy
            } else {
                BackupStrategyWrapper(backupStrategy)
            }

            BackupUtil.verifyBackupStrategy(this.backupStrategy)
            return this
        }

        /**
         * Set the clean strategy for log file.
         *
         * @param cleanStrategy the clean strategy for log file
         * @return the builder
         * @since 1.5.0
         */
        fun cleanStrategy(cleanStrategy: CleanStrategy): Builder {
            this.cleanStrategy = cleanStrategy
            return this
        }

        /**
         * Set the flattener when print a log.
         *
         * @param flattener the flattener when print a log
         * @return the builder
         * @deprecated [Flattener] is deprecated, use [flattener] instead,
         * since 1.6.0
         */
        @Deprecated("Use flattener(Flattener2) instead")
        fun logFlattener(flattener: Flattener): Builder {
            return flattener(object : Flattener2 {
                override fun flatten(timeMillis: Long, logLevel: Int, tag: String, message: String): CharSequence {
                    return flattener.flatten(logLevel, tag, message)
                }
            })
        }

        /**
         * Set the flattener when print a log.
         *
         * @param flattener the flattener when print a log
         * @return the builder
         * @since 1.6.0
         */
        fun flattener(flattener: Flattener2): Builder {
            this.flattener = flattener
            return this
        }

        /**
         * Set the writer to write log into log file.
         *
         * @param writer the writer to write log into log file
         * @return the builder
         * @since 1.11.0
         */
        fun writer(writer: Writer): Builder {
            this.writer = writer
            return this
        }

        /**
         * Build configured [FilePrinter] object.
         *
         * @return the built configured [FilePrinter] object
         */
        fun build(): FilePrinter {
            fillEmptyFields()
            return FilePrinter(this)
        }

        private fun fillEmptyFields() {
            if (fileNameGenerator == null) {
                fileNameGenerator = DefaultsFactory.createFileNameGenerator()
            }
            if (backupStrategy == null) {
                backupStrategy = DefaultsFactory.createBackupStrategy()
            }
            if (cleanStrategy == null) {
                cleanStrategy = DefaultsFactory.createCleanStrategy()
            }
            if (flattener == null) {
                flattener = DefaultsFactory.createFlattener2()
            }
            if (writer == null) {
                writer = DefaultsFactory.createWriter()
            }
        }
    }

    private data class LogItem(
        val timeMillis: Long,
        val level: Int,
        val tag: String,
        val msg: String
    )

    /**
     * Work in background, we can enqueue the logs, and the worker will dispatch them.
     */
    private inner class Worker : Runnable {

        private val logs: BlockingQueue<LogItem> = LinkedBlockingQueue()

        @Volatile
        private var started = false

        /**
         * Enqueue the log.
         *
         * @param log the log to be written to file
         */
        fun enqueue(log: LogItem) {
            try {
                logs.put(log)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

        /**
         * Whether the worker is started.
         *
         * @return true if started, false otherwise
         */
        fun isStarted(): Boolean {
            synchronized(this) {
                return started
            }
        }

        /**
         * Start the worker.
         */
        fun start() {
            synchronized(this) {
                if (started) {
                    return
                }
                Thread(this).start()
                started = true
            }
        }

        override fun run() {
            try {
                while (true) {
                    val log = logs.take()
                    doPrintln(log.timeMillis, log.level, log.tag, log.msg)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
                synchronized(this) {
                    started = false
                }
            }
        }
    }

    companion object {
        /**
         * Use worker, write logs asynchronously.
         */
        private const val USE_WORKER = true
    }
}