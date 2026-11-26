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
import com.cl.zlog.internal.printer.file.ManagedFileRegistry
import com.cl.zlog.internal.printer.file.backup.BackupStrategyWrapper
import com.cl.zlog.internal.printer.file.backup.BackupUtil
import com.cl.zlog.printer.Printer
import com.cl.zlog.printer.file.backup.BackupStrategy
import com.cl.zlog.printer.file.backup.BackupStrategy2
import com.cl.zlog.printer.file.clean.CleanStrategy
import com.cl.zlog.printer.file.naming.FileNameGenerator
import com.cl.zlog.printer.file.writer.Writer
import java.io.Closeable
import java.io.File
import java.io.Flushable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CountDownLatch

/**
 * Log [Printer] using file system. When print a log, it will print it to the specified file.
 *
 * Use the [Builder] to construct a [FilePrinter] object.
 */
class FilePrinter internal constructor(builder: Builder) : Printer, Flushable, Closeable {

    /**
     * The folder path of log file.
     */
    private val logFolder: File

    private val managedFileRegistry: ManagedFileRegistry

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

    private val worker: Worker

    init {
        logFolder = File(builder.folderPath).canonicalFile
        fileNameGenerator = builder.fileNameGenerator
        backupStrategy = builder.backupStrategy
        cleanStrategy = builder.cleanStrategy
        flattener = builder.flattener
        writer = builder.writer
        checkLogFolder()
        managedFileRegistry = ManagedFileRegistry(logFolder)
        worker = Worker(builder.queueCapacity)
    }

    /**
     * Make sure the folder of log file exists.
     */
    private fun checkLogFolder() {
        if (!logFolder.exists() && !logFolder.mkdirs()) {
            throw IllegalStateException("Log folder not created: $logFolder")
        }
        if (!logFolder.isDirectory) {
            throw IllegalStateException("Log folder is not a directory: $logFolder")
        }
    }

    override fun println(logLevel: Int, tag: String, msg: String) {
        worker.enqueue(LogItem(System.currentTimeMillis(), logLevel, tag, msg))
    }

    override fun flush() {
        worker.flush()
    }

    override fun close() {
        worker.close()
    }

    /**
     * Do the real job of writing log to file.
     */
    private fun doPrintln(timeMillis: Long, logLevel: Int, tag: String, msg: String) {
        val lastFileName = writer.openedFileName
        val isWriterClosed = !writer.isOpened
        if (lastFileName == null || isWriterClosed || fileNameGenerator.isFileNameChangeable) {
            val newFileName = fileNameGenerator.generateFileName(logLevel, timeMillis)
            if (newFileName.isNullOrBlank()) {
                Platform.get().error("File name should not be empty, ignore file log")
                return
            }
            if (newFileName != lastFileName || isWriterClosed) {
                val newLogFile = resolveLogFile(newFileName) ?: return
                writer.close()
                cleanLogFilesIfNecessary()
                if (!writer.open(newLogFile)) {
                    return
                }
                managedFileRegistry.register(newLogFile)
            }
        }

        val lastFile = writer.openedFile ?: return
        if (backupStrategy.shouldBackup(lastFile)) {
            // Backup the log file, and create a new log file.
            val loggingFileName = writer.openedFileName ?: lastFile.name
            writer.close()
            val backupResult = BackupUtil.backupWithResult(lastFile, backupStrategy)
            managedFileRegistry.register(backupResult.backupFiles)
            if (!backupResult.success) {
                Platform.get().error("Backup log file failed: ${lastFile.name}")
            }
            cleanLogFilesIfNecessary()
            val loggingFile = resolveLogFile(loggingFileName) ?: return
            if (!writer.open(loggingFile)) {
                return
            }
            managedFileRegistry.register(loggingFile)
        }
        val flattenedLog = flattener.flatten(timeMillis, logLevel, tag, msg).toString()
        writer.appendLog(flattenedLog)
    }

    /**
     * Clean log files if should clean follow strategy
     */
    private fun cleanLogFilesIfNecessary() {
        for (file in managedFileRegistry.files()) {
            if (!file.exists()) {
                managedFileRegistry.unregister(file)
            } else if (cleanStrategy.shouldClean(file) && file.delete()) {
                managedFileRegistry.unregister(file)
            }
        }
    }

