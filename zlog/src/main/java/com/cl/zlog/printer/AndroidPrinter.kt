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

package com.cl.zlog.printer

/**
 * Log [Printer] using [android.util.Log].
 */
class AndroidPrinter @JvmOverloads constructor(
    /**
     * Whether the log should be separated by line separator automatically.
     */
    private val autoSeparate: Boolean = false,
    private val maxChunkSize: Int = DEFAULT_MAX_CHUNK_SIZE
) : Printer {

    /**
     * Constructor.
     *
     * @param maxChunkSize the max size of each chunk. If the message is too long, it will be
     *                     separated to several chunks automatically
     * @since 1.4.1
     */
    constructor(maxChunkSize: Int) : this(false, maxChunkSize)

    override fun println(logLevel: Int, tag: String, msg: String) {
        val msgLength = msg.length
        var start = 0
        var end: Int
        while (start < msgLength) {
            if (msg[start] == '\n') {
                start++
                continue
            }
            end = minOf(start + maxChunkSize, msgLength)
            if (autoSeparate) {
                val newLine = msg.indexOf('\n', start)
                end = if (newLine != -1) minOf(end, newLine) else end
            } else {
                end = adjustEnd(msg, start, end)
            }
            printChunk(logLevel, tag, msg.substring(start, end))

            start = end
        }
    }

    /**
     * Print single chunk of log in new line.
     *
     * @param logLevel the level of log
     * @param tag      the tag of log
     * @param msg      the msg of log
     */
    internal fun printChunk(logLevel: Int, tag: String, msg: String) {
        android.util.Log.println(logLevel, tag, msg)
    }

    companion object {
        /**
         * Generally, android has a default length limit of 4096 for single log, but
         * some device(like HUAWEI) has its own shorter limit, so we just use 4000
         * and wish it could run well in all devices.
         */
        const val DEFAULT_MAX_CHUNK_SIZE = 4000

        /**
         * Move the end to the nearest line separator('\n') (if exist).
         */
        internal fun adjustEnd(msg: String, start: Int, originEnd: Int): Int {
            if (originEnd == msg.length) {
                // Already end of message.
                return originEnd
            }
            if (msg[originEnd] == '\n') {
                // Already prior to '\n'.
                return originEnd
            }
            // Search back for '\n'.
            var last = originEnd - 1
            while (start < last) {
                if (msg[last] == '\n') {
                    return last
                }
                last--
            }
            return originEnd
        }
    }
}