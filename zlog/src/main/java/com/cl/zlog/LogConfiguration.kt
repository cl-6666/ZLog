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

import com.cl.zlog.formatter.border.BorderFormatter
import com.cl.zlog.formatter.message.json.JsonFormatter
import com.cl.zlog.formatter.message.`object`.ObjectFormatter
import com.cl.zlog.formatter.message.throwable.ThrowableFormatter
import com.cl.zlog.formatter.message.xml.XmlFormatter
import com.cl.zlog.formatter.stacktrace.StackTraceFormatter
import com.cl.zlog.formatter.thread.ThreadFormatter
import com.cl.zlog.interceptor.Interceptor
import com.cl.zlog.internal.DefaultsFactory

/**
 * The configuration used for logging, always attached to a [Logger], will affect all logs
 * logged by the [Logger].
 *
 * Use the [Builder] to construct a [LogConfiguration] object.
 */
class LogConfiguration internal constructor(builder: Builder) {

    /**
     * The log level, the logs below of which would not be printed.
     */
    val logLevel: Int = builder.logLevel

    /**
     * The tag string.
     */
    val tag: String = builder.tag

    /**
     * Whether we should log with thread info.
     */
    val withThread: Boolean = builder.withThread

    /**
     * Whether we should log with stack trace.
     */
    val withStackTrace: Boolean = builder.withStackTrace

    /**
     * The origin of stack trace elements from which we should not log when logging with stack trace,
     * it can be a package name like "com.cl.zlog", a class name like "com.yourdomain.logWrapper",
     * or something else between package name and class name, like "com.yourdomain.".
     *
     * It is mostly used when you are using a logger wrapper.
     *
     * @since 1.4.0
     */
    val stackTraceOrigin: String? = builder.stackTraceOrigin

    /**
     * The number of stack trace elements we should log when logging with stack trace,
     * 0 if no limitation.
     */
    val stackTraceDepth: Int = builder.stackTraceDepth

    /**
     * Whether we should log with border.
     */
    val withBorder: Boolean = builder.withBorder

    /**
     * The JSON formatter used to format the JSON string when log a JSON string.
     */
    @JvmField
    val jsonFormatter: JsonFormatter = builder.jsonFormatter!!

    /**
     * The XML formatter used to format the XML string when log a XML string.
     */
    @JvmField
    val xmlFormatter: XmlFormatter = builder.xmlFormatter!!

    /**
     * The throwable formatter used to format the throwable when log a message with throwable.
     */
    @JvmField
    val throwableFormatter: ThrowableFormatter = builder.throwableFormatter!!

    /**
     * The thread formatter used to format the thread when logging.
     */
    @JvmField
    val threadFormatter: ThreadFormatter = builder.threadFormatter!!

    /**
     * The stack trace formatter used to format the stack trace when logging.
     */
    @JvmField
    val stackTraceFormatter: StackTraceFormatter = builder.stackTraceFormatter!!

    /**
     * The border formatter used to format the border when logging.
     */
    @JvmField
    val borderFormatter: BorderFormatter = builder.borderFormatter!!

    /**
     * The object formatters, used when logging an object.
     */
    private val objectFormatters: Map<Class<*>, ObjectFormatter<*>>? = builder.objectFormatters

    /**
     * The interceptors, used to intercept the log when logging.
     *
     * @since 1.3.0
     */
    val interceptors: List<Interceptor>? = builder.interceptors

    /**
     * Get [ObjectFormatter] for specific object.
     *
     * @param object the object
     * @param T    the type of object
     * @return the object formatter for the object, or null if not found
     * @since 1.1.0
     */
    fun <T> getObjectFormatter(`object`: T): ObjectFormatter<in T>? {
        if (objectFormatters == null) {
            return null
        }

        var clazz: Class<in T>? = `object`!!::class.java as Class<in T>
        var formatter: ObjectFormatter<in T>?
        do {
            formatter = objectFormatters?.get(clazz as Class<*>) as? ObjectFormatter<in T>
            clazz = clazz?.superclass as? Class<in T>
        } while (formatter == null && clazz != null)
        return formatter
    }

    /**
     * Whether logs with specific level is loggable.
     *
     * @param level the specific level
     * @return true if loggable, false otherwise
     */
    internal fun isLoggable(level: Int): Boolean {
        return level >= logLevel
    }