    private fun resolveLogFile(fileName: String): File? {
        if (managedFileRegistry.isReserved(fileName)) {
            Platform.get().error("Reserved log file name: $fileName")
            return null
        }
        return try {
            val file = File(logFolder, fileName).canonicalFile
            if (file.parentFile != logFolder) {
                Platform.get().error("Log file must be inside log folder: $fileName")
                null
            } else {
                file
            }
        } catch (e: Exception) {
            Platform.get().error("Resolve log file failed: ${e.message}")
            null
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
         * Maximum number of pending file logs. Producers apply backpressure when this queue is full.
         */
        internal var queueCapacity: Int = DEFAULT_QUEUE_CAPACITY

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
         * Set the maximum number of logs waiting for file IO.
         */
        fun queueCapacity(queueCapacity: Int): Builder {
            require(queueCapacity > 0) { "Queue capacity should be greater than 0" }
            this.queueCapacity = queueCapacity
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

    private sealed interface QueueItem

    private data class LogItem(
        val timeMillis: Long,
        val level: Int,
        val tag: String,
        val msg: String
    ) : QueueItem

    private data class FlushItem(val completion: CountDownLatch) : QueueItem

    private data object StopItem : QueueItem

    /**
     * Work in background, we can enqueue the logs, and the worker will dispatch them.
     */
    private inner class Worker(queueCapacity: Int) : Runnable, Flushable, Closeable {

        private val queue: BlockingQueue<QueueItem> = ArrayBlockingQueue(queueCapacity)
        private val lifecycleLock = Any()
        private val terminated = CountDownLatch(1)

        @Volatile
        private var acceptingLogs = true

        private var started = false

        /**
         * Enqueue the log.
         *
         * @param log the log to be written to file
         */
        fun enqueue(log: LogItem) {
            synchronized(lifecycleLock) {
                if (!acceptingLogs) {
                    Platform.get().warn("File printer is closed, ignore file log")
                    return
                }
                startLocked()
                putUninterruptibly(log)
            }
        }

        override fun flush() {
            var completion: CountDownLatch? = null
            var waitForTermination = false
            synchronized(lifecycleLock) {
                if (!acceptingLogs) {
                    waitForTermination = true
                } else if (started) {
                    completion = CountDownLatch(1)
                    putUninterruptibly(FlushItem(completion!!))
                }
            }
            if (waitForTermination) {
                awaitUninterruptibly(terminated)
            } else {
                completion?.let(::awaitUninterruptibly)
            }
        }

        override fun close() {
            synchronized(lifecycleLock) {
                if (acceptingLogs) {
                    acceptingLogs = false
                    startLocked()
                    putUninterruptibly(StopItem)
                }
            }
            awaitUninterruptibly(terminated)
        }

        private fun startLocked() {
            if (started) {
                return
            }
            started = true
            Thread(this, WORKER_THREAD_NAME).apply {
                isDaemon = true
                start()
            }
        }

        override fun run() {
            try {
                while (true) {
                    when (val item = queue.take()) {
                        is LogItem -> printSafely(item)
                        is FlushItem -> {
                            try {
                                writer.flush()
                            } catch (e: Exception) {
                                Platform.get().error("Flush file log failed: ${e.message}")
                            } finally {
                                item.completion.countDown()
                            }
                        }
                        StopItem -> return
                    }
                }
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                Platform.get().warn("File log worker interrupted")
            } finally {
                acceptingLogs = false
                try {
                    writer.close()
                } catch (e: Exception) {
                    Platform.get().error("Close file log failed: ${e.message}")
                } finally {
                    // Drain first to free any caller holding lifecycleLock while waiting to enqueue.
                    discardPendingItems()
                    synchronized(lifecycleLock) {
                        discardPendingItems()
                    }
                    terminated.countDown()
                }
            }
        }

        private fun discardPendingItems() {
            while (true) {
                val item = queue.poll() ?: return
                if (item is FlushItem) {
                    item.completion.countDown()
                }
            }
        }

        private fun printSafely(log: LogItem) {
            try {
                doPrintln(log.timeMillis, log.level, log.tag, log.msg)
            } catch (e: Exception) {
                Platform.get().error("Write file log failed: ${e.message}")
            }
        }

        private fun putUninterruptibly(item: QueueItem) {
            var interrupted = false
            while (true) {
                try {
                    queue.put(item)
                    break
                } catch (_: InterruptedException) {
                    interrupted = true
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt()
            }
        }

        private fun awaitUninterruptibly(latch: CountDownLatch) {
            var interrupted = false
            while (true) {
                try {
                    latch.await()
                    break
                } catch (_: InterruptedException) {
                    interrupted = true
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt()
            }
        }
    }

    companion object {
        private const val DEFAULT_QUEUE_CAPACITY = 1024
        private const val WORKER_THREAD_NAME = "ZLog-FilePrinter"
    }
}
