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

package com.cl.zlog

import android.app.Application
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
import com.cl.zlog.internal.util.StackTraceUtil
import com.cl.zlog.printer.Printer
import com.cl.zlog.printer.PrinterSet

/**
 * A log tool which can be used in android or java, the most important feature is it can print the
 * logs to multiple place in the same time, such as android shell, console and file, you can
 * even print the log to the remote server if you want, all of these can be done just within one
 * calling.
 * <br>Also, ZLog is very flexible, almost every component is replaceable.
 * <p>
 * <b>How to use in a general way:</b>
 * <p>
 * <b>1. Initial the log system.</b>
 * <br>Using one of
 * <br>[ZLog.init]
 * <br>[ZLog.init],
 * <br>[ZLog.init],
 * <br>[ZLog.init],
 * <br>[ZLog.init],
 * <br>[ZLog.init],
 * <br>that will setup a [Logger] for a global usage.
 * If you want to use a customized configuration instead of the global one to log something, you can
 * start a customization logging.
 * <p>
 * For android, a best place to do the initialization is [Application.onCreate].
 * <p>
 * <b>2. Start to log.</b>
 * <br>[v], [v] and [v] are for
 * logging a [LogLevel.VERBOSE] message.
 * <br>[d], [d] and [d] are for
 * logging a [LogLevel.DEBUG] message.
 * <br>[i], [i] and [i] are for
 * logging a [LogLevel.INFO] message.
 * <br>[w], [w] and [w] are for
 * logging a [LogLevel.WARN] message.
 * <br>[e], [e] and [e] are for
 * logging a [LogLevel.ERROR] message.
 * <br>[log], [log] and
 * [log] are for logging a specific level message.
 * <br>[json] is for logging a [LogLevel.DEBUG] JSON string.
 * <br>[xml] is for logging a [LogLevel.DEBUG] XML string.
 * <br> Also, you can directly log any object with specific log level, like [v],
 * and any object array with specific log level, like [v].
 * <p>
 * <b>How to use in a dynamically customizing way after initializing the log system:</b>
 * <p>
 * <b>1. Start a customization.</b>
 * <br>Call any of
 * <br>[logLevel]
 * <br>[tag],
 * <br>[enableThreadInfo],
 * <br>[disableThreadInfo],
 * <br>[enableStackTrace],
 * <br>[enableStackTrace],
 * <br>[disableStackTrace],
 * <br>[enableBorder],
 * <br>[disableBorder],
 * <br>[jsonFormatter],
 * <br>[xmlFormatter],
 * <br>[threadFormatter],
 * <br>[stackTraceFormatter],
 * <br>[throwableFormatter]
 * <br>[borderFormatter]
 * <br>[addObjectFormatter]
 * <br>[addInterceptor]
 * <br>[printers],
 * <br>it will return a [Logger.Builder] object.
 * <p>
 * <b>2. Finish the customization.</b>
 * <br>Continue to setup other fields of the returned [Logger.Builder].
 * <p>
 * <b>3. Build a dynamically generated [Logger].</b>
 * <br>Call the [Logger.Builder.build] of the returned [Logger.Builder].
 * <p>
 * <b>4. Start to log.</b>
 * <br>The logging methods of a [Logger] is completely same as that ones in [ZLog].
 * <br>As a convenience, you can ignore the step 3, just call the logging methods of
 * [Logger.Builder], it will automatically build a [Logger] and call the target
 * logging method.
 * <p>
 * <b>Compatibility:</b>
 * <p>
 * In order to be compatible with [android.util.Log], all the methods of
 * [android.util.Log] are supported here.
 * See:
 * <br>[Log.v], [Log.v]
 * <br>[Log.d], [Log.d]
 * <br>[Log.i], [Log.i]
 * <br>[Log.w], [Log.w]
 * <br>[Log.wtf], [Log.wtf]
 * <br>[Log.e], [Log.e]
 * <br>[Log.println]
 * <br>[Log.isLoggable]
 * <br>[Log.getStackTraceString]
 * <p>
 */
object ZLog {

    /**
     * Global logger for all direct logging via [ZLog].
     */
    private var sLogger: Logger? = null

    /**
     * Global log configuration.
     */
    @JvmField
    var sLogConfiguration: LogConfiguration? = null