    /**
     * Builder for [LogConfiguration].
     */
    class Builder {

        companion object {
            private const val DEFAULT_LOG_LEVEL = LogLevel.ALL
            private const val DEFAULT_TAG = "X-LOG"
        }

        /**
         * The log level, the logs below of which would not be printed.
         */
        internal var logLevel: Int = DEFAULT_LOG_LEVEL

        /**
         * The tag string used when log.
         */
        internal var tag: String = DEFAULT_TAG

        /**
         * Whether we should log with thread info.
         */
        internal var withThread: Boolean = false

        /**
         * Whether we should log with stack trace.
         */
        internal var withStackTrace: Boolean = false

        /**
         * The origin of stack trace elements from which we should NOT log when logging with stack trace,
         * it can be a package name like "com.cl.zlog", a class name like "com.yourdomain.logWrapper",
         * or something else between package name and class name, like "com.yourdomain.".
         *
         * It is mostly used when you are using a logger wrapper.
         */
        internal var stackTraceOrigin: String? = null

        /**
         * The number of stack trace elements we should log when logging with stack trace,
         * 0 if no limitation.
         */
        internal var stackTraceDepth: Int = 0

        /**
         * Whether we should log with border.
         */
        internal var withBorder: Boolean = false

        /**
         * The JSON formatter used to format the JSON string when log a JSON string.
         */
        internal var jsonFormatter: JsonFormatter? = null

        /**
         * The XML formatter used to format the XML string when log a XML string.
         */
        internal var xmlFormatter: XmlFormatter? = null

        /**
         * The throwable formatter used to format the throwable when log a message with throwable.
         */
        internal var throwableFormatter: ThrowableFormatter? = null

        /**
         * The thread formatter used to format the thread when logging.
         */
        internal var threadFormatter: ThreadFormatter? = null

        /**
         * The stack trace formatter used to format the stack trace when logging.
         */
        internal var stackTraceFormatter: StackTraceFormatter? = null

        /**
         * The border formatter used to format the border when logging.
         */
        internal var borderFormatter: BorderFormatter? = null

        /**
         * The object formatters, used when logging an object.
         */
        internal var objectFormatters: MutableMap<Class<*>, ObjectFormatter<*>>? = null

        /**
         * The interceptors, used to intercept the log when logging.
         */
        internal var interceptors: MutableList<Interceptor>? = null

        /**
         * Construct a builder with all default configurations.
         */
        constructor()

        /**
         * Construct a builder with all configurations from another [LogConfiguration].
         *
         * @param logConfiguration the [LogConfiguration] to copy configurations from
         */
        constructor(logConfiguration: LogConfiguration) {
            this.logLevel = logConfiguration.logLevel
            this.tag = logConfiguration.tag
            this.withThread = logConfiguration.withThread
            this.withStackTrace = logConfiguration.withStackTrace
            this.stackTraceOrigin = logConfiguration.stackTraceOrigin
            this.stackTraceDepth = logConfiguration.stackTraceDepth
            this.withBorder = logConfiguration.withBorder
            this.jsonFormatter = logConfiguration.jsonFormatter
            this.xmlFormatter = logConfiguration.xmlFormatter
            this.throwableFormatter = logConfiguration.throwableFormatter
            this.threadFormatter = logConfiguration.threadFormatter
            this.stackTraceFormatter = logConfiguration.stackTraceFormatter
            this.borderFormatter = logConfiguration.borderFormatter

            if (logConfiguration.objectFormatters != null) {
                this.objectFormatters = HashMap(logConfiguration.objectFormatters)
            }

            if (logConfiguration.interceptors != null) {
                this.interceptors = ArrayList(logConfiguration.interceptors)
            }
        }

        /**
         * Set the log level, the logs below of which would not be printed.
         *
         * @param logLevel the log level
         * @return the builder
         * @since 1.3.0
         */
        fun logLevel(logLevel: Int): Builder {
            this.logLevel = logLevel
            return this
        }

        /**
         * Set the tag string used when log.
         *
         * @param tag the tag string used when log
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
         * @deprecated use [enableThreadInfo] instead, since 1.7.1
         */
        @Deprecated("use enableThreadInfo() instead, since 1.7.1")
        fun t(): Builder {
            return enableThreadInfo()
        }

