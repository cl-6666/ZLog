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

package com.cl.zlog

import com.cl.zlog.formatter.border.BorderFormatter
import com.cl.zlog.formatter.message.json.JsonFormatter
import com.cl.zlog.formatter.message.`object`.ObjectFormatter
import com.cl.zlog.formatter.message.throwable.ThrowableFormatter
import com.cl.zlog.formatter.message.xml.XmlFormatter
import com.cl.zlog.formatter.stacktrace.StackTraceFormatter
import com.cl.zlog.formatter.thread.ThreadFormatter
import com.cl.zlog.interceptor.Interceptor
import com.cl.zlog.internal.DefaultsFactory
import com.cl.zlog.internal.Platform
import com.cl.zlog.internal.SystemCompat
import com.cl.zlog.internal.util.StackTraceUtil
import com.cl.zlog.printer.Printer
import com.cl.zlog.printer.PrinterSet
import java.util.*

/**
 * A logger is used to do the real logging work, can use multiple log printers to print the log.
 * <p>
 * A [Logger] is always generated and mostly accessed by [ZLog], but for customization
 * purpose, you can configure a [Logger] via the [Builder] which is returned by
 * [ZLog] when you trying to start a customization using [ZLog.tag]
 * or other configuration method, and to use the customized [Logger], you should call
 * the [Builder.build] to build a [Logger], and then you can log using
 * the [Logger] assuming that you are using the [ZLog] directly.
 */
class Logger {

    /**
     * The log configuration which you should respect to when logging.
     */
    private val logConfiguration: LogConfiguration

    /**
     * The log printer used to print the logs.
     */
    private val printer: Printer

    /**
     * Construct a logger.
     *
     * @param logConfiguration the log configuration which you should respect to when logging
     * @param printer          the log printer used to print the log
     */
    internal constructor(logConfiguration: LogConfiguration, printer: Printer) {
        this.logConfiguration = logConfiguration
        this.printer = printer
    }

    /**
     * Construct a logger using builder.
     *
     * @param builder the logger builder
     */
    internal constructor(builder: Builder) {
        val logConfigBuilder = LogConfiguration.Builder(ZLog.sLogConfiguration!!)

        if (builder.logLevel != 0) {
            logConfigBuilder.logLevel(builder.logLevel)
        }

        if (builder.tag != null) {
            logConfigBuilder.tag(builder.tag!!)
        }

        if (builder.threadSet) {
            if (builder.withThread) {
                logConfigBuilder.enableThreadInfo()
            } else {
                logConfigBuilder.disableThreadInfo()
            }
        }
        if (builder.stackTraceSet) {
            if (builder.withStackTrace) {
                logConfigBuilder.enableStackTrace(builder.stackTraceOrigin, builder.stackTraceDepth)
            } else {
                logConfigBuilder.disableStackTrace()
            }
        }
        if (builder.borderSet) {
            if (builder.withBorder) {
                logConfigBuilder.enableBorder()
            } else {
                logConfigBuilder.disableBorder()
            }
        }

        if (builder.jsonFormatter != null) {
            logConfigBuilder.jsonFormatter(builder.jsonFormatter!!)
        }
        if (builder.xmlFormatter != null) {
            logConfigBuilder.xmlFormatter(builder.xmlFormatter!!)
        }
        if (builder.throwableFormatter != null) {
            logConfigBuilder.throwableFormatter(builder.throwableFormatter!!)
        }
        if (builder.threadFormatter != null) {
            logConfigBuilder.threadFormatter(builder.threadFormatter!!)
        }
        if (builder.stackTraceFormatter != null) {
            logConfigBuilder.stackTraceFormatter(builder.stackTraceFormatter!!)
        }
        if (builder.borderFormatter != null) {
            logConfigBuilder.borderFormatter(builder.borderFormatter!!)
        }
        if (builder.objectFormatters != null) {
            logConfigBuilder.objectFormatters(builder.objectFormatters!!)
        }
        if (builder.interceptors != null) {
            logConfigBuilder.interceptors(builder.interceptors!!)
        }
        logConfiguration = logConfigBuilder.build()

        printer = builder.printer ?: ZLog.sPrinter!!
    }