    /**
     * Global log printer.
     */
    internal var sPrinter: Printer? = null

    @JvmField
    var sIsInitialized = false

    /**
     * Initialize log system, should be called only once.
     *
     * @since 1.3.0
     */
    @JvmStatic
    fun init() {
        init(LogConfiguration.Builder().build(), DefaultsFactory.createPrinter())
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel the log level, logs with a lower level than which would not be printed
     */
    @JvmStatic
    fun init(logLevel: Int) {
        init(
            LogConfiguration.Builder().logLevel(logLevel).build(),
            DefaultsFactory.createPrinter()
        )
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel         the log level, logs with a lower level than which would not be printed
     * @param logConfiguration the log configuration
     * @deprecated the log level is part of log configuration now, use [init] instead, since 1.3.0
     */
    @Deprecated("the log level is part of log configuration now, use init(LogConfiguration) instead, since 1.3.0")
    @JvmStatic
    fun init(logLevel: Int, logConfiguration: LogConfiguration) {
        init(LogConfiguration.Builder(logConfiguration).logLevel(logLevel).build())
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logConfiguration the log configuration
     * @since 1.3.0
     */
    @JvmStatic
    fun init(logConfiguration: LogConfiguration) {
        init(logConfiguration, DefaultsFactory.createPrinter())
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param printers the printers, each log would be printed by all of the printers
     * @since 1.3.0
     */
    @JvmStatic
    fun init(vararg printers: Printer) {
        init(LogConfiguration.Builder().build(), *printers)
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel the log level, logs with a lower level than which would not be printed
     * @param printers the printers, each log would be printed by all of the printers
     */
    @JvmStatic
    fun init(logLevel: Int, vararg printers: Printer) {
        init(LogConfiguration.Builder().logLevel(logLevel).build(), *printers)
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logLevel         the log level, logs with a lower level than which would not be printed
     * @param logConfiguration the log configuration
     * @param printers         the printers, each log would be printed by all of the printers
     * @deprecated the log level is part of log configuration now,
     * use [init] instead, since 1.3.0
     */
    @Deprecated("the log level is part of log configuration now, use init(LogConfiguration, vararg Printer) instead, since 1.3.0")
    @JvmStatic
    fun init(logLevel: Int, logConfiguration: LogConfiguration, vararg printers: Printer) {
        init(LogConfiguration.Builder(logConfiguration).logLevel(logLevel).build(), *printers)
    }

    /**
     * Initialize log system, should be called only once.
     *
     * @param logConfiguration the log configuration
     * @param printers         the printers, each log would be printed by all of the printers
     * @since 1.3.0
     */
    @JvmStatic
    fun init(logConfiguration: LogConfiguration, vararg printers: Printer) {
        if (sIsInitialized) {
            Platform.get().warn("ZLog is already initialized, do not initialize again")
        }
        sIsInitialized = true

        sLogConfiguration = logConfiguration
        sPrinter = PrinterSet(*printers)
        sLogger = Logger(sLogConfiguration!!, sPrinter!!)
    }

    /**
     * Throw an IllegalStateException if not initialized.
     */
    @JvmStatic
    fun assertInitialization() {
        if (!sIsInitialized) {
            throw IllegalStateException("Do you forget to initialize ZLog?")
        }
    }

    /**
     * Start to customize a [Logger] and set the log level.
     *
     * @param logLevel the log level to customize
     * @return the [Logger.Builder] to build the [Logger]
     * @since 1.3.0
     */
    @JvmStatic
    fun logLevel(logLevel: Int): Logger.Builder {
        return Logger.Builder().logLevel(logLevel)
    }

    /**
     * Start to customize a [Logger] and set the tag.
     *
     * @param tag the tag to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun tag(tag: String): Logger.Builder {
        return Logger.Builder().tag(tag)
    }

    /**
     * Start to customize a [Logger] and enable thread info.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @deprecated use [enableThreadInfo] instead, since 1.7.0
     */
    @Deprecated("use enableThreadInfo() instead, since 1.7.0")
    @JvmStatic
    fun t(): Logger.Builder {
        return enableThreadInfo()
    }

    /**
     * Start to customize a [Logger] and enable thread info, the thread info would be printed
     * with the log message.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @see ThreadFormatter
     * @since 1.7.0
     */
    @JvmStatic
    fun enableThreadInfo(): Logger.Builder {
        return Logger.Builder().enableThreadInfo()
    }

    /**
     * Start to customize a [Logger] and disable thread info.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @deprecated use [disableThreadInfo] instead, since 1.7.0
     */
    @Deprecated("use disableThreadInfo() instead, since 1.7.0")
    @JvmStatic
    fun nt(): Logger.Builder {
        return disableThreadInfo()
    }

    /**
     * Start to customize a [Logger] and disable thread info, the thread info won't be printed
     * with the log message.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @since 1.7.0
     */
    @JvmStatic
    fun disableThreadInfo(): Logger.Builder {
        return Logger.Builder().disableThreadInfo()
    }

    /**
     * Start to customize a [Logger] and enable stack trace.
     *
     * @param depth the number of stack trace elements we should log, 0 if no limitation
     * @return the [Logger.Builder] to build the [Logger]
     * @deprecated use [enableStackTrace] instead, since 1.7.0
     */
    @Deprecated("use enableStackTrace(int) instead, since 1.7.0")
    @JvmStatic
    fun st(depth: Int): Logger.Builder {
        return enableStackTrace(depth)
    }

    /**
     * Start to customize a [Logger] and enable stack trace, the stack trace would be printed
     * with the log message.
     *
     * @param depth the number of stack trace elements we should log, 0 if no limitation
     * @return the [Logger.Builder] to build the [Logger]
     * @see StackTraceFormatter
     * @since 1.7.0
     */
    @JvmStatic
    fun enableStackTrace(depth: Int): Logger.Builder {
        return Logger.Builder().enableStackTrace(depth)
    }

    /**
     * Start to customize a [Logger] and enable stack trace.
     *
     * @param stackTraceOrigin the origin of stack trace elements from which we should NOT log,
     *                         it can be a package name like "com.cl.zlog", a class name
     *                         like "com.yourdomain.logWrapper", or something else between
     *                         package name and class name, like "com.yourdomain.".
     *                         It is mostly used when you are using a logger wrapper
     * @param depth            the number of stack trace elements we should log, 0 if no limitation
     * @return the [Logger.Builder] to build the [Logger]
     * @since 1.4.0
     * @deprecated use [enableStackTrace] instead, since 1.7.0
     */
    @Deprecated("use enableStackTrace(String, int) instead, since 1.7.0")
    @JvmStatic
    fun st(stackTraceOrigin: String, depth: Int): Logger.Builder {
        return enableStackTrace(stackTraceOrigin, depth)
    }

    /**
     * Start to customize a [Logger] and enable stack trace, the stack trace would be printed
     * with the log message.
     *
     * @param stackTraceOrigin the origin of stack trace elements from which we should NOT log,
     *                         it can be a package name like "com.cl.zlog", a class name
     *                         like "com.yourdomain.logWrapper", or something else between
     *                         package name and class name, like "com.yourdomain.".
     *                         It is mostly used when you are using a logger wrapper
     * @param depth            the number of stack trace elements we should log, 0 if no limitation
     * @return the [Logger.Builder] to build the [Logger]
     * @see StackTraceFormatter
     * @since 1.7.0
     */
    @JvmStatic
    fun enableStackTrace(stackTraceOrigin: String, depth: Int): Logger.Builder {
        return Logger.Builder().enableStackTrace(stackTraceOrigin, depth)
    }

    /**
     * Start to customize a [Logger] and disable stack trace.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @deprecated use [disableStackTrace] instead, since 1.7.0
     */
    @Deprecated("use disableStackTrace() instead, since 1.7.0")
    @JvmStatic
    fun nst(): Logger.Builder {
        return disableStackTrace()
    }

    /**
     * Start to customize a [Logger] and disable stack trace, the stack trace won't be printed
     * with the log message.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @since 1.7.0
     */
    @JvmStatic
    fun disableStackTrace(): Logger.Builder {
        return Logger.Builder().disableStackTrace()
    }

    /**
     * Start to customize a [Logger] and enable border.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @deprecated use [enableBorder] instead, since 1.7.0
     */
    @Deprecated("use enableBorder() instead, since 1.7.0")
    @JvmStatic
    fun b(): Logger.Builder {
        return enableBorder()
    }

    /**
     * Start to customize a [Logger] and enable border, the border would surround the entire log
     * content, and separate the log message, thread info and stack trace.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @see BorderFormatter
     * @since 1.7.0
     */
    @JvmStatic
    fun enableBorder(): Logger.Builder {
        return Logger.Builder().enableBorder()
    }

    /**
     * Start to customize a [Logger] and disable border.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @deprecated use [disableBorder] instead, since 1.7.0
     */
    @Deprecated("use disableBorder() instead, since 1.7.0")
    @JvmStatic
    fun nb(): Logger.Builder {
        return disableBorder()
    }

    /**
     * Start to customize a [Logger] and disable border, the log content won't be surrounded
     * by a border.
     *
     * @return the [Logger.Builder] to build the [Logger]
     * @since 1.7.0
     */
    @JvmStatic
    fun disableBorder(): Logger.Builder {
        return Logger.Builder().disableBorder()
    }

    /**
     * Start to customize a [Logger] and set the [JsonFormatter].
     *
     * @param jsonFormatter the [JsonFormatter] to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun jsonFormatter(jsonFormatter: JsonFormatter): Logger.Builder {
        return Logger.Builder().jsonFormatter(jsonFormatter)
    }

    /**
     * Start to customize a [Logger] and set the [XmlFormatter].
     *
     * @param xmlFormatter the [XmlFormatter] to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun xmlFormatter(xmlFormatter: XmlFormatter): Logger.Builder {
        return Logger.Builder().xmlFormatter(xmlFormatter)
    }

    /**
     * Start to customize a [Logger] and set the [ThrowableFormatter].
     *
     * @param throwableFormatter the [ThrowableFormatter] to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun throwableFormatter(throwableFormatter: ThrowableFormatter): Logger.Builder {
        return Logger.Builder().throwableFormatter(throwableFormatter)
    }

    /**
     * Start to customize a [Logger] and set the [ThreadFormatter].
     *
     * @param threadFormatter the [ThreadFormatter] to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun threadFormatter(threadFormatter: ThreadFormatter): Logger.Builder {
        return Logger.Builder().threadFormatter(threadFormatter)
    }

    /**
     * Start to customize a [Logger] and set the [StackTraceFormatter].
     *
     * @param stackTraceFormatter the [StackTraceFormatter] to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun stackTraceFormatter(stackTraceFormatter: StackTraceFormatter): Logger.Builder {
        return Logger.Builder().stackTraceFormatter(stackTraceFormatter)
    }

    /**
     * Start to customize a [Logger] and set the [BorderFormatter].
     *
     * @param borderFormatter the [BorderFormatter] to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun borderFormatter(borderFormatter: BorderFormatter): Logger.Builder {
        return Logger.Builder().borderFormatter(borderFormatter)
    }

    /**
     * Start to customize a [Logger] and add an object formatter for specific class of object.
     *
     * @param objectClass     the class of object
     * @param objectFormatter the object formatter to add
     * @param T             the type of object
     * @return the [Logger.Builder] to build the [Logger]
     * @since 1.1.0
     */
    @JvmStatic
    fun <T> addObjectFormatter(
        objectClass: Class<T>,
        objectFormatter: ObjectFormatter<in T>
    ): Logger.Builder {
        return Logger.Builder().addObjectFormatter(objectClass, objectFormatter)
    }

    /**
     * Start to customize a [Logger] and add an interceptor.
     *
     * @param interceptor the interceptor to add
     * @return the [Logger.Builder] to build the [Logger]
     * @since 1.3.0
     */
    @JvmStatic
    fun addInterceptor(interceptor: Interceptor): Logger.Builder {
        return Logger.Builder().addInterceptor(interceptor)
    }

    /**
     * Start to customize a [Logger] and set the [Printer] array.
     *
     * @param printers the [Printer] array to customize
     * @return the [Logger.Builder] to build the [Logger]
     */
    @JvmStatic
    fun printers(vararg printers: Printer): Logger.Builder {
        return Logger.Builder().printers(*printers)
    }

    /**
     * Log an object with level [LogLevel.VERBOSE].
     *
     * @param object the object to log
     */
    @JvmStatic
    fun v(`object`: Any?) {
        assertInitialization()
        sLogger!!.v(`object`)
    }

    /**
     * Log an object array with level [LogLevel.VERBOSE].
     *
     * @param array the object array to log
     */
    @JvmStatic
    fun v(array: Array<Any?>?) {
        assertInitialization()
        sLogger!!.v(array)
    }

    /**
     * Log a message with level [LogLevel.VERBOSE].
     *
     * @param format the format of the message to log
     * @param args   the arguments of the message to log
     */
    @JvmStatic
    fun v(format: String, vararg args: Any?) {
        assertInitialization()
        sLogger!!.v(format, *args)
    }

    /**
     * Log a message with level [LogLevel.VERBOSE].
     *
     * @param msg the message to log
     */
    @JvmStatic
    fun v(msg: String) {
        assertInitialization()
        sLogger!!.v(msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.VERBOSE].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    @JvmStatic
    fun v(msg: String, tr: Throwable) {
        assertInitialization()
        sLogger!!.v(msg, tr)
    }

    /**
     * Log an object with level [LogLevel.DEBUG].
     *
     * @param object the object to log
     */
    @JvmStatic
    fun d(`object`: Any?) {
        assertInitialization()
        sLogger!!.d(`object`)
    }

    /**
     * Log an object array with level [LogLevel.DEBUG].
     *
     * @param array the object array to log
     */
    @JvmStatic
    fun d(array: Array<Any?>?) {
        assertInitialization()
        sLogger!!.d(array)
    }

    /**
     * Log a message with level [LogLevel.DEBUG].
     *
     * @param format the format of the message to log
     * @param args   the arguments of the message to log
     */
    @JvmStatic
    fun d(format: String, vararg args: Any?) {
        assertInitialization()
        sLogger!!.d(format, *args)
    }

    /**
     * Log a message with level [LogLevel.DEBUG].
     *
     * @param msg the message to log
     */
    @JvmStatic
    fun d(msg: String) {
        assertInitialization()
        sLogger!!.d(msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.DEBUG].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    @JvmStatic
    fun d(msg: String, tr: Throwable) {
        assertInitialization()
        sLogger!!.d(msg, tr)
    }

    /**
     * Log an object with level [LogLevel.INFO].
     *
     * @param object the object to log
     */
    @JvmStatic
    fun i(`object`: Any?) {
        assertInitialization()
        sLogger!!.i(`object`)
    }

    /**
     * Log an object array with level [LogLevel.INFO].
     *
     * @param array the object array to log
     */
    @JvmStatic
    fun i(array: Array<Any?>?) {
        assertInitialization()
        sLogger!!.i(array)
    }

    /**
     * Log a message with level [LogLevel.INFO].
     *
     * @param format the format of the message to log
     * @param args   the arguments of the message to log
     */
    @JvmStatic
    fun i(format: String, vararg args: Any?) {
        assertInitialization()
        sLogger!!.i(format, *args)
    }

    /**
     * Log a message with level [LogLevel.INFO].
     *
     * @param msg the message to log
     */
    @JvmStatic
    fun i(msg: String) {
        assertInitialization()
        sLogger!!.i(msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.INFO].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    @JvmStatic
    fun i(msg: String, tr: Throwable) {
        assertInitialization()
        sLogger!!.i(msg, tr)
    }

    /**
     * Log an object with level [LogLevel.WARN].
     *
     * @param object the object to log
     */
    @JvmStatic
    fun w(`object`: Any?) {
        assertInitialization()
        sLogger!!.w(`object`)
    }

    /**
     * Log an object array with level [LogLevel.WARN].
     *
     * @param array the object array to log
     */
    @JvmStatic
    fun w(array: Array<Any?>?) {
        assertInitialization()
        sLogger!!.w(array)
    }

    /**
     * Log a message with level [LogLevel.WARN].
     *
     * @param format the format of the message to log
     * @param args   the arguments of the message to log
     */
    @JvmStatic
    fun w(format: String, vararg args: Any?) {
        assertInitialization()
        sLogger!!.w(format, *args)
    }

    /**
     * Log a message with level [LogLevel.WARN].
     *
     * @param msg the message to log
     */
    @JvmStatic
    fun w(msg: String) {
        assertInitialization()
        sLogger!!.w(msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.WARN].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    @JvmStatic
    fun w(msg: String, tr: Throwable) {
        assertInitialization()
        sLogger!!.w(msg, tr)
    }

    /**
     * Log an object with level [LogLevel.ERROR].
     *
     * @param object the object to log
     */
    @JvmStatic
    fun e(`object`: Any?) {
        assertInitialization()
        sLogger!!.e(`object`)
    }

    /**
     * Log an object array with level [LogLevel.ERROR].
     *
     * @param array the object array to log
     */
    @JvmStatic
    fun e(array: Array<Any?>?) {
        assertInitialization()
        sLogger!!.e(array)
    }

    /**
     * Log a message with level [LogLevel.ERROR].
     *
     * @param format the format of the message to log
     * @param args   the arguments of the message to log
     */
    @JvmStatic
    fun e(format: String, vararg args: Any?) {
        assertInitialization()
        sLogger!!.e(format, *args)
    }

    /**
     * Log a message with level [LogLevel.ERROR].
     *
     * @param msg the message to log
     */
    @JvmStatic
    fun e(msg: String) {
        assertInitialization()
        sLogger!!.e(msg)
    }

    /**
     * Log a message and a throwable with level [LogLevel.ERROR].
     *
     * @param msg the message to log
     * @param tr  the throwable to be log
     */
    @JvmStatic
    fun e(msg: String, tr: Throwable) {
        assertInitialization()
        sLogger!!.e(msg, tr)
    }

    /**
     * Log an object with specific log level.
     *
     * @param logLevel the specific log level
     * @param object   the object to log
     */
    @JvmStatic
    fun log(logLevel: Int, `object`: Any?) {
        assertInitialization()
        sLogger!!.log(logLevel, `object`)
    }

    /**
     * Log an object array with specific log level.
     *
     * @param logLevel the specific log level
     * @param array    the object array to log
     */
    @JvmStatic
    fun log(logLevel: Int, array: Array<Any?>?) {
        assertInitialization()
        sLogger!!.log(logLevel, array)
    }

    /**
     * Log a message with specific log level.
     *
     * @param logLevel the specific log level
     * @param format   the format of the message to log
     * @param args     the arguments of the message to log
     */
    @JvmStatic
    fun log(logLevel: Int, format: String, vararg args: Any?) {
        assertInitialization()
        sLogger!!.log(logLevel, format, *args)
    }

    /**
     * Log a message with specific log level.
     *
     * @param logLevel the specific log level
     * @param msg      the message to log
     */
    @JvmStatic
    fun log(logLevel: Int, msg: String) {
        assertInitialization()
        sLogger!!.log(logLevel, msg)
    }

    /**
     * Log a message and a throwable with specific log level.
     *
     * @param logLevel the specific log level
     * @param msg      the message to log
     * @param tr       the throwable to be log
     */
    @JvmStatic
    fun log(logLevel: Int, msg: String, tr: Throwable) {
        assertInitialization()
        sLogger!!.log(logLevel, msg, tr)
    }

    /**
     * Log a JSON string, with level [LogLevel.DEBUG] by default.
     *
     * @param json the JSON string to log
     */
    @JvmStatic
    fun json(json: String) {
        assertInitialization()
        sLogger!!.json(json)
    }

    /**
     * Log a XML string, with level [LogLevel.DEBUG] by default.
     *
     * @param xml the XML string to log
     */
    @JvmStatic
    fun xml(xml: String) {
        assertInitialization()
        sLogger!!.xml(xml)
    }

    /**
     * Compatibility class with [android.util.Log].
     */
    object Log {

        /**
         * Send a [LogLevel.VERBOSE] log message.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        @JvmStatic
        fun v(tag: String, msg: String) {
            tag(tag).v(msg)
        }

        /**
         * Send a [LogLevel.VERBOSE] log message and log the exception.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr  An exception to log
         */
        @JvmStatic
        fun v(tag: String, msg: String, tr: Throwable) {
            tag(tag).v(msg, tr)
        }

        /**
         * Send a [LogLevel.DEBUG] log message.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        @JvmStatic
        fun d(tag: String, msg: String) {
            tag(tag).d(msg)
        }

        /**
         * Send a [LogLevel.DEBUG] log message and log the exception.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr  An exception to log
         */
        @JvmStatic
        fun d(tag: String, msg: String, tr: Throwable) {
            tag(tag).d(msg, tr)
        }

        /**
         * Send an [LogLevel.INFO] log message.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        @JvmStatic
        fun i(tag: String, msg: String) {
            tag(tag).i(msg)
        }

        /**
         * Send a [LogLevel.INFO] log message and log the exception.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr  An exception to log
         */
        @JvmStatic
        fun i(tag: String, msg: String, tr: Throwable) {
            tag(tag).i(msg, tr)
        }

        /**
         * Send a [LogLevel.WARN] log message.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        @JvmStatic
        fun w(tag: String, msg: String) {
            tag(tag).w(msg)
        }

        /**
         * Send a [LogLevel.WARN] log message and log the exception.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr  An exception to log
         */
        @JvmStatic
        fun w(tag: String, msg: String, tr: Throwable) {
            tag(tag).w(msg, tr)
        }

        /**
         * Send a [LogLevel.WARN] log message and log the exception.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param tr  An exception to log
         */
        @JvmStatic
        fun w(tag: String, tr: Throwable) {
            tag(tag).w("", tr)
        }

        /**
         * Send an [LogLevel.ERROR] log message.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         */
        @JvmStatic
        fun e(tag: String, msg: String) {
            tag(tag).e(msg)
        }

        /**
         * Send a [LogLevel.ERROR] log message and log the exception.
         *
         * @param tag Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg The message you would like logged.
         * @param tr  An exception to log
         */
        @JvmStatic
        fun e(tag: String, msg: String, tr: Throwable) {
            tag(tag).e(msg, tr)
        }

        /**
         * What a Terrible Failure: Report a condition that should never happen.
         * The error will always be logged at level ASSERT with the call stack.
         * Depending on system configuration, a report may be added to the
         * [android.os.DropBoxManager] and/or the process may be terminated
         * immediately with an error dialog.
         *
         * @param tag Used to identify the source of a log message.
         * @param msg The message you would like logged.
         */
        @JvmStatic
        fun wtf(tag: String, msg: String) {
            tag(tag).log(LogLevel.ASSERT, msg)
        }

        /**
         * What a Terrible Failure: Report an exception that should never happen.
         * Similar to [wtf], with an exception to log.
         *
         * @param tag Used to identify the source of a log message.
         * @param tr  An exception to log.
         */
        @JvmStatic
        fun wtf(tag: String, tr: Throwable) {
            tag(tag).log(LogLevel.ASSERT, "", tr)
        }

        /**
         * What a Terrible Failure: Report an exception that should never happen.
         * Similar to [wtf], with a message as well.
         *
         * @param tag Used to identify the source of a log message.
         * @param msg The message you would like logged.
         * @param tr  An exception to log. May be null.
         */
        @JvmStatic
        fun wtf(tag: String, msg: String, tr: Throwable) {
            tag(tag).log(LogLevel.ASSERT, msg, tr)
        }

        /**
         * Low-level logging call.
         *
         * @param logLevel The priority/type of this log message
         * @param tag      Used to identify the source of a log message. It usually identifies
         * the class or activity where the log call occurs.
         * @param msg      The message you would like logged.
         * @return The number of bytes written.
         */
        @JvmStatic
        fun println(logLevel: Int, tag: String, msg: String): Int {
            tag(tag).log(logLevel, msg)
            return 0
        }

        /**
         * Checks to see whether or not a log for the specified tag is loggable at the specified level.
         *
         * @param tag   Used to identify the source of a log message.
         * @param level The level to check.
         * @return Whether or not that this is allowed to be logged.
         */
        @JvmStatic
        fun isLoggable(tag: String, level: Int): Boolean {
            return sLogConfiguration?.isLoggable(level) ?: false
        }

        /**
         * Handy function to get a loggable stack trace from a Throwable
         *
         * @param tr An exception to log
         */
        @JvmStatic
        fun getStackTraceString(tr: Throwable): String {
            return StackTraceUtil.getStackTraceString(tr)
        }
    }
}