        /**
         * Enable thread info, the thread info would be printed with the log message.
         *
         * @return the builder
         * @see ThreadFormatter
         * @since 1.7.1
         */
        fun enableThreadInfo(): Builder {
            this.withThread = true
            return this
        }

        /**
         * Disable thread info.
         *
         * @return the builder
         * @deprecated use [disableThreadInfo] instead, since 1.7.1
         */
        @Deprecated("use disableThreadInfo() instead, since 1.7.1")
        fun nt(): Builder {
            return disableThreadInfo()
        }

        /**
         * Disable thread info, the thread info won't be printed with the log message.
         *
         * @return the builder
         * @since 1.7.1
         */
        fun disableThreadInfo(): Builder {
            this.withThread = false
            return this
        }

        /**
         * Enable stack trace.
         *
         * @param depth the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @deprecated use [enableStackTrace] instead, since 1.7.1
         */
        @Deprecated("use enableStackTrace(int) instead, since 1.7.1")
        fun st(depth: Int): Builder {
            enableStackTrace(depth)
            return this
        }

        /**
         * Enable stack trace, the stack trace would be printed with the log message.
         *
         * @param depth the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @see StackTraceFormatter
         * @since 1.7.1
         */
        fun enableStackTrace(depth: Int): Builder {
            enableStackTrace(null, depth)
            return this
        }

        /**
         * Enable stack trace.
         *
         * @param stackTraceOrigin the origin of stack trace elements from which we should NOT log when
         *                         logging with stack trace, it can be a package name like
         *                         "com.cl.zlog", a class name like "com.yourdomain.logWrapper",
         *                         or something else between package name and class name, like "com.yourdomain.".
         *                         It is mostly used when you are using a logger wrapper
         * @param depth            the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @since 1.4.0
         * @deprecated use [enableStackTrace] instead, since 1.7.1
         */
        @Deprecated("use enableStackTrace(String, int) instead, since 1.7.1")
        fun st(stackTraceOrigin: String, depth: Int): Builder {
            return enableStackTrace(stackTraceOrigin, depth)
        }

        /**
         * Enable stack trace, the stack trace would be printed with the log message.
         *
         * @param stackTraceOrigin the origin of stack trace elements from which we should NOT log when
         *                         logging with stack trace, it can be a package name like
         *                         "com.cl.zlog", a class name like "com.yourdomain.logWrapper",
         *                         or something else between package name and class name, like "com.yourdomain.".
         *                         It is mostly used when you are using a logger wrapper
         * @param depth            the number of stack trace elements we should log, 0 if no limitation
         * @return the builder
         * @see StackTraceFormatter
         * @since 1.7.1
         */
        fun enableStackTrace(stackTraceOrigin: String?, depth: Int): Builder {
            this.withStackTrace = true
            this.stackTraceOrigin = stackTraceOrigin
            this.stackTraceDepth = depth
            return this
        }

        /**
         * Disable stack trace.
         *
         * @return the builder
         * @deprecated use [disableStackTrace] instead, since 1.7.1
         */
        @Deprecated("use disableStackTrace() instead, since 1.7.1")
        fun nst(): Builder {
            return disableStackTrace()
        }

        /**
         * Disable stack trace, the stack trace won't be printed with the log message.
         *
         * @return the builder
         * @see StackTraceFormatter
         * @since 1.7.1
         */
        fun disableStackTrace(): Builder {
            this.withStackTrace = false
            this.stackTraceOrigin = null
            this.stackTraceDepth = 0
            return this
        }

        /**
         * Enable border.
         *
         * @return the builder
         * @deprecated use [enableBorder] instead, since 1.7.1
         */
        @Deprecated("use enableBorder() instead, since 1.7.1")
        fun b(): Builder {
            return enableBorder()
        }

        /**
         * Enable border, the border would surround the entire log content, and separate the log
         * message, thread info and stack trace.
         *
         * @return the builder
         * @see BorderFormatter
         * @since 1.7.1
         */
        fun enableBorder(): Builder {
            this.withBorder = true
            return this
        }