    /**
     * Log an object with level [LogLevel.VERBOSE].
     *
     * @param object the object to log
     * @see Builder.addObjectFormatter
     * @since 1.1.0
     */
    fun v(`object`: Any?) {
        println(LogLevel.VERBOSE, `object`)
    }

    /**
     * Log an array with level [LogLevel.VERBOSE].
     *
     * @param array the array to log
     */
    fun v(array: Array<Any?>?) {
        println(LogLevel.VERBOSE, array)
    }

    /**
     * Log a message with level [LogLevel.VERBOSE].
     *
     * @param format the format of the message to log
     * @param args   the arguments of the message to log
     */
    fun v(format: String, vararg args: Any?) {
        printlnWithFormat(LogLevel.VERBOSE, format, *args)
    }

    /**
     * Log a message with level [LogLevel.VERBOSE].
     *
     * @param msg the message to log
     */
    fun v(msg: String) {
        println(LogLevel.VERBOSE, msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.VERBOSE].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    fun v(msg: String, tr: Throwable) {
        println(LogLevel.VERBOSE, msg, tr)
    }

    /**
     * Log an object with level [LogLevel.DEBUG].
     *
     * @param object the object to log
     * @see Builder.addObjectFormatter
     * @since 1.1.0
     */
    fun d(`object`: Any?) {
        println(LogLevel.DEBUG, `object`)
    }

    /**
     * Log an array with level [LogLevel.DEBUG].
     *
     * @param array the array to log
     */
    fun d(array: Array<Any?>?) {
        println(LogLevel.DEBUG, array)
    }

    /**
     * Log a message with level [LogLevel.DEBUG].
     *
     * @param format the format of the message to log, null if just need to concat arguments
     * @param args   the arguments of the message to log
     */
    fun d(format: String, vararg args: Any?) {
        printlnWithFormat(LogLevel.DEBUG, format, *args)
    }

    /**
     * Log a message with level [LogLevel.DEBUG].
     *
     * @param msg the message to log
     */
    fun d(msg: String) {
        println(LogLevel.DEBUG, msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.DEBUG].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    fun d(msg: String, tr: Throwable) {
        println(LogLevel.DEBUG, msg, tr)
    }

    /**
     * Log an object with level [LogLevel.INFO].
     *
     * @param object the object to log
     * @see Builder.addObjectFormatter
     * @since 1.1.0
     */
    fun i(`object`: Any?) {
        println(LogLevel.INFO, `object`)
    }

    /**
     * Log an array with level [LogLevel.INFO].
     *
     * @param array the array to log
     */
    fun i(array: Array<Any?>?) {
        println(LogLevel.INFO, array)
    }

    /**
     * Log a message with level [LogLevel.INFO].
     *
     * @param format the format of the message to log, null if just need to concat arguments
     * @param args   the arguments of the message to log
     */
    fun i(format: String, vararg args: Any?) {
        printlnWithFormat(LogLevel.INFO, format, *args)
    }

    /**
     * Log a message with level [LogLevel.INFO].
     *
     * @param msg the message to log
     */
    fun i(msg: String) {
        println(LogLevel.INFO, msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.INFO].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    fun i(msg: String, tr: Throwable) {
        println(LogLevel.INFO, msg, tr)
    }

    /**
     * Log an object with level [LogLevel.WARN].
     *
     * @param object the object to log
     * @see Builder.addObjectFormatter
     * @since 1.1.0
     */
    fun w(`object`: Any?) {
        println(LogLevel.WARN, `object`)
    }

    /**
     * Log an array with level [LogLevel.WARN].
     *
     * @param array the array to log
     */
    fun w(array: Array<Any?>?) {
        println(LogLevel.WARN, array)
    }

    /**
     * Log a message with level [LogLevel.WARN].
     *
     * @param format the format of the message to log, null if just need to concat arguments
     * @param args   the arguments of the message to log
     */
    fun w(format: String, vararg args: Any?) {
        printlnWithFormat(LogLevel.WARN, format, *args)
    }

    /**
     * Log a message with level [LogLevel.WARN].
     *
     * @param msg the message to log
     */
    fun w(msg: String) {
        println(LogLevel.WARN, msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.WARN].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    fun w(msg: String, tr: Throwable) {
        println(LogLevel.WARN, msg, tr)
    }

    /**
     * Log an object with level [LogLevel.ERROR].
     *
     * @param object the object to log
     * @see Builder.addObjectFormatter
     * @since 1.1.0
     */
    fun e(`object`: Any?) {
        println(LogLevel.ERROR, `object`)
    }

    /**
     * Log an array with level [LogLevel.ERROR].
     *
     * @param array the array to log
     */
    fun e(array: Array<Any?>?) {
        println(LogLevel.ERROR, array)
    }

    /**
     * Log a message with level [LogLevel.ERROR].
     *
     * @param format the format of the message to log, null if just need to concat arguments
     * @param args   the arguments of the message to log
     */
    fun e(format: String, vararg args: Any?) {
        printlnWithFormat(LogLevel.ERROR, format, *args)
    }

    /**
     * Log a message with level [LogLevel.ERROR].
     *
     * @param msg the message to log
     */
    fun e(msg: String) {
        println(LogLevel.ERROR, msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.ERROR].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    fun e(msg: String, tr: Throwable) {
        println(LogLevel.ERROR, msg, tr)
    }

    /**
     * Log an object with specific log level.
     *
     * @param logLevel the specific log level
     * @param object   the object to log
     * @see Builder.addObjectFormatter
     * @since 1.4.0
     */
    fun log(logLevel: Int, `object`: Any?) {
        println(logLevel, `object`)
    }

    /**
     * Log an array with specific log level.
     *
     * @param logLevel the specific log level
     * @param array    the array to log
     * @since 1.4.0
     */
    fun log(logLevel: Int, array: Array<Any?>?) {
        println(logLevel, array)
    }

    /**
     * Log a message with specific log level.
     *
     * @param logLevel the specific log level
     * @param format   the format of the message to log, null if just need to concat arguments
     * @param args     the arguments of the message to log
     * @since 1.4.0
     */
    fun log(logLevel: Int, format: String, vararg args: Any?) {
        printlnWithFormat(logLevel, format, *args)
    }

    /**
     * Log a message with specific log level.
     *
     * @param logLevel the specific log level
     * @param msg      the message to log
     * @since 1.4.0
     */
    fun log(logLevel: Int, msg: String) {
        println(logLevel, msg)
    }

    /**
     * Log a message and a throwable with specific log level.
     *
     * @param logLevel the specific log level
     * @param msg      the message to log
     * @param tr       the throwable to be log
     * @since 1.4.0
     */
    fun log(logLevel: Int, msg: String, tr: Throwable) {
        println(logLevel, msg, tr)
    }

    /**
     * Log a JSON string, with level [LogLevel.DEBUG] by default.
     *
     * @param json the JSON string to log
     */
    fun json(json: String) {
        if (LogLevel.DEBUG < logConfiguration.logLevel) {
            return
        }
        printlnInternal(LogLevel.DEBUG, logConfiguration.jsonFormatter.format(json))
    }

    /**
     * Log a XML string, with level [LogLevel.DEBUG] by default.
     *
     * @param xml the XML string to log
     */
    fun xml(xml: String) {
        if (LogLevel.DEBUG < logConfiguration.logLevel) {
            return
        }
        printlnInternal(LogLevel.DEBUG, logConfiguration.xmlFormatter.format(xml))
    }

    /**
     * Print an object in a new line.
     *
     * @param logLevel the log level of the printing object
     * @param object   the object to print
     */
    private fun <T> println(logLevel: Int, `object`: T?) {
        if (logLevel < logConfiguration.logLevel) {
            return
        }
        val objectString: String = if (`object` != null) {
            val objectFormatter = logConfiguration.getObjectFormatter(`object`)
            objectFormatter?.format(`object`) ?: `object`.toString()
        } else {
            "null"
        }
        printlnInternal(logLevel, objectString)
    }

    /**
     * Print an array in a new line.
     *
     * @param logLevel the log level of the printing array
     * @param array    the array to print
     */
    private fun println(logLevel: Int, array: Array<Any?>?) {
        if (logLevel < logConfiguration.logLevel) {
            return
        }
        printlnInternal(logLevel, Arrays.deepToString(array))
    }

    /**
     * Print a log in a new line.
     *
     * @param logLevel the log level of the printing log
     * @param format   the format of the printing log, null if just need to concat arguments
     * @param args     the arguments of the printing log
     */
    private fun printlnWithFormat(logLevel: Int, format: String, vararg args: Any?) {
        if (logLevel < logConfiguration.logLevel) {
            return
        }
        printlnInternal(logLevel, formatArgs(format, *args))
    }

    /**
     * Print a log in a new line.
     *
     * @param logLevel the log level of the printing log
     * @param msg      the message you would like to log
     */
    internal fun println(logLevel: Int, msg: String?) {
        if (logLevel < logConfiguration.logLevel) {
            return
        }
        printlnInternal(logLevel, msg ?: "")
    }

    /**
     * Print a log in a new line.
     *
     * @param logLevel the log level of the printing log
     * @param msg      the message you would like to log
     * @param tr       a throwable object to log
     */
    private fun println(logLevel: Int, msg: String?, tr: Throwable) {
        if (logLevel < logConfiguration.logLevel) {
            return
        }
        printlnInternal(
            logLevel, (if (msg.isNullOrEmpty())
                "" else (msg + SystemCompat.lineSeparator))
                    + logConfiguration.throwableFormatter.format(tr)
        )
    }

    /**
     * Print a log in a new line internally.
     *
     * @param logLevel the log level of the printing log
     * @param msg      the message you would like to log
     */
    private fun printlnInternal(logLevel: Int, msg: String) {
        var currentLogLevel = logLevel
        var tag = logConfiguration.tag
        val thread = if (logConfiguration.withThread)
            logConfiguration.threadFormatter.format(Thread.currentThread())
        else null
        val stackTrace = if (logConfiguration.withStackTrace)
            logConfiguration.stackTraceFormatter.format(
                StackTraceUtil.getCroppedRealStackTrack(
                    Throwable().stackTrace,
                    logConfiguration.stackTraceOrigin,
                    logConfiguration.stackTraceDepth
                )
            )
        else null

        var currentThread = thread
        var currentStackTrace = stackTrace
        var currentMsg = msg

        if (logConfiguration.interceptors != null) {
            var log = LogItem(currentLogLevel, tag, currentThread, currentStackTrace, currentMsg)
            for (interceptor in logConfiguration.interceptors!!) {
                log = interceptor.intercept(log)
                if (log == null) {
                    // Log is eaten, don't print this log.
                    return
                }

                // Check if the log still healthy.
                if (log.tag == null || log.msg == null) {
                    Platform.get().error(
                        "Interceptor $interceptor" +
                                " should not remove the tag or message of a log," +
                                " if you don't want to print this log," +
                                " just return a null when intercept."
                    )
                    return
                }
            }

            // Use fields after interception.
            currentLogLevel = log.level
            tag = log.tag
            currentThread = log.threadInfo
            currentStackTrace = log.stackTraceInfo
            currentMsg = log.msg
        }

        printer.println(
            currentLogLevel, tag, if (logConfiguration.withBorder)
                logConfiguration.borderFormatter.format(arrayOf(currentThread ?: "", currentStackTrace ?: "", currentMsg))
            else
                ((if (currentThread != null) (currentThread + SystemCompat.lineSeparator) else "") +
                        (if (currentStackTrace != null) (currentStackTrace + SystemCompat.lineSeparator) else "") +
                        currentMsg)
        )
    }

    /**
     * Format a string with arguments.
     *
     * @param format the format string, null if just to concat the arguments
     * @param args   the arguments
     * @return the formatted string
     */
    private fun formatArgs(format: String?, vararg args: Any?): String {
        return if (format != null) {
            String.format(format, *args)
        } else {
            val sb = StringBuilder()
            for (i in args.indices) {
                if (i != 0) {
                    sb.append(", ")
                }
                sb.append(args[i])
            }
            sb.toString()
        }
    }

    /**
     * Builder for [Logger].
     */
    class Builder {

        /**
         * The log level, the logs below of which would not be printed.
         */
        internal var logLevel: Int = 0

        /**
         * The tag string when [Logger] log.
         */
        internal var tag: String? = null

        /**
         * Whether we should log with thread info.
         */
        internal var withThread: Boolean = false

        /**
         * Whether we have enabled/disabled thread info.
         */
        internal var threadSet: Boolean = false

        /**
         * Whether we should log with stack trace.
         */
        internal var withStackTrace: Boolean = false

        /**
         * The origin of stack trace elements from which we should NOT log when logging with stack trace,
         * it can be a package name like "com.cl.zlog", a class name like "com.yourdomain.logWrapper",
         * or something else between package name and class name, like "com.yourdomain.".
         * <p>
         * It is mostly used when you are using a logger wrapper.
         */
        internal var stackTraceOrigin: String? = null

        /**
         * The number of stack trace elements we should log when logging with stack trace,
         * 0 if no limitation.
         */
        internal var stackTraceDepth: Int = 0

        /**
         * Whether we have enabled/disabled stack trace.
         */
        internal var stackTraceSet: Boolean = false

        /**
         * Whether we should log with border.
         */
        internal var withBorder: Boolean = false

        /**
         * Whether we have enabled/disabled border.
         */
        internal var borderSet: Boolean = false

        /**
         * The JSON formatter when [Logger] log a JSON string.
         */
        internal var jsonFormatter: JsonFormatter? = null

        /**
         * The XML formatter when [Logger] log a XML string.
         */
        internal var xmlFormatter: XmlFormatter? = null

        /**
         * The throwable formatter when [Logger] log a message with throwable.
         */
        internal var throwableFormatter: ThrowableFormatter? = null

        /**
         * The thread formatter when [Logger] logging.
         */
        internal var threadFormatter: ThreadFormatter? = null

        /**
         * The stack trace formatter when [Logger] logging.
         */
        internal var stackTraceFormatter: StackTraceFormatter? = null

        /**
         * The border formatter when [Logger] logging.
         */
        internal var borderFormatter: BorderFormatter? = null

        /**
         * The object formatters, used when [Logger] logging an object.
         */
        internal var objectFormatters: MutableMap<Class<*>, ObjectFormatter<*>>? = null

        /**
         * The interceptors, used when [Logger] logging.
         */
        internal var interceptors: MutableList<Interceptor>? = null

        /**
         * The printer used to print the logs.
         */
        internal var printer: Printer? = null

        /**
         * Construct a builder.
         */
        init {
            ZLog.assertInitialization()
        }

        /**
         * Set the log level.
         *
         * @param logLevel the log level to set
         * @return the builder
         */
        fun logLevel(logLevel: Int): Builder {
            this.logLevel = logLevel
            return this
        }

        /**
         * Set the tag.
         *
         * @param tag the tag to set
         * @return the builder
         */
        fun tag(tag: String): Builder {
            this.tag = tag
            return this
        }

        /**
         * Enable thread info.
         *
         * @return the builder
         * @deprecated use [enableThreadInfo] instead, since 1.7.0
         */
        @Deprecated("use enableThreadInfo() instead, since 1.7.0")
        fun t(): Builder {
            return enableThreadInfo()
        }

        /**
         * Enable thread info, the thread info would be printed with the log message.
         *
         * @return the builder
         * @see ThreadFormatter
         * @since 1.7.0
         */
        fun enableThreadInfo(): Builder {
            withThread = true
            threadSet = true
            return this
        }

        /**
         * Disable thread info.
         *
         * @return the builder
         * @deprecated use [disableThreadInfo] instead, since 1.7.0
         */
        @Deprecated("use disableThreadInfo() instead, since 1.7.0")
        fun nt(): Builder {
            return disableThreadInfo()
        }

        /**
         * Disable thread info, the thread info won't be printed with the log message.
         *
         * @return the builder
         * @since 1.7.0
         */
        fun disableThreadInfo(): Builder {
            withThread = false
            threadSet = true
            return this
        }

        /**
         * Enable stack trace.
         *
         * @param depth the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @deprecated use [enableStackTrace] instead, since 1.7.0
         */
        @Deprecated("use enableStackTrace(int) instead, since 1.7.0")
        fun st(depth: Int): Builder {
            return enableStackTrace(depth)
        }

        /**
         * Enable stack trace, the stack trace would be printed with the log message.
         *
         * @param depth the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @see StackTraceFormatter
         * @since 1.7.0
         */
        fun enableStackTrace(depth: Int): Builder {
            withStackTrace = true
            stackTraceDepth = depth
            stackTraceSet = true
            return this
        }

        /**
         * Enable stack trace.
         *
         * @param stackTraceOrigin the origin of stack trace elements from which we should NOT log,
         *                         it can be a package name like "com.cl.zlog", a class name
         *                         like "com.yourdomain.logWrapper", or something else between
         *                         package name and class name, like "com.yourdomain.".
         *                         It is mostly used when you are using a logger wrapper
         * @param depth            the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @since 1.4.0
         * @deprecated use [enableStackTrace] instead, since 1.7.0
         */
        @Deprecated("use enableStackTrace(String, int) instead, since 1.7.0")
        fun st(stackTraceOrigin: String, depth: Int): Builder {
            return enableStackTrace(stackTraceOrigin, depth)
        }

        /**
         * Enable stack trace, the stack trace would be printed with the log message.
         *
         * @param stackTraceOrigin the origin of stack trace elements from which we should NOT log,
         *                         it can be a package name like "com.cl.zlog", a class name
         *                         like "com.yourdomain.logWrapper", or something else between
         *                         package name and class name, like "com.yourdomain.".
         *                         It is mostly used when you are using a logger wrapper
         * @param depth            the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @see StackTraceFormatter
         * @since 1.7.0
         */
        fun enableStackTrace(stackTraceOrigin: String?, depth: Int): Builder {
            withStackTrace = true
            this.stackTraceOrigin = stackTraceOrigin
            stackTraceDepth = depth
            stackTraceSet = true
            return this
        }

        /**
         * Disable stack trace.
         *
         * @return the builder
         * @deprecated use [disableStackTrace] instead, since 1.7.0
         */
        @Deprecated("use disableStackTrace() instead, since 1.7.0")
        fun nst(): Builder {
            return disableStackTrace()
        }

        /**
         * Disable stack trace, the stack trace won't be printed with the log message.
         *
         * @return the builder
         * @since 1.7.0
         */
        fun disableStackTrace(): Builder {
            withStackTrace = false
            stackTraceOrigin = null
            stackTraceDepth = 0
            stackTraceSet = true
            return this
        }

        /**
         * Enable border.
         *
         * @return the builder
         * @deprecated use [enableBorder] instead, since 1.7.0
         */
        @Deprecated("use enableBorder() instead, since 1.7.0")
        fun b(): Builder {
            return enableBorder()
        }

        /**
         * Enable border, the border would surround the entire log content, and separate the log
         * message, thread info and stack trace.
         *
         * @return the builder
         * @see BorderFormatter
         * @since 1.7.0
         */
        fun enableBorder(): Builder {
            withBorder = true
            borderSet = true
            return this
        }

        /**
         * Disable border.
         *
         * @return the builder
         * @deprecated use [disableBorder] instead, since 1.7.0
         */
        @Deprecated("use disableBorder() instead, since 1.7.0")
        fun nb(): Builder {
            return disableBorder()
        }

        /**
         * Disable border, the log content won't be surrounded by a border.
         *
         * @return the builder
         * @since 1.7.0
         */
        fun disableBorder(): Builder {
            withBorder = false
            borderSet = true
            return this
        }

        /**
         * Set the [JsonFormatter].
         *
         * @param jsonFormatter the [JsonFormatter] to set
         * @return the builder
         */
        fun jsonFormatter(jsonFormatter: JsonFormatter): Builder {
            this.jsonFormatter = jsonFormatter
            return this
        }

        /**
         * Set the [XmlFormatter].
         *
         * @param xmlFormatter the [XmlFormatter] to set
         * @return the builder
         */
        fun xmlFormatter(xmlFormatter: XmlFormatter): Builder {
            this.xmlFormatter = xmlFormatter
            return this
        }

        /**
         * Set the [ThrowableFormatter].
         *
         * @param throwableFormatter the [ThrowableFormatter] to set
         * @return the builder
         */
        fun throwableFormatter(throwableFormatter: ThrowableFormatter): Builder {
            this.throwableFormatter = throwableFormatter
            return this
        }

        /**
         * Set the [ThreadFormatter].
         *
         * @param threadFormatter the [ThreadFormatter] to set
         * @return the builder
         */
        fun threadFormatter(threadFormatter: ThreadFormatter): Builder {
            this.threadFormatter = threadFormatter
            return this
        }

        /**
         * Set the [StackTraceFormatter].
         *
         * @param stackTraceFormatter the [StackTraceFormatter] to set
         * @return the builder
         */
        fun stackTraceFormatter(stackTraceFormatter: StackTraceFormatter): Builder {
            this.stackTraceFormatter = stackTraceFormatter
            return this
        }

        /**
         * Set the [BorderFormatter].
         *
         * @param borderFormatter the [BorderFormatter] to set
         * @return the builder
         */
        fun borderFormatter(borderFormatter: BorderFormatter): Builder {
            this.borderFormatter = borderFormatter
            return this
        }

        /**
         * Add an object formatter for specific class of object.
         *
         * @param objectClass     the class of object
         * @param objectFormatter the object formatter to add
         * @param T             the type of object
         * @return the builder
         * @since 1.1.0
         */
        fun <T> addObjectFormatter(
            objectClass: Class<T>,
            objectFormatter: ObjectFormatter<in T>
        ): Builder {
            if (objectFormatters == null) {
                objectFormatters = HashMap()
            }
            objectFormatters!![objectClass] = objectFormatter
            return this
        }

        /**
         * Add an interceptor.
         *
         * @param interceptor the interceptor to add
         * @return the builder
         * @since 1.3.0
         */
        fun addInterceptor(interceptor: Interceptor): Builder {
            if (interceptors == null) {
                interceptors = ArrayList()
            }
            interceptors!!.add(interceptor)
            return this
        }

        /**
         * Set the [Printer] array.
         *
         * @param printers the [Printer] array to set
         * @return the builder
         */
        fun printers(vararg printers: Printer): Builder {
            printer = if (printers.size == 1) {
                printers[0]
            } else {
                PrinterSet(*printers)
            }
            return this
        }

        /**
         * Log an object with level [LogLevel.VERBOSE].
         *
         * @param object the object to log
         */
        fun v(`object`: Any?) {
            build().v(`object`)
        }

        /**
         * Log an array with level [LogLevel.VERBOSE].
         *
         * @param array the array to log
         */
        fun v(array: Array<Any?>?) {
            build().v(array)
        }

        /**
         * Log a message with level [LogLevel.VERBOSE].
         *
         * @param format the format of the message to log
         * @param args   the arguments of the message to log
         */
        fun v(format: String, vararg args: Any?) {
            build().v(format, *args)
        }

        /**
         * Log a message with level [LogLevel.VERBOSE].
         *
         * @param msg the message to log
         */
        fun v(msg: String) {
            build().v(msg)
        }

        /**
         * Log a message and a throwable with level [LogLevel.VERBOSE].
         *
         * @param msg the message to log
         * @param tr  the throwable to be log
         */
        fun v(msg: String, tr: Throwable) {
            build().v(msg, tr)
        }

        /**
         * Log an object with level [LogLevel.DEBUG].
         *
         * @param object the object to log
         */
        fun d(`object`: Any?) {
            build().d(`object`)
        }

        /**
         * Log an array with level [LogLevel.DEBUG].
         *
         * @param array the array to log
         */
        fun d(array: Array<Any?>?) {
            build().d(array)
        }

        /**
         * Log a message with level [LogLevel.DEBUG].
         *
         * @param format the format of the message to log
         * @param args   the arguments of the message to log
         */
        fun d(format: String, vararg args: Any?) {
            build().d(format, *args)
        }

        /**
         * Log a message with level [LogLevel.DEBUG].
         *
         * @param msg the message to log
         */
        fun d(msg: String) {
            build().d(msg)
        }

        /**
         * Log a message and a throwable with level [LogLevel.DEBUG].
         *
         * @param msg the message to log
         * @param tr  the throwable to be log
         */
        fun d(msg: String, tr: Throwable) {
            build().d(msg, tr)
        }

        /**
         * Log an object with level [LogLevel.INFO].
         *
         * @param object the object to log
         */
        fun i(`object`: Any?) {
            build().i(`object`)
        }

        /**
         * Log an array with level [LogLevel.INFO].
         *
         * @param array the array to log
         */
        fun i(array: Array<Any?>?) {
            build().i(array)
        }

        /**
         * Log a message with level [LogLevel.INFO].
         *
         * @param format the format of the message to log
         * @param args   the arguments of the message to log
         */
        fun i(format: String, vararg args: Any?) {
            build().i(format, *args)
        }

        /**
         * Log a message with level [LogLevel.INFO].
         *
         * @param msg the message to log
         */
        fun i(msg: String) {
            build().i(msg)
        }

        /**
         * Log a message and a throwable with level [LogLevel.INFO].
         *
         * @param msg the message to log
         * @param tr  the throwable to be log
         */
        fun i(msg: String, tr: Throwable) {
            build().i(msg, tr)
        }

        /**
         * Log an object with level [LogLevel.WARN].
         *
         * @param object the object to log
         */
        fun w(`object`: Any?) {
            build().w(`object`)
        }

        /**
         * Log an array with level [LogLevel.WARN].
         *
         * @param array the array to log
         */
        fun w(array: Array<Any?>?) {
            build().w(array)
        }

        /**
         * Log a message with level [LogLevel.WARN].
         *
         * @param format the format of the message to log
         * @param args   the arguments of the message to log
         */
        fun w(format: String, vararg args: Any?) {
            build().w(format, *args)
        }

        /**
         * Log a message with level [LogLevel.WARN].
         *
         * @param msg the message to log
         */
        fun w(msg: String) {
            build().w(msg)
        }

        /**
         * Log a message and a throwable with level [LogLevel.WARN].
         *
         * @param msg the message to log
         * @param tr  the throwable to be log
         */
        fun w(msg: String, tr: Throwable) {
            build().w(msg, tr)
        }

        /**
         * Log an object with level [LogLevel.ERROR].
         *
         * @param object the object to log
         */
        fun e(`object`: Any?) {
            build().e(`object`)
        }

        /**
         * Log an array with level [LogLevel.ERROR].
         *
         * @param array the array to log
         */
        fun e(array: Array<Any?>?) {
            build().e(array)
        }

        /**
         * Log a message with level [LogLevel.ERROR].
         *
         * @param format the format of the message to log
         * @param args   the arguments of the message to log
         */
        fun e(format: String, vararg args: Any?) {
            build().e(format, *args)
        }

        /**
         * Log a message with level [LogLevel.ERROR].
         *
         * @param msg the message to log
         */
        fun e(msg: String) {
            build().e(msg)
        }

        /**
         * Log a message and a throwable with level [LogLevel.ERROR].
         *
         * @param msg the message to log
         * @param tr  the throwable to be log
         */
        fun e(msg: String, tr: Throwable) {
            build().e(msg, tr)
        }

        /**
         * Log an object with specific log level.
         *
         * @param logLevel the specific log level
         * @param object   the object to log
         */
        fun log(logLevel: Int, `object`: Any?) {
            build().log(logLevel, `object`)
        }

        /**
         * Log an array with specific log level.
         *
         * @param logLevel the specific log level
         * @param array    the array to log
         */
        fun log(logLevel: Int, array: Array<Any?>?) {
            build().log(logLevel, array)
        }

        /**
         * Log a message with specific log level.
         *
         * @param logLevel the specific log level
         * @param format   the format of the message to log
         * @param args     the arguments of the message to log
         */
        fun log(logLevel: Int, format: String, vararg args: Any?) {
            build().log(logLevel, format, *args)
        }

        /**
         * Log a message with specific log level.
         *
         * @param logLevel the specific log level
         * @param msg      the message to log
         */
        fun log(logLevel: Int, msg: String) {
            build().log(logLevel, msg)
        }

        /**
         * Log a message and a throwable with specific log level.
         *
         * @param logLevel the specific log level
         * @param msg      the message to log
         * @param tr       the throwable to be log
         */
        fun log(logLevel: Int, msg: String, tr: Throwable) {
            build().log(logLevel, msg, tr)
        }

        /**
         * Log a JSON string, with level [LogLevel.DEBUG] by default.
         *
         * @param json the JSON string to log
         */
        fun json(json: String) {
            build().json(json)
        }

        /**
         * Log a XML string, with level [LogLevel.DEBUG] by default.
         *
         * @param xml the XML string to log
         */
        fun xml(xml: String) {
            build().xml(xml)
        }

        /**
         * Build the [Logger].
         *
         * @return the built logger
         */
        fun build(): Logger {
            return Logger(this)
        }
    }
}