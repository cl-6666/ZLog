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

package com.cl.zlog.flattener

/**
 * The flattener used to flatten log elements(log level, tag and message) to a single CharSequence.
 *
 * @since 1.3.0
 * @deprecated use [Flattener2] instead, since 1.6.0
 */
@Deprecated("Use Flattener2 instead")
interface Flattener {

    /**
     * Flatten the log.
     *
     * @param logLevel the level of log
     * @param tag      the tag of log
     * @param message  the message of log
     * @return the formatted final log Charsequence
     */
    fun flatten(logLevel: Int, tag: String, message: String): CharSequence
}