        /**
         * Disable border.
         *
         * @return the builder
         * @deprecated use [disableBorder] instead, since 1.7.1
         */
        @Deprecated("use disableBorder() instead, since 1.7.1")
        fun nb(): Builder {
            return disableBorder()
        }

        /**
         * Disable border, the log content won't be surrounded by a border.
         *
         * @return the builder
         * @since 1.7.1
         */
        fun disableBorder(): Builder {
            this.withBorder = false
            return this
        }

        /**
         * Set the JSON formatter used when log a JSON string.
         *
         * @param jsonFormatter the JSON formatter used when log a JSON string
         * @return the builder
         */
        fun jsonFormatter(jsonFormatter: JsonFormatter): Builder {
            this.jsonFormatter = jsonFormatter
            return this
        }

        /**
         * Set the XML formatter used when log a XML string.
         *
         * @param xmlFormatter the XML formatter used when log a XML string
         * @return the builder
         */
        fun xmlFormatter(xmlFormatter: XmlFormatter): Builder {
            this.xmlFormatter = xmlFormatter
            return this
        }

        /**
         * Set the throwable formatter used when log a message with throwable.
         *
         * @param throwableFormatter the throwable formatter used when log a message with throwable
         * @return the builder
         */
        fun throwableFormatter(throwableFormatter: ThrowableFormatter): Builder {
            this.throwableFormatter = throwableFormatter
            return this
        }

        /**
         * Set the thread formatter used when logging.
         *
         * @param threadFormatter the thread formatter used when logging
         * @return the builder
         */
        fun threadFormatter(threadFormatter: ThreadFormatter): Builder {
            this.threadFormatter = threadFormatter
            return this
        }

        /**
         * Set the stack trace formatter used when logging.
         *
         * @param stackTraceFormatter the stack trace formatter used when logging
         * @return the builder
         */
        fun stackTraceFormatter(stackTraceFormatter: StackTraceFormatter): Builder {
            this.stackTraceFormatter = stackTraceFormatter
            return this
        }

        /**
         * Set the border formatter used when logging.
         *
         * @param borderFormatter the border formatter used when logging
         * @return the builder
         */
        fun borderFormatter(borderFormatter: BorderFormatter): Builder {
            this.borderFormatter = borderFormatter
            return this
        }

        /**
         * Add a [ObjectFormatter] for specific class of object.
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
                objectFormatters = HashMap(DefaultsFactory.builtinObjectFormatters())
            }
            objectFormatters!![objectClass] = objectFormatter
            return this
        }

        /**
         * Copy all object formatters, only for internal usage.
         *
         * @param objectFormatters the object formatters to copy
         * @return the builder
         */
        internal fun objectFormatters(objectFormatters: Map<Class<*>, ObjectFormatter<*>>): Builder {
            this.objectFormatters = objectFormatters.toMutableMap()
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
         * Copy all interceptors, only for internal usage.
         *
         * @param interceptors the interceptors to copy
         * @return the builder
         */
        internal fun interceptors(interceptors: List<Interceptor>): Builder {
            this.interceptors = interceptors.toMutableList()
            return this
        }

        /**
         * Builds configured [LogConfiguration] object.
         *
         * @return the built configured [LogConfiguration] object
         */
        fun build(): LogConfiguration {
            initEmptyFieldsWithDefaultValues()
            return LogConfiguration(this)
        }

        private fun initEmptyFieldsWithDefaultValues() {
            if (jsonFormatter == null) {
                jsonFormatter = DefaultsFactory.createJsonFormatter()
            }
            if (xmlFormatter == null) {
                xmlFormatter = DefaultsFactory.createXmlFormatter()
            }
            if (throwableFormatter == null) {
                throwableFormatter = DefaultsFactory.createThrowableFormatter()
            }
            if (threadFormatter == null) {
                threadFormatter = DefaultsFactory.createThreadFormatter()
            }
            if (stackTraceFormatter == null) {
                stackTraceFormatter = DefaultsFactory.createStackTraceFormatter()
            }
            if (borderFormatter == null) {
                borderFormatter = DefaultsFactory.createBorderFormatter()
            }
            if (objectFormatters == null) {
                objectFormatters = HashMap(DefaultsFactory.builtinObjectFormatters())
            }
        }
    }
}