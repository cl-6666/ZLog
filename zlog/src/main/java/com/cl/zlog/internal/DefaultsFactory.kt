/*
 * Copyright 2016 cl
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

package com.cl.zlog.internal

import com.cl.zlog.flattener.DefaultFlattener
import com.cl.zlog.flattener.Flattener
import com.cl.zlog.flattener.Flattener2
import com.cl.zlog.formatter.border.BorderFormatter
import com.cl.zlog.formatter.border.DefaultBorderFormatter
import com.cl.zlog.formatter.message.json.DefaultJsonFormatter
import com.cl.zlog.formatter.message.json.JsonFormatter
import com.cl.zlog.formatter.message.`object`.ObjectFormatter
import com.cl.zlog.formatter.message.throwable.DefaultThrowableFormatter
import com.cl.zlog.formatter.message.throwable.ThrowableFormatter
import com.cl.zlog.formatter.message.xml.DefaultXmlFormatter
import com.cl.zlog.formatter.message.xml.XmlFormatter
import com.cl.zlog.formatter.stacktrace.DefaultStackTraceFormatter
import com.cl.zlog.formatter.stacktrace.StackTraceFormatter
import com.cl.zlog.formatter.thread.DefaultThreadFormatter
import com.cl.zlog.formatter.thread.ThreadFormatter
import com.cl.zlog.printer.Printer
import com.cl.zlog.printer.file.FilePrinter
import com.cl.zlog.printer.file.backup.BackupStrategy2
import com.cl.zlog.internal.printer.file.backup.BackupStrategyWrapper
import com.cl.zlog.printer.file.backup.FileSizeBackupStrategy
import com.cl.zlog.printer.file.clean.CleanStrategy
import com.cl.zlog.printer.file.clean.NeverCleanStrategy
import com.cl.zlog.printer.file.naming.ChangelessFileNameGenerator
import com.cl.zlog.printer.file.naming.FileNameGenerator
import com.cl.zlog.printer.file.writer.SimpleWriter
import com.cl.zlog.printer.file.writer.Writer

/**
 * Factory for providing default implementation.
 */
object DefaultsFactory {

    private const val DEFAULT_LOG_FILE_NAME = "log"

    private const val DEFAULT_LOG_FILE_MAX_SIZE = 1024 * 1024L // 1M bytes

    /**
     * Create the default JSON formatter.
     */
    fun createJsonFormatter(): JsonFormatter {
        return DefaultJsonFormatter()
    }

    /**
     * Create the default XML formatter.
     */
    fun createXmlFormatter(): XmlFormatter {
        return DefaultXmlFormatter()
    }

    /**
     * Create the default throwable formatter.
     */
    fun createThrowableFormatter(): ThrowableFormatter {
        return DefaultThrowableFormatter()
    }

    /**
     * Create the default thread formatter.
     */
    fun createThreadFormatter(): ThreadFormatter {
        return DefaultThreadFormatter()
    }

    /**
     * Create the default stack trace formatter.
     */
    fun createStackTraceFormatter(): StackTraceFormatter {
        return DefaultStackTraceFormatter()
    }

    /**
     * Create the default border formatter.
     */
    fun createBorderFormatter(): BorderFormatter {
        return DefaultBorderFormatter()
    }

    /**
     * Create the default [Flattener].
     */
    fun createFlattener(): Flattener {
        return DefaultFlattener()
    }

    /**
     * Create the default [Flattener2].
     */
    fun createFlattener2(): Flattener2 {
        return DefaultFlattener()
    }

    /**
     * Create the default printer.
     */
    fun createPrinter(): Printer {
        return Platform.get().defaultPrinter()
    }

    /**
     * Create the default file name generator for [FilePrinter].
     */
    fun createFileNameGenerator(): FileNameGenerator {
        return ChangelessFileNameGenerator(DEFAULT_LOG_FILE_NAME)
    }

    /**
     * Create the default backup strategy for [FilePrinter].
     */
    fun createBackupStrategy(): BackupStrategy2 {
        return BackupStrategyWrapper(FileSizeBackupStrategy(DEFAULT_LOG_FILE_MAX_SIZE))
    }

    /**
     * Create the default clean strategy for [FilePrinter].
     */
    fun createCleanStrategy(): CleanStrategy {
        return NeverCleanStrategy()
    }

    /**
     * Create the default writer for [FilePrinter].
     */
    fun createWriter(): Writer {
        return SimpleWriter()
    }

    /**
     * Get the builtin object formatters.
     *
     * @return the builtin object formatters
     */
    fun builtinObjectFormatters(): Map<Class<*>, ObjectFormatter<*>> {
        return Platform.get().builtinObjectFormatters()
    }
}