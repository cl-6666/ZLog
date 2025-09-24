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

package com.cl.zlog.printer

import com.cl.zlog.internal.DefaultsFactory
import com.cl.zlog.flattener.Flattener

/**
 * Log [Printer] using `System.out.println(String)`.
 *
 * @since 1.3.0
 */
open class ConsolePrinter @JvmOverloads constructor(
    /**
     * The log flattener when print a log.
     */
    private val flattener: Flattener = DefaultsFactory.createFlattener()
) : Printer {

    override fun println(logLevel: Int, tag: String, msg: String) {
        val flattenedLog = flattener.flatten(logLevel, tag, msg).toString()
        println(flattenedLog)
    }
}